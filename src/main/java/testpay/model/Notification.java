package testpay.model;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Currency;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import testpay.AuthServerConfig;

@Entity
@Table(name = "notifications")
public class Notification {
  @JsonIgnore
  private URL notificationUrl;
  @JsonIgnore
  private int failedAttempts = 0;
  @JsonIgnore
  private Instant lastAttempt;
  private Currency currency;
  private String amount;
  @Id
  private long id;
  private String externalId;
  private PaymentState paymentState;
  private String sha2sig;

  public Notification() {}

  public Notification(Request request, long id, PaymentState paymentState) throws NoSuchAlgorithmException {
    this.notificationUrl = request.getNotificationUrl();
    this.currency = request.getTransaction().getAmount().getCurrency();
    this.amount = request.getTransaction().getAmount().getValue();
    this.id = id;
    this.externalId = request.getTransaction().getExternalId();
    this.paymentState = paymentState;
    this.lastAttempt = Instant.now();
    this.sign();
  }

  public URL getNotificationUrl() {
    return notificationUrl;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  public Instant getLastAttempt() {
    return lastAttempt;
  }

  public Currency getCurrency() {
    return currency;
  }

  public String getAmount() {
    return amount;
  }

  public long getId() {
    return id;
  }

  @JsonProperty("external_id")
  public String getExternalId() {
    return externalId;
  }

  @JsonProperty("status")
  public PaymentState getPaymentState() {
    return paymentState;
  }

  public String getSha2sig() {
    return sha2sig;
  }

  public void attemptFailed() {
    failedAttempts++;
    lastAttempt = Instant.now();
  }

  private void sign() throws NoSuchAlgorithmException {
    StringBuilder builder = new StringBuilder();

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    builder.append(this.currency.getCurrencyCode());
    builder.append(this.amount);
    builder.append(AuthServerConfig.SHA2_MERCHANTS_SECRET);
    builder.append(this.id);
    builder.append(this.externalId);
    builder.append(this.paymentState.toString());
    this.sha2sig = new String(digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
  }
}
