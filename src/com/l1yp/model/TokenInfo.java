package com.l1yp.model;

public class TokenInfo {
    public int token = 0;
    public int offset = 0;

    public int start;
    public boolean isBlank = true;

    public int notBlankLeft = -1;
    public int notBlankRight = -1;

    public boolean hasDot = false;
    public boolean hasE = false;

    public Object val = null;
    public boolean succ = false;

    @Override
    public String toString() {
        return Token.desc(token);
    }
}