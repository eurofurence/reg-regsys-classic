package org.eurofurence.regsys.service.camtv8;

import java.util.List;

// Entry is a mapped and filtered (BOOK only, not INFO or PDNG) Ntry
public class Entry {
    public long amount; // in cents as seen from our perspective, < 0 means CRDT, > 0 DBIT
    public String currency;

    public String bookingDate;
    public String valuationDate;

    public String debitorAccount;
    public String debitorName;
    public String debitorBIC;
    public String debitorBank;

    public List<String> information; // RmtInf.Ustrd

    @Override
    public String toString() {
        return "Entry [amount=" + amount + ", currency=" + currency + ", bookingDate="
                + bookingDate + ", valuationDate=" + valuationDate + ", debitorAccount="
                + debitorAccount + ", debitorName=" + debitorName + ", debitorBIC="
                + debitorBIC + ", debitorBank=" + debitorBank + ", information="
                + information + "]";
    }

    // mapping and basic filtering helpers

    public static boolean filterValid(Camtv8Document.Ntry candidate) {
        if (candidate == null) {
            return false;
        }
        return candidate.getStatus() == Camtv8Document.Ntry.StsEnum.BOOK;
    }

    public static Entry fromNtry(Camtv8Document.Ntry source) {
        Entry entry = new Entry();
        entry.amount = source.getAmount();
        entry.currency = source.getCurrency();

        entry.bookingDate = source.getBookingDate();
        entry.valuationDate = source.getValuationDate();

        entry.debitorAccount = source.getDebitorAccount();
        entry.debitorName = source.getDebitorName();
        entry.debitorBIC = source.getDebitorBIC();
        entry.debitorBank = source.getDebitorBank();

        entry.information = source.getInformation();
        return entry;
    }
}
