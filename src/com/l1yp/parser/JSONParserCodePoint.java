package com.l1yp.parser;

import com.l1yp.JSONArray;
import com.l1yp.JSONObject;
import com.l1yp.exception.JSONException;
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
 * @Date 2020/6/13
 * @Email l1yp@qq.com
 */
public class JSONParserCodePoint {

    private int[] buffer;
    private int offset;

    /**
     * {       => next token must be " }
     * left"   => next token must be right"
     * right"  => next token must be , } [ ] :
     * :       => next token must be " , }
     * ,       => next token must be , [ ] { "
     * [       => next token must be [ { ] ,
     * ]       => next token must be , ] }
     * }       => next token must be , ] }
     * \       => escape
     */
    public JSONParserCodePoint(int[] chs) {
        buffer = chs;
    }

    public Object parse() {
        int limit = buffer.length;
        TokenInfo last = null;
        String key = null;
        Object val = null;
        Deque<Object> deque = new LinkedList<>();
        Object root = null;

        while (offset < limit && !(root != null && deque.isEmpty())) {
            TokenInfo ti = nextToken();
            Object top = deque.peek();
            switch (ti.token) {
                case Token.SINGLE_COMMENT_PN: {
                    nextLine();
                    // TokenInfo tokenInfo = nextLine();
                    // System.out.println("read single comment: " + tokenInfo.val);
                    break;
                }
                case Token.SLASH:{
                    if (buffer[offset] == Token.SLASH){
                        offset ++;
                        nextLine();
                        // TokenInfo tokenInfo = nextLine();
                        // System.out.println("read single comment: " + tokenInfo.val);
                    }else if (buffer[offset] == '*'){
                        offset ++;
                        nextCommentBlockEnd();
                        // TokenInfo tokenInfo = nextCommentBlockEnd();
                        // System.out.println("read block comment: " + tokenInfo.val);
                    }else {
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
                    if (!deque.isEmpty()){
                        if (top instanceof Map) {
                            JSONObject map = (JSONObject) top;
                            if (last.token == Token.COLON) {
                                map.put(key, curVal);
                            }else {
                                throw new JSONException(String.format(
                                        "occur error, token mismatch, curPos: %d, lastPos: %d",
                                        ti.offset, last.offset));
                            }
                        }else if (top instanceof List) {
                            JSONArray list = (JSONArray) top;
                            if (last.token == Token.BRACKET_START || last.token == Token.COMMA){
                                list.add(curVal);
                            }else {
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
                            if (!ti.isBlank){
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
                    if (!(top instanceof Map)){
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
                                                offset, ti.start
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
                    ti.token != Token.SINGLE_COMMENT_PN){
                last = ti;
            }
        }
        return root;
    }

    public JSONObject parseObject(){
        return (JSONObject) parse();
    }

    public JSONArray parseArray(){
        return (JSONArray) parse();
    }

    private TokenInfo nextLine() {
        int limit = buffer.length;
        int pos = offset;
        int ch;
        TokenInfo ti = new TokenInfo();
        while (pos < limit) {
            ch = buffer[pos];
            switch (ch) {
                case Token.CR:
                case Token.LF:{
                    ti.token = Token.SINGLE_COMMENT;
                    ti.start = offset;
                    ti.offset = pos;
                    ti.val = new String(buffer, ti.start, pos - ti.start);
                    offset = pos + 1;
                    return ti;
                }
                default: {
                    if (!Character.isWhitespace(ch)) {
                        ti.isBlank = false;
                    }
                }
            }
            pos++;
        }
        ti.token = Token.SINGLE_COMMENT;
        ti.start = offset;
        ti.offset = limit - 1;
        offset = limit;
        return ti;
    }

    private TokenInfo nextCommentBlockEnd() {
        int limit = buffer.length;
        int pos = offset;
        int ch;
        TokenInfo ti = new TokenInfo();
        while (pos < limit) {
            ch = buffer[pos];
            switch (ch) {
                case '*': {
                    if (buffer[pos + 1] == '/'){
                        ti.token = Token.BLOCK_COMMENT;
                        ti.start = offset;
                        ti.offset = pos;
                        ti.val = new String(buffer, ti.start, ti.offset - ti.start);
                        offset = pos + 2;

                        return ti;
                    }
                }
                default: {

                }
            }
            pos++;
        }
        throw new JSONException("cannot found block comment end: */");
    }

    private TokenInfo nextDoubleQuote() {
        int limit = buffer.length;
        int pos = offset;
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (pos < limit) {
            ch = buffer[pos];
            switch (ch) {
                case Token.DOUBLE_QUOTES: {
                    ti.token = Token.DOUBLE_QUOTES;
                    ti.start = offset;
                    ti.offset = pos;
                    ti.val = sb.toString();
                    offset = pos + 1;
                    return ti;
                }
                case Token.BACKSLASH: {
                    if (pos == limit - 1) {
                        throw new JSONException("cannot resolve backslash with length");
                    }

                    int nextCh = buffer[pos + 1];
                    if (nextCh == 'x' || nextCh == 'X') {
                        pos ++;
                        if (!CharUtil.validUnicodeHex(buffer, pos + 1, 2, buffer.length)) {
                            throw new JSONException("cannot resolve backslash with \\x");
                        }
                        int codePoint = Byte.parseByte(new String(buffer, pos + 1, 2), 16);
                        sb.appendCodePoint(codePoint);
                        pos += 2;
                    } else if (nextCh == 'u' || nextCh == 'U') {
                        pos ++;
                        if (!CharUtil.validUnicodeHex(buffer, pos + 1, 4, limit)) {
                            throw new JSONException("cannot resolve backslash with \\u");
                        }
                        int codePoint1 = Integer.parseInt(new String(buffer, pos + 1, 4), 16);
                        if (CharUtil.isHighUTF16Surrogate(codePoint1)){
                            if (pos + 10 < limit && buffer[pos + 5] =='\\' && buffer[pos + 6] =='u'){
                                if (!CharUtil.validUnicodeHex(buffer, pos + 7, 4, limit)) {
                                    throw new JSONException("cannot resolve backslash with \\u");
                                }
                                int codePoint2 = Integer.parseInt(new String(buffer, pos + 7, 4), 16);
                                sb.appendCodePoint(CharUtil.toCodePoint(codePoint1, codePoint2));
                                pos += 6;
                            }else {
                                throw new JSONException("Malformed Unicode characters, curPos: " + pos);
                            }
                        }else {
                            sb.appendCodePoint(codePoint1);
                        }
                        pos += 4;
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
                        pos++;
                    } else {
                        // System.out.println(new String(buffer, 0, pos));
                        throw new JSONException("cannot resolve backslash with " + Token.desc(nextCh));
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
            pos++;
        }
        throw new JSONException("Cannot read close quotes");
    }

    private TokenInfo nextSingleQuote() {
        int limit = buffer.length;
        int pos = offset;
        int ch;
        TokenInfo ti = new TokenInfo();
        StringBuilder sb = new StringBuilder();
        while (pos < limit) {
            ch = buffer[pos];
            switch (ch) {
                case Token.SINGLE_QUOTE: {
                    ti.token = Token.SINGLE_QUOTE;
                    ti.start = offset;
                    ti.offset = pos;
                    ti.val = sb.toString();
                    offset = pos + 1;
                    return ti;
                }
                case Token.BACKSLASH: {
                    if (pos == limit - 1) {
                        throw new JSONException("cannot resolve backslash with length");
                    }

                    int nextCh = buffer[pos + 1];
                    if (nextCh == 'x' || nextCh == 'X') {
                        pos ++;
                        if (!CharUtil.validUnicodeHex(buffer, pos + 1, 2, buffer.length)) {
                            throw new JSONException("cannot resolve backslash with \\x");
                        }
                        int codePoint = Integer.parseInt(new String(buffer, pos + 1, 2), 16);
                        sb.appendCodePoint(codePoint);
                        pos += 2;
                    } else if (nextCh == 'u' || nextCh == 'U') {
                        pos ++;
                        if (!CharUtil.validUnicodeHex(buffer, pos + 1, 4, limit)) {
                            throw new JSONException("cannot resolve backslash with \\u");
                        }
                        int codePoint1 = Integer.parseInt(new String(buffer, pos + 1, 4), 16);
                        if (CharUtil.isHighUTF16Surrogate(codePoint1)){
                            if (pos + 10 < limit && buffer[pos + 5] =='\\' && buffer[pos + 6] =='u'){
                                if (!CharUtil.validUnicodeHex(buffer, pos + 7, 4, limit)) {
                                    throw new JSONException("cannot resolve backslash with \\u");
                                }
                                int codePoint2 = Integer.parseInt(new String(buffer, pos + 7, 4), 16);
                                sb.appendCodePoint(CharUtil.toCodePoint(codePoint1, codePoint2));
                                pos += 6;
                            }else {
                                throw new JSONException("Malformed Unicode characters, curPos: " + pos);
                            }
                        }else {
                            sb.appendCodePoint(codePoint1);
                        }
                        pos += 4;
                    } else if (nextCh == '\'' ||
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
                        pos++;
                    } else {
                        // System.out.println(new String(buffer, 0, pos));
                        throw new JSONException("cannot resolve backslash with " + Token.desc(nextCh));
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
            pos++;
        }
        throw new JSONException("Cannot read close quotes");
    }

    public TokenInfo nextToken() {
        int pos = offset;
        int limit = buffer.length;
        int ch;
        TokenInfo ti = new TokenInfo();
        ti.start = pos;
        int blankToNotCnt = 0;
        boolean lastIsBlank = true;

        while (pos < limit) {
            ch = buffer[pos];
            ti.offset = pos;
            switch (ch) {
                case Token.SINGLE_COMMENT_PN:{
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
                        if (ch == '.'){
                            ti.hasDot = true;
                        }

                        if (ch == 'e' || ch == 'E'){
                            ti.hasE = true;
                        }

                        if (lastIsBlank) {
                            lastIsBlank = false;
                            if (ti.notBlankLeft == -1){
                                ti.notBlankLeft = pos;
                            }
                            blankToNotCnt++;
                            if (blankToNotCnt > 1){
                                throw new JSONException("Incorrect format of value");
                            }
                        }

                        ti.isBlank = false;
                    } else {
                        if (!lastIsBlank) {
                            if (ti.notBlankRight == -1) {
                                ti.notBlankRight = pos;
                            }
                        }
                        lastIsBlank = true;
                    }
                }
            }

            pos++;

            if (ti.token != 0) {
                if (ti.notBlankRight == -1) {
                    ti.notBlankRight = pos - 1;
                }
                break;
            }
        }
        if (ti.token == 0){
            throw new JSONException("Token cannot be found, the stream has ended, offset: " + offset);
        }

        if (!ti.isBlank && ti.notBlankLeft > -1 && ti.notBlankRight > -1 && ti.notBlankRight > ti.notBlankLeft){
            readValue(ti.notBlankLeft, ti.notBlankRight, ti);
        }

        offset = pos;
        return ti;
    }

    private void readValue(int start, int end, TokenInfo ti){
        if (!ti.hasDot && !ti.hasE){
            if (end - start >= 2 && buffer[start] == '0' &&
                    (buffer[start + 1] == 'x' || buffer[start + 1] == 'X')){
                ti.val = Long.decode(new String(buffer, start, end - start));
                ti.succ = true;
                return;
            }
        }


        if (end - start == 4){
            if (buffer[start] == 't' || buffer[start] == 'T'){
                boolean succ =  buffer[start + 0] == 't' || buffer[start + 0] == 'T';
                succ = succ && (buffer[start + 1] == 'r' || buffer[start + 1] == 'R');
                succ = succ && (buffer[start + 2] == 'u' || buffer[start + 2] == 'U');
                succ = succ && (buffer[start + 3] == 'e' || buffer[start + 3] == 'E');
                if (!succ){
                    throw new JSONException("unsupport value: " + new String(buffer, start, end - start + 1));
                }
                ti.val = Boolean.TRUE;
                ti.succ = true;
            }else if (buffer[start] == 'n' || buffer[start] == 'N'){
                boolean succ =  buffer[start + 0] == 'n' || buffer[start + 0] == 'N';
                succ = succ && (buffer[start + 1] == 'u' || buffer[start + 1] == 'U');
                succ = succ && (buffer[start + 2] == 'l' || buffer[start + 2] == 'L');
                succ = succ && (buffer[start + 3] == 'l' || buffer[start + 3] == 'L');
                if (!succ){
                    throw new JSONException("unsupport value: " + new String(buffer, start, end - start + 1));
                }
                ti.val = null;
                ti.succ = true;
            }else {
                if (ti.hasDot || ti.hasE){
                    ti.val = Double.parseDouble(new String(buffer, start, end - start));
                }else {
                    ti.val = Long.decode(new String(buffer, start, end - start));
                }
                ti.succ = true;
            }
        }else if (end - start == 5){
            boolean succ =  buffer[start + 0] == 'f' || buffer[start + 0] == 'F';
            succ = succ && (buffer[start + 1] == 'a' || buffer[start + 1] == 'A');
            succ = succ && (buffer[start + 2] == 'l' || buffer[start + 2] == 'L');
            succ = succ && (buffer[start + 3] == 's' || buffer[start + 3] == 'S');
            succ = succ && (buffer[start + 4] == 'e' || buffer[start + 4] == 'E');
            if (!succ){
                ti.val = new BigDecimal(new String(buffer, start, end - start));
                ti.succ = true;
            }
            ti.val = Boolean.FALSE;
            ti.succ = true;

        }else {
            if (ti.hasDot || ti.hasE){
                ti.val = Double.parseDouble(new String(buffer, start, end - start));
            }else {
                ti.val = Long.decode(new String(buffer, start, end - start));
            }
            ti.succ = true;
        }
    }



}
