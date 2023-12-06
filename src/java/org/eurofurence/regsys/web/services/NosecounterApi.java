package org.eurofurence.regsys.web.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.TypeChecks;
import org.eurofurence.regsys.backend.types.IsoDate;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class NosecounterApi extends AbstractAttendeeListService {

    // ---------------- parameter constants -------------------------------------

    public static final String TOKEN = "token";

    public static final String YEAR = "year";

    public static final String SHOW_CREATED = "show-created";

    // ---------------- parameter values ----------------------------------------

    private int year;

    private boolean showCreated;

    // ---------------- DTOs ----------------------------------------------------

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected class ResponseDTO extends AbstractResponseDTO {
        @JsonProperty("CurrentDateTimeUtc")
        public Date currentDate = new Date();

        @JsonProperty("LastChangeDateTimeUtc")
        public Date cachingDate = new Date();

        @JsonProperty("Year")
        public int yearValue = year;

        @JsonProperty("Convention")
        public String convention = getConName();

        @JsonProperty("TotalCount")
        public int total = 0;

        @JsonProperty("Age")
        public SortedMap<String, Integer> ages = new TreeMap<>(new AgeComparator());

        @JsonProperty("Gender")
        public SortedMap<String, Integer> genders = new TreeMap<>();

        @JsonProperty("Status")
        public SortedMap<String, Integer> status = new TreeMap<>(new StatusComparator());

        @JsonProperty("ShirtSize")
        public SortedMap<String, Integer> shirtsizes = new TreeMap<>(new TshirtSizeComparator());

        @JsonProperty("Country")
        public SortedMap<String, Integer> countries = new TreeMap<>();

        @JsonProperty("Sponsor")
        public SortedMap<String, Integer> sponsors = new TreeMap<>();

        @JsonProperty("SpecialInterest")
        public SortedMap<String, Integer> interests = new TreeMap<>();

        @JsonProperty("Created")
        public SortedMap<Date, Integer> byMinute = new TreeMap<>();
    }

    // ------------------------ filters and orders ------------------------

    private class AgeComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            try {
                int a1 = Integer.parseInt(o1);
                int a2 = Integer.parseInt(o2);
                return a1 - a2;
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private void addRegularAndLadySize(Map<String, String> m, String size) {
        m.put(size, size);
        m.put("w" + size, size);
    }

    private Map<String, String> createTshirtSizeMap() {
        Map<String, String> m = new HashMap<>();
        addRegularAndLadySize(m, "XS");
        addRegularAndLadySize(m, "S");
        addRegularAndLadySize(m, "M");
        addRegularAndLadySize(m, "L");
        addRegularAndLadySize(m, "XL");
        addRegularAndLadySize(m, "XXL");
        return m;
    }

    private Map<String, String> applicableTshirtSizesMap = createTshirtSizeMap();

    private class TshirtSizeComparator implements Comparator<String> {
        private int valueOf(String tshirtsize) {
            switch (tshirtsize) {
            case "XS": return -2;
            case "S" : return -1;
            case "M" : return 0;
            case "L" : return 1;
            case "XL" : return 2;
            case "XXL" : return 3;
            default: return 0;
            }
        }

        @Override
        public int compare(String o1, String o2) {
            return valueOf(o1) - valueOf(o2);
        }
    }

    private class StatusComparator implements Comparator<String> {
        private int valueOf(String status) {
            switch (status) {
            // non-attending
            case "cancelled": return -2;
            case "waiting": return -1;
            // pre-attending
            case "new": return 6;
            // "attending" statuses
            case "approved": return 10;
            case "partially paid": return 11;
            case "paid": return 12;
            case "checked In": return 14;
            default: return 0;
            }
        }

        @Override
        public int compare(String o1, String o2) {
            return valueOf(o1) - valueOf(o2);
        }
    }

    // ------------------------ mapper -----------------------------------------

    private void register(SortedMap<String, Integer> map, String key) {
        if (map != null && key != null) {
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
    }

    private void register(SortedMap<String, Integer> map, String key, Map<String, String> applicableKeyMapping) {
        if (!applicableKeyMapping.containsKey(key))
            return;
        String mappedKey = applicableKeyMapping.get(key);
        if (map.containsKey(mappedKey)) {
            map.put(mappedKey, map.get(mappedKey) + 1);
        } else {
            map.put(mappedKey, 1);
        }
    }

    private static final long QUANTISE_TO_MILLIS = 1000L * 60L;

    private void registerCreationTime(SortedMap<Date, Integer> map, Date created) {
        if (created != null) {
            long time = created.getTime();
            long roundedTime = (time / QUANTISE_TO_MILLIS) * QUANTISE_TO_MILLIS;
            Date key = new Date(roundedTime);
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
    }

    private String getFirstDayOfCon() {
        switch (year) {
            case 1995: return "30.06.1995";
            case 1996: return "18.07.1996";
            case 1997: return "21.08.1997";
            case 1998: return "01.08.1998";
            case 1999: return "22.07.1999";
            case 2000: return "10.08.2000";
            case 2001: return "22.07.2001";
            case 2002: return "15.08.2002";
            case 2003: return "21.08.2003";
            case 2004: return "26.08.2004";
            case 2005: return "21.07.2005";
            case 2006: return "23.08.2006";
            case 2007: return "05.09.2007";
            case 2008: return "27.08.2008";
            case 2009: return "26.08.2009";
            case 2010: return "01.09.2010";
            case 2011: return "17.08.2011";
            case 2012: return "29.08.2012";
            case 2013: return "01.09.2013";
            case 2014: return "20.08.2014";
            case 2015: return "19.08.2015";
            case 2016: return "17.08.2016";
            case 2017: return "16.08.2017";
            case 2018: return "22.08.2018";
            case 2019: return "14.08.2019";
            case 2020: return "19.08.2020";
            case 2022: return "24.08.2022";
            case 2023: return "03.09.2023";
            case 2024: return "18.09.2024";
            default: return Strings.conf.conStart;
        }
    }

    private Set<String> mapOptionList(String commaSeparated) {
        if (commaSeparated == null) {
            return new HashSet<>();
        }
        return Arrays.stream(commaSeparated.split(","))
                .filter(v -> !"".equals(v))
                .collect(Collectors.toSet());
    }

    private void recordInfo(AttendeeSearchResultList.AttendeeSearchResult attendee, ResponseDTO response) {
        Constants.MemberStatus status = Constants.MemberStatus.SEARCH_IGNORE;
        try {
            status = Constants.MemberStatus.byNewRegsysValue(attendee.status);
        } catch (Exception ignored) {
        }

        Set<String> flags = mapOptionList(attendee.flags);
        Set<String> packages = mapOptionList(attendee.packages);
        Set<String> options = mapOptionList(attendee.options);

        boolean isGuest = flags.contains("guest");
        boolean isDay = packages.stream().anyMatch(v -> v.startsWith("day_"));
        boolean isRegular = !isGuest && !isDay;

        int age = 18;
        try {
            Calendar firstDayOfCon = Calendar.getInstance();
            firstDayOfCon.setTime(TypeChecks.parseStaticDate(getFirstDayOfCon()));

            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.setTime(new IsoDate().fromIsoFormat(attendee.birthday).getAsDate());

            age = firstDayOfCon.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
            if (dateOfBirth.get(Calendar.MONTH) > firstDayOfCon.get(Calendar.MONTH) ||
                    (dateOfBirth.get(Calendar.MONTH) == firstDayOfCon.get(Calendar.MONTH) && dateOfBirth.get(Calendar.DATE) > firstDayOfCon.get(Calendar.DATE))) {
                age--;
            }
        } catch (Exception ignored) {

        }

        String gender = attendee.gender;
        String shirtsize = attendee.tshirtSize;
        String country = attendee.country;

        boolean isSupersponsor = isRegular && packages.contains("sponsor2");
        boolean isSponsor = isRegular && !isSupersponsor && packages.contains("sponsor");
        boolean isNormal = isRegular && !isSupersponsor && !isSponsor;

        boolean isArtist = options.contains("art");
        boolean isAnimator = options.contains("anim");
        boolean isMusician = options.contains("music");
        boolean isSuiter = options.contains("suit");

        response.total++;
        register(response.ages, Integer.toString(age));
        register(response.genders, gender);
        register(response.status, status.newRegsysValue());
        register(response.shirtsizes, shirtsize, applicableTshirtSizesMap);
        register(response.countries, country);
        if (isSupersponsor)
            register(response.sponsors, "supersponsor");
        if (isSponsor)
            register(response.sponsors, "sponsor");
        if (isNormal)
            register(response.sponsors, "normal");
        if (isDay)
            register(response.sponsors, "day");

        if (isArtist)
            register(response.interests, "artist");
        if (isAnimator)
            register(response.interests, "animator");
        if (isMusician)
            register(response.interests, "musician");
        if (isSuiter)
            register(response.interests, "fursuiter");

        // TODO
//        if (year >= 2017 && showCreated) {
//            registerCreationTime(response.byMinute, attendee.getCreated());
//        }
    }

    private String getConName() {
        if (year <= 2020) {
            return "EF" + (year - 1994);
        } else {
            return "EF" + (year - 1996);
        }
    }

    // -------------------- caching --------------------------------------------

    private static final long ONE_MINUTE_IN_MILLIS = 1000L * 60L;

    private static ResponseCache<AbstractResponseDTO> cache = new ResponseCache<>(ONE_MINUTE_IN_MILLIS);

    // -------------------- implementation -------------------------------------

    @Override
    protected void finderAdditionalSetup(AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion) {
        criterion.status = Arrays.asList("new", "approved", "partially paid", "paid", "checked in", "waiting");
    }

    @Override
    protected void parseParameters() {
        String yearString = getRequest().getParameter(YEAR);
        if (yearString == null || !yearString.matches("[1-9][0-9]{3}"))
            throw new ServiceException("parameter.invalid.year", HttpServletResponse.SC_BAD_REQUEST, getRequestId());

        year = Integer.parseInt(yearString);

        showCreated = (getRequest().getParameter(SHOW_CREATED) != null);
    }

    @Override
    protected void authenticate() {
        String token = getRequest().getParameter(TOKEN);
        if (!getConfiguration().downstream.nosecounterToken.equals(token)) {
            throw new ServiceException("security.invalid.token", HttpServletResponse.SC_FORBIDDEN, getRequestId());
        }
    }

    protected AbstractResponseDTO calculateResponse(String cacheKey) {
        ResponseDTO output = new ResponseDTO();
        loopOverAllAttendees(a -> recordInfo(a, output), a -> true, "nosecounter.dberror.find.attendee.failure");
        if (!showCreated)
            output.byMinute = null;
        cache.put(cacheKey, output);
        return output;
    }

    @Override
    protected AbstractResponseDTO createResponse() {
        String cacheKey = Integer.toString(year) + (showCreated ? "-yes" : "-no");
        AbstractResponseDTO result = cache.get(cacheKey);
        if (result == null)
            result = calculateResponse(cacheKey);
        return result;
    }
}
