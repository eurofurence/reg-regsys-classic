package org.eurofurence.regsys.repositories.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eurofurence.regsys.repositories.config.Option.OptionAssembler;
import org.eurofurence.regsys.repositories.config.Option.OptionTypes;

public class OptionList implements Iterable<Option> {
    private List<Option> liste;
    private OptionTypes  optionType;

    // ------------------ Constructor -------------------------------------

    public OptionList(Option.OptionTypes _type) {
        liste = new ArrayList<>();
        optionType = _type;
    }

    public OptionList(Option.OptionTypes _type, Map<String, Configuration.ChoiceConfig> fromConfiguration, Predicate<Configuration.ChoiceConfig> filter) {
        liste = new ArrayList<>();
        optionType = _type;
        fromConfiguration.forEach(
                (key, value) -> {
                    if (filter.test(value)) {
                        Option option = new Option(_type, key, value);
                        liste.add(option);
                    }
                }
        );
    }

    // ----------------- Low Level Access ---------------------------------

    // n runs from [0..size)
    public Option get(int n) {
        return liste.get(n);
    }

    /**
     * Append this option to the end of the option list. Makes sure only fitting options are added by comparing the
     * optionType. However, <code>Accomodation</code> is treated as type <code>SalesOption</code> and PersonalFlag as Flag.
     */
    public void add(Option o) {
        if ((o.optionType == optionType)
                || ((optionType == OptionTypes.Package) && (o.optionType == OptionTypes.Accommodation))) {
            liste.add(o);
        } else {
            throw new IllegalArgumentException("Option Type does not match the List type!");
        }
    }

    /** Adds a new instance created by the given OptionAssembler to the list. Convenience-function. */
    public void add(OptionAssembler oa) {
        add(oa.createInstance());
    }

    public int size() {
        return liste.size();
    }

    // ------------------ Reset all options ------------------------------

    // -1: default, 0: no, 1: yes
    public void initialize(int value) {
        for (int i = 0; i < size(); i++) {
            Option o = get(i);
            o.optionEnable = value < 0 ? o.optionDefault : value == 1;
        }
    }

    // ------------------ Database convenience methods -------------------

    public String getDbString() {
        StringBuffer buf = new StringBuffer("");

        for (int i = 0; i < size(); i++) {
            Option o = get(i);
            if (o.optionEnable) buf.append(o.code + ",");
        }

        return buf.toString().replaceFirst(",$", "");
    }

    public void parseFromDbString(String condensed) {
        String[] arr = condensed.split(",");

        initialize(0);

        for (int i = 0; i < arr.length; i++) {
            String code = arr[i];
            for (int j = 0; j < size(); j++) {
                Option o = get(j);
                if (o.code.equals(code)) {
                    o.optionEnable = true;
                }
            }
        }
    }

    public Iterator<Option> iterator() {
        return liste.iterator();
    }

    public List<Option> list() { return liste; }
}
