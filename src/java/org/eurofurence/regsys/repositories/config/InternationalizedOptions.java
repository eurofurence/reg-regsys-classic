package org.eurofurence.regsys.repositories.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class InternationalizedOptions extends ArrayList<InternationalizedOptions.KeyWithNameByLanguage> {
    // innermost helper class
    public static class NameByLanguage extends HashMap<String, String> {
        public String byLanguage(String language) {
            String value = get(language);
            if (value == null) {
                // try en-US
                value = get("en-US");
                if (value == null) {
                    // find any value in the map, or return "", never null
                    return entrySet().stream().findFirst().map(Entry::getValue).orElse("");
                }
            }
            return value;
        }
    }

    // outer helper class
    public static class KeyWithNameByLanguage extends HashMap<String, NameByLanguage> {
        public String key() {
            return entrySet().stream().findFirst().map(Entry::getKey).orElse("");
        }

        public String byLanguage(String language) {
            return entrySet().stream().findFirst().map(e -> e.getValue().byLanguage(language)).orElse("");
        }
    }

    // helper methods
    public List<String> keyList() {
        return stream().map(KeyWithNameByLanguage::key).collect(Collectors.toList());
    }

    public List<String> valueList(String language) {
        return stream().map(e -> e.byLanguage(language)).collect(Collectors.toList());
    }

    public List<String> valueListWithKey(String language) {
        return stream().map(e -> e.byLanguage(language) + " (" + e.key() + ")").collect(Collectors.toList());
    }
}
