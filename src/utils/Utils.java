package utils;

// Utility class
public final class Utils {
    private static final int CHARS_LENGTH;
    private static final char[] CHARS;

    static {
        CHARS_LENGTH = 26 + 26;
        CHARS = new char[CHARS_LENGTH];
        for (int i = 0; i < 26; i++) {
            CHARS[i] = (char) ('a' + i);
        }
        for (int i = 0; i < 26; i++) {
            CHARS[26 + i] = (char) ('A' + i);
        }
    }

    private Utils() {
    }

    // generate a random string of a given length
    public static String random_string(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int rnd = (int) Math.floor(Math.random() * CHARS_LENGTH);
            builder.append(CHARS[rnd]);
        }
        return builder.toString();
    }
}
