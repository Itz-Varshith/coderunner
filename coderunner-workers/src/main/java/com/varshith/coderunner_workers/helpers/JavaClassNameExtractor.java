package com.varshith.coderunner_workers.helpers;

import org.springframework.stereotype.Component;

@Component
public class JavaClassNameExtractor {

    /**
     * Extracts the primary class name from raw Java code.
     * Prioritizes public classes/records/enums. If none exist, falls back
     * to the first declared class/record/enum found in the file.
     * * @param code The raw Java source code
     * @return The name of the class to compile/run, or null if invalid
     */
    public String extract(String code) {
        int i = 0, n = code.length();
        String firstFoundType = null;

        while (i < n) {
            char c = code.charAt(i);

            // --- skip single-line comment ---
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '/') {
                i += 2;
                while (i < n && code.charAt(i) != '\n') i++;
                continue;
            }

            // --- skip multi-line comment ---
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n && !(code.charAt(i) == '*' && code.charAt(i + 1) == '/')) i++;
                i += 2;
                continue;
            }

            // --- skip string ---
            if (c == '"') {
                i++;
                while (i < n) {
                    if (code.charAt(i) == '\\') i += 2;
                    else if (code.charAt(i) == '"') { i++; break; }
                    else i++;
                }
                continue;
            }

            // --- skip char ---
            if (c == '\'') {
                i++;
                while (i < n) {
                    if (code.charAt(i) == '\\') i += 2;
                    else if (code.charAt(i) == '\'') { i++; break; }
                    else i++;
                }
                continue;
            }

            // --- check for explicitly public type ---
            if (matchWord(code, i, "public")) {
                int j = i + 6;
                j = skipSpace(code, j);

                while (j < n) {
                    char tempC = code.charAt(j);

                    // If we hit structural characters, "public" was for a method/variable
                    if (tempC == '{' || tempC == '(' || tempC == ';') break;

                    if (matchWord(code, j, "class") || matchWord(code, j, "record") ||
                            matchWord(code, j, "enum") || matchWord(code, j, "interface")) {

                        j += readWordLen(code, j);
                        j = skipSpace(code, j);
                        return readIdentifier(code, j); // 🔥 Found public type, return immediately
                    }

                    // Skip unknown modifiers or annotations (e.g., @Deprecated, final, sealed)
                    if (tempC == '@') j++;
                    int wordLen = readWordLen(code, j);
                    j += (wordLen == 0) ? 1 : wordLen;
                    j = skipSpace(code, j);
                }
            }

            // --- fallback: log the first non-public type found ---
            if (firstFoundType == null) {
                if (matchWord(code, i, "class") || matchWord(code, i, "record") ||
                        matchWord(code, i, "enum") || matchWord(code, i, "interface")) {

                    int j = i + readWordLen(code, i);
                    j = skipSpace(code, j);
                    firstFoundType = readIdentifier(code, j);
                }
            }

            i++;
        }

        // Return the first type found if no public type exists (common in CP)
        return firstFoundType;
    }

    private static boolean matchWord(String s, int i, String word) {
        int len = word.length();
        if (i + len > s.length()) return false;

        if (!s.substring(i, i + len).equals(word)) return false;

        // Prevent matching inside other words
        if (i > 0 && Character.isJavaIdentifierPart(s.charAt(i - 1))) return false;
        if (i + len < s.length() && Character.isJavaIdentifierPart(s.charAt(i + len))) return false;

        // Prevent matching field accesses like `String.class`
        if (i > 0 && s.charAt(i - 1) == '.') return false;

        return true;
    }

    private static int skipSpace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    private static int readWordLen(String s, int i) {
        int start = i;
        while (i < s.length() && Character.isJavaIdentifierPart(s.charAt(i))) i++;
        return i - start;
    }

    private static String readIdentifier(String s, int i) {
        int start = i;
        while (i < s.length() && Character.isJavaIdentifierPart(s.charAt(i))) i++;
        return s.substring(start, i);
    }
}