package org.eurofurence.regsys.repositories.attendees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.function.Consumer;

public class CriteriaUtils {
    public static AttendeeSearchCriteria constructCriteriaByIdSet(SortedSet<Long> ids) {
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        criterion.ids = new ArrayList<>(ids);

        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        criteria.matchAny = Collections.singletonList(criterion);
        criteria.sortBy = "id";
        criteria.sortOrder = "ascending";
        return criteria;
    }

    public static AttendeeSearchCriteria constructCriteriaUsing(Consumer<AttendeeSearchCriteria.AttendeeSearchSingleCriterion> setupCriterion) {
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        setupCriterion.accept(criterion);

        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        criteria.matchAny = Collections.singletonList(criterion);
        criteria.sortBy = "nickname";
        criteria.sortOrder = "ascending";
        return criteria;
    }


}
