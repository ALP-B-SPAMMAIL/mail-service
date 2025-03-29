package com.example.mail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mail.model.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, Integer> {
} 