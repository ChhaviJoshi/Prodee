package com.chhavi.prodee.common.repository;

import com.chhavi.prodee.common.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    boolean existsByNotificationKey(String notificationKey);

    @Modifying
    @Query("UPDATE SystemNotification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllReadByUserId(Long userId);
}
