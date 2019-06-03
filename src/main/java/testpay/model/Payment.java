package testpay.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Payment {
  @Id
  @GeneratedValue
  private long id;
  private String externalId;
  private Instant createTime;
  private PaymentState state;
  private static DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());

  public Payment() {  }

  public Payment(String externalId, PaymentState state) {
    this.externalId = externalId;
    this.createTime = Instant.now();
    this.state = state;
  }

  public long getId() {
    return id;
  }

  @JsonProperty("id")
  public String getStringId() {
    return String.valueOf(id);
  }

  @JsonIgnore
  public Instant getCreateTime() {
    return createTime;
  }

  @JsonProperty("create_time")
  public String getCreateTimeString() {
    return formatter.format(createTime);
  }

  public PaymentState getState() {
    return state;
  }

  @JsonIgnore
  public String getExternalId() {
    return externalId;
  }
}
