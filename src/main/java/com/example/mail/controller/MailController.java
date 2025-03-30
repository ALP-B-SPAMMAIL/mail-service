package com.example.mail.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.mail.service.MailService;

import lombok.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
}
