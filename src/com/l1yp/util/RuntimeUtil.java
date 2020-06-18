package com.l1yp.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author Lyp
 * @Date 2020-06-17
 * @Email l1yp@qq.com
 */
public class RuntimeUtil {


    /**
     * 根据clazz获取class版本, 例如传入Object.class可以获取运行时的JRE版本
     * @param clazz
     * @return 目标版本
     * @throws IOException
     */
    public static Target readJavaVersion(Class<?> clazz) throws IOException {
        String clazzName = clazz.getName();
        clazzName = clazzName.replaceAll("\\.", "/") + ".class";
        InputStream is = ClassLoader.getSystemResourceAsStream(clazzName);

        byte[] bytes = new byte[4];
        is.read(bytes);
        is.read(bytes);

        int minorVersion = (((bytes[0] << 8) & 65280) + (bytes[1] & 255));
        int majorVersion = (((bytes[2] << 8) & 65280) + (bytes[3] & 255));

        Target target = Target.lookup(majorVersion, minorVersion);
        is.close();
        return target;
    }

}
