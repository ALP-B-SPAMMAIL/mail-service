package com.example.mail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.mail.model.Monitoring;

@Repository
public interface MonitorRepository extends JpaRepository<Monitoring, Integer> {
    
}
