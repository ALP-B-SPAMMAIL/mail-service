package com.example.mail.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.mail.dto.MailReportDto;
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

    @PostMapping("/{mailId}/report") 
    public ResponseEntity<Integer> reportMail(
        @PathVariable int mailId,
        @RequestBody MailReportDto mailReportDto
    ) throws JsonProcessingException {
        int ret = mailService.reportMail(mailId, mailReportDto);
        if (ret == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1);
        }
        return ResponseEntity.ok(ret);
    }
    
    @DeleteMapping("/spams/{mailId}")
    public ResponseEntity<Integer> deleteSpam(@PathVariable int mailId) throws JsonProcessingException {
        int ret = mailService.deleteMail(mailId);
        if (ret == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1);
        }
        return ResponseEntity.ok(ret);
    }
}