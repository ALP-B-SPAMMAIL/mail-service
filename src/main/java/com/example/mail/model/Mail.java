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
    private Integer mailId; 

    private int userId;
    
    @Column(columnDefinition = "TEXT")
    private String mailContent;  // 텍스트 형식의 메일 내용
    
    @Column(columnDefinition = "TEXT")
    private String mailHtmlContent;  // HTML 형식의 메일 내용
    
    private String mailSender;
    private boolean isSpam;
    private String mailSummarize;
    private String mailTopic;
    private String mailTitle;
    private LocalDateTime arrivedAt;

    public void setIsSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }

    public void setMailSummarize(String mailSummarize) {
        this.mailSummarize = mailSummarize;
    }

    public void setMailTopic(String mailTopic) {
        this.mailTopic = mailTopic;
    }

    public boolean getIsSpam() {
        return this.isSpam;
    }
}
