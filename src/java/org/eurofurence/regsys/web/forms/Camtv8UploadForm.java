package org.eurofurence.regsys.web.forms;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.service.camtv8.Entry;
import org.eurofurence.regsys.service.camtv8.Parser;
import org.eurofurence.regsys.web.pages.Camtv8ImportPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  Represents the form used to upload a Camtv8 file.
 */
public class Camtv8UploadForm extends Form {

    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String UPLOADFORM = "uploadform";
    public static final String UPLOADFILE = "uploadfile";

    // ------------ attributes -----------------------

    List<Entry> entries = new ArrayList<>();

    // ------------ constructors -----------------------

    public Camtv8UploadForm() {
    }

    public void initialize() {
        entries = new ArrayList<>();
    }

    // ---------- proxy methods for entity access -------

    public List<Entry> getEntries() {
        return entries;
    }

    //
    // Business methods
    //

    public void processUpload() {
        try {
            final Collection<Part> fileItemsList = getPage().getRequest().getParts();
            fileItemsList.stream()
                    .filter(fileItem -> "uploadfile".equals(fileItem.getName()))
                    .forEach(fileItem -> {
                try {
                    entries = Parser.parse(fileItem.getInputStream());
                } catch (IOException e) {
                    addError(Strings.camtv8ImportPage.parseError + e.getMessage());
                }
            });
        } catch (IOException | ServletException e) {
            addError(Strings.camtv8ImportPage.uploadError + e.getMessage());
        }
    }

    // --------------------- form output methods ------------------------------------------------

    public String getFormHeader() {
        String result = "";

        result += "<FORM id='uploadform' name='" + UPLOADFORM + "' action='camtv8' method='POST' enctype='multipart/form-data' accept-charset='UTF-8'>\n";
        result += "    " + Form.hiddenField(Camtv8ImportPage.MODE, Camtv8ImportPage.MODE_UPLOAD) + "\n";

        return result;
    }

    public String getFileSelector() {
        String result = "";

        result += "<INPUT id='uploadfile' name='" + UPLOADFILE + "' type='file' name='" + UPLOADFORM + "' value='' accept='.xml'/>\n";

        return result;
    }

    public String getFormSubmitButton(String caption, String style) {
        return "<INPUT ID='uploadsubmit' TYPE='SUBMIT' VALUE='" + escape(caption) + "' CLASS='" + escape(style) + "'/>";
    }

    public String getFormFooter() {
        return "</FORM>";
    }

}
