package com.example.mail.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mailId; 

    private int userId;
    
    @Column(columnDefinition = "TEXT")
    private String mailContent;  // 텍스트 형식의 메일 내용
    
    @Column(columnDefinition = "TEXT")
    private String mailHtmlContent;  // HTML 형식의 메일 내용
    
    private String mailSender;
    private boolean isSpam;
    private String mailSummarize;
    private String mailTopic;
    private LocalDateTime whenArrived;
}
