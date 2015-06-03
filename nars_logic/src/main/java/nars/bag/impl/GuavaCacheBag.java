package nars.bag.impl;

import com.google.common.cache.*;
import nars.Memory;
import nars.nal.Itemized;

import java.util.Iterator;

/**
 * Index of stored Items (ex: concepts) which is optimized for
 * random access storage and retrieval and not prioritized active processing.
 * not really a bag, maybe it should be called ItemCache
 * 
 * http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/cache/package-summary.html*
 */
public class GuavaCacheBag<K, V extends Itemized<K>> extends CacheBag<K, V> implements RemovalListener<K, V>, Iterable<V> {

    public final Cache<K, V> data;

    public GuavaCacheBag(int capacity) {
        super();

        data = CacheBuilder.newBuilder()
            .maximumSize(capacity)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
               /*.weakKeys()
               .weakValues()
               .weigher(null)*/
        .removalListener(this)
        .build();
    }


    /** empty contents */
    @Override
    public void clear() {
        data.invalidateAll();
        data.cleanUp();
    }


    @Override
    public V get(K key) {
        return data.getIfPresent(key);
    }

    @Override
    public V take(K key) {
        V v = data.getIfPresent(key);
        if (v !=null) {
            data.invalidate(v);
            data.cleanUp();
            return v;
        }
        return null;
    }

    @Override
    public void put(V v) {
        data.put(v.name(), v);
    }

    @Override
    public long size() {
        return data.size();
    }

    @Override
    public void onRemoval(RemovalNotification<K, V> rn) {
        if (rn.getCause()==RemovalCause.SIZE) {
            V v = rn.getValue();
            if (v!=null) v.delete();
        }
    }

    @Override
    public Iterator<V> iterator() {
        return data.asMap().values().iterator();
    }
}