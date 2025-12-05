package com.MagicLook.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import com.MagicLook.data.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    
}
