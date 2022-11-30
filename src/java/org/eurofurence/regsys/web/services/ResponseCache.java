package org.eurofurence.regsys.web.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseCache<T> {
    private static final long FOREVER_IN_MILLISECONDS = 1000L * 3600L * 24L * 365L * 10L;

    private long retainmentPeriodMilliseconds;

    private Map<String, T> content = new ConcurrentHashMap<>();
    private Map<String, Long> expiry = new ConcurrentHashMap<>();

    public ResponseCache(long retainmentPeriodMilliseconds) {
        this.retainmentPeriodMilliseconds = retainmentPeriodMilliseconds;
    }

    protected void put(String key, T value, long millis) {
        if (key == null)
            return;
        if (value == null || millis <= 0) {
            expiry.remove(key); // removing this first because it is checked first
            content.remove(key);
        } else {
            content.put(key, value);
            expiry.put(key, System.currentTimeMillis() + millis); // adding last because it is checked first
        }
    }

    public void put(String key, T value) {
        put(key, value, retainmentPeriodMilliseconds);
    }

    public void putPermanent(String key, T value) {
        put(key, value, FOREVER_IN_MILLISECONDS);
    }

    public T get(String key) {
        if (expiry.containsKey(key)) {
            long exp = expiry.get(key).longValue();
            long curr = System.currentTimeMillis();
            if (exp > curr) {
                if (content.containsKey(key)) {
                    T val = content.get(key);
                    // concurrency may have removed the value
                    return val;
                }
            }
        }
        return null;
    }
}
