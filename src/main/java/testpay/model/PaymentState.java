package testpay.model;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentState {
  CREATED,
  APPROVED,
  FAILED;

  private static Random random = new Random();

  @JsonCreator
  public PaymentState fromString(String name) {
    switch(name.toLowerCase()) {
    case "created":
      return PaymentState.CREATED;
    case "approved":
      return PaymentState.APPROVED;
    case "failed":
      return PaymentState.FAILED;
    default:
      throw new IllegalArgumentException(String.format("Unknown state %s", name));
    }
  }

  @JsonValue
  public String toString() {
    return super.toString().toLowerCase();
  }

  public static PaymentState getRandomState() {
    //let's say failed is not so common state
    if (random.nextInt(4) == 0) {
      return FAILED;
    }
    return APPROVED;
  }
}
