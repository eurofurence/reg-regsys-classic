package org.eurofurence.regsys.service;

import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TransactionCalculator {
    private final PaymentService paymentService = new PaymentService();

    protected List<Transaction> cachedTransactions;
    public void resetCache() {
        cachedTransactions = null;
    }
    public void loadTransactionsFor(long debitorId, String token, String requestId) {
        if (cachedTransactions == null) {
            try {
                TransactionResponse response = paymentService.performFindTransactions(debitorId, null, null, null, token, requestId);
                if (response != null && response.payload != null) {
                    cachedTransactions = response.payload;
                } else {
                    Logging.warn("[" + requestId + "] received unexpected null as transaction response");
                    cachedTransactions = new ArrayList<>();
                }
            } catch (NotFoundException ignore) {
                cachedTransactions = new ArrayList<>();
            }
        }
    }

    public List<Transaction> getTransactions() {
        return cachedTransactions;
    }

    private boolean is(Transaction.Status status, Transaction tx) {
        return status.getValue().equals(tx.status);
    }
    private boolean is(Transaction.TransactionType typ, Transaction tx) {
        return typ.getValue().equals(tx.transactionType);
    }
    public String getDueDate() {
        String newestDueDate = "";
        for (Transaction tx: cachedTransactions) {
            if (is(Transaction.Status.VALID, tx) && is(Transaction.TransactionType.DUE, tx) && !"".equals(tx.dueDateISO)) {
                newestDueDate = tx.dueDateISO;
            }
        }
        return newestDueDate;
    }
    private long sumTx(Predicate<Transaction> condition) {
        long amount = 0L;
        for (Transaction tx: cachedTransactions) {
            if (condition.test(tx) && tx.amount != null) {
                amount += tx.amount.grossCent;
            }
        }
        return amount;
    }
    public long getOpenPayments() {
        return sumTx(tx ->
                (is(Transaction.Status.TENTATIVE, tx) || is(Transaction.Status.PENDING, tx))
                        && is(Transaction.TransactionType.PAYMENT, tx)
        );
    }
    public long getTotalPayments() {
        return sumTx(tx ->
                is(Transaction.Status.VALID, tx) && is(Transaction.TransactionType.PAYMENT, tx)
        );
    }
    public long getTotalDues() {
        return sumTx(tx ->
                is(Transaction.Status.VALID, tx) && is(Transaction.TransactionType.DUE, tx)
        );
    }
    public long getRemainingDues() {
        return getTotalDues() - getTotalPayments();
    }
}
