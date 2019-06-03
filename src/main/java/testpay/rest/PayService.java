package testpay.rest;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import testpay.event.NotificationEvent;
import testpay.model.Notification;
import testpay.model.PaymentState;
import testpay.model.Request;
import testpay.model.Payment;
import testpay.persistence.PaymentsRepository;

@RestController
public class PayService implements ApplicationEventPublisherAware {
  private ApplicationEventPublisher applicationEventPublisher;
  @Autowired
  private PaymentsRepository paymentsRepository;
  private static final Logger logger = LogManager.getLogger(PayService.class);

  @RequestMapping(value = "/payments/payment", method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Payment> makePayment(@Valid @RequestBody Request request) {
    Supplier<Payment> createPayment = () -> {
      Payment payment = new Payment(request.getTransaction().getExternalId(), PaymentState.CREATED);
      paymentsRepository.save(payment);

      try {
        Notification notification = new Notification(request, payment.getId(), PaymentState.getRandomState());

        applicationEventPublisher.publishEvent(new NotificationEvent(this, notification));
      } catch (NoSuchAlgorithmException ex) {
        logger.fatal("Unable to create notification. {}", ex.getMessage());
        throw new RuntimeException("Unable to create notification", ex);
      }

      return payment;
    };

    Payment p = Optional.ofNullable(request.getTransaction().getExternalId())
      .map(extId -> paymentsRepository.findByExternalId(extId) //if externalId is absent, consider request as new
        .orElseGet(createPayment))
      .orElseGet(createPayment);

    return new ResponseEntity<>(p, HttpStatus.OK);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
}
