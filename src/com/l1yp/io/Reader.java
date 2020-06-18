package com.l1yp.io;

/**
 * @Author Lyp
 * @Date 2020-06-16
 * @Email l1yp@qq.com
 */
public interface Reader {

    /**
     * 读取下一个unicode码点
     * @return codePoint
     */
    int read();

    /**
     * 读取下一个unicode码点, 但是偏移不变
     */
    int peek();

    /**
     * 读取N个unicode码点, 但是偏移不变
     */
    int[] peek(int count);


    /**
     * 读取下一个码点的字节数
     */
    int peekCodePointSize();

    /**
     * 标记当前位置
     */
    void mark();

    /**
     * 重置offset 到mark() 方法标记的位置
     */
    void reset();

    /**
     * 剩余字节数
     */
    int remaining();

    void skip(int count);

    int limit();

    int offset();

    void offset(int offset);

    String toSubString(int start, int end);
}
