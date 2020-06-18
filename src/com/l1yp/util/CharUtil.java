package com.l1yp.util;

import com.l1yp.util.Target;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author Lyp
 * @Date 2020-06-16
 * @Email l1yp@qq.com
 */
public class CharUtil {

    public static boolean isHex(int ch) {
        return (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') || (ch >= '0' && ch <= '9');
    }

    public static int toByte(int hi, int lo) {
        int h, l;
        if (hi >= 'a' && hi <= 'f') {
            h = hi - 'a' + 10;
        }else if (hi >= 'A' && hi <= 'F'){
            h = hi - 'A' + 10;
        }else{
            h = hi - '0';
        }

        if (lo >= 'a' && lo <= 'f') {
            l = lo - 'a' + 10;
        }else if (lo >= 'A' && lo <= 'F'){
            l = lo - 'A' + 10;
        }else{
            l = lo - '0';
        }
        return (h << 4 & 240) | l;
    }

    public static boolean validUnicodeHex(int[] chs, int off, int size, int limit) {
        if (off + size > limit){
            return false;
        }
        for (int i = 0; i < size; i++) {

            if (!((chs[off + i] >= 'a' && chs[off + i] <= 'f') ||
                    (chs[off + i] >= 'A' && chs[off + i] <= 'F')) &&
                    !Character.isDigit(chs[off + i])) {
                return false;
            }
        }
        return true;
    }



    public static boolean isHighUTF16Surrogate(int ch) {
        return ('\uD800' <= ch && ch <= '\uDBFF');
    }

    public static boolean isLowUTF16Surrogate(int ch) {
        return ('\uDC00' <= ch && ch <= '\uDFFF');
    }

    public static int toCodePoint(int highSurrogate, int lowSurrogate) {
        int codePoint =
                ((highSurrogate - 0xd800) << 10)
                        + (lowSurrogate - 0xdc00)
                        + 0x10000;
        return codePoint;
    }

}
