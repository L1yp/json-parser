package com.l1yp.io;

/**
 * @Author Lyp
 * @Date 2020-06-17
 * @Email l1yp@qq.com
 */
public abstract class AbstractReader implements Reader {

    protected byte[] buffer;
    protected int offset;
    protected int mark;
    protected int limit;

    protected AbstractReader(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }

    protected AbstractReader(byte[] buffer, int offset, int size){
        this.buffer = buffer;
        this.offset = 0;
        this.limit = offset + size;
        this.mark = -1;
    }


    @Override
    public void mark() {
        mark = offset;
    }

    @Override
    public void reset() {
        offset = mark;
    }

    @Override
    public int remaining() {
        return limit - offset;
    }

    public void skip(int count) {
        int pos = offset;
        offset += count;
        if (offset >= limit){
            throw new IndexOutOfBoundsException(String.format("limit: %d, offset: %d, skip: %d", limit, pos, count));
        }
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public void offset(int offset) {
        this.offset = offset;
    }

    @Override
    public int peek() {
        mark();
        int codePoint = read();
        reset();
        return codePoint;
    }

    @Override
    public int[] peek(int count) {
        mark();
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = read();
        }
        reset();
        return result;
    }

    @Override
    public int peekCodePointSize() {
        int pos = offset;
        read();
        int result = offset - pos;
        offset = pos;
        return result;
    }

    @Override
    public String toSubString(int start, int end) {
        int mark = offset;
        offset(start);
        StringBuilder sb = new StringBuilder();
        while (offset < end){
            int cp = read();
            sb.appendCodePoint(cp);
        }
        offset(mark);
        return sb.toString();
    }

}
