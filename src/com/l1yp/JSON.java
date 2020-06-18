package com.l1yp;

import com.l1yp.exception.JSONException;
import com.l1yp.parser.JSONParserCodePoint;

/**
 * @Author Lyp
 * @Date 2020-06-16
 * @Email l1yp@qq.com
 */
public abstract class JSON {

    public abstract Object get(String path);

    public abstract int getIntValue(String path);

    public abstract long getLongValue(String path);

    public abstract double getDoubleValue(String path);

    public abstract float getFloatValue(String path);

    public abstract boolean getBooleanValue(String path);


    public abstract String getString(String path);

    public abstract JSONObject getJSONObject(String path);

    public abstract JSONArray getJSONArray(String path);


    protected int castToInt(Object object) {
        if (object instanceof Integer) {
            return (Integer) object;
        } else if (object instanceof String) {
            return Integer.decode((String) object);
        } else if (object instanceof Double) {
            return ((Double) object).intValue();
        } else if (object instanceof Float) {
            return ((Float) object).intValue();
        } else {
            throw new JSONException(String.format("cannot convert from %s to int", object.getClass()));
        }
    }

    protected long castToLong(Object object) {
        if (object instanceof Long) {
            return (Long) object;
        } else if (object instanceof String) {
            return Long.decode((String) object);
        } else if (object instanceof Double) {
            return ((Double) object).longValue();
        } else if (object instanceof Float) {
            return ((Float) object).longValue();
        } else {
            throw new JSONException(String.format("cannot convert from %s to long", object.getClass()));
        }
    }

    protected double castToDouble(Object object) {
        if (object instanceof Double) {
            return (Double) object;
        } else if (object instanceof String) {
            return Double.parseDouble((String) object);
        } else if (object instanceof Float) {
            return ((Float) object).doubleValue();
        } else {
            throw new JSONException(String.format("cannot convert from %s to double", object.getClass()));
        }
    }

    protected float castToFloat(Object object) {
        if (object instanceof Float) {
            return (Float) object;
        } else if (object instanceof String) {
            return Float.parseFloat((String) object);
        } else if (object instanceof Double) {
            return ((Double) object).floatValue();
        } else {
            throw new JSONException(String.format("cannot convert from %s to float", object.getClass()));
        }
    }

    protected boolean castToBoolean(Object object) {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof Number) {
            return ((Number) object).intValue() > 0;
        } else {
            throw new JSONException(String.format("cannot convert from %s to float", object.getClass()));
        }
    }

    public static JSONObject parseObject(int[] codePoints) {
        return new JSONParserCodePoint(codePoints).parseObject();
    }

    public static JSONObject parseObject(String json) {
        return new JSONParserCodePoint(json.codePoints().toArray()).parseObject();
    }

    public static JSONArray parseArray(int[] codePoints) {
        return new JSONParserCodePoint(codePoints).parseArray();
    }

    public static JSONArray parseArray(String json) {
        return new JSONParserCodePoint(json.codePoints().toArray()).parseArray();
    }

}
