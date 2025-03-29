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
    private int employeeId; 

    private LocalDateTime lastMailTime;
    private String protocolType;
    private String mailServerAddress;
    private String authority;

    private String mailAddress;
    
    public void setLastMailTime(LocalDateTime lastProcessedMailDate) {
        this.lastMailTime = lastProcessedMailDate;
    }
}
