package org.eurofurence.regsys.repositories.paygate;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eurofurence.regsys.repositories.payments.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PaymentResponse {
    // several other fields omitted

    @JsonProperty("id")
    public String id;

    @JsonProperty("reference_id")
    public String referenceId;

    @JsonProperty("status")
    public String status;

    @JsonProperty("response_code")
    public String responseCode;
}
