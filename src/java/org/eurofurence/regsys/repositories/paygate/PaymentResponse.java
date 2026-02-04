package org.eurofurence.regsys.repositories.paygate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("reference_id")
    public String referenceId;

    @JsonProperty("amount_due")
    public long amountDue;

    @JsonProperty("amount_paid")
    public long amountPaid;

    @JsonProperty("currency")
    public String currency;

    @JsonProperty("status")
    public String status;

    @JsonProperty("response_code")
    public String responseCode;

    @JsonProperty("payment_method")
    public String paymentMethod;
}
