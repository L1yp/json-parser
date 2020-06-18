package com.l1yp.parser;

import com.l1yp.JSONArray;
import com.l1yp.JSONObject;
import com.l1yp.exception.JSONException;
import com.l1yp.io.Reader;
import com.l1yp.model.Token;
import com.l1yp.model.TokenInfo;
import com.l1yp.util.CharUtil;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author Lyp
 * @Date   2020-06-17
 * @Email l1yp@qq.com
 */
public class JSONParser {

    private Reader reader;

    public JSONParser(Reader reader) {
        this.reader = reader;
    }


    public Object parse() {
        int limit = this.reader.limit();
        TokenInfo last = null;
        String key = null;
        Object val = null;
        Deque<Object> deque = new LinkedList<>();
        Object root = null;

        while (this.reader.remaining() > 0 && !(root != null && deque.isEmpty())) {
            TokenInfo ti = nextToken();
            Object top = deque.peek();
            switch (ti.token) {
                case Token.SINGLE_COMMENT_PN: {
                    nextLine();
                    // TokenInfo tokenInfo = nextLine();
                    // System.out.println("read single comment: " + tokenInfo.val);
                    break;
                }
                case Token.SLASH: {
                    int cp = reader.read();
                    if (cp == Token.SLASH) {
                        reader.skip(1);
                        nextLine();
                        // TokenInfo tokenInfo = nextLine();
                        // System.out.println("read single comment: " + tokenInfo.val);
                    } else if (cp == '*') {
                        reader.skip(1);
                        nextCommentBlockEnd();
                        // TokenInfo tokenInfo = nextCommentBlockEnd();
                        // System.out.println("read block comment: " + tokenInfo.val);
                    } else {
                        throw new JSONException("cannot resolve single slash");
                    }

                    break;
                }
                case Token.DELIM_START: {
                    JSONObject curVal = new JSONObject();
                    if (!deque.isEmpty()) {
                        if (last.token == Token.COLON) {
                            if (top instanceof Map) {
                                JSONObject map = (JSONObject) top;
                                map.put(key, curVal);
                            } else {
                                throw new JSONException(String.format(
                                        "occur error, expect root, provide list, curPos: %d, lastPos: %d",
                                        ti.offset, last.offset));
                            }
                        } else if (last.token == Token.COMMA || last.token == Token.BRACKET_START) {
                            if (top instanceof List) {
                                JSONArray list = (JSONArray) top;
                                list.add(curVal);
                            } else {
                                throw new JSONException(String.format(
                                        "occur error, expect list, provide root, curPos: %d, lastPos: %d",
                                        ti.offset, last.offset));
                            }
                        } else {
                            throw new JSONException(String.format(
                                    "occur error, invalid token: last token is %s, curPos: %d, lastPos: %d",
                                    Token.desc(last.token), ti.offset, last.offset));
                        }
                    } else {
                        root = curVal;
                    }
                    deque.push(curVal);
                    break;
                }
                case Token.DELIM_END: {
                    if (top instanceof Map) {
                        JSONObject map = (JSONObject) top;
                        if (last.token == Token.COLON) {
                            if (ti.isBlank) {
                                throw new JSONException(String.format(
                                        "occur error, There is blank between %s and %s, curPos: %d, lastPos: %d",
                                        Token.desc(last.token), Token.desc(ti.token),
                                        ti.offset, last.offset));
                            }
                            map.put(key, ti.val);
                        } else if (last.token == Token.DELIM_START) {
                            // empty map  ignore
                        } else if (last.token == Token.DOUBLE_QUOTES || last.token == Token.SINGLE_QUOTE) {
                            // ignore
                        } else if (last.token == Token.DELIM_END) {
                            // ignore
                        } else if (last.token == Token.BRACKET_END) {
                            // ignore
                        } else {
                            throw new JSONException(String.format(
                                    "occur error, invalid token: last token: %s, current token: %s, curPos: %d, lastPos: %d",
                                    Token.desc(last.token), Token.desc(ti.token),
                                    ti.offset, last.offset));
                        }
                    } else if (top instanceof List) {
                        // ignore
                    }
                    deque.pop();
                    break;
                }
                case Token.BRACKET_START: {
                    JSONArray curVal = new JSONArray();
                    if (!deque.isEmpty()) {
                        if (top instanceof Map) {
                            JSONObject map = (JSONObject) top;
                            if (last.token == Token.COLON) {
                                map.put(key, curVal);
                            } else {
                                throw new JSONException(String.format(
                                        "occur error, token mismatch, curPos: %d, lastPos: %d",
                                        ti.offset, last.offset));
                            }
                        } else if (top instanceof List) {
                            JSONArray list = (JSONArray) top;
                            if (last.token == Token.BRACKET_START || last.token == Token.COMMA) {
                                list.add(curVal);
                            } else {
                                throw new JSONException(String.format(
                                        "occur error, token mismatch, curPos: %d, lastPos: %d",
                                        ti.offset, last.offset));
                            }
                        }
                    } else {
                        root = curVal;
                    }

                    deque.push(curVal);
                    break;
                }
                case Token.BRACKET_END: {
                    if (top instanceof List) {
                        JSONArray list = (JSONArray) top;
                        if (last.token == Token.COMMA) {
                            if (ti.isBlank) {
                                throw new JSONException(String.format(
                                        "occur error, There is blank between %s and %s, curPos: %d, lastPos: %d",
                                        Token.desc(last.token), Token.desc(ti.token),
                                        ti.offset, last.offset));
                            }
                            list.add(ti.val);
                        } else if (last.token == Token.BRACKET_START) {
                            if (!ti.isBlank) {
                                list.add(ti.val);
                            }
                        } else if (last.token == Token.DOUBLE_QUOTES || last.token == Token.SINGLE_QUOTE) {
                        } else if (last.token == Token.BRACKET_END ||
                                last.token == Token.DELIM_END) {
                        } else {
                            throw new JSONException(String.format(
                                    "occur error, token mismatch, curPos: %d, lastPos: %d",
                                    ti.offset, last.offset));
                        }
                        deque.pop();
                    } else {
                        throw new JSONException(String.format(
                                "occur error, type mismatch, curPos: %d, lastPos: %d",
                                ti.offset, last.offset));
                    }
                    break;
                }
                case Token.DOUBLE_QUOTES: {
                    // TODO 转义 \ 字符
                    TokenInfo tokenInfo = nextDoubleQuote();
                    if (last.token == Token.COLON) {
                        val = tokenInfo.val;
                    } else {
                        key = (String) tokenInfo.val;
                    }

                    if (top instanceof Map) {
                        JSONObject map = (JSONObject) top;
                        if (last.token == Token.COLON) {
                            map.put(key, val);
                        }
                    } else if (top instanceof List) {
                        JSONArray list = (JSONArray) top;
                        list.add(key);
                    }
                    break;
                }
                case Token.SINGLE_QUOTE: {
                    // TODO 转义 \ 字符
                    TokenInfo tokenInfo = nextSingleQuote();
                    if (last.token == Token.COLON) {
                        val = tokenInfo.val;
                    } else {
                        key = (String) tokenInfo.val;
                    }

                    if (top instanceof Map) {
                        JSONObject map = (JSONObject) top;
                        if (last.token == Token.COLON) {
                            map.put(key, val);
                        }
                    } else if (top instanceof List) {
                        JSONArray list = (JSONArray) top;
                        list.add(key);
                    }
                    break;
                }

                case Token.COLON: {
                    if (last.token != Token.DOUBLE_QUOTES && last.token != Token.SINGLE_QUOTE) {
                        throw new JSONException(String.format(
                                "occur error, cannot found key, curPos: %d, lastPos: %d",
                                ti.offset, last.offset));
                    }
                    if (!(top instanceof Map)) {
                        throw new JSONException(String.format(
                                "occur error, the top of stack is not root, curPos: %d, lastPos: %d",
                                ti.offset, last.offset));
                    }

                    break;
                }
                case Token.COMMA: {
                    if (top instanceof Map) {
                        JSONObject map = (JSONObject) top;
                        if (last.token == Token.COLON) {
                            if (ti.isBlank) {
                                throw new JSONException(String.format(
                                        "occur error, There is blank between %s and %s, curPos: %d, lastPos: %d",
                                        Token.desc(last.token), Token.desc(ti.token),
                                        ti.offset, last.offset));
                            }
                            // val = new String(buffer, ti.notBlankLeft, ti.notBlankRight - ti.notBlankLeft);
                            map.put(key, ti.val);
                        } else if (last.token == Token.DOUBLE_QUOTES || last.token == Token.SINGLE_QUOTE) {
                            //已处理
                        } else if (last.token == Token.BRACKET_END) {
                            //已处理
                        } else if (last.token == Token.DELIM_END) {
                            //已处理
                        } else {
                            throw new JSONException(String.format(
                                    "occur error, token mismatch, curPos: %d, lastPos: %d",
                                    ti.offset, last.offset));
                        }
                    } else if (top instanceof List) {
                        JSONArray list = (JSONArray) top;
                        if (last.token == Token.BRACKET_START || last.token == Token.COMMA) {
                            if (ti.isBlank) {
                                throw new JSONException(
                                        String.format("No data can be read between %s and %s, curPos: %d, lastPos: %d",
                                                Token.desc(last.token),
                                                Token.desc(ti.token),
                                                this.reader.offset(), ti.start
                                        ));
                            }
                            list.add(ti.val);
                        } else {
                            //ignored
                        }
                    }
                    break;
                }
                default: {

                }
            }
            if (ti.token != Token.SLASH &&
                    ti.token != Token.SINGLE_COMMENT_PN) {
                last = ti;
            }
        }
        return root;
    }

    public JSONObject parseObject() {
        return (JSONObject) parse();
    }

    public JSONArray parseArray() {
        return (JSONArray) parse();
    }

    private TokenInfo nextLine() {
        int off = this.reader.offset();
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (this.reader.remaining() > 0) {
            ch = this.reader.read();
            switch (ch) {
                case Token.CR:
                case Token.LF: {
                    ti.token = Token.SINGLE_COMMENT;
                    ti.start = off;
                    ti.offset = this.reader.offset();
                    ti.val = sb.toString();
                    return ti;
                }
                default: {
                    sb.appendCodePoint(ch);
                    if (!Character.isWhitespace(ch)) {
                        ti.isBlank = false;
                    }
                }
            }
        }
        ti.token = Token.SINGLE_COMMENT;
        ti.start = off;
        ti.offset = this.reader.offset(); // TODO double check
        return ti;
    }

    private TokenInfo nextCommentBlockEnd() {
        int off = this.reader.offset();
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (this.reader.remaining() > 0) {
            ch = this.reader.read();
            switch (ch) {
                case '*': {
                    int cp = this.reader.peek();
                    if (cp == '/') {
                        ti.token = Token.BLOCK_COMMENT;
                        ti.start = off;
                        ti.offset = this.reader.offset();
                        ti.val = sb.toString();
                        this.reader.skip(1);
                        return ti;
                    }
                }
                default: {
                    sb.appendCodePoint(ch);
                }
            }
        }
        throw new JSONException("cannot found block comment end: */");
    }

    private TokenInfo nextDoubleQuote() {
        int limit = this.reader.limit();
        int off = this.reader.offset();
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (this.reader.remaining() > 0) {
            ch = this.reader.read();
            switch (ch) {
                case Token.DOUBLE_QUOTES: {
                    ti.token = Token.DOUBLE_QUOTES;
                    ti.start = off;
                    ti.offset = this.reader.offset();
                    ti.val = sb.toString();
                    return ti;
                }
                case Token.BACKSLASH: {
                    if (this.reader.remaining() <= 0) {
                        throw new JSONException("cannot resolve backslash with \\ has no remaining, offset: "
                                + this.reader.offset());
                    }

                    this.reader.mark();
                    int nextCh = this.reader.read();
                    if (nextCh == 'x' || nextCh == 'X') {
                        int b1 = this.reader.read();
                        int b2 = this.reader.read();
                        if (CharUtil.isHex(b1) && CharUtil.isHex(b2)) {
                            int codePoint = CharUtil.toByte(b1, b2);
                            sb.appendCodePoint(codePoint);
                        } else {
                            throw new JSONException("cannot resolve backslash with \\x with invalid hex, offset: "
                                    + this.reader.offset());
                        }


                    } else if (nextCh == 'u' || nextCh == 'U') {
                        int b1 = this.reader.read();
                        int b2 = this.reader.read();
                        int b3 = this.reader.read();
                        int b4 = this.reader.read();
                        if (!CharUtil.isHex(b1) || !CharUtil.isHex(b2) || !CharUtil.isHex(b3) || !CharUtil.isHex(b4)) {
                            throw new JSONException("cannot resolve backslash with \\u with invalid hex, offset: "
                                    + this.reader.offset());
                        }


                        int hi = CharUtil.toByte(b1, b2);
                        int lo = CharUtil.toByte(b3, b4);
                        int cp1 = ((hi << 8) | lo);
                        if (CharUtil.isHighUTF16Surrogate(cp1)) {
                            int nextB1 = this.reader.read();
                            int nextB2 = this.reader.read();
                            if (nextB1 != '\\' || !(nextB2 == 'u' || nextB2 == 'U')){
                                throw new JSONException("cannot resolve backslash with \\u LowUTF16Surrogate, offset: "
                                        + this.reader.offset());
                            }

                            int nextB3 = this.reader.read();
                            int nextB4 = this.reader.read();
                            int nextB5 = this.reader.read();
                            int nextB6 = this.reader.read();
                            if (!CharUtil.isHex(nextB3) ||
                                !CharUtil.isHex(nextB4) ||
                                !CharUtil.isHex(nextB5) ||
                                !CharUtil.isHex(nextB6)) {
                                throw new JSONException("cannot resolve backslash with \\u with invalid hex, offset: "
                                        + this.reader.offset());

                            }

                            int nextHi = CharUtil.toByte(nextB3, nextB4);
                            int nextLo = CharUtil.toByte(nextB5, nextB6);
                            int cp2 = ((nextHi << 8) | nextLo);
                            if (!CharUtil.isLowUTF16Surrogate(cp2)){
                                throw new JSONException("cannot resolve backslash with \\u LowUTF16Surrogate, offset: "
                                        + this.reader.offset());
                            }

                            sb.appendCodePoint(CharUtil.toCodePoint(cp1, cp2));

                        } else {
                            sb.appendCodePoint(cp1);
                        }

                    } else if (nextCh == '"' ||
                            nextCh == '\\' ||
                            nextCh == '/' ||
                            nextCh == 'b' ||
                            nextCh == 'f' ||
                            nextCh == 'n' ||
                            nextCh == 'r' ||
                            nextCh == 't') {
                        sb.appendCodePoint(nextCh == '"' ? '"' : (nextCh == '\\' ? '\\' : (nextCh == '/' ? '/' : (
                                nextCh == 'b' ? '\b' : (nextCh == 'f' ? '\f' : (nextCh == 'n' ? '\n' : (
                                        nextCh == 'r' ? '\r' : (nextCh == 't' ? '\t' : nextCh)
                                )))
                        ))));
                        // ignore
                    } else {
                        // System.out.println(new String(buffer, 0, pos));
                        throw new JSONException(String.format("cannot resolve backslash with %s, offset: %d",
                                Token.desc(nextCh), this.reader.offset()));

                    }
                    break;
                }
                default: {
                    sb.appendCodePoint(ch);
                    if (!Character.isWhitespace(ch)) {
                        ti.isBlank = false;
                    }
                }
            }
        }
        throw new JSONException("Cannot read close quotes");
    }

    private TokenInfo nextSingleQuote() {
        int start = this.reader.offset();
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (this.reader.remaining() > 0) {
            ch = this.reader.read();
            switch (ch) {
                case Token.SINGLE_QUOTE: {
                    ti.token = Token.SINGLE_QUOTE;
                    ti.start = start;
                    ti.offset = this.reader.offset();
                    ti.val = sb.toString();
                    return ti;
                }
                case Token.BACKSLASH: {
                    if (this.reader.remaining() <= 0) {
                        throw new JSONException("cannot resolve backslash with \\ has no remaining, offset: "
                                + this.reader.offset());
                    }

                    this.reader.mark();
                    int nextCh = this.reader.read();
                    if (nextCh == 'x' || nextCh == 'X') {
                        int b1 = this.reader.read();
                        int b2 = this.reader.read();
                        if (CharUtil.isHex(b1) && CharUtil.isHex(b2)) {
                            int codePoint = CharUtil.toByte(b1, b2);
                            sb.appendCodePoint(codePoint);
                        } else {
                            throw new JSONException("cannot resolve backslash with \\x with invalid hex, offset: "
                                    + this.reader.offset());
                        }


                    } else if (nextCh == 'u' || nextCh == 'U') {
                        int b1 = this.reader.read();
                        int b2 = this.reader.read();
                        int b3 = this.reader.read();
                        int b4 = this.reader.read();
                        if (!CharUtil.isHex(b1) || !CharUtil.isHex(b2) || !CharUtil.isHex(b3) || !CharUtil.isHex(b4)) {
                            throw new JSONException("cannot resolve backslash with \\u with invalid hex, offset: "
                                    + this.reader.offset());
                        }

                        int hi = CharUtil.toByte(b1, b2);
                        int lo = CharUtil.toByte(b3, b4);
                        int cp1 = ((hi << 8) | lo);
                        if (CharUtil.isHighUTF16Surrogate(cp1)) {
                            int nextB1 = this.reader.read();
                            int nextB2 = this.reader.read();
                            if (nextB1 != '\\' || !(nextB2 == 'u' || nextB2 == 'U')){
                                throw new JSONException("cannot resolve backslash with \\u LowUTF16Surrogate, offset: "
                                        + this.reader.offset());
                            }

                            int nextB3 = this.reader.read();
                            int nextB4 = this.reader.read();
                            int nextB5 = this.reader.read();
                            int nextB6 = this.reader.read();
                            if (!CharUtil.isHex(nextB3) ||
                                    !CharUtil.isHex(nextB4) ||
                                    !CharUtil.isHex(nextB5) ||
                                    !CharUtil.isHex(nextB6)) {
                                throw new JSONException("cannot resolve backslash with \\u with invalid hex, offset: "
                                        + this.reader.offset());

                            }

                            int nextHi = CharUtil.toByte(nextB3, nextB4);
                            int nextLo = CharUtil.toByte(nextB5, nextB6);
                            int cp2 = ((nextHi << 8) | nextLo);
                            if (!CharUtil.isLowUTF16Surrogate(cp2)){
                                throw new JSONException("cannot resolve backslash with \\u LowUTF16Surrogate, offset: "
                                        + this.reader.offset());
                            }

                            sb.appendCodePoint(CharUtil.toCodePoint(cp1, cp2));

                        } else {
                            sb.appendCodePoint(cp1);
                        }

                    } else if (nextCh == '"' ||
                            nextCh == '\\' ||
                            nextCh == '/' ||
                            nextCh == 'b' ||
                            nextCh == 'f' ||
                            nextCh == 'n' ||
                            nextCh == 'r' ||
                            nextCh == 't') {
                        sb.appendCodePoint(nextCh == '"' ? '"' : (nextCh == '\\' ? '\\' : (nextCh == '/' ? '/' : (
                                nextCh == 'b' ? '\b' : (nextCh == 'f' ? '\f' : (nextCh == 'n' ? '\n' : (
                                        nextCh == 'r' ? '\r' : (nextCh == 't' ? '\t' : nextCh)
                                )))
                        ))));
                        // ignore
                    } else {
                        // System.out.println(new String(buffer, 0, pos));
                        throw new JSONException(String.format("cannot resolve backslash with %s, offset: %d",
                                Token.desc(nextCh), this.reader.offset()));

                    }
                    break;
                }
                default: {
                    sb.appendCodePoint(ch);
                    if (!Character.isWhitespace(ch)) {
                        ti.isBlank = false;
                    }
                }
            }
        }
        throw new JSONException("Cannot read close quotes");
    }

    public TokenInfo nextToken() {

        int ch;
        TokenInfo ti = new TokenInfo();
        ti.start = this.reader.offset();
        int blankToNotCnt = 0;
        boolean lastIsBlank = true;

        while (this.reader.remaining() > 0) {
            ch = this.reader.read();
            switch (ch) {
                case Token.SINGLE_COMMENT_PN: {
                    ti.token = Token.SINGLE_COMMENT_PN;
                    break;
                }
                case Token.SLASH: {
                    ti.token = Token.SLASH;
                    break;
                }
                case Token.BRACKET_END: {
                    ti.token = Token.BRACKET_END;
                    break;
                }
                case Token.BRACKET_START: {
                    ti.token = Token.BRACKET_START;
                    break;
                }
                case Token.DELIM_START: {
                    ti.token = Token.DELIM_START;
                    break;
                }
                case Token.DELIM_END: {
                    ti.token = Token.DELIM_END;
                    break;
                }
                case Token.COMMA: {
                    ti.token = Token.COMMA;
                    break;
                }
                case Token.COLON: {
                    ti.token = Token.COLON;
                    break;
                }
                case Token.SINGLE_QUOTE: {
                    ti.token = Token.SINGLE_QUOTE;
                    break;
                }
                case Token.DOUBLE_QUOTES: {
                    ti.token = Token.DOUBLE_QUOTES;
                    break;
                }
                default: {
                    if (!Character.isWhitespace(ch)) {
                        if (ch == '.') {
                            ti.hasDot = true;
                        }

                        if (ch == 'e' || ch == 'E') {
                            ti.hasE = true;
                        }

                        if (lastIsBlank) {
                            lastIsBlank = false;
                            if (ti.notBlankLeft == -1) {
                                ti.notBlankLeft = this.reader.offset() - 1;
                            }
                            blankToNotCnt++;
                            if (blankToNotCnt > 1) {
                                throw new JSONException("Incorrect format of value");
                            }
                        }

                        ti.isBlank = false;
                    } else {
                        if (!lastIsBlank) {
                            if (ti.notBlankRight == -1) {
                                ti.notBlankRight = this.reader.offset() - 1;
                            }
                        }
                        lastIsBlank = true;
                    }
                }
            }

            if (ti.token != 0) {
                if (ti.notBlankRight == -1) {
                    ti.notBlankRight = this.reader.offset() - 1;
                }
                break;
            }
        }
        if (ti.token == 0) {
            throw new JSONException("Token cannot be found, the stream has ended, offset: " + this.reader.offset());
        }

        if (!ti.isBlank && ti.notBlankLeft > -1 && ti.notBlankRight > -1 && ti.notBlankRight > ti.notBlankLeft) {
            readValue(ti.notBlankLeft, ti.notBlankRight, ti);
        }

        return ti;
    }

    private void readValue(int start, int end, TokenInfo ti) {
        int mark = this.reader.offset();

        this.reader.offset(start);
        if (!ti.hasDot && !ti.hasE) {
            int b1 = this.reader.read();
            int b2 = this.reader.read();
            if (end - start >= 2 && b1 == '0' &&
                    (b2 == 'x' || b2 == 'X')) {

                ti.val = Long.decode(this.reader.toSubString(start, end));
                ti.succ = true;
                this.reader.offset(mark);
                return;
            }
            this.reader.offset(start);
        }


        if (end - start == 4) {
            int b1 = this.reader.read();
            int b2 = this.reader.read();
            int b3 = this.reader.read();
            int b4 = this.reader.read();
            if (b1 == 't' || b1 == 'T') {
                boolean succ = b1 == 't' || b1 == 'T';
                succ = succ && (b2 == 'r' || b2 == 'R');
                succ = succ && (b3 == 'u' || b3 == 'U');
                succ = succ && (b4 == 'e' || b4 == 'E');
                if (!succ) {
                    throw new JSONException("unsupport value: " + this.reader.toSubString(start, end));
                }
                ti.val = Boolean.TRUE;
                ti.succ = true;
                this.reader.offset(mark);
                return;
            } else if (b1 == 'n' || b1 == 'N') {
                boolean succ = b1== 'n' || b1 == 'N';
                succ = succ && (b2 == 'u' || b2 == 'U');
                succ = succ && (b3 == 'l' || b3 == 'L');
                succ = succ && (b4 == 'l' || b4 == 'L');
                if (!succ) {
                    throw new JSONException("unsupport value: " + this.reader.toSubString(start, end));
                }
                ti.val = null;
                ti.succ = true;
                this.reader.offset(mark);
                return;
            } else {
                if (ti.hasDot || ti.hasE) {
                    ti.val = Double.parseDouble(this.reader.toSubString(start, end));
                } else {
                    ti.val = Long.decode(this.reader.toSubString(start, end));
                }
                ti.succ = true;
            }
            this.reader.offset(mark);
            return;
        } else if (end - start == 5) {
            int b1 = this.reader.read();
            int b2 = this.reader.read();
            int b3 = this.reader.read();
            int b4 = this.reader.read();
            int b5 = this.reader.read();
            boolean succ =  b1 == 'f' || b1 == 'F';
            succ = succ && (b2 == 'a' || b2 == 'A');
            succ = succ && (b3 == 'l' || b3 == 'L');
            succ = succ && (b4 == 's' || b4 == 'S');
            succ = succ && (b5 == 'e' || b5 == 'E');
            if (!succ) {
                ti.val = new BigDecimal(this.reader.toSubString(start, end));
                ti.succ = true;
            }
            ti.val = Boolean.FALSE;
            ti.succ = true;
            this.reader.offset(start);
        } else {
            if (ti.hasDot || ti.hasE) {
                ti.val = Double.parseDouble(this.reader.toSubString(start, end));
            } else {
                ti.val = Long.decode(this.reader.toSubString(start, end));
            }
            ti.succ = true;
        }
        this.reader.offset(mark);
    }

}
