package com.l1yp.parser;

import com.l1yp.JSONArray;
import com.l1yp.JSONObject;
import com.l1yp.exception.JSONException;

/**
 * @Author Lyp
 * @Date 2020-06-16
 * @Email l1yp@qq.com
 */
public class PathParser {

    private Object object;

    public PathParser(Object obj){
        this.object = obj;
    }

    public Object parse(String k){
        int[] points = k.codePoints().toArray();
        int ch;
        Object top = object;
        String ck = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            ch = points[i];
            switch (ch){
                case '\\':{
                    int nextCh = points[i + 1];
                    if (nextCh == '.' || nextCh == '[' || nextCh == ']'){
                        sb.appendCodePoint(nextCh);
                        i++;
                    } else {
                        throw new JSONException("invalid json path: " + k);
                    }
                    break;
                }
                case '.':
                case '[': {
                    if (sb.length() > 0){
                        ck = sb.toString();
                        top = ((JSONObject) top).read(ck);
                    }
                    sb.setLength(0);
                    break;
                }
                case ']':{
                    if (sb.length() == 0){
                        throw new JSONException("invalid json path: " + k);
                    }
                    ck = sb.toString();
                    int index = Integer.parseInt(ck);
                    top = ((JSONArray) top).get(index);
                    sb.setLength(0);
                    break;
                }
                default:{
                    sb.appendCodePoint(ch);
                }
            }
            if (top == null){
                throw new JSONException(new String(points, 0, i) + " does not exist");
            }
        }
        if (sb.length() > 0){
            ck = sb.toString();
            top = ((JSONObject) top).read(ck);
        }
        return top;
    }

}
