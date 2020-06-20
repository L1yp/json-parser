package com.l1yp;


import com.l1yp.io.UTF8Reader;
import com.l1yp.parser.JSONParser;

import java.io.InputStream;

/**
 * @Author Lyp
 * @Date 2020/6/14
 * @Email l1yp@qq.com
 */
public class Main {
    public static void main(String[] args) throws Exception {

        InputStream is = ClassLoader.getSystemResourceAsStream("7.json");
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        is.close();

        UTF8Reader reader = new UTF8Reader(bytes);
        // System.out.println(Arrays.toString(new String(bytes).getBytes("GBK")));
        // GBKReader reader = new GBKReader(new String(bytes).getBytes("GBK"));
        JSONParser parser = new JSONParser(reader);
        System.out.println(parser.parseObject());

    }

}
