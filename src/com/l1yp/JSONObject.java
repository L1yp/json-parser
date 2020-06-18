package com.l1yp;

import com.l1yp.exception.JSONException;
import com.l1yp.parser.PathParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author Lyp
 * @Date 2020-06-15
 * @Email l1yp@qq.com
 */
public class JSONObject extends JSON implements Map<String, Object> {

    private Map<String, Object> map;

    public JSONObject(){
        this(16);
    }

    public JSONObject(boolean ordered){
        this(16, ordered);
    }

    public JSONObject(int initialCapacity){
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        if (ordered){
            map = new LinkedHashMap<>(initialCapacity);
        }else {
            map = new HashMap<>(initialCapacity);
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * 根据键直接取值, 无需解析路径表达式
     * @param k 键
     * @return 值
     */
    public Object read(String k){
        return this.map.get(k);
    }

    @Override
    public Object get(String path) {
        return new PathParser(this).parse(path);
    }

    @Override
    public Object get(Object path) {
        String k = null;
        if (path instanceof String) {
            k = (String) path;
        } else {
            k = String.valueOf(path);
        }

        return new PathParser(this).parse(k);
        // return map.get(path);
    }



    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }




    @Override
    public int getIntValue(String path) {
        Object ret = get(path);
        if (ret == null){
            return 0;
        }
        return castToInt(ret);
    }

    @Override
    public long getLongValue(String path) {
        Object ret = get(path);
        if (ret == null){
            return 0L;
        }
        return castToLong(ret);
    }

    @Override
    public double getDoubleValue(String path) {
        Object ret = get(path);
        if (ret == null){
            return 0.0d;
        }
        return castToDouble(ret);
    }

    @Override
    public float getFloatValue(String path) {
        Object ret = get(path);
        if (ret == null){
            return 0.0f;
        }
        return castToFloat(ret);
    }

    @Override
    public boolean getBooleanValue(String path) {
        Object ret = get(path);
        return castToBoolean(ret);
    }

    @Override
    public String getString(String path) {
        Object ret = get(path);
        if (ret == null){
            return null;
        }
        return ret instanceof String ? (String) ret : String.valueOf(ret);
    }

    @Override
    public JSONObject getJSONObject(String path) {
        Object ret = get(path);
        if (ret == null){
            return null;
        }
        if (ret instanceof JSONObject){
            return (JSONObject) ret;
        }
        throw new JSONException(String.format("cannot convert from %s to JSONObject", ret.getClass()));
    }

    @Override
    public JSONArray getJSONArray(String path) {
        Object ret = get(path);
        if (ret == null){
            return null;
        }
        if (ret instanceof JSONArray){
            return (JSONArray) ret;
        }
        throw new JSONException(String.format("cannot convert from %s to JSONArray", ret.getClass()));
    }


}
