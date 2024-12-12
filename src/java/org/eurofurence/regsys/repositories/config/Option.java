package org.eurofurence.regsys.repositories.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* This class is more or less a struct to hold the configuration and values of optional registration
 * components. Use the static createXXX methods to build your set of options in a convenient way.
 *
 * Options are stored in the database as comma separated strings, see class OptionList for some
 * help in handling that and searching in these strings correctly.
 */
public class Option {
    public enum OptionTypes {
        Choice, // a simple choice, used in the list of countries
        InfoOption, // an informational marker (fursuiter, ...) with no effect on business logic in the regsys, but searchable
        Flag, // flags as defined in configuration, can be admin only (guest, ...)
        Accommodation, // room choices (might have a price too) (not currently in use)
        Package, // con-related things that have a price (con-fee etc.)
    }

    public OptionTypes            optionType;

    /** short unique code of a few small caps letters, used in the database and for web parameters. [a-z_]+ */
    public String                 code;
    /** Name of the option (human-readable), used in web pages */
    public String                 name;
    /** Optional shorter name, for admin list views. Defaults to <code>name</code> if not set. */
    public String                 shortname;
    /** Possibly longer description, may or may not be displayed */
    public String                 description;

    public boolean                optionDefault; // initial default value

    // Properties only relevant for Optiontypes Accomodation & Sales Option
    public long                   price;   // only for type 2 or 3
    public float                  vat;          // value added tax, in percent

    // Properties only relevant for Packages (Sales Options)
    public int           maxCount; // always filled and always >= 1
    public List<Integer> allowedCounts; // always filled with the list of allowed counts, never null/empty

    // Properties only relevant for Optiontype Accomodation
    public int                    bedsPerRoom;

    public int                    searchSetting; // -1 any, 0 no, 1 yes

    public boolean                readonly;     // false = can be changed, true = cannot be changed (previously called
    // "forced sales option")

    // used to mark choices that the user cannot (de)select
    // (but will see once admin has selected them for him)
    public boolean                adminOnly;

    // is the option actually selected
    public int                count;

    public Option(Option.OptionTypes _type, String _code, Configuration.ChoiceConfig fromConfiguration) {
        this.optionType = _type;
        this.code = _code;
        this.name = fromConfiguration.description;
        this.shortname = fromConfiguration.description;
        this.description = fromConfiguration.description;
        this.optionDefault= fromConfiguration.defaultValue;
        this.count = fromConfiguration.defaultValue ? 1 : 0;
        this.price = fromConfiguration.price;
        this.vat = fromConfiguration.vatPercent;
        this.readonly = fromConfiguration.readOnly;
        this.adminOnly = fromConfiguration.adminOnly;
        if (fromConfiguration.maxCount >= 1) {
            this.maxCount = fromConfiguration.maxCount;
            if (fromConfiguration.allowedCounts != null && !fromConfiguration.allowedCounts.isEmpty()) {
                this.allowedCounts = fromConfiguration.allowedCounts;
            } else {
                this.allowedCounts = IntStream.range(1, fromConfiguration.maxCount+1).boxed().collect(Collectors.toList());
            }
        } else {
            this.maxCount = 1;
            this.allowedCounts = List.of(1);
        }
    }

    public String toString() {
        return this.name;
    }

    /**
     * Equal assumes equality is based on the option code and does not check any of the other fields. This works as long
     * as option codes are unique and option properties are not changed anywhere. As the database only stores the code
     * this is mandatory anyway.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return (code != null) ? code.equals(((Option) o).code) : ((Option) o).code == null;
    }

    // needs to be overwritten due to overwritten equals method
    @Override
    public int hashCode() {
        return code != null ? this.code.hashCode() : 0;
    }
}
