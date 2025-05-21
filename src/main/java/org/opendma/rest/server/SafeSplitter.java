package org.opendma.rest.server;

import java.util.ArrayList;
import java.util.List;

public class SafeSplitter {
    
    public static final char SPLIT_CHAR = ';';

    public static final char ESCAPE_CHAR = '\\';

    public static List<String> split(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escapeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escapeNext) {
                // Always add the next char, regardless of what it is
                current.append(c);
                escapeNext = false;
            } else {
                if (c == ESCAPE_CHAR) {
                    escapeNext = true;
                } else if (c == SPLIT_CHAR) {
                    result.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }

        // Add the last part
        result.add(current.toString());

        return result;
    }

    public static String encode(String input) {
        // Encode \ as \\ first, then ; as \;
        return input.replace("\\", "\\\\").replace(";", "\\;");
    }

}
