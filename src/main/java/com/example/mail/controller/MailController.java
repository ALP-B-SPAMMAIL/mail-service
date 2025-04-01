package com.example.mail.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.mail.model.Mail;
import com.example.mail.service.MailService;

import lombok.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @GetMapping("/{userId}")
    public String getMails(@PathVariable int userId) {
        return mailService.getMails(userId);
    }

    @GetMapping("/spams/{userId}")
    public String getSpams(@PathVariable int userId) {
        return mailService.getMailsByIsSpam(userId, true);
    }

    @GetMapping("/normals/{userId}")
    public String getNormals(@PathVariable int userId) {
        return mailService.getMailsByIsSpam(userId, false);
    }
}