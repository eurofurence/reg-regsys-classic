package org.eurofurence.regsys.backend;

/**
 * Externalize human-readable message strings from the backend.
 *
 * See Strings.java for explanation.
 *
 * This is the English version of the strings, used by Eurofurence.
 */
public class StringsEF {
    public static class ConfigStrings {
        public String conventionLongname = "Eurofurence 28";
        public String conventionHumanReadableDate = "September 18-21, 2024"; // used in the navbar
        public String conventionYear = "2024"; // used in lots of places on the web page and for credit card payment subject

        public String termsURL = "https://help.eurofurence.org/legal/terms";
        public String rulesURL = "https://help.eurofurence.org/legal/roc";

        public String regMgmtEmailNontechnical = "nobody@eurofurence.org"; // otrs ticket queue
        public String regMgmtEmailTechnical = "nobody@eurofurence.org";

        public String imprintURL = "https://help.eurofurence.org/legal/imprint";
        public String imprintText = "Imprint";
        public String imprintHTML = "<font size=\"-2\">(Impressum)</font>";
        public String privacyStatementURL = "https://help.eurofurence.org/legal/privacy";
        public String privacyStatementText = "Privacy&nbsp;Statement";
        public String privacyStatementHTML = "<font size=\"-2\">(Datenschutzerkl&auml;rung)</font><br/>";

        public String navbarBgcolor = "#006458";
        public String navbarWidth = "168";
        public String navbarLogoWidth = "168";
        public String navbarLogoHeight = "487";

        public String dateFormat = "dd.MM.yyyy";

        public String defaultBirthday = "31.12.1900";

        public String paymentStart = "01.12.2023";
        public String paymentEnd = "31.12.2024";

        public String conStart = "18.09.2024";
        public String conEnd = "21.09.2024";
    }

    public static class UtilMsgs {
        public String fieldMandatory = "Field '%1$s' is mandatory.";
        public String invalidInt = "Wrong value for numeric field '%1$s': %2$s";
        public String intTooSmall = "Value too small for '%1$s'.";
        public String intTooLarge = "Value too large for '%1$s'.";
        public String invalidCurrencyAmount = "Field '%1$s' was not a valid currency amount: %2$s";
        public String invalidFloat = "Field '%1$s' was not a valid number: %2$s";
        public String floatBounds = "Field '%1$s' has to be between %2$.2f and %3$.2f.";
        public String nonexistantDate = "Some values were out of range. The date you specified doesn't exist.";
        public String invalidDate = "Invalid date '%2$s' for field '%1$s'. ";
        public String invalidEnum = "Invalid value '%2$s' for field '%1$s'. Allowed values are %3$s. ";
        public String invalidEmailAddress = "Invalid or empty email address '%2$s' for field '%1$s'. ";

        public String dateFormatMessage = "Please use format DD.MM.YYYY, as in 06.11.1972";
        public String tooOldMessage = "Over 115 years of age? Come on!";
        public String tooYoungMessage = "Sorry, people under 18 years cannot attend Eurofurence.";
        public String regDateMessage = "Registration starts Jan 2018 and ends on the last day of the con!";
        public String payDateMessage = "Payments must be received within 2024.";
    }

    public static class AccountingPageMsgs {
        public String pageTitle = "Accounting Page";
    }

    public static class AnnouncementPageMsgs {
        public String functionSave = "Save Mail Template";
        public String functionSaveNew = "Save New Mail Template";
        public String functionEdit = "Edit Mail Template";
        public String functionEditNew = "New Mail Template";
        public String functionList = "List Mail Templates";
        public String functionDeleteConfirm = "Delete Mail Template?";
        public String testSendFailure = "Failed to send test email: ";
        public String dbErrorLoadList = "Error loading list of templates: ";
        public String dbErrorLoadById = "Error loading template '%1$s': ";
        public String dbErrorSave = "Error saving template: ";
        public String dbErrorDelete = "Error deleting template: ";
    }

    public static class BansPageMsgs {
        public String functionSave = "Save Ban";
        public String functionSaveNew = "Save New Ban";
        public String functionEdit = "Edit Ban";
        public String functionEditNew = "New Ban";
        public String functionList = "List Bans";
        public String functionDeleteConfirm = "Delete Ban?";
        public String dbErrorLoadList = "Error loading list of bans: ";
        public String dbErrorLoadById = "Error loading ban '%1$d': ";
        public String dbErrorSave = "Error saving ban: ";
        public String dbErrorDelete = "Error deleting ban: ";
    }

    public static class BulkmailPageMsgs {
        public String pageTitle = "Bulkmailer";
        public String notifyWaitingForInput = "Waiting for input";
        public String notifyReadyToSendFormat = "Ready to send bulkmail to %1$d recipients.";
        public String everyone = "everyone";
        public String notifyNoEmailsSent = "No emails were sent. Please try again.";
        public String notifyEmailsSent = "Emails have been sent to %1$d of %2$d attendees.";
        public String errorLoadList = "Error loading list of templates: ";
        public String errorsMsg = "There were errors (see the box above).";
        public String mustSelectTemplate = "You did not select a valid template";
        public String nullError = "Could not obtain attendees information - no mails were sent: got null or empty list";
        public String sendError = "Downstream error while mailing attendee with id %1$s. Skipping and proceeding. Error was: ";
        public String permMail = "Permission denied to mail attendee with id %1$s. Skipping and proceeding.";
    }

    public static class Camtv8ImportPageMsgs {
        public String pageTitle = "Import Bank Statement";
        public String uploadError = "Error uploading file: ";
        public String parseError = "Error parsing file: ";
        public String emptyError = "No entries found, or no entries with positive amounts";
        public String noAttendeesReceived = "Loading attendees failed";
        public String internalNeedId = "No value for badge number specified. This is a bug.";
    }

    public static class CommentsPageMsgs {
        public String pageTitle = "Comment Listing";
    }

    public static class EmailPageMsgs {
        public String notifySent = "Emails have been sent to %1$d of %1$d attendees:";
        public String notifyWaiting = "Waiting for your confirmation. Emails will be sent to the following %1$d users:";
        public String errorsMsg = "There were errors (see the box above).";
        public String none = "none";
        public String pageTitle = "Re-Send Status Emails";
        public String dbErrorAccept = "Downstream error while processing id %1$s. Skipping and proceeding. Error was: ";
        public String permAccept = "Permission denied for attendee with id %1$s. Skipping and proceeding.";
    }

    public static class ExportPageMsgs {
        public String pageTitle = "Exports";
    }

    public static class InputFormMsgs {
        public String invalidUpdatedParam = "Invalid value '%1$s' for param_updated.";
        public String roommateNotFound = "Roommate number %1$d not found";
        public String roommateNotGiven = "No roommate specified";
        public String dbError = "Downstream error: ";
        public String permError = "Permission denied: ";
        public String sessionErrorCouldNotSetupAuth = "Couldn't set authentication for this session. You might need to close your browser and login again. You should get an email "
                + "with a link to continue your registration. Otherwise, please contact " + Strings.conf.regMgmtEmailTechnical;
        public String confirmSuccess = "Email address has been confirmed.";
        public String confirmSendSuccess = "Confirmation email has been sent.";
        public String errorTemplateLoadConfirmNew = "Couldn't load email template. Your registration was saved, but "
                + "we were unable to send mail to you with a confirmation link. Please contact " + Strings.conf.regMgmtEmailNontechnical + " to have your registration activated.";
        public String errorSendMailConfirmNew = "Couldn't send email. Your registration was saved, but "
                + "we were unable to send mail to you with a confirmation link. Please contact " + Strings.conf.regMgmtEmailNontechnical + " to have your registration activated.";
        public String errorTemplateLoadInitialPasswordMail = "Couldn't load email template. Your registration was saved, but "
                + "we were unable to send mail to you with your initial password. Please contact " + Strings.conf.regMgmtEmailNontechnical + " to have your registration activated.";
        public String errorSendMailInitialPasswordMail = "Couldn't send email. Your registration was saved, but "
                + "we were unable to send mail to you with your initial password. Please contact " + Strings.conf.regMgmtEmailNontechnical + " to have your registration activated.";
        public String errorCreateToken = "Internal Error: email token is not set. Please inform " + Strings.conf.regMgmtEmailTechnical + " about this. Email NOT sent.";
        public String errorTemplateLoadChangedEmail = "Couldn't load email template. Your email address change cannot be activated - "
                + "please try changing it again. Please contact " + Strings.conf.regMgmtEmailTechnical + " in case of continued trouble.";
        public String errorSendMailChangedEmail = "Couldn't send email. Your email address change cannot be activated - "
                + "please try changing it again. Please contact " + Strings.conf.regMgmtEmailTechnical + " in case of continued trouble.";
        public String invalidFlagCode = "Flag code '%1$s' not recognized";
        public String needYourAgreement = "You must read and agree to the Terms and Conditions to be allowed to register.";
        public String emailsDoNotMatch = "The two email addresses do not match. Please enter the same email address twice.";
        public String dbErrorSaveManualDues = "Unable to save manual dues settings due to database error: ";
        public String tshirtSizeSelectPrompt = "[Select Size]";
        public String roomSelectPrompt = "[Select Room]";
        public String adminLinkRoommateNoneDescription = "No id given";
        public String adminLinkRoommateInvalid = "invalid id:";
        public String displayRoommateNoneDescription = "No id given";
        public String displayRoommateInvalid = "invalid id:";
        private String onlyRoommateCaption = "Roommate";
        private String firstRoommateCaption = "First Roommate";
        private String secondRoommateCaption = "Second Roommate";
        private String thirdRoommateCaption = "Third Roommate";
        public String noRoomSelectorOption = "-- NONE --";
        public String errorCannotLoadRoomList = "Unable to load the list of rooms from the database: ";
        public String needToSelectAtLeastOnePackage = "You need to have at least one of the attendance packages selected, either full attendance or any selection of the day guest packages.";
        public String invalidAttendeeType = "Please write to us to be switched back from day guest to full membership. This needs to be done by an administrator.";
        public String mustProvideCancelReason = "You must enter a reason for the cancellation";

        public String parameterParseError(String param, String msg) {
            return "internal error: failed to parse parameter " + param + ": " + msg;
        }

        public String roommateCaption(int nr, int outOf) {
            if (outOf > 1) {
                if (nr == 1)
                    return firstRoommateCaption;
                if (nr == 2)
                    return secondRoommateCaption;
                if (nr == 3)
                    return thirdRoommateCaption;
            }
            return onlyRoommateCaption;
        }
    }

    public static class InputPageMsgs {
        public String pageTitleNew = "Registration Form";
        public String pageTitleEdit = "Registration Form - Edit Mode";
        public String permCanOnlyEditYourself = "Permission denied to edit anyone except yourself!";
        public String dbErrorReadRoomQuotas = "Could not read room quotas from database: ";
        public String roomQuotaNotAvailable = "Room quota unavailable";
        public String roomQuotaNoRoom = "No room package selected!";
        private String unlimitedSupplyDisplayFormat = "%1$d taken";
        private String limitedSupplyDisplayFormat = "%1$d of %2$d taken (%3$s)";

        public String unlimitedSupplyDisplay(int taken) {
            return String.format(unlimitedSupplyDisplayFormat, taken);
        }
        public String limitedSupplyDisplay(int taken, int available, String percentage) {
            return String.format(limitedSupplyDisplayFormat, taken, available, percentage);
        }
    }

    public static class ListPageMsgs {
        public String pageTitle = "Search Result Listing";
    }

    public static class LoginPageMsgs {
        public String pageTitle = "Attendee Login";
    }

    public static class MassApproveFormMsgs {
        public String attendeeNotNew = "Attendee with id %1$s was not in status NEW - skipping and proceeding.";
        public String dbErrorAccept = "Downstream error while setting attendee with id %1$s to APPROVED. Skipping and proceeding. Error was: ";
        public String permAccept = "Permission denied to set attendee with id %1$s to APPROVED. Skipping and proceeding.";
        public String successMsg = "Successfully approved %1$d of %2$d attendees. ";
        public String readyMsg = "Ready to mass approve these %1$d attendees";
        public String errorsMsg = "There were errors. Please review the error box!";
    }

    public static class MassApprovePageMsgs {
        public String pageTitle = "Mass Approve";
    }

    public static class PaymentFormMsgs {
        public String fieldPaymentAmount = "amount";
        public String internalNeedId = "No value for param_id specified.";
        public String checkinNotAvailable = "Cannot check that attendee in. Either already is, or hasn't fully paid.";
        public String permNotAuthorizedManualChanges = "You are not authorized to make manual changes to payments.";
        public String noAttendeeReceived = "Loading attendee failed, maybe no such id?";
        public String noSuchTransactionForAttendee = "There is no transaction with this id for the current attendee.";
        public String duesExternallyManaged = "Transactions of type dues are internally managed. You cannot change them directly.";

        public String permSetAttId = "Permission denied to change attendee id for payment.";
        public String permEditAmount = "Only admin can edit payment amount.";
        public String invalidAmountZero = "Only accepting nonzero amounts for payments!";
        public String permEditComments = "Only admin can edit payment comments.";
        public String permEditReceived = "Only admin can edit payment received date.";
        public String permEditType = "Only admin can edit payment type.";
        public String invalidTypeMissing = "Please specify a Transaction type";
        public String invalidMethodMissing = "Please specify a Payment method";
        public String invalidStatusMissing = "Please specify a Transaction status";
        public String permTokenOnlyOnNew = "Identity token can only be set on new payments.";
        public String dbErrorParse = "Error parsing get query result: ";
        public String needStatus = "Payment Status must be set";
        public String needType = "Payment Type must be set";
        public String permCancelAdminOnly = "Only the admin can cancel payments!";
        public String permCancelBeforeStore = "Cannot cancel unstored payments!";
        public String tooLateForCancel = "Cannot cancel payments that weren't made in the last " + HardcodedConfig.PAYMENT_CANCEL_HOURS + " hours!";
        public String permConfirmAdminOnly = "Only an admin can confirm payments!";
        public String invalidStatusForCancel = "Payment with status %1$s cannot be cancelled. Only new and pending payments can be confirmed.";
        public String invalidStatusForConfirm = "Payment with status %1$s cannot be confirmed. Only new and pending payments can be confirmed.";
    }

    public static class PaymentPageMsgs {
        public String pageTitle = "Payments";
        public String needsAttendeeId = "You need to specify a positive numerical attendee id as param_id. Or maybe you're not logged in?";
    }

    public static class SearchFormMsgs {
        public String nobodyFound = "Sorry, but no corresponding registration could be found. Please try again with a different set of search parameters.";
        public String invalidSearchOrder = "Invalid search order. Must be one of: ";
        public String invalidFlagValue = "Can only set flag '%1$s' to values 0 or 1";
        public String invalidPackageValue = "Can only set package '%1$s' to values 0 or 1";
    }

    public static class SearchPageMsgs {
        public String pageTitle = "Search Form";
    }

    public static class SepaPageMsgs {
        public String pageTitle = "Sepa Payment Information";
        public String nothingToPay = "You have no open payments";
        public String wrongStatus = "Your status does not permit payments";
        public String backendError = "Backend Request Failure";
        public String transactionNotFound = "Transaction Not Found";
        public String transactionWrongStatus = "Wrong Transaction Status (maybe already paid?)";
        public String transactionNotEligible = "Wrong Transaction Type (maybe not a sepa payment or a dues booking?)";
        public String wrongAttendee = "Transaction does not belong to logged in attendee";
    }

    public static class StartPageMsgs {
        public String notFoundTitle = "Registration System - Not Found";
        public String pageTitle = "Registration System - Start Page";
        public String unexpectedError = "An unexpected error has occurred.";
        private String timeUntilStartFormat = "That is in %1$d Days, %2$d Hours, %3$d Minutes, %4$d Seconds";
        public String shouldHaveStarted = "Umm ... well ... it should have started already. Getting there...";

        public String timeUntilStart(long days, long hours, long mins, long secs) {
            return String.format(timeUntilStartFormat, days, hours, mins, secs);
        }
    }

    public static class StatsPageMsgs {
        public String pageTitle = "Statistics";
    }

    public static class GroupsPageMsgs {
        public String pageTitle = "Roomshare Groups";
    }

    public static class RoomsPageMsgs {
        public String pageTitle = "Rooms";
    }
}
