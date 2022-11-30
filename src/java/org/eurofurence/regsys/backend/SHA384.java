package org.eurofurence.regsys.backend;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SHA384 {

    public static String getSHA384HashWithHmac(String input, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA384");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA384");
        mac.init(secret);
        byte[] shaDigest = mac.doFinal(input.getBytes());

        BigInteger HashInt = new BigInteger(1, shaDigest);
        String Hash = HashInt.toString(16); // Get hex string
        // Pad with zeroes in the beginning
        while (Hash.length() < 96)
            Hash = "0" + Hash;

        return Hash.toLowerCase();
    }

}
