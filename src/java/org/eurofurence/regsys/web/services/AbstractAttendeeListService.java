package org.eurofurence.regsys.web.services;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.config.ConfigService;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractAttendeeListService extends Service {
    protected AttendeeService attendeeService = new AttendeeService();
    protected ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    /**
     * your response DTO should inherit from this
     */
    protected static class AbstractResponseDTO {
        public boolean ok = true;
    }

    /**
     * set any additional search parameters on the finder by overriding this.
     *
     * Default is all nondeleted attendees.
     */
    protected void finderAdditionalSetup(AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion) {
    }

    /**
     * goes through all attendees. Those that match filter are given to the collector.
     */
    protected void loopOverAllAttendees(Consumer<AttendeeSearchResultList.AttendeeSearchResult> collector, Predicate<AttendeeSearchResultList.AttendeeSearchResult> filter, String errorKey) {
        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        finderAdditionalSetup(criterion);
        criteria.matchAny.add(criterion);

        String adminToken = configService.getConfig().downstream.apiToken;

        try {
            AttendeeSearchResultList resultList = attendeeService.performFindAttendees(criteria, adminToken, getRequestId());
            for (AttendeeSearchResultList.AttendeeSearchResult attendee: resultList.attendees) {
                if (filter.test(attendee)) {
                    collector.accept(attendee);
                }
            }
        } catch (Exception e) {
            throw new ServiceException(errorKey, getRequestId(), e);
        }
    }

    /**
     * override if you need parameters.
     *
     * throw a ServiceException if anything is wrong with them.
     */
    protected void parseParameters() {
    }

    /**
     * throw a ServiceException if access is forbidden.
     *
     * example: throw new ServiceException("security.invalid.token");
     */
    protected abstract void authenticate();

    /**
     * throw a ServiceException if anything goes wrong.
     *
     * Typically you would use loopOverAllAttendees() in this
     */
    protected abstract AbstractResponseDTO createResponse();

    @Override
    public void handle() {
        try {
            authenticate();
            parseParameters();
            jsonServiceResponse(createResponse());
        } catch (ServiceException e) {
            jsonServiceResponse(e);
        }
    }
}
