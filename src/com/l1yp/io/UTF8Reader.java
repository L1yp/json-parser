package com.l1yp.io;

import com.l1yp.exception.ByteSequenceException;
import com.l1yp.util.CharUtil;

/**
 * @Author Lyp
 * @Date   2020-06-16
 * @Email  l1yp@qq.com
 */
public class UTF8Reader extends AbstractReader {

    public UTF8Reader(byte[] buffer) {
        super(buffer);
    }

    public UTF8Reader(byte[] buffer, int offset, int size) {
        super(buffer, offset, size);
    }

    @Override
    public int read() {

        // get first byte
        int b0 = buffer[offset++] & 0x00FF;


        int c;
        // UTF-8:   [0xxx xxxx]
        // Unicode: [0000 0000] [0xxx xxxx]
        if (b0 < 0x80) {
            c = (char) b0;
        }

        // UTF-8:   [110y yyyy] [10xx xxxx]
        // Unicode: [0000 0yyy] [yyxx xxxx]
        // (b0 & 0x1E) 是为了避免解码后 codePoint小于128
        else if ((b0 & 0xE0) == 0xC0 && (b0 & 0x1E) != 0) {
            int b1 = buffer[offset++] & 0x00FF;
            if ((b1 & 0xC0) != 0x80) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            c = ((b0 << 6) & 0x07C0) | (b1 & 0x003F);
        }

        // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
        // Unicode: [zzzz yyyy] [yyxx xxxx]
        else if ((b0 & 0xF0) == 0xE0) {
            int b1 = buffer[offset++] & 0x00FF;
            if ((b1 & 0xC0) != 0x80
                    || (b0 == 0xED && b1 >= 0xA0)
                    || ((b0 & 0x0F) == 0 && (b1 & 0x20) == 0)) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            int b2 = buffer[offset++] & 0x00FF;
            if ((b2 & 0xC0) != 0x80) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            c = ((b0 << 12) & 0xF000) | ((b1 << 6) & 0x0FC0) |
                    (b2 & 0x003F);
        }

        // UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
        // Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
        //          [1101 11yy] [yyxx xxxx] (low surrogate)
        //          * uuuuu = wwww + 1
        else if ((b0 & 0xF8) == 0xF0) {
            int b1 = buffer[offset++] & 0x00FF;
            if ((b1 & 0xC0) != 0x80
                    || ((b1 & 0x30) == 0 && (b0 & 0x07) == 0)) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            int b2 = buffer[offset++] & 0x00FF;
            if ((b2 & 0xC0) != 0x80) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            int b3 = buffer[offset++] & 0x00FF;
            if ((b3 & 0xC0) != 0x80) {
                throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
            }
            int uuuuu = ((b0 << 2) & 0x001C) | ((b1 >> 4) & 0x0003);
            if (uuuuu > 0x10) {
                throw new ByteSequenceException(String.format("invalidSurrogate, pos: %d,", offset));
            }
            int wwww = uuuuu - 1;
            int hs = 0xD800 |
                    ((wwww << 6) & 0x03C0) | ((b1 << 2) & 0x003C) |
                    ((b2 >> 4) & 0x0003);
            int ls = 0xDC00 | ((b2 << 6) & 0x03C0) | (b3 & 0x003F);
            // c = hs;
            // fSurrogate = ls;
            c = CharUtil.toCodePoint(hs, ls);
        }

        // error
        else {
            throw new ByteSequenceException(String.format("invalidByte, pos: %d,", offset));
        }

        // return character
        // if (true) {
        //     System.out.println("read(): 0x" + Integer.toHexString(c));
        // }
        return c;

    } // read():int




}
