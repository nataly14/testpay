package testpay.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import testpay.event.NotificationEvent;
import testpay.model.Notification;
import testpay.persistence.NotificationsRepository;

@Service
public class WebhookService {

  private static Logger logger = LogManager.getLogger(WebhookService.class);

  @Autowired
  private NotificationsRepository notificationsRepository;
  private final RestTemplate restTemplate;
  private static final int MAX_FAILED_ATTEMPTS = 25;
  public WebhookService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  @Async
  @EventListener
  public void notificationReceivedListener(NotificationEvent event) {
    Notification notification = event.getNotification();

    send(notification);
  }

  @Scheduled(fixedDelay = 60000)
  public void scheduledNotifications() {

    List<Notification> pendingNotifications = notificationsRepository.findByOrderByLastAttemptDesc();
    pendingNotifications.forEach(notification -> {
      if (isTimeToResend(notification)) {
        send(notification);
      }
    });
  }

  private boolean isTimeToResend(Notification notification) {
   return notification.getLastAttempt().plus(getCurrentTimeout(notification), ChronoUnit.MINUTES)
      .isBefore(Instant.now());
  }

  private int getCurrentTimeout(Notification notification) {
    return timeoutMinutes[notification.getFailedAttempts()-1];
  }

  private void send(Notification notification) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
      headers.set(HttpHeaders.CONTENT_ENCODING, "UTF-8");
      HttpEntity<Notification> request = new HttpEntity<>(notification, headers);

      ResponseEntity<String> entity = restTemplate.postForEntity(notification.getNotificationUrl().toURI(), request, String.class);

      if (entity.getStatusCode().equals(HttpStatus.OK)) {
        onSendNotificationSuccess(notification);
      } else {
        logger.info("Unexpected HTTP response code while sending notification. Code {}, entity {}", entity.getStatusCode(),
          entity.toString());
        onSendMessageError(notification);
      }
    } catch (Exception ex) {
      logger.error("Exception while sending payment notification {}", ex.getMessage());
      onSendMessageError(notification);
    }
  }

  private void onSendNotificationSuccess(Notification notification) {
    notificationsRepository.delete(notification);
  }

  private void onSendMessageError(Notification notification) {
    notification.attemptFailed();
    if (notification.getFailedAttempts() < MAX_FAILED_ATTEMPTS) {
      notificationsRepository.save(notification);
    } else {
      logger.info("Maximum attempts reached for notification id {}, externalId {}, url {}", notification.getId(),
        notification.getExternalId(), notification.getNotificationUrl());
      notificationsRepository.delete(notification);
    }
  }

  private static int[] timeoutMinutes = new int[]{
    1,
    2,
    4,
    8,
    16,
    16,
    32,
    32,
    32,
    64,
    64,
    64,
    128,
    128,
    128,
    128,
    256,
    256,
    256,
    256,
    256,
    512,
    512,
    512,
    512
  };

}
