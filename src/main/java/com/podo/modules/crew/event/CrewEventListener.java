package com.podo.modules.crew.event;

import com.podo.infra.config.AppProperties;
import com.podo.infra.mail.EmailMessage;
import com.podo.infra.mail.EmailService;
import com.podo.modules.account.Account;
import com.podo.modules.account.AccountRepository;
import com.podo.modules.account.validator.AccountPredicates;
import com.podo.modules.crew.Crew;
import com.podo.modules.crew.CrewRepository;
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
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class CrewEventListener {

    private final CrewRepository crewRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleCrewCreatedEvent(CrewCreatedEvent crewCreatedEvent) {
        Crew crew = crewRepository.findCrewWithTagsAndZonesById(crewCreatedEvent.getCrew().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(crew.getTags(), crew.getZones()));
        accounts.forEach(account -> {
            if (account.isClubCreatedByEmail()) {
                sendCrewCreatedEmail(crew, account, "새로운 모임이 생겼습니다",
                        "포도, '" + crew.getTitle() + "' 모임이 생겼습니다.");
            }

            if (account.isClubCreatedByWeb()) {
                createNotification(crew, account, crew.getShortDescription(), NotificationType.CREW_CREATED);
            }
        });
    }

    @EventListener
    public void handleCrewUpdateEvent(CrewUpdateEvent crewUpdateEvent) {
        Crew crew = crewRepository.findCrewWithManagersAndMemebersById(crewUpdateEvent.getCrew().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(crew.getManagers());
        accounts.addAll(crew.getMembers());

        accounts.forEach(account -> {
            if (account.isClubUpdatedByEmail()) {
                sendCrewCreatedEmail(crew, account, crewUpdateEvent.getMessage(),
                        "포도, '" + crew.getTitle() + "' 모임에 새소식이 있습니다.");
            }

            if (account.isClubUpdatedByWeb()) {
                createNotification(crew, account, crewUpdateEvent.getMessage(), NotificationType.CREW_UPDATED);
            }
        });
    }

    private void createNotification(Crew crew, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(crew.getTitle());
        notification.setLink("/crew/" + crew.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendCrewCreatedEmail(Crew crew, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/crew/" + crew.getEncodedPath());
        context.setVariable("linkName", crew.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}
