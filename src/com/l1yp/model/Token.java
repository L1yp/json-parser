package com.l1yp.model;

public class Token {
    public static final int SLASH = '/';
    public static final int BACKSLASH = '\\';
    public static final int COMMA = ',';
    public static final int DELIM_START = '{';
    public static final int DELIM_END = '}';
    public static final int BRACKET_START = '[';
    public static final int BRACKET_END = ']';
    public static final int COLON = ':';
    public static final int DOUBLE_QUOTES = '"';
    public static final int SINGLE_QUOTE = '\'';
    public static final int LF = '\n';
    public static final int CR = '\r';

    public static final int SINGLE_COMMENT = -101;
    public static final int SINGLE_COMMENT_PN = '#';
    public static final int BLOCK_COMMENT = -102;

    public static String desc(int token) {
        switch (token) {
            case BACKSLASH:
                return "BACKSLASH(\\)";
            case COMMA:
                return "COMMA(,)";
            case DELIM_START:
                return "DELIM_START({)";
            case DELIM_END:
                return "DELIM_END(})";
            case BRACKET_START:
                return "BRACKET_START([)";
            case BRACKET_END:
                return "BRACKET_END(])";
            case COLON:
                return "COLON(:)";
            case DOUBLE_QUOTES:
                return "DOUBLE_QUOTES(\")";
            case SINGLE_COMMENT:
                return "SINGLE_COMMENT(//)";
            case SINGLE_COMMENT_PN:
                return "SINGLE_COMMENT_PN(#)";
            case BLOCK_COMMENT:
                return "BLOCK_COMMENT(/*)";
            default:
                return String.valueOf(new char[]{(char) token});
        }
    }

}