package com.example.mail.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.mail.dto.MailReportDto;
import com.example.mail.event.LastMailPolledEvent;
import com.example.mail.event.MailChangedToNormalEvent;
import com.example.mail.event.MailChangedToSpamEvent;
import com.example.mail.event.MailInboundedEvent;
import com.example.mail.event.MailNotTaggedSpamEvent;
import com.example.mail.event.MailTaggedSpamEvent;
import com.example.mail.eventDto.LastMailPolledEventDto;
import com.example.mail.eventDto.MailChangedToNormalEventDto;
import com.example.mail.eventDto.MailChangedToSpamEventDto;
import com.example.mail.eventDto.MailInboundedEventDto;
import com.example.mail.eventDto.MailNotTaggedSpamEventDto;
import com.example.mail.eventDto.MailSummarizedEventDto;
import com.example.mail.eventDto.MailTaggedSpamEventDto;
import com.example.mail.eventDto.MonitoringTriggeredEventDto;
import com.example.mail.kafka.KafkaProducer;
import com.example.mail.model.Mail;
import com.example.mail.repository.MailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Transactional
    private void publishMailInboundedEvent(List<Mail> emails, MonitoringTriggeredEventDto monitoringTriggeredEventDto) {
        try {
            if (monitoringTriggeredEventDto == null) return;
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

    @Transactional
    public boolean tagIsSpamOrNot(int mailId, boolean isSpam) throws JsonProcessingException {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null) {
            return false;
        }
        mail.setIsSpam(isSpam);
        mailRepository.save(mail);

        if (isSpam) {
            MailTaggedSpamEventDto mailTaggedSpamEventDto = new MailTaggedSpamEventDto();
            mailTaggedSpamEventDto.setMailId(mailId);
            mailTaggedSpamEventDto.setMailContent(mail.getMailContent());
            MailTaggedSpamEvent mailTaggedSpamEvent = new MailTaggedSpamEvent(mailTaggedSpamEventDto);
            kafkaProducer.publish(mailTaggedSpamEvent);
        } else {
            MailNotTaggedSpamEventDto mailNotTaggedSpamEventDto = new MailNotTaggedSpamEventDto();
            mailNotTaggedSpamEventDto.setMailId(mailId);
            mailNotTaggedSpamEventDto.setMailContent(mail.getMailContent());
            MailNotTaggedSpamEvent mailNotTaggedSpamEvent = new MailNotTaggedSpamEvent(mailNotTaggedSpamEventDto);
            kafkaProducer.publish(mailNotTaggedSpamEvent);
        }

        return mail.getIsSpam();
    }

    private void addMessageToMailList(MonitoringTriggeredEventDto monitoringTriggeredEventDto, Message message, List<Mail> emailList) {
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
                    .mailTitle(message.getSubject())
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

    public void getEmailsFromMailServer(MonitoringTriggeredEventDto monitoringTriggeredEventDto) 
    throws NoSuchProviderException, MessagingException, JsonProcessingException {
        List<Mail> emails = new ArrayList<>();


        Date processedLastDate = null;
        
        try {
        // check every field in dto is not null
            if (monitoringTriggeredEventDto.getServerAddress() != null 
            && monitoringTriggeredEventDto.getEmailAddress() != null 
            && monitoringTriggeredEventDto.getEmailPassword() != null 
            && monitoringTriggeredEventDto.getProtocolType() != null) {        
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

                for (int i = 0; i < totalMessages / amount + 1 && hasMore; i++) {
                    int start = (int)(totalMessages / amount) == i ? 1 : totalMessages - (i + 1) * amount + 1;
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
                            (internalDate.before(LDT2Date(monitoringTriggeredEventDto.getLastReadTime())) 
                            || internalDate.equals(LDT2Date(monitoringTriggeredEventDto.getLastReadTime())))) {
                                hasMore = false;
                                break;
                            }
        
                        // Message 객체를 Mail 객체로 변환하여 리스트에 추가
                        addMessageToMailList(monitoringTriggeredEventDto, message, emails);
                        if (processedLastDate == null || internalDate.after(processedLastDate)) {
                            processedLastDate = internalDate;
                        }
                    }
                }

                emailFolder.close(false);
                store.close();
            }

            publishMailInboundedEvent(emails, monitoringTriggeredEventDto);
            if (processedLastDate == null) {
                LocalDateTime lastReadTime = monitoringTriggeredEventDto.getLastReadTime();
                processedLastDate = Date.from(lastReadTime.atZone(ZoneId.systemDefault()).toInstant());
            }
            LastMailPolledEventDto lastMailPolledEventDto = new LastMailPolledEventDto(
                monitoringTriggeredEventDto.getUserId(), LocalDateTime.ofInstant(processedLastDate.toInstant(), ZoneId.systemDefault()));
            LastMailPolledEvent event = new LastMailPolledEvent(lastMailPolledEventDto);
            kafkaProducer.publish(event);

            return;
        } catch (Exception e) {
            if (processedLastDate == null) {
                LocalDateTime lastReadTime = monitoringTriggeredEventDto.getLastReadTime();
                processedLastDate = Date.from(lastReadTime.atZone(ZoneId.systemDefault()).toInstant());
            }
            LastMailPolledEventDto lastMailPolledEventDto = new LastMailPolledEventDto(
                monitoringTriggeredEventDto.getUserId(), LocalDateTime.ofInstant(processedLastDate.toInstant(), ZoneId.systemDefault()));
            LastMailPolledEvent event = new LastMailPolledEvent(lastMailPolledEventDto);
            kafkaProducer.publish(event);
        }
    }

    public Page<Mail> getMails(int userId, int page) {
        Page<Mail> mails = mailRepository.findAllByUserIdOrderByArrivedAtDesc(userId, PageRequest.of(page, 10));
        return mails;
    }

    public Page<Mail> getMailsByIsSpam(int userId, boolean isSpam, int page) {
        Page<Mail> mails = mailRepository.findAllByUserIdAndIsSpamOrderByArrivedAtDesc(userId, isSpam, PageRequest.of(page, 10));
        return mails;
    }

    @Transactional
    public int reportMail(int mailId, String reason) throws JsonProcessingException {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null) {
            return -1;
        }
        mail.setIsSpam(true);
        mailRepository.save(mail);

        logger.info("REPORT MAIL IN SERVICE: {}", reason);
        MailChangedToSpamEventDto mailChangedToSpamEventDto = new MailChangedToSpamEventDto(mail, reason);
        MailChangedToSpamEvent mailChangedToSpamEvent = new MailChangedToSpamEvent(mailChangedToSpamEventDto);
        kafkaProducer.publish(mailChangedToSpamEvent);

        return mailId;
    }

    @Transactional
    public int deleteMailReportance(int mailId) throws JsonProcessingException {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null) {
            return -1;
        }
        mail.setIsSpam(false);
        
        mailRepository.save(mail);
        
        MailChangedToNormalEventDto mailChangedToNormalEventDto = new MailChangedToNormalEventDto(mail);
        MailChangedToNormalEvent mailChangedToNormalEvent = new MailChangedToNormalEvent(mailChangedToNormalEventDto);
        kafkaProducer.publish(mailChangedToNormalEvent);
        return mailId;
    }

    @Transactional
    public int assignMailSummary(MailSummarizedEventDto mailSummarizedEventDto) {
        Mail mail = mailRepository.findById(mailSummarizedEventDto.getMailId()).orElse(null);
        if (mail == null) {
            return -1;
        }
        mail.setMailSummarize(mailSummarizedEventDto.getSummary());
        mailRepository.save(mail);
        return mailSummarizedEventDto.getMailId();
    }

    public String getMailSummary(int mailId) {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null) {
            return null;
        }
        return mail.getMailSummarize();
    }
}

