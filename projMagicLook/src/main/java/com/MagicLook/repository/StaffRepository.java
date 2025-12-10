package com.MagicLook.repository;

import com.MagicLook.data.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}