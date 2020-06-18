package com.l1yp.io;


import com.l1yp.exception.ByteSequenceException;
import com.l1yp.util.RuntimeUtil;
import com.l1yp.util.Target;
import sun.nio.cs.CharsetMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Author Lyp
 * @Date 2020-06-17
 * @Email l1yp@qq.com
 */
public class GBKReader extends AbstractReader {

    private static char[][] b2c;
    private static char[] b2cSB;
    private static final int b2Min = 0x40;
    private static final int b2Max = 0xFE;


    public GBKReader(byte[] buffer) {
        super(buffer);
    }

    public GBKReader(byte[] buffer, int offset, int size) {
        super(buffer, offset, size);
    }

    @Override
    public int read() {
        int b1 = buffer[offset++] & 0x00FF;
        char c = b2cSB[b1];
        if (c == CharsetMapping.UNMAPPABLE_DECODING){
            if (offset + 1 >= limit){
                throw new IndexOutOfBoundsException(String.format(
                        "curPos: %d, limit: %d, remaining: %d", offset, limit, remaining()));
            }
            //  0x81-0xFE
            if (b1 < 0x81 || b1 > b2Max) {
                throw new ByteSequenceException("The GBK first byte range must be between 0x81-0xFE, curPos: " + offset);
            }

            //0x40-0xFE
            int b2 = buffer[offset++] & 255;
            if (b2 < b2Min || b2 > b2Max) {
                throw new ByteSequenceException("GBK's second byte range must be between 0x40-0xFE, curPos: " + offset);
            }
            c = b2c[b1][b2 - b2Min];
            if (c == CharsetMapping.UNMAPPABLE_DECODING){
                throw new ByteSequenceException(String.format("cannot conver to char from gbk table, b1: %d, b2: %d", b1, b2));
            }
        }

        return c;
    }




    static {
        try {
            String clsName;
            Target bootstrapVersion = RuntimeUtil.readJavaVersion(Object.class);
            if (bootstrapVersion.majorVersion > Target.JDK1_8.majorVersion){
                clsName = "sun.nio.cs.GBK";
            }else {
                clsName = "sun.nio.cs.ext.GBK";
            }


            Class<?> clazz = Class.forName(clsName);
            Field b2cField = clazz.getDeclaredField("b2c");
            Field b2cSBField = clazz.getDeclaredField("b2cSB");
            Method initb2cMethod = clazz.getDeclaredMethod("initb2c");

            b2cField.setAccessible(true);
            b2cSBField.setAccessible(true);
            initb2cMethod.setAccessible(true);

            initb2cMethod.invoke(null);
            b2c = (char[][]) b2cField.get(null);
            b2cSB = (char[]) b2cSBField.get(null);
        } catch (Exception e) {
            throw new Error("cannot read gbk table", e);
        }
    }

}
