package org.eurofurence.regsys.repositories.config;

import java.util.*;
import java.util.function.Predicate;

import org.eurofurence.regsys.repositories.attendees.PackageInfo;
import org.eurofurence.regsys.repositories.config.Option.OptionTypes;

public class OptionList implements Iterable<Option> {
    private List<Option> liste;
    private OptionTypes  optionType;

    // ------------------ Constructor -------------------------------------

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

    public Option getByCodeMaybeNull(String code) {
        for (int i = 0; i < size(); i++) {
            Option option = get(i);
            if (option.code.equals(code)) {
                return option;
            }
        }
        return null;
    }

    public int size() {
        return liste.size();
    }

    // ------------------ Reset all options ------------------------------

    // -1: default, 0: no, 1+: count (rarely useful)
    public void initialize(int value) {
        for (int i = 0; i < size(); i++) {
            Option o = get(i);
            if (value < 0) {
                o.count = o.optionDefault ? 1 : 0;
            } else {
                o.count = value;
            }
        }
    }

    // ------------------ conversion convenience methods -------------------

    // getDbString converts the option list state into a comma separated string
    // suitable sending to the attendee service.
    //
    // The name "DbString" is historical.
    public String getDbString() {
        StringBuffer buf = new StringBuffer("");

        for (int i = 0; i < size(); i++) {
            Option o = get(i);
            if (o.count > 0) buf.append(o.code + ",");
        }

        return buf.toString().replaceFirst(",$", "");
    }

    // parseFromDbString parses the values in an option list from a comma separated
    // string coming from the attendee service.
    //
    // The name "DbString" is historical.
    public void parseFromDbString(String condensed) {
        String[] arr = condensed.split(",");

        initialize(0);

        for (int i = 0; i < arr.length; i++) {
            String code = arr[i];
            for (int j = 0; j < size(); j++) {
                Option o = get(j);
                if (o.code.equals(code)) {
                    o.count = 1;
                }
            }
        }
    }

    public List<PackageInfo> getAsPackagesList() {
        List<PackageInfo> packages = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            Option o = get(i);
            if (o.count > 0 && o.code != null && !o.code.isEmpty()) {
                PackageInfo entry = new PackageInfo();
                entry.name = o.code;
                entry.count = o.count;
                packages.add(entry);
            };
        }

        packages.sort(Comparator.comparing(e -> e.name));

        return packages;
    }

    public void parseFromPackagesList(List<PackageInfo> fromAttendee) {
        initialize(0);

        if (fromAttendee == null) return;
        fromAttendee.forEach(entry -> {
            for (int j = 0; j < size(); j++) {
                Option o = get(j);
                if (o.code.equals(entry.name)) {
                    o.count += (int) entry.count;
                }
            }
        });
    }

    public Iterator<Option> iterator() {
        return liste.iterator();
    }

    public List<Option> list() { return liste; }
}
