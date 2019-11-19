package com.timebusker.phoenix.dialect;

import java.util.regex.Pattern;

/**
 *
 */
public final class QueryUtils {
    // TODO Ignore hints
    private static final Pattern MULTILINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("-- (.*)?$", Pattern.MULTILINE);

    public static final String removeQueryComments(String query) {
        return SINGLE_LINE_COMMENT.matcher(MULTILINE_COMMENT.matcher(query).replaceAll("")).replaceAll("");
    }
}