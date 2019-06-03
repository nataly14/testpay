package testpay.event;

import org.springframework.context.ApplicationEvent;

import testpay.model.Notification;

public class NotificationEvent extends ApplicationEvent {

  private final Notification notification;

  public NotificationEvent(Object source, Notification notification) {
    super(source);
    this.notification = notification;
  }

  public Notification getNotification() {
    return notification;
  }

}

