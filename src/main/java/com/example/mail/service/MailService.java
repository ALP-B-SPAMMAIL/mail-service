package com.example.mail.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.mail.event.LastMailPolledEvent;
import com.example.mail.event.MailInboundedEvent;
import com.example.mail.eventDto.LastMailPolledEventDto;
import com.example.mail.eventDto.MailInboundedEventDto;
import com.example.mail.eventDto.MonitoringTriggeredEventDto;
import com.example.mail.kafka.KafkaProducer;
import com.example.mail.model.Mail;
import com.example.mail.repository.MailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Properties;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailRepository mailRepository;
    private final KafkaProducer kafkaProducer;


    @Transactional
    public void pullEmails(MonitoringTriggeredEventDto monitoringTriggeredEventDto) {
        try {
            if (monitoringTriggeredEventDto == null) return;
            List<Mail> emails = getEmailsFromMailServer(monitoringTriggeredEventDto);
            for (Mail email : emails) {
                mailRepository.save(email);
                MailInboundedEventDto mailInboundedEventDto = new MailInboundedEventDto(email);
                MailInboundedEvent event = new MailInboundedEvent(mailInboundedEventDto);
                kafkaProducer.publish(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public boolean tagIsSpamOrNot(int mailId, boolean isSpam) {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null) {
            return false;
        }
        mail.setIsSpam(isSpam);
        mailRepository.save(mail);
        return mail.getIsSpam();
    }

    private void processMessage(MonitoringTriggeredEventDto monitoringTriggeredEventDto, Message message, List<Mail> emailList) {
        try {
            // Parsing Content
            String textContent = "";
            String htmlContent = "";
            
            if (message.getContent() instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    String contentType = bodyPart.getContentType().toLowerCase();
                    if (contentType.contains("text/plain")) {
                        textContent = bodyPart.getContent().toString();
                    } else if (contentType.contains("text/html")) {
                        htmlContent = bodyPart.getContent().toString();
                    }
                }
            } else {
                String contentType = message.getContentType().toLowerCase();
                if (contentType.contains("text/plain")) {
                    textContent = message.getContent().toString();
                } else if (contentType.contains("text/html")) {
                    htmlContent = message.getContent().toString();
                } else {
                    textContent = message.getContent().toString();
                }
            }

            Date receivedDate = message.getReceivedDate();
            if (receivedDate == null) {
                receivedDate = message.getSentDate();
            }

            if (receivedDate != null) {
                Mail mail = Mail.builder()
                    .userId(monitoringTriggeredEventDto.getUserId())
                    .mailContent(textContent)
                    .mailHtmlContent(htmlContent)
                    .mailSender(message.getFrom()[0].toString())
                    .isSpam(false)
                    .arrivedAt(receivedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
                emailList.add(mail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date LDT2Date(LocalDateTime ldt) {
        return java.sql.Timestamp.valueOf(ldt);
    }

    private List<Mail> getEmailsFromMailServer(MonitoringTriggeredEventDto monitoringTriggeredEventDto) 
    throws NoSuchProviderException, MessagingException, JsonProcessingException {
        List<Mail> emails = new ArrayList<>();
        String host = monitoringTriggeredEventDto.getServerAddress();
        String folderName = "INBOX";

        Properties properties = new Properties();
        properties.put("mail.store.protocol", monitoringTriggeredEventDto.getProtocolType());

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore(monitoringTriggeredEventDto.getProtocolType());
        store.connect(host, monitoringTriggeredEventDto.getEmailAddress(), monitoringTriggeredEventDto.getEmailPassword());

        Folder emailFolder = store.getFolder(folderName);
        emailFolder.open(Folder.READ_WRITE);

        int totalMessages = emailFolder.getMessageCount();
        int amount = 10;
        boolean hasMore = true;
        Date processedLastDate = null;
        for (int i = 0; i < totalMessages / amount + 1 && hasMore; i++) {
            int start = (int)(totalMessages / amount) == i ? 1 : totalMessages - (i + 1) * amount;
            int end = totalMessages - i * amount;

            Message[] messages = emailFolder.getMessages(start, end);
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getReceivedDate().compareTo(m1.getReceivedDate());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return 0;
            });
            for (Message message : messages) {
                Date internalDate = message.getReceivedDate();
                
                // 마지막 처리된 날짜 이후의 메시지만 처리
                if (monitoringTriggeredEventDto.getLastReadTime() != null && 
                    (internalDate.before(LDT2Date(monitoringTriggeredEventDto.getLastReadTime())) || internalDate.equals(LDT2Date(monitoringTriggeredEventDto.getLastReadTime())))) {
                        hasMore = false;
                        break;
                    }

                // 메시지 처리 로직
                processMessage(monitoringTriggeredEventDto, message, emails);
                if (processedLastDate == null || internalDate.after(processedLastDate)) {
                    processedLastDate = internalDate;
                }
            }
        }
        if (processedLastDate == null) {
            processedLastDate = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        }
        LastMailPolledEventDto lastMailPolledEventDto = new LastMailPolledEventDto(
            monitoringTriggeredEventDto.getUserId(), LocalDateTime.ofInstant(processedLastDate.toInstant(), ZoneId.systemDefault()));
        LastMailPolledEvent event = new LastMailPolledEvent(lastMailPolledEventDto);
        kafkaProducer.publish(event);
        emailFolder.close(false);
        store.close();
        return emails;
    }
}

