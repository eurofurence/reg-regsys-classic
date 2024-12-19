package org.eurofurence.regsys.service.camtv8;

import jakarta.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name="Document")
@XmlAccessorType(XmlAccessType.FIELD)
public class Camtv8Document {
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Ntry")
    public static class Ntry {
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "Amt")
        public static class Amt {
            @XmlAttribute(name = "Ccy")
            public String ccy;

            @XmlValue
            public BigDecimal value;
        }

        @XmlType(name = "CdtDbtEnum")
        @XmlEnum
        public enum CdtDbtEnum {
            CRDT,
            DBIT;

            public String value() {
                return name();
            }

            public static CdtDbtEnum fromValue(String v) {
                return valueOf(v);
            }
        }

        @XmlType(name = "StsEnum")
        @XmlEnum
        public enum StsEnum {
            BOOK,
            INFO,
            PDNG;

            public String value() {
                return name();
            }

            public static StsEnum fromValue(String v) {
                return valueOf(v);
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "StsContainer")
        public static class StsContainer {
            @XmlElement(name = "Cd")
            public StsEnum cd;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "DtContainer")
        public static class DtContainer {
            @XmlElement(name = "Dt")
            @XmlSchemaType(name = "date")
            public XMLGregorianCalendar dt;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "RmtInfContainer")
        public static class RmtInfContainer {
            @XmlElement(name = "Ustrd")
            public List<String> ustrd;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "PartyDetails")
        public static class PartyDetails {
            @XmlElement(name = "Nm")
            public String nm;

            @XmlElement(name = "CtryOfRes")
            public String ctryOfRes;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "PartyInfos")
        public static class PartyInfos {
            @XmlElement(name = "Pty")
            public PartyDetails pty;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "AccountDetails")
        public static class AccountDetails {
            @XmlElement(name = "IBAN")
            public String iban;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "AccountInfos")
        public static class AccountInfos {
            @XmlElement(name = "Id")
            public AccountDetails id;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "RltdPties")
        public static class RltdPties {
            @XmlElement(name = "Dbtr")
            public PartyInfos dbtr;

            @XmlElement(name = "DbtrAcct")
            public AccountInfos dbtrAcct;

            @XmlElement(name = "Cdtr")
            public PartyInfos cdtr;

            @XmlElement(name = "CdtrAcct")
            public AccountInfos cdtrAcct;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "FinInstnId")
        public static class FinInstnId {
            @XmlElement(name = "BIC")
            public String bic;

            @XmlElement(name = "BICFI")
            public String bicfi;

            @XmlElement(name = "Nm")
            public String nm;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "AgentDetails")
        public static class AgentDetails {
            @XmlElement(name = "FinInstnId")
            public FinInstnId finInstnId;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "RltdAgts")
        public static class RltdAgts {

            @XmlElement(name = "DbtrAgt")
            public AgentDetails dbtrAgt;

            @XmlElement(name = "CdtrAgt")
            public AgentDetails cdtrAgt;
        }
        
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "TxDtls")
        public static class TxDtls {
            @XmlElement(name = "RmtInf")
            public RmtInfContainer rmtInf;

            @XmlElement(name = "RltdPties")
            public RltdPties rltdPties;

            @XmlElement(name = "RltdAgts")
            public RltdAgts rltdAgts;
        }
        
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "NtryDtlsContainer")
        public static class NtryDtlsContainer {
            @XmlElement(name = "TxDtls")
            public List<TxDtls> txDtls;
        }

        @XmlElement(name = "Amt")
        public Amt amt;

        @XmlElement(name = "CdtDbtInd")
        @XmlSchemaType(name = "string")
        public CdtDbtEnum cdtDbtInd;

        @XmlElement(name = "Sts")
        public StsContainer sts;

        @XmlElement(name = "BookgDt")
        public DtContainer bookgDt;

        @XmlElement(name = "ValDt")
        public DtContainer valDt;

        @XmlElement(name = "NtryDtls")
        public List<NtryDtlsContainer> ntryDtls;

        // convenience accessors for Ntry

        public StsEnum getStatus() {
            if (sts == null || sts.cd == null) {
                return StsEnum.INFO;
            }
            return sts.cd;
        }

        public long getAmount() {
            if (amt == null || amt.value == null || cdtDbtInd == null) {
                return 0L;
            }
            long value = amt.value.movePointRight(2).longValue();
            if (cdtDbtInd == CdtDbtEnum.DBIT) {
                return -value;
            } else {
                return value;
            }
        }

        public String getCurrency() {
            if (amt == null || amt.ccy == null) {
                return "EUR";
            }
            return amt.ccy;
        }

        public String getBookingDate() {
            if (bookgDt == null || bookgDt.dt == null) {
                return "";
            }
            return bookgDt.dt.toString();
        }

        public String getValuationDate() {
            if (valDt == null || valDt.dt == null) {
                return "";
            }
            return valDt.dt.toString();
        }

        private List<String> obtain(Function<TxDtls,List<String>> mapper) {
            List<String> result = new ArrayList<>();
            if (ntryDtls != null) {
                ntryDtls.forEach(d -> {
                    if (d.txDtls != null) {
                        d.txDtls.forEach(dt -> {
                            if (dt != null) {
                                List<String> mapped = mapper.apply(dt);
                                if (mapped != null) {
                                    result.addAll(mapped);
                                }
                            }
                        });
                    }
                });
            }
            return result;
        }

        public String getDebitorAccount() {
            return obtain(dt -> {
                if (dt.rltdPties != null && dt.rltdPties.dbtrAcct != null && dt.rltdPties.dbtrAcct.id != null && dt.rltdPties.dbtrAcct.id.iban != null) {
                    return Collections.singletonList(dt.rltdPties.dbtrAcct.id.iban);
                } else {
                    return null;
                }
            }).stream().findFirst().orElse("");
        }

        public String getDebitorName() {
            return obtain(dt -> {
                if (dt.rltdPties != null && dt.rltdPties.dbtr != null && dt.rltdPties.dbtr.pty != null && dt.rltdPties.dbtr.pty.nm != null) {
                    return Collections.singletonList(dt.rltdPties.dbtr.pty.nm);
                } else {
                    return null;
                }
            }).stream().findFirst().orElse("");
        }

        public String getDebitorBIC() {
            return obtain(dt -> {
                if (dt.rltdAgts != null && dt.rltdAgts.dbtrAgt != null && dt.rltdAgts.dbtrAgt.finInstnId != null) {
                    if (dt.rltdAgts.dbtrAgt.finInstnId.bic != null) {
                        return Collections.singletonList(dt.rltdAgts.dbtrAgt.finInstnId.bic);
                    } else if (dt.rltdAgts.dbtrAgt.finInstnId.bicfi != null) {
                        return Collections.singletonList(dt.rltdAgts.dbtrAgt.finInstnId.bicfi);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }).stream().findFirst().orElse("");
        }

        public String getDebitorBank() {
            return obtain(dt -> {
                if (dt.rltdAgts != null && dt.rltdAgts.dbtrAgt != null && dt.rltdAgts.dbtrAgt.finInstnId != null && dt.rltdAgts.dbtrAgt.finInstnId.nm != null) {
                    return Collections.singletonList(dt.rltdAgts.dbtrAgt.finInstnId.nm);
                } else {
                    return null;
                }
            }).stream().findFirst().orElse("");
        }


        public List<String> getInformation() {
            return obtain(dt -> {
                if (dt.rmtInf != null && dt.rmtInf.ustrd != null) {
                    return dt.rmtInf.ustrd;
                }
                return new ArrayList<>();
            });
        }
    }

    // we support both camt052v8 and camt053v8, because it depends on what you download

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "RptOrStmt")
    public static class RptOrStmt {
        @XmlElement(name = "CreDtTm")
        @XmlSchemaType(name = "string")
        public String creDtTm;

        @XmlElement(name = "Ntry")
        public List<Ntry> ntry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "BkToCstmrRptOrStmt")
    public static class BkToCstmrRptOrStmt {
        @XmlElement(name = "Rpt")
        public RptOrStmt rpt;

        @XmlElement(name = "Stmt")
        public RptOrStmt stmt;
    }

    @XmlElement(name = "BkToCstmrAcctRpt")
    public BkToCstmrRptOrStmt bkToCstmrAcctRpt;

    @XmlElement(name = "BkToCstmrStmt")
    public BkToCstmrRptOrStmt bkToCstmrStmt;

    // --- convenience accessors (never null) ---

    public String getCreDtTm() { // creation date time ISO datetime string
        if (bkToCstmrAcctRpt != null && bkToCstmrAcctRpt.rpt != null && bkToCstmrAcctRpt.rpt.creDtTm != null) {
            return bkToCstmrAcctRpt.rpt.creDtTm;
        }
        if (bkToCstmrStmt != null && bkToCstmrStmt.stmt != null && bkToCstmrStmt.stmt.creDtTm != null) {
            return bkToCstmrStmt.stmt.creDtTm;
        }
        return "";
    }

    public List<Ntry> getNtryList() {
        if (bkToCstmrAcctRpt != null && bkToCstmrAcctRpt.rpt != null && bkToCstmrAcctRpt.rpt.ntry != null) {
            return bkToCstmrAcctRpt.rpt.ntry;
        }
        if (bkToCstmrStmt != null && bkToCstmrStmt.stmt != null && bkToCstmrStmt.stmt.ntry != null) {
            return bkToCstmrStmt.stmt.ntry;
        }
        return new ArrayList<>();
    }
}
