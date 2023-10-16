package org.eurofurence.regsys.web.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eurofurence.regsys.repositories.config.Option;
import org.eurofurence.regsys.repositories.config.OptionList;
import org.eurofurence.regsys.repositories.errors.ErrorDto;
import org.eurofurence.regsys.web.pages.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Semi-abstract base class that represents a general purpose form in a web application.
 *
 *  This class can be used in two ways:
 *
 *  1. subclass it to implement a specific form
 *
 *     in this scenario, the subclass should have String fields to hold the parameters
 *     passed when the form is submitted, and you should override the parseRequest
 *     method.
 *
 *  2.  use its static methods as a helper when generating html forms
 */
public abstract class Form {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    // setup ---------------------------------------------------------
    private Page page;

    /**
     * pass in the associated page
     */
    public void givePage(Page page) {
        this.page = page;
    }

    /**
     * The page that this form is currently used in, also allows access to its authentication object
     */
    public Page getPage() {
        return page;
    }

    // static methods for use scenario 2 ---------------------------------

    /**
     * Make a string suitable for a html VALUE=""-tag: escape any ", <, >.
     *
     * Note that & is not escaped to allow passing in entities. At worst, the result will look a little weird.
     *
     * @param value
     * @return escaped value, suitable for html VALUE="" tag (must be using double quotes as shown)
     */
    public static String escape(String value) {
        return escape(value, false);
    }

    /**
     * use this if you also want to replace Ampersands, setting the second parameter to true.
     *
     * Note that this will rob users of the chance to enter special characters as entities in form fields.
     * Which is why we only use it for certain things where entities must really be preserved as-is.
     *
     * Example: email templates where we explicitly allow using entities that are replaced before sending the mails.
     */
    public static String escape(String value, boolean replaceAmps) {
        String tmp = value;
        if (tmp == null) {
            tmp = "";
        }
        try {
            if (replaceAmps)
                tmp = tmp.replaceAll("&", "&amp;");
            tmp = tmp.replaceAll("\"", "&quot;");
            tmp = tmp.replaceAll("<", "&lt;");
            tmp = tmp.replaceAll(">", "&gt;");
        } catch (Exception e) {
            tmp = "";
        }
        return tmp;
    }

    /**
     * build html for a standard text field with optional css class parameter
     *
     * @param editable is editing the field allowed/desired (will render as display if not)
     * @param name name of request parameter when the form is submitted
     * @param value preassigned value for the field
     * @param displaySize controls the width of the field on the screen in characters
     * @param maxContentLength controls how many characters are accepted from the user
     * @param style optional css class name
     * @return String containing valid xhtml
     */
    public static String textField(boolean editable, String name, String value, int displaySize, int maxContentLength, String style) {
        if (!editable)
            return escape(value);

        String field = "<INPUT TYPE=\"text\" NAME=\"" + escape(name) + "\" SIZE=\"" + displaySize + "\" MAXLENGTH=\"" + maxContentLength
                     + "\" VALUE=\"" + escape(value) + "\"";
        if (style != null) {
            field += " CLASS=\"" + escape(style) + "\"";
        }
        field += "/>";

        return field;
    }

    /**
     * build html for a standard text field (convenience method that omits css class parameter)
     */
    public static String textField(boolean editable, String name, String value, int displaySize, int maxContentLength) {
        return textField(editable, name, value, displaySize, maxContentLength, null);
    }

    /**
     * build html for a standard password field with optional css class parameter
     *
     * @param editable is editing the field allowed/desired (will simply render as ******** if not)
     * @param name name of request parameter when the form is submitted
     * @param value preassigned value for the field (should usually be "")
     * @param displaySize controls the width of the field on the screen in characters
     * @param maxContentLength controls how many characters are accepted from the user
     * @param style optional css class name
     * @return String containing valid xhtml
     */
    public static String passwordField(boolean editable, String name, String value, int displaySize, int maxContentLength, String style) {
        if (!editable)
            return "********";

        String field = "<INPUT TYPE=\"password\" NAME=\"" + escape(name) + "\" SIZE=\"" + displaySize + "\" MAXLENGTH=\"" + maxContentLength
                + "\" VALUE=\"" + escape(value) + "\"";
        if (style != null) {
            field += " CLASS=\"" + escape(style) + "\"";
        }
        field += "/>";

        return field;
    }

    /**
     * build html for a standard password field (convenience method that omits css class parameter)
     */
    public static String passwordField(boolean editable, String name, String value, int displaySize, int maxContentLength) {
        return passwordField(editable, name, value, displaySize, maxContentLength, null);
    }

    /**
     * build html for a standard hidden field
     *
     * @param name name of request parameter when the form is submitted
     * @param value value for the parameter
     * @return String containing valid xhtml
     */
    public static String hiddenField(String name, String value) {
        String field = "<INPUT TYPE=\"hidden\" NAME=\"" + escape(name) + "\" ID=\"" + escape(name) + "\" VALUE=\"" + escape(value) + "\"/>";
        return field;
    }

    /**
     * build html for a multiline text area
     *
     * @param editable is editing the field allowed/desired (will render as text if not)
     * @param name name of request parameter when the form is submitted
     * @param value content (can be multiline)
     * @param rows number of rows
     * @param columns number of columns
     * @param style optional css class name
     * @return String containing valid xhtml
     */
    public static String textArea(boolean editable, String name, String value, int rows, int columns, String style) {
        if (!editable)
            return escape(value);

        String field = "<TEXTAREA NAME=\"" + escape(name) + "\" ROWS=\"" + rows + "\" COLS=\"" + columns + "\"";
        if (style != null) {
            field += " CLASS=\"" + escape(style) + "\"";
        }
        field += ">" + escape(value, true) + "</TEXTAREA>";

        return field;
    }

    /**
     * build html for a multiline text area (convenience method that omits css class parameter)
     */
    public static String textArea(boolean editable, String name, String value, int rows, int columns) {
        return textArea(editable, name, value, rows, columns, null);
    }

    /**
     * build html for a radio button
     *
     * @param editable is editing the field allowed/desired
     * @param name name of request parameter when the form is submitted
     * @param value value to give this parameter if this button is selected
     * @param currentValue if this matches value, the button is shown as initially selected
     * @param style optional css class name
     * @return String containing valid xhtml
     */
    public static String radioButton(boolean editable, String name, String value, String currentValue, String style) {
        if (!editable)
            return (value != null && value.equals(currentValue) ? "(X)" : "(&nbsp;)");

        String field = "<INPUT TYPE=\"radio\" ";
        if (style != null) {
            field += "CLASS=\"" + style + "\" ";
        }
        field += "NAME=\"" + escape(name) + "\" ID=\"" + escape(name + value) + "\" VALUE=\""
                + escape(value) + "\" " + (value != null && value.equals(currentValue) ? "CHECKED" : "") + "/>";
        return field;
    }

    /**
     * build html for a radio button (convenience method that omits css class parameter)
     */
    public static String radioButton(boolean editable, String name, String value, String currentValue) {
        return radioButton(editable, name, value, currentValue, null);
    }

    /**
     * build html for a checkbox
     *
     * @param editable is editing the field allowed/desired
     * @param name name of request parameter when the form is submitted
     * @param value value to give this parameter if this checkbox is selected
     * @param currentValue if this matches value, the checkbox is shown as initially selected
     * @param style optional css class name
     * @param disabled flag to show the checkbox as greyed out (also makes it impossible to change its value)
     * @return String containing valid xhtml
     */
    public static String checkbox(boolean editable, String name, String value, String currentValue, String style, boolean disabled) {
        if (!editable)
            return (value != null && value.equals(currentValue) ? "[X]" : "[&nbsp;]");

        String field = "<INPUT TYPE=\"checkbox\" NAME=\"" + escape(name) + "\" ID=\"" + escape(name) + "\" VALUE=\"" + escape(value) + "\" "
                + (value != null && value.equals(currentValue) ? "CHECKED" : "")
                + (disabled ? " DISABLED" : "")
                + (style != null ? " CLASS=\"" + escape(style) + "\"" : "") + "/>";
        return field;
    }

    /**
     * build html for a checkbox (convenience method that omits css class parameter)
     */
    public static String checkbox(boolean editable, String name, String value, String currentValue, boolean disabled) {
        return checkbox(editable, name, value, currentValue, null, disabled);
    }

    /**
     * build html for a checkbox (convenience method that omits disabled flag)
     */
    public static String checkbox(boolean editable, String name, String value, String currentValue, String style) {
        return checkbox(editable, name, value, currentValue, style, false);
    }

    /**
     * build html for a checkbox (convenience method that omits css class parameter and disabled flag)
     */
    public static String checkbox(boolean editable, String name, String value, String currentValue) {
        return checkbox(editable, name, value, currentValue, null, false);
    }

    /**
     * build html for a selector
     *
     * @param editable is editing the field allowed/desired (will render as a textual representation if not)
     * @param name name of request parameter when the form is submitted
     * @param values List of Strings containing the values as they are represented in request parameters
     * @param showValuesAs optional List of Strings containing the values as they will be displayed to the web page user
     *                     if this List contains fewer elements than the values List, the missing ones will be displayed as in values
     *                     null is allowed for this parameter
     * @param currentValues if this Set contains a value, the value is pre-selected
     * @param size number of list elements to display at a time, 1 for a dropdown list
     * @param multi allow multiple selections
     * @param style optional css class name, may be null
     * @param onChange optional javascript code snippet for the onChange handler, may be null
     * @return String containing valid xhtml
     */
    public static String selector(
            boolean editable,
            String name,
            List<String> values,
            List<String> showValuesAs,
            Set<String> currentValues,
            int size,
            boolean multi,
            String style,
            String onChange)
    {
        // prevent unreadable null pointer handling in what follows
        if (showValuesAs == null) {
            showValuesAs = new LinkedList<String>();
        }
        if (currentValues == null) {
            currentValues = new HashSet<String>();
        }

        String field = "<SELECT ID=\"" + escape(name) + "\" NAME=\"" + escape(name) + "\" " + "SIZE=\"" + size + "\""
                    + (multi ? " MULTIPLE" : "")
                    + (style != null ? " CLASS=\"" + style + "\"" : "")
                    + (onChange != null ? " onChange=\"" + onChange + "\"" : "")
                    + ">\n";
        String display = "";

        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            String displayVal = val;
            try {
                displayVal = showValuesAs.get(i);
            } catch (IndexOutOfBoundsException e) {
                // ok, it will remain set to val
            }

            field += "<OPTION VALUE=\"" + escape(val) + "\""
                        + (currentValues.contains(val) ? " SELECTED" : "")
                        + ">"
                        + escape(displayVal)
                        + "</OPTION>\n";
            if (currentValues.contains(val))
                display += ("".equals(display) ? "" : ",") + escape(displayVal);
        }
        field += "</SELECT>";

        return editable ? field : display;
    }

    /**
     * build html for a selector (convenience method that omits css class parameter and javascript handlers)
     */
    public static String selector(
            boolean editable,
            String name,
            List<String> values,
            List<String> showValuesAs,
            Set<String> currentValues,
            int size,
            boolean multi)
    {
        return Form.selector(editable, name, values, showValuesAs, currentValues, size, multi, null, null);
    }

    /**
     * build html for a selector, no support for multiple selections but simpler parameters
     *
     * @param editable is editing the field allowed/desired (will render as a textual representation if not)
     * @param name name of request parameter when the form is submitted
     * @param values List of Strings containing the values as they are represented in request parameters
     * @param showValuesAs optional List of Strings containing the values as they will be displayed to the web page user
     *                     if this List contains fewer elements than the values List, the missing ones will be displayed as in values
     *                     null is allowed for this parameter
     * @param currentValue this value is pre-selected, null permitted, works ok if this value is not present in the List of values
     * @param size number of list elements to display at a time, 1 for a dropdown list
     * @param style optional css class name
     * @param onChange optional javascript code snippet for the onChange handler, may be null
     * @return String containing valid xhtml
     */
    public static String selector(
            boolean editable,
            String name,
            List<String> values,
            List<String> showValuesAs,
            String currentValue,
            int size,
            String style,
            String onChange)
    {
        Set<String> currentValuesSet = new HashSet<String>();
        if (currentValue != null) {
            currentValuesSet.add(currentValue);
        }
        return selector(editable, name, values, showValuesAs, currentValuesSet, size, false, style, onChange);
    }

    /**
     * build html for a selector, no support for multiple selections (convenience method that omits css class parameter and javascript handlers)
     */
    public static String selector(
            boolean editable,
            String name,
            List<String> values,
            List<String> showValuesAs,
            String currentValue,
            int size)
    {
        return selector(editable, name, values, showValuesAs, currentValue, size, null, null);
    }

    /**
     * build html for a selector, variant that takes an OptionList instead of a List of Strings
     */
    public static String selector(
            boolean editable,
            final String name,
            OptionList choices,
            String currentValue,
            int size,
            boolean multi,
            final String style,
            final String onChange) {
        int iMax = choices.size();
        List<String> vals = new ArrayList<String>(iMax);
        List<String> displ = new ArrayList<String>(iMax);

        for (int i = 0; i < iMax; i++) {
            Option o = choices.get(i);
            vals.add(o.code);
            displ.add(o.name);
        }

        Set<String> curr = new HashSet<String>();
        if (currentValue != null) {
            curr.add(currentValue);
        }

        return selector(editable, name, vals, displ, curr, size, multi, style, onChange);
    }

    /**
     * build html for a selector, variant that takes an OptionList instead of a List of Strings
     */
    public static String selector(
            boolean editable,
            final String name,
            OptionList choices,
            String currentValue,
            int size,
            boolean multi) {
        return selector(editable, name, choices, currentValue, size, multi, null, null);
    }

    /**
     * build html for a selector, variant that takes two Object[] instead of Lists
     */
    public static String selector(
            boolean editable,
            final String name,
            final Object[] values,
            final Object[] show_as,
            String currentValue,
            int size,
            boolean multi,
            final String style) {
        try {
            List<String> vals = (values != null) ? Arrays.asList((String[]) values) : new ArrayList<String>();
            List<String> displ = (show_as != null) ? Arrays.asList((String[]) show_as) : null;
            Set<String> curr = new HashSet<String>();
            if (currentValue != null) {
                curr.add(currentValue);
            }

            return Form.selector(editable, name, vals, displ, curr, size, multi, style, null);
        } catch (Exception e) {
            throw new Error("Field " + name + ":" + e.getMessage());
        }
    }

    /**
     * convenience method so forms can use addError directly
     */
    public synchronized Vector<String> getErrors()       { return new Vector<String>(getPage().getErrors()); }
    public synchronized void   addError(String msg)    { getPage().addError(msg); }
    public synchronized boolean hasErrors()              { return getPage().hasErrors(); }
    public synchronized void   resetErrors()             { getPage().resetErrors(); }
    public synchronized void   resetErrors(String msg) {
                                                            getPage().resetErrors();
                                                            getPage().addError(msg);
                                                        }

    public void addWebErrors(ErrorDto err) {
        if (err != null && err.details != null) {
            err.details.forEach(
                    (k, v) -> {
                        if (v != null) {
                            v.forEach(
                                    msg -> addError(k + ": " + msg)
                            );
                        }
                    }
            );
        }
    }

}
