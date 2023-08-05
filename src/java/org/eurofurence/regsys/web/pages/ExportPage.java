package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.web.forms.Util;
import org.eurofurence.regsys.backend.*;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.CriteriaUtils;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.NotAllowedException;
import org.eurofurence.regsys.web.exports.*;
import org.eurofurence.regsys.web.forms.ListForm;
import org.eurofurence.regsys.web.forms.SearchForm;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Predicate;

public class ExportPage extends Page {
    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!isAdmin() && !canExportConbook()) {
            return forward("page/start");
        }

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.exportPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "export.vm";
    }

    @Override
    public void handle() {
        try {
            createSearchForm();
            if (!exportCsv()) {
                // render the regular page
                super.handle();
            }
        } catch (Exception e) {
            getResponse().setContentType("text/html; charset=utf-8");
            handleException(e);
        }
    }

    public boolean isAdmin() {
        return hasPermission(Constants.Permission.ADMIN);
    }

    public boolean canExportConbook() {
        return hasPermission(Constants.Permission.EXPORT_CONBOOK);
    }

    // ------- search form (provides search/find logic) ------------------

    private SearchForm searchForm;

    public void createSearchForm() {
        searchForm = new SearchForm();
        searchForm.givePage(this);
    }

    // --- business logic ---

    public boolean exportCsv() throws IOException, ServletException {
        refreshSessionTimeout();

        if (!Util.isNullOrEmpty(getRequest().getParameter("bulkmail"))) {
            if (!hasPermission(Constants.Permission.ANNOUNCE))
                throw new NotAllowedException("must have ANNOUNCE privileges for bulkmails");
            forward("page/bulkmail");
            return true;
        }

        if (!Util.isNullOrEmpty(getRequest().getParameter("resend-status-email"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges to resend status mails");
            forward("page/email");
            return true;
        }

        if (!Util.isNullOrEmpty(getRequest().getParameter("export-conbook-ss"))) {
            if (!hasPermission(Constants.Permission.EXPORT_CONBOOK))
                throw new NotAllowedException("must have EXPORT_CONBOOK privileges for conbook exports");
            exportConbook( true);
            return true;
        }

        if (!Util.isNullOrEmpty(getRequest().getParameter("export-conbook-s"))) {
            if (!hasPermission(Constants.Permission.EXPORT_CONBOOK))
                throw new NotAllowedException("must have EXPORT_CONBOOK privileges for conbook exports");
            exportConbook( false);
            return true;
        }

        if (!Util.isNullOrEmpty(getRequest().getParameter("export-csv-new"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportAttendeeCsv();
            return true;
        }
        if (!Util.isNullOrEmpty(getRequest().getParameter("export-badge-new"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportBadgesCsv();
            return true;
        }

        if (!Util.isNullOrEmpty(getRequest().getParameter("export-csv"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportAttendeeCsv_old();
            return true;
        }
        if (!Util.isNullOrEmpty(getRequest().getParameter("export-badge"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportBadgesCsv_old();
            return true;
        }
        if (!Util.isNullOrEmpty(getRequest().getParameter("mass-approve"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for mass approve");
            forward("page/mass-approve");
            return true;
        }
        if (!Util.isNullOrEmpty(getRequest().getParameter("export-email"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportEmail();
            return true;
        }
        if (!Util.isNullOrEmpty(getRequest().getParameter("export-waiver"))) {
            if (!hasPermission(Constants.Permission.ADMIN))
                throw new NotAllowedException("must have ADMIN privileges for csv/spreadsheet exports");
            exportWaiver();
            return true;
        }

        return false;
    }

    private PrintStream sendAsCsvDownload(String downloadFilename, Constants.ExportEncoding encoding) throws IOException {
        ServletOutputStream responseOutputStream = getResponse().getOutputStream();
        PrintStream printStream;
        try {
            printStream = new PrintStream(responseOutputStream, false, encoding.getStreamEncoding());
        } catch (UnsupportedEncodingException err) {
            Logging.warn("[" + getRequestId() + "] got UnsupportedEncodingException when instantiating Csv Printstream, response encoding will be wrong!");
            printStream = new PrintStream(responseOutputStream, false);
        }
        String mimetype = getServletContext().getMimeType(downloadFilename);
        getResponse().setContentType(((mimetype != null) ? mimetype : "application/octet-stream") + "; charset=" + encoding.getWebResponseCharset());
        getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");

        if (encoding.shouldWriteByteorderMark()) {
            // send byte order mark at beginning of csv file that will make Excel and other Windows programs load it correctly
            printStream.write(0xef);
            printStream.write(0xbb);
            printStream.write(0xbf);
        }
        return printStream;
    }

    private void genericExport(AbstractCsvExport exporter, String downloadFilename,
                               Predicate<AttendeeSearchResultList.AttendeeSearchResult> shouldBeIncluded) throws IOException {

        try (PrintStream printStream = sendAsCsvDownload(downloadFilename, Constants.ExportEncoding.UTF8)) {
            printStream.println(exporter.getHeader());
            while (searchForm.loadNext()) {
                AttendeeSearchResultList.AttendeeSearchResult attendee = searchForm.getAttendee();
                if (shouldBeIncluded.test(attendee))
                    printStream.println(exporter.getRecord(attendee));
            }
        } catch (Exception e) {
            Logging.warn("[" + getRequestId() + "] exception while doing csv/spreadsheet export");
            Logging.exception(e);
        }
    }

    // ------------ exports that loop through all attendees -------------------

    protected void exportAttendeeCsv() throws IOException, ServletException {
        AttendeeSearchCriteria criteria = CriteriaUtils.constructCriteriaUsing(c -> {
           // TODO status participating - not yet supported
           // a.getStatus().isParticipating() if supported
           c.packages.put("room-none", 0);
        });
        searchForm.setCriteria(criteria);
        if (!searchForm.find()) {
            throw new DownstreamException("no matching data found during csv/spreadsheet export");
        }

        genericExport(new StandardExport(), "hotel_attendees.csv",
                a -> true
                // TODO status - not yet returned. Either use this or use filter
                // a -> a.getStatus().isParticipating()
        );
    }

    protected void exportBadgesCsv() throws IOException, ServletException {
        AttendeeSearchCriteria criteria = CriteriaUtils.constructCriteriaUsing(c -> {
            // TODO status not yet supported
        });
        searchForm.setCriteria(criteria);
        if (!searchForm.find()) {
            throw new DownstreamException("no matching data found during csv/spreadsheet export");
        }

        genericExport(new BadgeExport(), "badges.csv",
                a -> true
                // only show those participating, but also include CANCELLED on request of Kralle
                // ("people show up and want to uncancel on-site")
                // a -> a.getStatus().isParticipating() || a.getStatus().equals(Constants.MemberStatus.CANCELLED)
        );
    }

    protected void exportConbook(boolean supersponsor) throws IOException {
        AttendeeSearchCriteria criteria = CriteriaUtils.constructCriteriaUsing(c -> {
            // TODO status participating - not yet supported
            // a.getStatus().isParticipating() if supported
            if (supersponsor) {
                c.packages.put("sponsor2", 1);
            } else {
                c.packages.put("sponsor", 1);
            }
        });
        searchForm.setCriteria(criteria);
        if (!searchForm.find()) {
            throw new DownstreamException("no matching data found during csv/spreadsheet export");
        }

        String downloadFilename = supersponsor ? "conbook_supersponsors.txt" : "conbook_sponsors.txt";

        genericExport(new SponsorExport(), downloadFilename, a -> {
            // TODO
            // if (!a.getStatus().isParticipating()) return false;
            return true;
        });
    }

    // ------------ exports that use a stored selection (coming from ListPage/Form) -------------------

    protected void exportBySelection(AbstractCsvExport exporter, String downloadFilename, Predicate<AttendeeSearchResultList.AttendeeSearchResult> filterCondition) throws IOException, ServletException {
        SortedSet<Long> ids = ListForm.extractIdsFromParams(getRequest());
        AttendeeSearchCriteria criteria = CriteriaUtils.constructCriteriaByIdSet(ids);
        searchForm.setCriteria(criteria);
        if (!searchForm.find()) {
            throw new DownstreamException("no matching data found during csv/spreadsheet export");
        }

        try (PrintStream printStream = sendAsCsvDownload(downloadFilename, exporter.getEncoding())) {
            printStream.println(exporter.getHeader());
            while (searchForm.loadNext()) {
                AttendeeSearchResultList.AttendeeSearchResult attendee = searchForm.getAttendee();
                if (filterCondition.test(attendee)) {
                    printStream.println(exporter.getRecord(attendee));
                }
            }
        }
    }

    protected void exportBySelection(AbstractCsvExport exporter, String downloadFilename) throws IOException, ServletException {
        exportBySelection(exporter, downloadFilename, a -> true);
    }

    protected void exportAttendeeCsv_old() throws IOException, ServletException {
        exportBySelection(new StandardExport(), "hotel_attendees.csv");
    }

    protected void exportBadgesCsv_old() throws IOException, ServletException {
        exportBySelection(new BadgeExport(), "badges.csv");
    }

    protected void exportEmail() throws IOException, ServletException {
        exportBySelection(new EmailExport(), "emails.csv");
    }

    protected void exportWaiver() throws IOException, ServletException {
        exportBySelection(new WaiverExport(), "waivers.csv");
    }


}
