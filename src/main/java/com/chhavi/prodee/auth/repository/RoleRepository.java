package com.chhavi.prodee.auth.repository;

import com.chhavi.prodee.auth.entity.ERole;
import com.chhavi.prodee.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
