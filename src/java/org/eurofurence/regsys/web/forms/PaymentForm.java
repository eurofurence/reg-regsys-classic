package org.eurofurence.regsys.web.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.PermissionDeniedException;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.backend.persistence.DbException;
import org.eurofurence.regsys.backend.persistence.TypeChecks;
import org.eurofurence.regsys.backend.types.IsoDate;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;
import org.eurofurence.regsys.web.pages.PaymentPage;

/**
 *  Represents the forms used on the payment page:
 *  EditPayment, DeletePayment, ShowPayment
 *
 *  This Form understands these request parameters:
 *      param_id             id of the attendee
 *      transaction_id       id of the payment/dues transaction
 *      amount               currency amount
 *      received             date the payment was received (effective date)
 *      transaction_type     due, payment
 *      transaction_method   bank transfer, credit card, ... (ignored for dues)
 *      transaction_status   tentative, pending, valid, ...
 *      action               delete => delete payment
 */
public class PaymentForm extends Form {

    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String TRANSACTION_ID = "transaction_id";
    public static final String AMOUNT = "amount";
    public static final String RECEIVED = "received";
    public static final String TRANSACTION_TYPE = "transaction_type";
    public static final String TRANSACTION_METHOD = "transaction_method";
    public static final String TRANSACTION_STATUS = "transaction_status";
    public static final String COMMENTS = "comments";
    public static final String ACTION = "action";

    // ------------ attributes -----------------------

    private AttendeeSearchResultList.AttendeeSearchResult attendee;
    private List<Transaction> transactions;
    private Transaction transaction;

    private final PaymentService paymentService = new PaymentService();

    // ------------ constructors -----------------------

    /**
     *  default constructor
     */
    public PaymentForm() {
        attendee = new AttendeeSearchResultList.AttendeeSearchResult();
        transaction = new Transaction();
        transaction.transactionType = Transaction.TransactionType.PAYMENT.getValue();
        transaction.method = Transaction.Method.TRANSFER.getValue(); // most likely for admin to want to enter
        transaction.status = Transaction.Status.VALID.getValue();
        transactions = new ArrayList<>();
    }

    // ---------- proxy methods for entity access -------
    // includes type conversions from web to java

    // field accessors for attendee
    public String getAttendeeId() {
        return Long.toString(attendee.id);
    }

    public String getAttendeeRegistered() {
        return attendee.registered;
    }

    public String getAttendeeNickname() {
        return attendee.nickname;
    }

    public String getAttendeeLastName() {
        return attendee.lastName;
    }

    public String getAttendeeFirstName() {
        return attendee.firstName;
    }

    public String getAttendeeEmail() {
        return attendee.email;
    }

    public String getAttendeeTotalDuesString() {
        return FormHelper.toCurrencyDecimals(attendee.totalDues);
    }

    public long getAttendeeCurrentDues() {
        return attendee.currentDues;
    }

    public String getAttendeeCurrentDuesString() {
        return FormHelper.toCurrencyDecimals(attendee.currentDues);
    }

    public String getAttendeeDueDate() {
        return attendee.dueDate;
    }

    public boolean hasPendingPayments() {
        // TODO
        return false;
        // return attendee.getPaymentsPending();
    }

    // field accessors for payments
    public String getAmount() {
        return FormHelper.toCurrencyDecimals(transaction.amount.grossCent);
    }

    public void setAmount(String t) {
        transaction.amount.grossCent = FormHelper.parseCurrencyDecimals(getPage(), t, Strings.paymentForm.fieldPaymentAmount, transaction.amount.grossCent);
    }

    public String getTransactionId() {
        return transaction.transactionIdentifier;
    } // read only

    public String getComments() {
        return escape(transaction.comment);
    }

    public void setComments(String t) {
        transaction.comment = t;
    }

    public String getReceived() {
        return transaction.effectiveDate;
    }

    public void setReceived(String t) {
        try {
            IsoDate tdate = new IsoDate().fromDate(TypeChecks.parseDate(t, "received", Strings.conf.paymentStart, Strings.util.payDateMessage,
                    Strings.conf.paymentEnd, Strings.util.payDateMessage));

            transaction.effectiveDate = t;
        } catch (DbDataException e) {
            getPage().addError(e.getMessage());
        }
    }

    public Transaction.TransactionType getTransactionType() {
        return Transaction.TransactionType.byValueOrDefault(transaction.transactionType, Transaction.TransactionType.PAYMENT);
    }

    public void setTransactionType(String v) {
        Transaction.TransactionType t = Transaction.TransactionType.byValueOrDefault(v, null);
        if (t == null) {
            getPage().addError(Strings.paymentForm.invalidTypeMissing);
        } else {
            transaction.transactionType = t.getValue();
        }
    }

    public Transaction.Method getMethod() {
        return Transaction.Method.byValueOrDefault(transaction.method, Transaction.Method.TRANSFER);
    }

    public void setMethod(String v) {
        Transaction.Method t = Transaction.Method.byValueOrDefault(v, null);
        if (t == null) {
            getPage().addError(Strings.paymentForm.invalidMethodMissing);
        } else {
            transaction.method = t.getValue();
        }
    }

    public Transaction.Status getStatus() {
        return Transaction.Status.byValueOrDefault(transaction.status, Transaction.Status.VALID);
    }

    public void setStatus(String v) {
        Transaction.Status t = Transaction.Status.byValueOrDefault(v, null);
        if (t == null) {
            getPage().addError(Strings.paymentForm.invalidStatusMissing);
        } else {
            transaction.status = t.getValue();
        }
    }

    public Constants.MemberStatus getAttendeeStatus() {
        // TODO
        return Constants.MemberStatus.NEW;
        // return Constants.MemberStatus.byNewRegsysValue(attendee.status);
    }

    //
    // Business methods
    //

    public void initializeForAttendee(long the_id) throws DbException {
        if (the_id <= 0) throw new DbException(Strings.paymentForm.internalNeedId);

        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        criterion.ids = Collections.singletonList(the_id);
        criteria.matchAny.add(criterion);
        AttendeeSearchResultList attendeeResult = getPage().getAttendeeService().performFindAttendees(criteria, getPage().getTokenFromRequest(), getPage().getRequestId());

        if (attendeeResult.attendees == null || attendeeResult.attendees.size() == 0) {
            throw new DbException(Strings.paymentForm.noAttendeeReceived);
        }
        attendee = attendeeResult.attendees.get(0);

        transaction.debitorId = the_id;

        if (getPage().hasPermission(Constants.Permission.ADMIN)) {
            // initialize new payment to most probably values
            IsoDate tdate = new IsoDate();
            transaction.effectiveDate = tdate.getIsoFormat();

            if (attendee.currentDues != null && attendee.currentDues > 0) {
                transaction.amount.grossCent = attendee.currentDues;
            }
            // TODO - configuration
            transaction.amount.currency = "EUR";
        }
    }

    public void processAndStorePayment() {
        // TODO - payment service unavailable right now
        /*
        payment.storeToDB();

        // wait a moment, then reload the attendee (because hook might have updated it)
         */
    }

    public int count = -1; // -1: not loaded, 0: loaded, 1..n (1-based) on entry

    public void loadFirst(long debitor_id) {
        try {
            TransactionResponse response = paymentService.performFindTransactions(debitor_id, null, null, null, getPage().getTokenFromRequest(), getPage().getRequestId());
            if (response.payload == null || response.payload.size() == 0) {
                // valid case, has no transactions yet
                count = 0;
                transactions = new ArrayList<>();
            } else {
                count = 0;
                transactions = response.payload;
            }
        } catch (NotFoundException e) {
            // valid case, has no transactions yet
            count = 0;
            transactions = new ArrayList<>();
        }
    }

    public boolean loadNext() {
        if (count < 0) {
            addError("internal: call to loadNext() before call to loadFirst() - this is a bug");
            return false;
        }

        count++;
        if (count > transactions.size())
            return false;

        transaction = transactions.get(count - 1);
        return true;
    }

    public void getById(String id) {
        for (Transaction t: transactions) {
            if (id.equals(t.transactionIdentifier)) {
                transaction = t;
            }
        }
        getPage().addError(Strings.paymentForm.noSuchTransactionForAttendee);
    }

    private boolean typeIs(Transaction.TransactionType t) {
        return t.getValue().equals(transaction.transactionType);
    }

    private boolean statusIs(Transaction.Status s) {
        return s.getValue().equals(transaction.status);
    }

    public boolean canCancel(boolean addErrors) {
        if (!getPage().hasPermission(Constants.Permission.ADMIN)) {
            if (addErrors)
                addError(Strings.paymentForm.permCancelAdminOnly);
            return false;
        }

        if (transaction.transactionIdentifier == null || "".equals(transaction.transactionIdentifier)) {
            if (addErrors)
                addError(Strings.paymentForm.permCancelBeforeStore);
            return false;
        }

        if (!typeIs(Transaction.TransactionType.PAYMENT)) {
            if (addErrors)
                addError(Strings.paymentForm.duesExternallyManaged);
            return false;
        }

        if (statusIs(Transaction.Status.TENTATIVE) || statusIs(Transaction.Status.PENDING)) {
            return true;
        }
        if (statusIs(Transaction.Status.DELETED)) {
            if (addErrors)
                addError(String.format(Strings.paymentForm.invalidStatusForCancel, transaction.status));
            return false;
        }

        // for status confirmed, only within 48 hours
        if (transaction.creationDateISO != null) {
            IsoDate iso = new IsoDate().fromIsoFormat(transaction.creationDateISO);
            if (StrictMath.abs(iso.getAsDate().getTime() - (new java.util.Date()).getTime()) > HardcodedConfig.PAYMENT_CANCEL_HOURS * 3600L * 1000L) {
                if (addErrors)
                    addError(String.format(Strings.paymentForm.tooLateForCancel, HardcodedConfig.PAYMENT_CANCEL_HOURS));
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean canConfirm(boolean addErrors) {
        if (!getPage().hasPermission(Constants.Permission.ADMIN)) {
            if (addErrors)
                addError(Strings.paymentForm.permConfirmAdminOnly);
            return false;
        }

        if (!typeIs(Transaction.TransactionType.PAYMENT)) {
            if (addErrors)
                addError(Strings.paymentForm.duesExternallyManaged);
            return false;
        }

        if (statusIs(Transaction.Status.TENTATIVE) || statusIs(Transaction.Status.PENDING)) {
            return true;
        }

        if (addErrors)
            addError(String.format(Strings.paymentForm.invalidStatusForConfirm, transaction.status));
        return false;
    }

    /** Cancels this payment (if allowed), commits to DB */
    public void cancelCurrentPayment() {
        if (canCancel(true)) {
            transaction.status = Transaction.Status.DELETED.getValue();
            processAndStorePayment();
        }
    }

    /** sets this payment status to confirmed, commits to DB */
    public void confirmCurrentPayment() {
        if (canConfirm(true)) {
            transaction.status = Transaction.Status.VALID.getValue();
            processAndStorePayment();
        }
    }

    // ----------- functionality ------------------------

    /**
     * Process submitted form data, making the requested changes to the current payment.
     *
     * Throws <code>PermissionDeniedException</code> if not admin.
     */
    public void processAdminRequest(HttpServletRequest request) throws PermissionDeniedException {
        if (!getPage().hasPermission(Constants.Permission.ADMIN)) {
            throw new PermissionDeniedException(Strings.paymentForm.permNotAuthorizedManualChanges);
        }

        String txId = request.getParameter(TRANSACTION_ID);
        if (txId == null) {
            txId = "";
        }
        if (!"".equals(txId)) {
            // load and change an existing payment
            getById(txId);
        }

        // TODO this needs more testing!!!

        // check for payment confirm
        if (request.getParameter(AMOUNT) != null) {
            if (Transaction.TransactionType.DUE.getValue().equals(transaction.transactionType)) {
                addError(Strings.paymentForm.duesExternallyManaged);
                return;
            }

            setAmount(request.getParameter(AMOUNT));
            setReceived(request.getParameter(RECEIVED));
            setMethod(request.getParameter(TRANSACTION_METHOD));
            setComments(request.getParameter(COMMENTS));

            if (!getPage().hasErrors()) {
                confirmCurrentPayment();
            }
        }

        // check for payment delete
        if (request.getParameter(ACTION) != null && request.getParameter(ACTION).equals("cancel")) {
            cancelCurrentPayment();
        }
    }

    private boolean showCurrentTransaction() {
        if (getPage().hasPermission(Constants.Permission.ADMIN) || getPage().hasPermission(Constants.Permission.VIEW)) {
            if (getStatus() != Transaction.Status.DELETED) {
                return true;
            }
        } else {
            // normal user
            if (getStatus() != Transaction.Status.DELETED && getStatus() != Transaction.Status.TENTATIVE) {
                return true;
            }
        }
        return false;
    }

    // list output methods
    public ArrayList<String[]> getPaymentWebListing(long attendee_id) {
        ArrayList<String[]> paymentlines = new ArrayList<String[]>();
        loadFirst(attendee_id);

        while (loadNext()) {
            if (!showCurrentTransaction())
                continue;

            String statusHtml = "";
            if (canConfirm(false)) {
                statusHtml += "<a href='#' onClick=\"fillInForm('" + getAttendeeId() + "', '" + getTransactionId()
                        + "', '" + getReceived() + "', '" + getAmount() + "', '"
                        + getStatus().getValue() + "', '" + getMethod().getValue() + "', '"
                        + getComments()
                        + "'); return false;\"><img src='images/confirm.gif' title='confirm this payment' border=0></a>";
                statusHtml += "&nbsp";
            }
            if (canCancel(false)) {
                statusHtml += "<a href='#' onClick=\"confirmCancel('" + getAttendeeId() + "', '"
                        + getTransactionId() + "', '" + getReceived() + "', '" + getAmount() + "', '"
                        + getStatus().getValue() + "', '" + getMethod().getValue() + "', '"
                        + getComments()
                        + "'); return false;\"><img src='images/delete.png' title='Cancel this payment' border=0></a>";
            }
            String styleClass = "searchlist";
            if (getStatus() == Transaction.Status.TENTATIVE)
                styleClass = "searchlist_prepending";
            if (getStatus() == Transaction.Status.PENDING)
                styleClass = "searchlist_pending";

            if (typeIs(Transaction.TransactionType.PAYMENT)) {
                paymentlines.add(new String[] {
                        escape(getAttendeeId()),
                        escape(getReceived()),
                        "&nbsp;",
                        escape(getAmount()),
                        escape(getStatus().getValue()),
                        statusHtml,
                        escape(getMethod().getValue()),
                        escape(getComments()),
                        styleClass
                });
            } else {
                paymentlines.add(new String[] {
                        escape(getAttendeeId()),
                        escape(getReceived()),
                        escape(getAmount()),
                        "&nbsp;",
                        escape(getStatus().getValue()),
                        "&nbsp;",
                        escape(getMethod().getValue()),
                        escape(getComments()),
                        styleClass
                });
            }
        }

        return paymentlines;
    }

    // --------------------- form output methods ------------------------------------------------

    // Delete Payment

    public String getDeleteFormComplete() {
        String result = "";

        result += "<FORM ID=\"payment_delete_form\" ACTION=\"payment\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
        result += "    " + Form.hiddenField(PaymentPage.ATTENDEE_ID, getAttendeeId()) + "\n";
        result += "    " + Form.hiddenField(TRANSACTION_ID, "0") + "\n";
        result += "    " + Form.hiddenField(ACTION, "cancel") + "\n";
        result += "</FORM>";

        return result;
    }

    // Edit Payment

    public String getEditFormHeader() {
        String result = "";

        result += "<FORM id=\"paymentform\" ACTION=\"payment\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
        result += "    " + Form.hiddenField(PaymentPage.ATTENDEE_ID, getAttendeeId()) + "\n";
        result += "    " + Form.hiddenField(TRANSACTION_ID, "0") + "\n";

        return result;
    }

    public String getEditFormAmount(int displaySize) {
        return Form.textField(true, AMOUNT, getAmount(), displaySize, 10);
    }

    public String getEditFormReceived(int displaySize) {
        return Form.textField(true, RECEIVED, getReceived(), displaySize, 10);
    }

    public String getEditFormMethod(String style) {
        String[] values = Arrays.stream(Transaction.Method.values())
                .map(Transaction.Method::getValue)
                .toArray(String[]::new);
        return selector(true, TRANSACTION_TYPE,
                values,
                values,
                getMethod().getValue(), 1, false, style);
    }

    public String getEditFormComments(int displaySize) {
        return Form.textField(true, COMMENTS, getComments(), displaySize, 200);
    }

    public String getEditFormSubmitButton(String caption, String style) {
        // TODO: there should be methods in Form to create submit and reset buttons
        return "<INPUT TYPE=\"SUBMIT\" VALUE=\"" + escape(caption) + "\" CLASS=\"" + escape(style) + "\"/>";
    }

    public String getEditFormFooter() {
        return "</FORM>";
    }

    // View Payment

    public String getViewFormAttendeeId() {
        return Form.escape(getAttendeeId());
    }

    public String getViewFormRegistrationDate() {
        return Form.escape(getAttendeeRegistered());
    }

    public String getAttendeeEditURL() {
        return "input?param_id=" + getAttendeeId();
    }

    public String getViewFormNickname() {
        return Form.escape(getAttendeeNickname());
    }

    public String getViewFormRLName() {
        return Form.escape(getAttendeeFirstName() + " " + getAttendeeLastName());
    }

    public String getViewFormEmail() {
        return Form.escape(getAttendeeEmail());
    }

    public String getViewFormTotalDues() {
        return Form.escape(getAttendeeTotalDuesString());
    }

    public boolean isParticipating() {
        return getAttendeeStatus().isParticipating();
    }

    public String getViewFormRemainingDues() {
        return Form.escape(getAttendeeCurrentDuesString());
    }

    public boolean hasRemainingDues() {
        return (getAttendeeCurrentDues() > 0);
    }

    public String getViewFormDueDate() {
        return getAttendeeDueDate();
    }
}
