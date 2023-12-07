package org.eurofurence.regsys.backend;

import org.eurofurence.regsys.backend.enums.RoomManagementOption;

public class HardcodedConfig {
    public static final String CONFIG_URL = "file:/config/config.yaml"; // location as provided by reg-helm-chart

    public static final long PAYMENT_CANCEL_HOURS = 48;

    public static final RoomManagementOption ROOM_CONFIGURATION = Strings.conf.roomConfiguration;
}
