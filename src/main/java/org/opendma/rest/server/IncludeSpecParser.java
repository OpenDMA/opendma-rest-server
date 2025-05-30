package org.opendma.rest.server;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IncludeSpecParser {

    public static class IncludeSpec {
        public final String nextTokenPrefix;
        public final String propertySpec;

        public IncludeSpec(String nextTokenPrefix, String propertySpec) {
            this.nextTokenPrefix = nextTokenPrefix;
            this.propertySpec = propertySpec;
        }

        @Override
        public String toString() {
            return "IncludeSpec(" + nextTokenPrefix + ", " + propertySpec + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IncludeSpec)) return false;
            IncludeSpec pair = (IncludeSpec) o;
            return Objects.equals(nextTokenPrefix, pair.nextTokenPrefix) && Objects.equals(propertySpec, pair.propertySpec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nextTokenPrefix, propertySpec);
        }
    }

    public static List<IncludeSpec> parse(String input) {
        List<IncludeSpec> result = new ArrayList<>();
        
        if(input == null) {
            return result;
        }

        StringBuilder segment = new StringBuilder();
        boolean escapeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escapeNext) {
                segment.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == ';') {
                processSegment(segment.toString(), result);
                segment.setLength(0);
            } else {
                segment.append(c);
            }
        }

        processSegment(segment.toString(), result);

        return result;
    }

    private static void processSegment(String segment, List<IncludeSpec> result) {
        if (segment.isEmpty()) return;

        StringBuilder prefix = new StringBuilder();
        StringBuilder value = new StringBuilder();

        boolean escapeNext = false;
        boolean foundAt = false;

        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);

            if (escapeNext) {
                if (foundAt) {
                    value.append(c);
                } else {
                    prefix.append(c);
                }
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '@' && !foundAt) {
                foundAt = true;
            } else {
                if (foundAt) {
                    value.append(c);
                } else {
                    prefix.append(c);
                }
            }
        }

        String finalPrefix = foundAt && prefix.length() > 0 ? prefix.toString() : null;
        String finalValue = foundAt ? value.toString() : prefix.toString();

        result.add(new IncludeSpec(finalPrefix, finalValue));
    }

}
