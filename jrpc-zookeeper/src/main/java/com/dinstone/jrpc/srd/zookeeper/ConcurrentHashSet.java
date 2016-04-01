
package com.dinstone.jrpc.srd.zookeeper;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> extends AbstractSet<E> implements Serializable {

    private static final long serialVersionUID = -8347878570391674042L;

    protected final Map<E, Boolean> map = new ConcurrentHashMap<E, Boolean>();

    public ConcurrentHashSet() {
    }

    public ConcurrentHashSet(Collection<E> c) {
        addAll(c);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(E o) {
        return map.put(o, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
