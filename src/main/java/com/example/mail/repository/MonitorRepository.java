package com.example.mail.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mail.model.Monitoring;

public interface MonitorRepository extends JpaRepository<Monitoring, Integer> {
    
}
