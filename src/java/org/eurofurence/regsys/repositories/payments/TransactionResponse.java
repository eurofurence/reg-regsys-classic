package org.eurofurence.regsys.repositories.payments;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TransactionResponse {
    @JsonProperty("payload")
    public List<Transaction> payload = new ArrayList<>();
}
