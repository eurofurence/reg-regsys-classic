package org.eurofurence.regsys.backend.enums;

/**
 *  System configuration options for room management.
 *
 *  You must pick one option before anyone can register, and then stick with it.
 */
public enum RoomManagementOption {
    /*
     * room management features are completely disabled.
     *
     * Completely hides all relevant pages and options, only displays a few simple
     * messages in some places.
     *
     * typical usecase: private party, housemeet, pay-at-the-door
     */
    ROOMS_OFF,

    /*
     * only show an info page to accepted attendees called "how to book a room".
     *
     * Use this option when the venue handles all room bookings directly.
     *
     * If you choose this option, there must ONLY be a single accommodation package configured in Config.java:
     *     OptionAssembler.accomodation("room-none", "No Room", "room_none", 1, 0.00f, 0.00f, 0.00f).setDefault()
     * It will be invisibly added to each registration. Please make sure its cost is set to zero as shown,
     * or else attendees will wonder where the extra charge came from.
     *
     * typical usecase: large hotels
     */
    ROOMS_EXTERNAL,

    /*
     * sell accomodation as packages.
     *
     * Use this when the convention is reselling individual rooms or beds
     * - allows individual pricing of room options
     * - attendees enter their roommate ids in their registration
     *   (currently supports up to 3 roommates, easy to expand to more)
     * - room quotas can be maintained in the database (table room_quotas), but the
     *   system has no knowledge of individual rooms or beds.
     * - options become unavailable when their quota is exceeded, but be careful,
     *   admins can still assign them.
     *
     * In this mode, you can go wild with accommodation packages. Configure them in Config.java any way you want.
     *
     * typical usecase: small hotels
     */
    ROOMS_AS_PACKAGES,

    /*
     * attendees can form room request groups which are then assigned to rooms by staff.
     *
     * Use this when you want staff to have fine grained control over room assignments
     * - staff needs to watch that the number of accepted attendees does not exceed available rooms
     * - staff needs to enter available rooms in the regsys
     * - staff needs to do room assignment into actual rooms prior to the convention
     * - conventions can track keys given to the attendees during onsite check-in
     * - individual pricing of room options is not directly supported, but through a trick you can do it anyway:
     *   simply add regular sales options with the various room prices.
     *   Staff will need to manually make sure noone unintentionally gets a room they haven't paid for.
     *
     * If you choose this option, there must ONLY be a single accommodation package configured in Config.java:
     *     OptionAssembler.accomodation("room-none", "No Room", "room_none", 1, 0.00f, 0.00f, 0.00f).setDefault()
     * It will be invisibly added to each registration. Please make sure its cost is set to zero as shown,
     * or else attendees will wonder where the extra charge came from.
     *
     * typical usecase: youth hostels
     */
    ROOMS_HOUSE_GROUPS
}
