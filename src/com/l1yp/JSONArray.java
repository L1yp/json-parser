package com.l1yp;

import com.l1yp.exception.JSONException;
import com.l1yp.parser.PathParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @Author Lyp
 * @Date   2020-06-15
 * @Email  l1yp@qq.com
 */
public class JSONArray extends JSON implements List<Object> {

    private List<Object> list;

    public JSONArray(){
        this(16);
    }

    public JSONArray(int initialCapacity){
        list = new ArrayList<>(initialCapacity);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object o) {
        return list.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public Object get(int index) {
        return list.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        list.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);

    }

    @Override
    public ListIterator<Object> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Object get(String path) {
        return new PathParser(this).parse(path);
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
        if (ret == null){
            return false;
        }
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
