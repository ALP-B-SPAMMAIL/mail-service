package com.example.mail.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mail.model.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, Integer> {
    Page<Mail> findAllByUserIdOrderByArrivedAtDesc(int userId, Pageable pageable);
    Page<Mail> findAllByUserIdAndIsSpamOrderByArrivedAtDesc(int userId, boolean isSpam, Pageable pageable);
} 