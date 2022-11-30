package org.eurofurence.regsys.backend;

import java.util.ArrayList;
import java.util.List;

import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.repositories.errors.DownstreamException;

/**
 * This class holds several enum declarations. Unfortunately it's not possible to access Enums directly from JSP pages,
 * so we need to declare them as inner classes.
 *
 * @author Zefiro
 */
public class Constants {

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=- MemberStatus
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    public enum MemberStatus {
        /** intval=0, new confirmed registration */
        NEW(0, "New", "new"),
        /** intval=1, accepted registration */
        APPROVED(1, "Approved", "approved"),
        /** intval=2, paid */
        PAID(2, "Paid", "paid"),
        /** intval=4, checked in at consite */
        CHECKED_IN(4, "Checked In", "checked in"),
        /** intval=8, on waiting list */
        WAITING(8, "Waiting", "waiting"),
        /** intval=9, Membership is cancelled. Please set reason */
        CANCELLED(9, "Cancelled", "cancelled"),
        /** intval=-1, used to ignore this field in search requests */
        SEARCH_IGNORE(-1, "Any", "(any)"),
        /** intval=-2, used to indicate bogus/duplicate entries which should not be shown at all */
        DELETED(-2, "[DELETED]", "deleted"),
        /** used to explicitly search including deleted attendees */
        EVEN_DELETED(-3, "Any Including Deleted", "(none)"),
        /** intval=6, Partially Paid, calculateDues() toggles between this and Paid */
        PARTIALLY_PAID(6, "Partially Paid", "partially paid");

        /**
         * int value as used in the database.
         * <p>
         * This is the int value as previously defined. Since the original values ranged from -1 and contained holes we
         * can't use the internal enum values.
         */
        private final int    dbValue;

        /** display name */
        private final String displayText;

        private final String newRegsysValue;

        MemberStatus(int intVal, String displayName, String newRegsysValue) {
            this.dbValue = intVal;
            this.displayText = displayName;
            this.newRegsysValue = newRegsysValue;
        }

        /** Returns the internal int value of the member status, as used in the database */
        public int dbValue() {
            return this.dbValue;
        }

        /** Returns the textual representation of the member status */
        public String toString() {
            return this.displayText;
        }

        public String newRegsysValue() {
            return this.newRegsysValue;
        }

        public static MemberStatus byNewRegsysValue(String value) {
            for (MemberStatus ms : MemberStatus.values()) {
                if (ms.newRegsysValue().equals(value)) {
                    return ms;
                }
            }
            throw new DownstreamException("received invalid value for status: " + value);
        }

        /**
         * Returns the MemberStatus instance corresponding to this int value.
         * <p>
         * throws DbDataException in case of invalid value.
         */
        public static MemberStatus getMemberStatus(int value) throws DbDataException {
            for (MemberStatus ms : MemberStatus.values()) {
                if (ms.dbValue() == value) {
                    return ms;
                }
            }
            throw new DbDataException("Invalid value for status encountered: " + value);
        }

        /**
         * checks whether this status is a valid membership status, i.e. whether it is not the special value
         * SEARCH_IGNORE
         */
        public boolean isValid() {
            return this != MemberStatus.SEARCH_IGNORE;
        }

        public boolean isParticipating() {
            if (MemberStatus.APPROVED == this || MemberStatus.PARTIALLY_PAID == this || MemberStatus.PAID == this
                    || MemberStatus.CHECKED_IN == this) return true;
            return false;
        }

        public boolean isNotParticipating() {
            if (MemberStatus.CANCELLED == this || MemberStatus.WAITING == this || MemberStatus.DELETED == this)
                return true;
            return false;
        }

        /**
         * Returns whether the due dates should be recalculated if the status is changed from this to ACCEPTED. I.e. it
         * should not be changed if previous (=this) state was already e.g. PAID.
         */
        public boolean isExtendingDueDate() {
            return (MemberStatus.NEW == this || MemberStatus.WAITING == this
                    || MemberStatus.CANCELLED == this || MemberStatus.DELETED == this);
        }

        /** Used by "set status" dropdown - filters out values which should not be displayed, also defines the ordering */
        public static List<Constants.MemberStatus> getDropDownList_Admin_AvailableOnly(Constants.MemberStatus current) {
            List<Constants.MemberStatus> statusList = new ArrayList<>();
            switch(current) {
                case NEW:
                    statusList.add(Constants.MemberStatus.NEW);
                    statusList.add(Constants.MemberStatus.APPROVED);
                    statusList.add(Constants.MemberStatus.WAITING);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    statusList.add(Constants.MemberStatus.DELETED);
                    break;
                case APPROVED:
                case PARTIALLY_PAID:
                case PAID:
                    statusList.add(Constants.MemberStatus.NEW);
                    statusList.add(current); // dues recalc triggers status change anyway
                    statusList.add(Constants.MemberStatus.CHECKED_IN);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    break;
                case CHECKED_IN:
                    statusList.add(Constants.MemberStatus.APPROVED);
                    statusList.add(Constants.MemberStatus.CHECKED_IN);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    break;
                case WAITING:
                    statusList.add(Constants.MemberStatus.NEW);
                    statusList.add(Constants.MemberStatus.APPROVED);
                    statusList.add(Constants.MemberStatus.WAITING);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    statusList.add(Constants.MemberStatus.DELETED);
                    break;
                case CANCELLED:
                    statusList.add(Constants.MemberStatus.NEW);
                    statusList.add(Constants.MemberStatus.APPROVED);
                    statusList.add(Constants.MemberStatus.WAITING);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    statusList.add(Constants.MemberStatus.DELETED);
                    break;
                case DELETED:
                    statusList.add(Constants.MemberStatus.NEW);
                    statusList.add(Constants.MemberStatus.APPROVED);
                    statusList.add(Constants.MemberStatus.WAITING);
                    statusList.add(Constants.MemberStatus.CANCELLED);
                    statusList.add(Constants.MemberStatus.DELETED);
                    break;
            }
            return statusList;
        }

        /** Used by "search" dropdown - filters out values which should not be displayed, also defines the ordering */
        public static List<Constants.MemberStatus> getDropDownList_Search() {
            List<Constants.MemberStatus> statusList = new ArrayList<Constants.MemberStatus>();
            statusList.add(Constants.MemberStatus.SEARCH_IGNORE);
            statusList.add(Constants.MemberStatus.NEW);
            statusList.add(Constants.MemberStatus.APPROVED);
            statusList.add(Constants.MemberStatus.PARTIALLY_PAID);
            statusList.add(Constants.MemberStatus.PAID);
            statusList.add(Constants.MemberStatus.CHECKED_IN);
            statusList.add(Constants.MemberStatus.WAITING);
            statusList.add(Constants.MemberStatus.CANCELLED);
            return statusList;
        }

        /** Used by "search" dropdown */
        public static String[] getDbValueArray(List<Constants.MemberStatus> statusList) {
            String[] arr = new String[statusList.size()];
            int idx = 0;
            for (Constants.MemberStatus status : statusList) {
                arr[idx++] = Integer.toString(status.dbValue());
            }
            return arr;
        }

        /** Used by "search" dropdown */
        public static String[] getStringArray(List<Constants.MemberStatus> statusList) {
            String[] arr = new String[statusList.size()];
            int idx = 0;
            for (Constants.MemberStatus status : statusList) {
                arr[idx++] = status.toString();
            }
            return arr;
        }

        /** Used by "inputForm" dropdown */
        public static String[] getDisplayNames(List<Constants.MemberStatus> statusList) {
            String[] arr = new String[statusList.size()];
            int idx = 0;
            for (Constants.MemberStatus status : statusList) {
                if (status == APPROVED || status == PARTIALLY_PAID || status == PAID)
                    arr[idx++] = "Approved/Partially Paid/Paid";
                else
                    arr[idx++] = status.toString();
            }
            return arr;
        }
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=- Permission constants
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    public enum Permission {
        /** Any logged in user */
        LOGIN("login", "Any logged in user"),
        /** Stats - may view the statistics page */
        STATS("stats", "May view the statistics"),
        /** Regdesk permission (can use the mobile regdesk page) */
        REGDESK("regdesk", "Regdesk / Conops Permission"),
        /** Full administrative rights (hard-coded "alway allow") */
        ADMIN("admin", "Full administrative rights"),
        /** Export_Conbook - may export names of (super)sponsors for the conbook */
        EXPORT_CONBOOK("export_conbook", "Export Sponsornames for Conbook"),
        /** Accounting - may view accounting pages and book or confirm payments */
        ACCOUNTING("accounting", "Book and confirm payments and see accounting pages"),
        /** View - may view all registrations and payments */
        VIEW("view", "View all registrations and their payment info"),
        /** Announce - may send announcements to attendees */
        ANNOUNCE("announce", "Edit announcements and bulk mail them"),
        /** Sponsor Item Desk permission (can use the mobile sponsor item desk page) */
        SPONSORDESK("sponsordesk", "Sponsor Item Desk Permission");

        private String dbValue;
        private String displayText;

        Permission(String dbValue, String displayText) {
            this.dbValue = dbValue;
            this.displayText = displayText;
        }

        /** Returns the internal int value of the member status, as used in the database */
        public String dbValue() {
            return this.dbValue;
        }

        /** Returns the textual representation of the member status */
        public String toString() {
            return this.displayText;
        }

        public static Permission getPermission(String value) throws DbDataException {
            for (Permission p : Permission.values()) {
                if (p.dbValue.equals(value)) {
                    return p;
                }
            }
            throw new DbDataException("Invalid value for permission encountered: " + value);
        }
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=- Export encoding configuration
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    public enum ExportEncoding {
        UTF8("UTF-8", "utf-8", true),
        ISO8859_1("ISO-8859-1", "iso-8859-1", false);

        private String streamEncoding;
        private String webResponseCharset;
        private boolean writeByteorderMark;

        ExportEncoding(String streamEncoding, String webResponseCharset, boolean writeByteorderMark) {
            this.streamEncoding = streamEncoding;
            this.webResponseCharset = webResponseCharset;
            this.writeByteorderMark = writeByteorderMark;
        }

        public String getStreamEncoding() {
            return this.streamEncoding;
        }

        public String getWebResponseCharset() {
            return this.webResponseCharset;
        }

        public boolean shouldWriteByteorderMark() {
            return this.writeByteorderMark;
        }
    }
}
