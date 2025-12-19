package com.magiclook.repository;

import com.magiclook.data.Notification;
import com.magiclook.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserOrderByDateDesc(User user);

    long countByUserAndReadFalse(User user);
}
