package org.eurofurence.regsys.repositories.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Transaction {
    @JsonIgnore
    public Long id;

    @JsonProperty("debitor_id")
    public Long debitorId;

    @JsonProperty("transaction_identifier")
    public String transactionIdentifier;

    public enum TransactionType {
        DUE("due"),
        PAYMENT("payment");

        private final String value;

        TransactionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TransactionType byValueOrDefault(String v, TransactionType defaultValue) {
            for (TransactionType e: TransactionType.values()) {
                if (e.getValue().equals(v)) {
                    return e;
                }
            }
            return defaultValue; // sensible default when created new
        }
    }

    @JsonProperty("transaction_type")
    public String transactionType;

    public enum Method {
        CREDIT("credit"),
        PAYPAL("paypal"),
        TRANSFER("transfer"),
        INTERNAL("internal"),
        GIFT("gift");

        private final String value;

        Method(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Method byValueOrDefault(String v, Method defaultValue) {
            for (Method e: Method.values()) {
                if (e.getValue().equals(v)) {
                    return e;
                }
            }
            return defaultValue; // sensible default when created new
        }
    }

    @JsonProperty("method")
    public String method;

    public static class Amount {
        @JsonProperty("currency")
        public String currency;

        @JsonProperty("gross_cent")
        public Long grossCent;

        @JsonProperty("vat_rate")
        public Double vatRatePercent;
    }

    @JsonProperty("amount")
    public Amount amount = new Amount();

    @JsonProperty("comment")
    public String comment;

    public enum Status {
        TENTATIVE("tentative"),
        PENDING("pending"),
        VALID("valid"),
        DELETED("deleted");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status byValueOrDefault(String v, Status defaultValue) {
            for (Status e: Status.values()) {
                if (e.getValue().equals(v)) {
                    return e;
                }
            }
            return defaultValue; // sensible default when created new
        }
    }

    @JsonProperty("status")
    public String status;

    @JsonProperty("payment_processor_information")
    public Map<String, Object> paymentProcessorInformation = new HashMap<>();

    @JsonProperty("payment_start_url")
    public String paymentStartUrl;

    @JsonProperty("effective_date")
    public String effectiveDate;

    @JsonProperty("due_date")
    public String dueDateISO;

    @JsonProperty("creation_date")
    public String creationDateISO;
}
