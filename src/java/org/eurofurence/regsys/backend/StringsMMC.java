package org.eurofurence.regsys.backend;

import org.eurofurence.regsys.backend.enums.RoomManagementOption;

/**
 * Externalize human-readable message strings from the backend.
 *
 * See Strings.java for explanation.
 *
 * This is the German version of the strings, used by MMC.
 */
public class StringsMMC {
    public static class ConfigStrings {
        public String conventionId = "mmc";
        public String conventionLongname = "Mephit Minicon 23";
        public String conventionShortname = "MMC";
        public String convention = "MMC 23";
        public String conventionHumanReadableDate = "26.-29.&nbsp;Mai&nbsp;2022"; // used in the navbar
        public String conventionYear = "2022"; // used in lots of places on the web page and for credit card payment subject

        public String policiesURL = "https://www.mephitminicon.de/hausordnung.html";
        public String hotelDescriptionURL = "http://TODO";
        public String termsURL = "https://www.mephitminicon.de/hausordnung.html";
        public String rulesURL = "https://www.mephitminicon.de/hausordnung.html";
        public String waiverURL = "https://www.mephitminicon.de/hausordnung.html";

        public String regMgmtEmailNontechnical = "nobody@mephitminicon.de";
        public String regMgmtEmailTechnical = "nobody@mephitminicon.de";

        public String goLiveDateFormat = "dd. MMMM yyyy, HH:mm:ss";
        public String goLiveDatePrefix = "Dienstag, ";

        public String imprintURL = "https://www.mephitminicon.de/impressum.html";
        public String imprintText = "Impressum";
        public String imprintHTML = "";
        public String privacyStatementURL = "https://www.mephitminicon.de/datenschutz.html";
        public String privacyStatementText = "Datenschutzerkl&auml;rung";
        public String privacyStatementHTML = "";

        public String navbarBgcolor = "#5c3f21";
        public String navbarWidth = "260";
        public String navbarLogoWidth = "254";
        public String navbarLogoHeight = "300";

        public String dateFormat = "dd.MM.yyyy";

        public String defaultBirthday = "31.12.1900";

        public String paymentStart = "01.01.2022";
        public String paymentEnd = "31.12.2022";

        public String conStart = "26.05.2022";
        public String conEnd = "29.05.2022";

        /* okay, so these are not strings, but it is useful to separate out all configuration */

        public RoomManagementOption roomConfiguration = RoomManagementOption.ROOMS_HOUSE_GROUPS;
    }

    public static class UtilMsgs {
        public String fieldMandatory = "Das Feld '%1$s' muss ausgef&uuml;llt werden.";
        public String invalidInt = "Falsche Eingabe. Nur Zahlen bitte. '%1$s': %2$s";
        public String intTooSmall = "Die angegebene Information ist zu kurz '%1$s'.";
        public String intTooLarge = "Die angegebene Information ist zu lang '%1$s'.";
        public String invalidFloat = "Die Zahl '%1$s' ist nicht g&uuml;ltig: %2$s";
        public String invalidCurrencyAmount = "Feld '%1$s' ist kein g&uuml;ltiger W&auml;hrungsbetrag: %2$s";
        public String floatBounds = "Die Angabe '%1$s' muss sich zwischen %2$.2f und %3$.2f bewegen.";
        public String nonexistantDate = "Diese Angabe ist nicht g&uuml;ltig. Das genannte Datum existiert nicht.";
        public String invalidDate = "Ung&uuml;ltiges Datum '%2$s' in Feld '%1$s'. ";
        public String invalidEnum = "Ung&uuml;ltiger Wert '%2$s' in Feld '%1$s'. G&uuml;ltige Werte sind %3$s. ";
        public String invalidEmailAddress = "E-Mail '%2$s' ung&uuml;ltig oder falsch in Eingabe '%1$s'. ";

        public String dateFormatMessage = "Bitte benutze folgendes Format f&uuml;r Datumsangaben DD.MM.YYYY, as in 06.11.1972";
        public String tooOldMessage = "115+ Jahre alt? Mumie oder Graf Dracula?";
        public String tooYoungMessage = "Als Gast der MMC musst Du mindestens 18 Jahre alt sein.";
        public String regDateMessage = "Die Registrierung startet am 25. November 2017!";
        public String payDateMessage = "Zahlungen m&uuml;ssen bis zur Con geleistet werden.";
    }

    public static class AccountingPageMsgs {
        public String pageTitle = "Buchhaltung";
    }

    public static class AnnouncementPageMsgs {
        public String functionSave = "Mailvorlage abspeichern";
        public String functionSaveNew = "Neue Mailvorlage abspeichern";
        public String functionEdit = "Mailvorlage bearbeiten";
        public String functionEditNew = "Neue Mailvorlage";
        public String functionList = "Mailvorlagen auflisten";
        public String functionDeleteConfirm = "Mailvorlage l&ouml;schen?";
        public String testSendFailure = "Fehler beim Versand der Testmail: ";
        public String dbErrorLoadList = "Fehler beim Laden der Mailvorlagen: ";
        public String dbErrorLoadById = "Fehler beim Laden der Mailvorlage '%1$s': ";
        public String dbErrorSave = "Fehler beim Speichern der Mailvorlage: ";
        public String dbErrorDelete = "Fehler beim L&ouml;schen der Mailvorlage: ";
    }

    public static class AttendeeSelectionFormMsgs {
        public String dbError = "Datenbankenfehler: ";
        public String permError = "Zugriff verweigert: ";
        public String mayBeBanned = "Gast mit ID %1$d ist wahrscheinlich gebannt. Die gew&uuml;nschte Operation ist mit dem Gast in diesem Kontext nicht erlaubt. Zur&uuml;ckgezogen!";
        public String mayBeBannedResultText = " ENTFERNT, WEIL H&Ouml;CHSTWAHRSCHEINLICH GEBANNT!";
        public String notFound = "Anmeldung mit ID %1$d nicht vorhanden. Entfernt. Fehler: ";
    }

    public static class BansPageMsgs {
        public String functionSave = "Ban abspeichern";
        public String functionSaveNew = "Neuen Ban abspeichern";
        public String functionEdit = "Ban bearbeiten";
        public String functionEditNew = "Neuer Ban";
        public String functionList = "Bans auflisten";
        public String functionDeleteConfirm = "Ban l&ouml;schen?";
        public String dbErrorLoadList = "Fehler beim Laden der Bans: ";
        public String dbErrorLoadById = "Fehler beim Laden des Bans '%1$d': ";
        public String dbErrorSave = "Fehler beim Speichern des Bans: ";
        public String dbErrorDelete = "Fehler beim L&ouml;schen des Bans: ";
    }

    public static class BulkmailPageMsgs {
        public String pageTitle = "Massenmailer";
        public String notifyWaitingForInput = "Warte auf Deine Eingabe";
        public String notifyReadyToSendFormat = "Bereit, um Mail an %1$d Teilnehmer zu senden.";
        public String everyone = "alle";
        public String notifyNoEmailsSent = "Es wurden keine Emails verschickt. Bitte erneut versuchen.";
        public String notifyEmailsSent = "Emails wurden an %1$d von %2$d Teilnehmern in der Empf&auml;ngerliste verschickt.";
        public String errorLoadList = "Fehler beim Laden der Mailvorlagen: ";
        public String errorsMsg = "Es sind Fehler aufgetreten (siehe oben).";
        public String mustSelectTemplate = "Bitte w&auml;hle ein Template aus";
        public String nullError = "Fehler bei Suchanfrage - keine Mails verschickt: null oder leere Liste";
        public String sendError = "Fehler beim Mailversand an Teilnehmer id %1$s. Teilnehmer &uuml;bersprungen. Fehler war: ";
        public String permMail = "Berechtigungsfehler beim Mailversand an Teilnehmer id %1$s. Teilnehmer &uuml;bersprungen.";
    }

    public static class CommentsPageMsgs {
        public String pageTitle = "Kommentare auflisten";
    }

    public static class EmailPageMsgs {
        public String notifySent = "An %1$d von %1$d G&auml;ste wurden E-Mails versendet:";
        public String notifyWaiting = "Warte auf Deine Best&auml;tigung. An folgende %1$d G&auml;ste werden E-Mails versendet:";
        public String errorsMsg = "Es sind Fehler aufgetreten (siehe oben).";
        public String none = "niemand";
        public String pageTitle = "Erneuter Statusmailversand";
        public String dbErrorAccept = "Es trat ein Fehler beim versenden an Teilnehmer %1$s auf. Fehler: ";
        public String permAccept = "Der Versand an Teilnehmer ID %1$s wurde verweigert. Fahre fort.";
    }

    public static class ExportPageMsgs {
        public String pageTitle = "Exporte";
    }

    public static class InputFormMsgs {
        public String invalidUpdatedParam = "Ung&uuml;ltiget Wert '%1$s' for param_updated.";
        public String roommateNotFound = "Zimmerpartner ID %1$d nicht gefunden";
        public String roommateNotGiven = "Kein Zimmerpartner wurde angegeben.";
        public String dbError = "Datenbankenfehler: ";
        public String permError = "Zugriff verweigert: ";
        public String sessionErrorCouldNotSetupAuth = "Die Sitzung konnte nicht angemeldet werden. Bitte schlie&szlig;e den Browser and versuche es erneut. Es sollte Dich eine E-Mail"
                + "mit einem Link zur Registrierung erreichen. Sollte das nicht der Fall sein, schreibe bitte an " + Strings.conf.regMgmtEmailTechnical;
        public String confirmSuccess = "Deine E-Mail Adresse wurde best&auml;tigt.";
        public String confirmSendSuccess = "Eine Best&auml;tigungsmail wurde verschickt.";
        public String errorTemplateLoadConfirmNew = "Die Vorlage f&uuml;r die E-Mail konnte nicht geladen werden. Deine Anmeldung wurde gespeichert, jedoch "
                + "konnte keine Mail mit dem Best&auml;tigungslink zu Die gesendet werden. Bitte schreibe an " + Strings.conf.regMgmtEmailNontechnical + " um Deine Registrierung zu aktivieren zu lassen.";
        public String errorSendMailConfirmNew = "Die E-Mail konnte nicht gesendet werden. Deine Anmeldung wurde gespeichert, jedoch "
                + "konnte keine Mail mit dem Best&auml;tigungslink zu Die gesendet werden. Bitte schreibe an " + Strings.conf.regMgmtEmailNontechnical + " um Deine Registrierung zu aktivieren zu lassen.";
        public String errorTemplateLoadInitialPasswordMail = "Die Vorlage f&uuml;r die E-Mail konnte nicht geladen werden. Deine Anmeldung wurde gespeichert, jedoch "
                + "konnte keine Mail mit dem Best&auml;tigungslink zu Die gesendet werden. Bitte schreibe an " + Strings.conf.regMgmtEmailNontechnical + " um Deine Registrierung zu aktivieren zu lassen.";
        public String errorSendMailInitialPasswordMail = "Die E-Mail konnte nicht gesendet werden. Deine Anmeldung wurde gespeichert, jedoch "
                + "konnte keine Mail mit dem Best&auml;tigungslink zu Die gesendet werden. Bitte schreibe an " + Strings.conf.regMgmtEmailNontechnical + " um Deine Registrierung zu aktivieren zu lassen.";
        public String errorCreateToken = "Interner Fehler. Status nicht gesetzt. Bitte informiere " + Strings.conf.regMgmtEmailTechnical + " &uuml;ber diesen Vorfall. Die Best&auml;tigungsmail konnte nicht gesendet werden.";
        public String errorTemplateLoadChangedEmail = "Die Vorlage f&uuml;r die E-Mail konnte nicht geladen werden. Die &Auml;nderung der E-Mail Adresse konnt nicht durchgef&uuml;hrt werden - "
                + "bitte versuche es erneut. Kontaktiere " + Strings.conf.regMgmtEmailTechnical + " sollten weiterhin Probleme bestehen.";
        public String errorSendMailChangedEmail = "Die E-Mail konnte nicht gesendet werden. Your email address change cannot be activated - "
                + "please try changing it again. Please contact " + Strings.conf.regMgmtEmailTechnical + " in case of continued trouble.";
        public String invalidFlagCode = "Status code '%1$s' nicht g&uuml;ltig";
        public String needYourAgreement = "Um Dich anzumelden, musst Du die Hausordnung gelesen und akzeptiert haben.";
        public String emailsDoNotMatch = "Die beiden Emailadressen sind nicht identisch. Bitte zweimal dasselbe eingeben.";
        public String dbErrorSaveManualDues = "Fehler beim setzen des manuellen Betrages in der Datenbank: ";
        public String tshirtSizeSelectPrompt = "[Gr&ouml;&szlig;e w&auml;hlen]";
        public String adminLinkRoommateNoneDescription = "Keine ID angegeben";
        public String adminLinkRoommateInvalid = "Ung&uuml;ltige ID id:";
        public String displayRoommateNoneDescription = "Keine ID angegeben";
        public String displayRoommateInvalid = "Ung&uuml;ltige ID:";
        private String onlyRoommateCaption = "Zimmerpartner";
        private String firstRoommateCaption = "Erster Zimmerpartner";
        private String secondRoommateCaption = "Zweiter Zimmerpartner";
        private String thirdRoommateCaption = "Dritter Zimmerpartner";
        public String noRoomSelectorOption = "-- KEIN ZIMMER ZUGEWIESEN --";
        public String errorCannotLoadRoomList = "Fehler beim Laden der Zimmerliste aus der Datenbank: ";
        public String needToSelectAtLeastOnePackage = "Du musst mindestens eine Teilnahmeoption ausw&auml;hlen.";
        public String invalidAttendeeType = "Bitte melde dich bei uns!";
        public String mustProvideCancelReason = "Du must einen Grund f&uuml;r die Absage eingeben.";

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
        public String pageTitleNew = "Registrierungsformular";
        public String pageTitleEdit = "Registrierungsformular - Daten bearbeiten";
        public String permCanOnlyEditYourself = "Zu wenig Rechte. Du kannst nur Deine eigenen Daten bearbeiten!";
        public String dbErrorReadRoomQuotas = "Der Zimmerpreis konnte nicht aus der Datenbank gelesen werden: ";
        public String roomQuotaNotAvailable = "Kein Zimmerpreis verf&uuml;gbar";
        public String roomQuotaNoRoom = "Keine Zimmeroption gew&auml;hlt!";
        private String unlimitedSupplyDisplayFormat = "%1$d gebucht";
        private String limitedSupplyDisplayFormat = "%1$d of %2$d gebucht (%3$s)";

        public String unlimitedSupplyDisplay(int taken) {
            return String.format(unlimitedSupplyDisplayFormat, taken);
        }
        public String limitedSupplyDisplay(int taken, int available, String percentage) {
            return String.format(limitedSupplyDisplayFormat, taken, available, percentage);
        }
    }

    public static class ListPageMsgs {
        public String pageTitle = "Ergebnisse der Suchanfrage";
    }

    public static class LoginPageMsgs {
        public String pageTitle = "G&auml;stelogin";
    }

    public static class MassApproveFormMsgs {
        public String attendeeNotNew = "Teilnehmer ID %1$s besitzt nicht den Status NEU - Schritt &uuml;bersprungen. Fahre fort.";
        public String dbErrorAccept = "Es trat ein Fehler beim Setzen des Status AKZEPTIERT von Teilnehmer %1$s . Fehler: ";
        public String permAccept = "Die &Auml;nderung des Status von Teilnehmer ID %1$s zu AKZEPTIERT wurde verweigert. Fahre fort.";
        public String successMsg = "Erfolgreich %1$d von %2$d Teilnehmern akzeptiert. ";
        public String readyMsg = "Bereit %1$d Teilnehmer zu akzeptieren. ";
        public String errorsMsg = "Fehler traten auf. Bitte beachte die Fehlermeldung!";
    }

    public static class MassApprovePageMsgs {
        public String pageTitle = "Stapelverarbeitung: Anmeldung Akzeptieren";
    }

    public static class PaymentFormMsgs {
        public String fieldPaymentAmount = "Betrag";
        public String internalNeedId = "Kein Wert wurde f&uuml;r param_id angegeben.";
        public String checkinNotAvailable = "Diese Teilnehmer ID kann nicht eingecheckt werden. Er ist es bereits oder es ist ein Restbetrag offen.";
        public String permNotAuthorizedManualChanges = "Du bist nicht berechtigung um &Auml;nderungen an den Betr&auml;gen vorzunehmen.";
        public String noAttendeeReceived = "Laden des Teilnehmers fehlgeschlagen, vielleicht gibt es keinen mit dieser id?";
        public String noSuchTransactionForAttendee = "Dieser Teilnehmer hat keine Transaktion mit dieser Id.";
        public String duesExternallyManaged = "Sollstellungen werden intern verwaltet und k&ouml;nnen nicht direkt bearbeitet werden.";

        public String permSetAttId = "Verweigert. Die Teilnehmer ID f&uuml;r die Zahlung wurde nicht ge&auml;ndert.";
        public String permEditAmount = "Nur ein Administrator kann den Zahlungsbetrag einpflegen.";
        public String invalidAmountZero = "Zahlungsbetr&auml;ge d&uuml;rfen NICHT 0 sein!";
        public String permEditComments = "Nur ein Administrator kann die Kommentare zum Zahlungseingang &auml;ndern.";
        public String permEditReceived = "Nur ein Administrator kann das Datum des Zahlungseingansg ver&auml;ndern.";
        public String permEditType = "Nur ein Administrator kann die Zahlungsart best&auml;tigen.";
        public String invalidTypeMissing = "Bitte spezifiziere den Transaktionstyp.";
        public String invalidMethodMissing = "Bitte spezifiziere die Zahlungsart.";
        public String invalidStatusMissing = "Bitte spezifiziere den Transaktionsstatus.";
        public String permTokenOnlyOnNew = "Identifikationsschl&uuml;ssel k&ouml;nnen nur bei neuen Zahlungen gesetzt werden.";
        public String dbErrorParse = "Fehler beim Lesen des Ergebnisses von 'get query': ";
        public String needStatus = "Der Status der Bezahlung muss gesetzt sein.";
        public String needType = "Die Zahlungsart muss angegeben sein.";
        public String permCancelAdminOnly = "Nur ein Administrator kann Zahlungen stornieren!";
        public String permCancelBeforeStore = "Ungespeicherte Zahlungen k&ouml;nnen nicht storniert werden!";
        public String tooLateForCancel = "Es k&ouml;nnen keine Zahlungen storniert werden, die in den letzten " + HardcodedConfig.PAYMENT_CANCEL_HOURS + " Stunden get&auml;tigt wurden!";
        public String permConfirmAdminOnly = "Nur ein Administrator kann Zahlungen best&auml;tigen!";
        public String invalidStatusForCancel = "Zahlungseingang mit Status %1$s kann nicht storniert werden.";
        public String invalidStatusForConfirm = "Zahlungseingang mit Status %1$s kann nicht best&auml;tigt werden. Nur neue oder ausstehende Zahlungen konnen best&auml;tigt werden.";
    }

    public static class PaymentPageMsgs {
        public String pageTitle = "Zahlung";
        public String needsAttendeeId = "Du musst eine positive Registrierungsnummer als param_id angeben. Dazu musst Du angemeldet sein?";
    }

    public static class SearchFormMsgs {
        public String nobodyFound = "Keine entsprechende Registrierung konnte gefunden werden. Bitte versuche es erneut, jedoch mit anderen Parametern.";
        public String invalidSearchOrder = "Ung&uuml;ltige Suchhreihenfolge. Bitte verwende: ";
        public String invalidFlagValue = "Der Wert '%1$s' kann nur auf 0 oder 1 ge&auml;ndert werden.";
        public String invalidPackageValue = "Die Art der Mitgliedschaft '%1$s' kann nur auf Wert 0 oder 1 ge&auml;ndert werden.";
    }

    public static class SearchPageMsgs {
        public String pageTitle = "Suchformular";
    }

    public static class StartPageMsgs {
        public String notFoundTitle = "Registrierung - Unbekannt";
        public String pageTitle = "Registrierung - Startseite";
        public String unexpectedError = "Ein unerwarteter Fehler traf auf.";
        private String timeUntilStartFormat = "Dies ist in %1$d Tagen, %2$d Stunden, %3$d Minuten, %4$d Sekunden";
        public String shouldHaveStarted = "Umm ... nunja ... es sollte bereits begonnen haben. Gleich...";

        public String timeUntilStart(long days, long hours, long mins, long secs) {
            return String.format(timeUntilStartFormat, days, hours, mins, secs);
        }
    }

    public static class StatsPageMsgs {
        public String pageTitle = "Statistiken";
    }
}
