package org.eurofurence.regsys.backend;

/**
 * Calculate a single checksum character from an integer id.
 *
 * @author Zefiro
 */
public class Checksummer {
    private static String CHECKSUM_LETTER_MAP = "FJQCEKNTWLVGYHSZXDBUARP"; // 23 letters (prime)

    public static char calculateChecksum(int id) {
        int[] weight = {3, 7, 11, 13, 17};
        int sum = 0;
        int place = 0;
        while (id > 0) {
            int digit = id % 10;
            sum += digit * weight[place];
            id /= 10;
            place++;
        }
        int idx = sum % (CHECKSUM_LETTER_MAP.length());
        return CHECKSUM_LETTER_MAP.charAt(idx);
    }
}
