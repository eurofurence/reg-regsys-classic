package org.eurofurence.regsys.backend;

import org.eurofurence.regsys.backend.enums.RoomManagementOption;

public class HardcodedConfig {
    public static final String CONFIG_URL = "file:/develop/projects/eurofurence/reg-regsys-classic/config.yaml";

    public static final long PAYMENT_CANCEL_HOURS = 48;

    public static final RoomManagementOption ROOM_CONFIGURATION = Strings.conf.roomConfiguration;
}
