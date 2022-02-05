package com.podo.modules.event.event;

import com.podo.infra.config.AppProperties;
import com.podo.infra.mail.EmailMessage;
import com.podo.infra.mail.EmailService;
import com.podo.modules.account.Account;
import com.podo.modules.crew.Crew;
import com.podo.modules.event.Enrollment;
import com.podo.modules.event.Event;
import com.podo.modules.notification.Notification;
import com.podo.modules.notification.NotificationRepository;
import com.podo.modules.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Crew crew = event.getCrew();

        if (account.isClubEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, crew);
        }

        if (account.isClubCreatedByWeb()) {
            createNotification(enrollmentEvent, account, event, crew);
        }
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Crew crew) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/crew/" + crew.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName", crew.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Crew crew) {
        Notification notification = new Notification();
        notification.setTitle(crew.getTitle() + " / " + event.getTitle());
        notification.setLink("/crew/" + crew.getEncodedPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

}