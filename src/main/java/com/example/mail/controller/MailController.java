package com.example.mail.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.mail.dto.MailReportDto;
import com.example.mail.dto.SendMailDto;
import com.example.mail.model.Mail;
import com.example.mail.service.MailService;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(MailController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{userId}")
    public ResponseEntity<Page<Mail>> getMails(@PathVariable int userId, @RequestParam int page) {
        return ResponseEntity.ok(mailService.getMails(userId, page));
    }

    @GetMapping("/spams/{userId}")
    public Page<Mail> getSpams(@PathVariable int userId, @RequestParam int page) {
        return mailService.getMailsByIsSpam(userId, true, page);
    }

    @GetMapping("/normals/{userId}")
    public Page<Mail> getNormals(@PathVariable int userId, @RequestParam int page) {
        return mailService.getMailsByIsSpam(userId, false, page);
    }

    @GetMapping("/report") 
    public ResponseEntity<Integer> reportMail(
        @RequestParam int mailId,
        @RequestParam String reason
    ) throws JsonProcessingException {
        int ret = mailService.reportMail(mailId, reason);
        if (ret == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1);
        }
        return ResponseEntity.ok(ret);
    }
    
    @DeleteMapping("/spams/{mailId}")
    public ResponseEntity<Integer> deleteSpam(@PathVariable int mailId) throws JsonProcessingException {
        int ret = mailService.deleteMailReportance(mailId);
        if (ret == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1);
        }
        return ResponseEntity.ok(ret);
    }

    @GetMapping("/{mailId}/summary")
    public ResponseEntity<String> getMailSummary(@PathVariable int mailId) {
        return ResponseEntity.ok(mailService.getMailSummary(mailId));
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMail(@RequestBody SendMailDto sendMailDto) throws JsonProcessingException {
        logger.info("Received mail request: {}", objectMapper.writeValueAsString(sendMailDto));
        return ResponseEntity.ok(mailService.sendMail(sendMailDto));
    }
}