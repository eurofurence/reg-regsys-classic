package org.eurofurence.regsys.repositories.config;

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

    public boolean                optionEnable;

    public boolean                optionDefault; // initial default value

    // Properties only relevant for Optiontypes Accomodation & Sales Option
    public long                   priceEarly;   // only for type 2 or 3
    public long                   priceLate;    // only for type 2 or 3
    public long                   priceAtCon;   // only for type 2 or 3
    public float                  vat;          // value added tax, in percent

    // Properties only relevant for Optiontype Accomodation
    public int                    bedsPerRoom;

    public int                    searchSetting; // -1 any, 0 no, 1 yes

    public int                    counter;      // for stats page

    public boolean                readonly;     // false = can be changed, true = cannot be changed (previously called
    // "forced sales option")

    // used to mark choices that the user cannot (de)select
    // (but will see once admin has selected them for him)
    public boolean                adminOnly;

    /** private Constructor - please use OptionAssembler to create Option instances. */
    private Option() {
    }

    public Option(Option.OptionTypes _type, String _code, Configuration.ChoiceConfig fromConfiguration) {
        this.optionType = _type;
        this.code = _code;
        this.name = fromConfiguration.description;
        this.shortname = fromConfiguration.description;
        this.description = fromConfiguration.description;
        this.optionDefault= fromConfiguration.defaultValue;
        this.optionEnable = fromConfiguration.defaultValue;
        this.priceEarly = fromConfiguration.priceEarly;
        this.priceLate = fromConfiguration.priceLate;
        this.priceAtCon = fromConfiguration.priceAtCon;
        this.vat = fromConfiguration.vatPercent;
        this.readonly = fromConfiguration.readOnly;
        this.adminOnly = fromConfiguration.adminOnly;
    }

    /** In preparation of immutable Options (at least their config parts) - a factory method */
    public static class OptionAssembler {

        private OptionTypes            optionType;
        private String                 code;
        private String                 name;
        private String                 shortname;
        private String                 description;
        private boolean                optionDefault;
        private long                   priceEarly;
        private long                   priceLate;
        private long                   priceAtCon;
        private float                  vat;
        // private Constants.DueDateType  dueDateType;
        private int                    bedsPerRoom;
        private boolean                adminOnly;
        private boolean                readOnly;

        public static OptionAssembler choice(String _code, String _name, String _description) {
            OptionAssembler oa = new OptionAssembler(_code, _name, _description);
            oa.optionType = OptionTypes.Choice;
            return oa;
        }

        public static OptionAssembler choice(String _code, String _name) {
            return choice(_code, _name, "");
        }

        public OptionAssembler(String _code, String _name, String _description) {
            this.code = _code;
            this.name = _name;
            this.description = _description;
            this.readOnly = false;
            this.adminOnly = false;
            this.vat = 19; // defaults to 19%
            //this.dueDateType = Constants.DueDateType.DATE2;
        }

        public OptionAssembler setDefault() {
            this.optionDefault = true;
            return this;
        }

        /** Creates an Option instance representing internal state of this OptionAssembler */
        public Option createInstance() {
            Option o = new Option();
            o.optionType = this.optionType;
            o.readonly = this.readOnly;
            o.code = this.code;
            o.name = this.name;
            o.shortname = this.shortname != null ? this.shortname : this.name;
            o.description = this.description;
            o.optionEnable = o.optionDefault = this.optionDefault;
            o.searchSetting = -1;
            o.priceEarly = this.priceEarly;
            o.priceLate = this.priceLate;
            o.priceAtCon = this.priceAtCon;
            o.vat = this.vat;
            //o.dueDateType = this.dueDateType;
            o.adminOnly = this.adminOnly;
            o.bedsPerRoom = this.bedsPerRoom;
            return o;
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
