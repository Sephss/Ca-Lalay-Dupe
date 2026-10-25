package com.example.ca_lalay.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfanityFilter {

    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(

            // ===== Filipino / Tagalog =====
            "putangina",
            "bwakanang shit",
            "putang ina",
            "tanginamo",
            "tangina",
            "amputa",
            "ampota",
            "bugok",
            "puta",
            "putanginamo",
            "punyeta",
            "pakyu",
            "pakshet",
            "tanga",
            "gago",
            "gaga",
            "gunggong",
            "bobo",
            "boba",
            "ungas",
            "ulol",
            "tarantado",
            "tarantada",
            "leche",
            "lintik",
            "hayop",
            "hayup",
            "hinayupak",
            "bwisit",
            "buwisit",
            "peste",
            "engot",
            "abnoy",
            "kupal",
            "kingina",
            "yawa",
            "hudas",
            "walanghiya",
            "walang hiya",
            "animal ka",
            "ptangina",

            // ===== English =====
            "fuck",
            "fucking",
            "fucker",
            "motherfucker",
            "shit",
            "bullshit",
            "bitch",
            "asshole",
            "ass",
            "dick",
            "bastard",
            "cunt",
            "slut",
            "whore",
            "damn",
            "crap",
            "prick",
            "douche",
            "nigger",
            "nigga",
            "retard",
            "retarded"
    ));

    // Common leetspeak / lookalike substitutions per letter.
    // Each entry lists extra characters that can stand in for that letter.
    private static final java.util.Map<Character, String> LEET_MAP = new java.util.HashMap<>();
    static {
        LEET_MAP.put('a', "a4@");
        LEET_MAP.put('b', "b8");
        LEET_MAP.put('c', "c(");
        LEET_MAP.put('e', "e3");
        LEET_MAP.put('g', "g9");
        LEET_MAP.put('i', "i1!|");
        LEET_MAP.put('l', "l1|");
        LEET_MAP.put('o', "o0");
        LEET_MAP.put('s', "s5$");
        LEET_MAP.put('t', "t7+");
        LEET_MAP.put('u', "uv");
    }

    // Allowed "noise" characters that may be inserted between letters
    // to evade detection: spaces, dots, dashes, underscores, asterisks, etc.
    private static final String SEPARATOR = "[\\s\\-_.*'\"]*";

    public static String filter(String text) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        for (String badWord : BAD_WORDS) {

            String regex = "(?i)" + buildEvasionResistantPattern(badWord);

            Pattern pattern;
            try {
                pattern = Pattern.compile(regex);
            } catch (Exception e) {
                // Skip malformed pattern rather than crash the whole filter
                continue;
            }

            Matcher matcher = pattern.matcher(result);

            StringBuffer filtered = new StringBuffer();

            while (matcher.find()) {

                String matched = matcher.group();

                // Only asterisk actual letters/digits; leave any
                // separators inside the match untouched.
                String replacement = matched.replaceAll("[A-Za-z0-9]", "*");

                matcher.appendReplacement(
                        filtered,
                        Matcher.quoteReplacement(replacement)
                );
            }

            matcher.appendTail(filtered);

            result = filtered.toString();
        }

        return result;
    }

    /**
     * Builds a regex for a bad word that tolerates:
     *  - letter elongation ("gagoooo")
     *  - leetspeak / lookalike substitution ("g4go", "sh1t")
     *  - inserted separators between letters ("g a g o", "g.a.g.o")
     *  - embedding inside larger words ("gooshit") — no \b anchors
     */
    private static String buildEvasionResistantPattern(String badWord) {

        StringBuilder regex = new StringBuilder();
        String lower = badWord.toLowerCase();

        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);

            if (Character.isWhitespace(c)) {
                // A literal space in the bad word itself (e.g. "putang ina")
                // just means "at least one separator here".
                regex.append("[\\s\\-_.*'\"]+");
                continue;
            }

            String variants = LEET_MAP.getOrDefault(c, String.valueOf(c));

            regex.append("[")
                    .append(Pattern.quote(variants).replace("\\Q", "").replace("\\E", ""))
                    .append("]+");

            // Allow optional separator noise between letters,
            // except after the very last letter.
            if (i < lower.length() - 1) {
                regex.append(SEPARATOR);
            }
        }

        return regex.toString();
    }
}