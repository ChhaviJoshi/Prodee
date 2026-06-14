package com.chhavi.prodee.auth.service;

import com.chhavi.prodee.auth.dto.*;
import com.chhavi.prodee.auth.entity.ERole;
import com.chhavi.prodee.auth.entity.PasswordResetToken;
import com.chhavi.prodee.auth.entity.Role;
import com.chhavi.prodee.auth.entity.User;
import com.chhavi.prodee.auth.repository.PasswordResetTokenRepository;
import com.chhavi.prodee.auth.repository.RoleRepository;
import com.chhavi.prodee.auth.repository.UserRepository;
import com.chhavi.prodee.auth.security.JwtTokenProvider;
import com.chhavi.prodee.common.exception.BadRequestException;
import com.chhavi.prodee.common.exception.ResourceNotFoundException;
import com.chhavi.prodee.journaling.service.PixelJournalService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PixelJournalService pixelJournalService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${prodee.google.client-id:}")
    private String googleClientId;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already in use");
        }

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .coins(15)
                .build();

        userRepository.save(user);
        pixelJournalService.createDefaultMoodTemplateForUser(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.username()));

        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        if (googleClientId == null || googleClientId.isEmpty()) {
            throw new BadRequestException("Google login is not configured on the server");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.token());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                
                Optional<User> userOptional = userRepository.findByEmail(email);
                User user;
                
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                } else {
                    // Create new user for Google login
                    String name = (String) payload.get("name");
                    String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);
                    
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

                    user = User.builder()
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random secure password
                            .avatarUrl((String) payload.get("picture"))
                            .roles(Set.of(userRole))
                            .coins(15)
                            .build();

                    userRepository.save(user);
                    pixelJournalService.createDefaultMoodTemplateForUser(user);
                }

                // Create Authentication for JWT using a dummy authority since we verified google token
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName().name()))
                        .collect(Collectors.toList())
                );
                
                String token = jwtTokenProvider.generateToken(authentication);
                return new AuthResponse(token, user.getId(), user.getUsername());
            } else {
                throw new BadRequestException("Invalid Google ID token.");
            }
        } catch (Exception e) {
            throw new BadRequestException("Google login failed: " + e.getMessage());
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        // Delete any existing tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate a 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(code)
                .user(user)
                .expiryDate(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.code())
                .orElseThrow(() -> new BadRequestException("Invalid reset code"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Reset code has expired");
        }

        if (!resetToken.getUser().getEmail().equals(request.email())) {
            throw new BadRequestException("Invalid reset code for this email");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), user.getXp(), user.getLevel(),
                user.getCoins(), roles, user.getCreatedAt());
    }
}
