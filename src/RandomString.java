public final class RandomString {
    private static final int CHARS_LENGTH;
    private static final char[] CHARS;

    static {
        CHARS_LENGTH = 26 + 26;
        CHARS = new char[CHARS_LENGTH];
        for (int i = 0; i < 26; i++) {
            CHARS[i] = (char)('a' + i);
        }
        for (int i = 0; i < 26; i++) {
            CHARS[26 + i] = (char)('A' + i);
        }
    }

    private RandomString() {}

    static String generate(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int rnd = (int) Math.floor(Math.random() * CHARS_LENGTH);
            builder.append(CHARS[rnd]);
        }
        return builder.toString();
    }
}
