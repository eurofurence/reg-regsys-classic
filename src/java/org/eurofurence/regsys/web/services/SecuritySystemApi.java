package org.eurofurence.regsys.web.services;

import org.apache.commons.text.StringEscapeUtils;
import org.eurofurence.regsys.backend.Checksummer;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.SHA384;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.config.Configuration;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *  Api for the EF security system.
 *
 *  Given your [token] and [sharedSecret] as configured in regsys-config.yaml
 *
 *  Request a [challenge] by invoking GET .../service/security-system-api?token=[token]
 *
 *  Generate [response] via
 *
 *      echo -n "[token][challenge]" | openssl sha384 -hmac "[sharedSecret]"
 *
 *  Request data via GET .../service/security-system-api?token=[token]&auth=[hash]
 */
public class SecuritySystemApi extends AbstractAttendeeListService {
    protected static class ListResponseDTO extends AbstractResponseDTO {
        public Map<Long, LassieInfoDTO> result = new TreeMap<>();
    }

    protected static class ChallengeResponseDTO extends AbstractResponseDTO {
        public String challenge;
        public Long serverTimeMS;
        public Long challengeExpiresMS;
    }

    protected static class LassieInfoDTO {
        public long id;
        public String nick;
        public String checkletter;
        public String attendeeType;

        public String firstname;
        public String lastname;
        public String dateOfBirth;

        public String street;
        public String zip;
        public String city;
        public String state;
        public String countryCode;
        public String country;
        public String countryBadgeCode;
        public String countryBadge;

        public String email;
        public String phone;
        public String telegram;

        public String status;

        public String comments;
        public String adminComments;

        public List<String> packages;
        public List<String> flags;
    }

    // ------------------------ mapper -----------------------------------------

    private LassieInfoDTO infoFromAttendee(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        LassieInfoDTO info = new LassieInfoDTO();
        info.id = attendee.id;
        if (!Constants.MemberStatus.DELETED.newRegsysValue().equals(attendee.status)) {
            info.status = attendee.status;
            info.nick = StringEscapeUtils.unescapeHtml4(attendee.nickname);
            info.checkletter = Character.toString(Checksummer.calculateChecksum((int) attendee.id));
            // TODO calculate based on guest flag and packages
            info.attendeeType = "UNKNOWN";
            info.firstname = StringEscapeUtils.unescapeHtml4(attendee.firstName);
            info.lastname = StringEscapeUtils.unescapeHtml4(attendee.lastName);
            info.dateOfBirth = attendee.birthday;
            info.street = StringEscapeUtils.unescapeHtml4(attendee.street);
            info.zip = StringEscapeUtils.unescapeHtml4(attendee.zip);
            info.city = StringEscapeUtils.unescapeHtml4(attendee.city);
            info.state = StringEscapeUtils.unescapeHtml4(attendee.state);
            info.countryCode = attendee.country;
            info.countryBadgeCode = attendee.countryBadge;
            info.country = info.countryCode;
            info.countryBadge = info.countryBadgeCode;
            info.email = attendee.email;
            info.phone = attendee.phone;
            info.telegram = attendee.telegram;
            info.comments = StringEscapeUtils.unescapeHtml4(attendee.userComments);
            info.adminComments = StringEscapeUtils.unescapeHtml4(attendee.adminComments);

            info.packages = mapOptionList(attendee.packages);
            info.flags = mapOptionList(attendee.flags);
        } else {
            info.status = "DELETED";
        }
        return info;
    }

    private List<String> mapOptionList(String commaSeparated) {
        if (commaSeparated == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(commaSeparated.split(","))
                .filter(v -> !"".equals(v))
                .collect(Collectors.toList());
    }

    // -------------------- implementation -------------------------------------

    @Override
    protected void finderAdditionalSetup(AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion) {
        criterion.status = Arrays.asList("new", "approved", "partially paid", "paid", "checked in", "waiting", "cancelled", "deleted");
    }

    @Override
    protected void authenticate() {
        String token = getRequest().getParameter("token");

        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream == null || downstream.secuToken == null || downstream.secuSecret == null || "".equals(downstream.secuToken) || "".equals(downstream.secuSecret)) {
            throw new ServiceException("security.config.error", getRequestId());
        }
        if (!downstream.secuToken.equals(token)) {
            throw new ServiceException("security.unauthorized", getRequestId());
        }
    }

    @Override
    protected AbstractResponseDTO createResponse() {
        // if no auth is given, send a challenge
        String auth = getRequest().getParameter("auth");
        if (checkAuth(auth)) {
            return createListResponse();
        } else {
            return createChallengeResponse();
        }
    }

    private static String challenge = null;
    private static Long challengeExpires = null;

    private boolean checkAuth(String auth) {
        if (auth != null && challenge != null && challengeExpires != null && System.currentTimeMillis() <= challengeExpires) {
            if (auth.equals(getExpectedHash())) {
                challenge = null;
                challengeExpires = null;
                return true;
            }
        }
        return false;
    }

    private AbstractResponseDTO createChallengeResponse() {
        ChallengeResponseDTO output = new ChallengeResponseDTO();
        output.challenge = UUID.randomUUID().toString();
        output.serverTimeMS = System.currentTimeMillis();
        output.challengeExpiresMS = output.serverTimeMS + 5L * 60L * 1000L;
        challenge = output.challenge;
        challengeExpires = output.challengeExpiresMS;
        return output;
    }

    private String getExpectedHash() {
        try {
            Configuration.DownstreamConfig downstream = getConfiguration().downstream;
            if (downstream == null || downstream.secuToken == null || downstream.secuSecret == null || "".equals(downstream.secuToken) || "".equals(downstream.secuSecret)) {
                throw new ServiceException("security.config.error", getRequestId());
            }

            String expectedRawMessage = downstream.secuToken + challenge;
            String expectedHash = SHA384.getSHA384HashWithHmac(expectedRawMessage, downstream.secuSecret);
            return expectedHash;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ServiceException("securitysystem.api.hash.failure", getRequestId(), e);
        }
    }

    private AbstractResponseDTO createListResponse() {
        ListResponseDTO output = new ListResponseDTO();
        loopOverAllAttendees(a -> output.result.put(a.id, infoFromAttendee(a)), a -> true, "securitysystem.api.dberror.find.attendee.failure");
        return output;
    }
}
