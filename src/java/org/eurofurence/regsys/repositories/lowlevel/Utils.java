package org.eurofurence.regsys.repositories.lowlevel;

import org.eurofurence.regsys.repositories.errors.DownstreamException;

public class Utils {
    public static long idFromLocationHeader(String location) {
        if (location == null)
            throw new DownstreamException("unexpectedly received no Location header");
        String[] locComponents = location.split("/");
        if (locComponents.length > 0) {
            String lastLocComponent = locComponents[locComponents.length-1];
            try {
                long badge = Long.parseLong(lastLocComponent);
                return badge;
            } catch (NumberFormatException e) {
                throw new DownstreamException("unexpectedly received unparseable Location header " + location);
            }
        } else {
            throw new DownstreamException("unexpectedly received unparseable Location header " + location);
        }
    }

    public static String uuidFromLocationHeader(String location) {
        if (location == null)
            throw new DownstreamException("unexpectedly received no Location header");
        String[] locComponents = location.split("/");
        if (locComponents.length > 0) {
            return locComponents[locComponents.length-1];
        } else {
            throw new DownstreamException("unexpectedly received unparseable Location header " + location);
        }
    }
}
