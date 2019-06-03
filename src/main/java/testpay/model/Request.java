package testpay.model;

import java.io.IOException;
import java.net.URL;
import java.util.Currency;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Request {
  @NotNull
  private Intent intent;
  @NotNull
  private URL notificationUrl;
  @NotNull
  @Valid
  private Payer payer;
  @NotNull
  @Valid
  private Transaction transaction;

  public Request(Intent intent, URL notificationUrl, Payer payer, Transaction transaction) {
    this.intent = intent;
    this.notificationUrl = notificationUrl;
    this.payer = payer;
    this.transaction = transaction;
  }

  public Intent getIntent() {
    return intent;
  }

  @JsonProperty("notification_url")
  public URL getNotificationUrl() {
    return notificationUrl;
  }

  public Payer getPayer() {
    return payer;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void setIntent(Intent intent) {
    this.intent = intent;
  }

  public void setNotificationUrl(URL notificationUrl) {
    this.notificationUrl = notificationUrl;
  }

  public void setPayer(@Valid Payer payer) {
    this.payer = payer;
  }

  public void setTransaction(@Valid Transaction transaction) {
    this.transaction = transaction;
  }

  public enum Intent {
    ORDER;

    @JsonCreator
    public static Intent fromString(String name) {
      switch (name.toLowerCase()) {
      case "order":
        return ORDER;
      default:
        throw new IllegalArgumentException(String.format("Unknown Intent name %s", name));
      }
    }
  }

  public static class Payer {
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    @NotNull
    private String email;

    @JsonCreator
    public Payer(String email) {
      this.email = email;
    }

    @JsonProperty("email")
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }

  public static class Transaction {
    private String externalId;
    private String description;
    @NotNull
    @Valid
    private Amount amount;

    public Transaction(String externalId, String description, Amount amount) {
      this.externalId = externalId;
      this.description = description;
      this.amount = amount;
    }

    @JsonProperty("external_id")
    public String getExternalId() {
      return externalId;
    }

    public String getDescription() {
      return description;
    }

    public Amount getAmount() {
      return amount;
    }

    public void setExternalId(String externalId) {
      this.externalId = externalId;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public void setAmount(Amount amount) {
      this.amount = amount;
    }

    public static class Amount {
      @Pattern(regexp = "^\\d{1,10}(\\.\\d{1,2})?$")
      @Size(min = 1, max = 10)
      @NotNull
      private String value;
      @NotNull
      private Currency currency;

      public Amount(String value, Currency currency) {
        this.value = value;
        this.currency = currency;
      }

      public String getValue() {
        return value;
      }

      public Currency getCurrency() {
        return currency;
      }

      @JsonProperty("currency")
      public String getCurrencyCode() {
        return currency.getCurrencyCode();
      }

      public void setValue(String value) {
        this.value = value;
      }

      @JsonDeserialize(using = CurrencyDeserializer.class)
      public void setCurrency(Currency currency) {
        this.currency = currency;
      }

      static class CurrencyDeserializer extends JsonDeserializer<Currency> {
        @Override
        public Currency deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
          JsonNode node = parser.getCodec().readTree(parser);
          String code = node.asText();
          return Currency.getInstance(code);
        }
      }
    }
  }


}
