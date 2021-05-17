/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.appserv.util.cache;

import java.text.MessageFormat;

import java.util.Properties;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * MultiLruCache -- in-memory bounded LRU cache with multiple LRU lists
 * Underlying Hashtable is made into logical segments, with each segment
 * having its own LRU list.
 */
public class MultiLruCache extends BaseCache {

    /* an array of LRU lists; each element in this array is actually
     * LruCacheItem[2] with LRU list (lru[0] is head and lru[1] the tail
     */
    public static final int LRU_HEAD = 0;
    public static final int LRU_TAIL = 1;
    public static final int DEFAULT_HASHTABLE_SEGMENT_SIZE = 4096;

    int segmentSize;
    LruCacheItem[][] lists;
    protected int[] listsLength;

    int trimCount;
    int trimIndex;
    Object trimIndexLk = new Object();

    /**
     * initialize the LRU cache
     * @param maxCapacity maximum number of entries this cache may hold
     */
    public void init(int maxCapacity, Properties props) throws Exception {
        super.init(maxCapacity, props);

        segmentSize = DEFAULT_HASHTABLE_SEGMENT_SIZE;

        if (props != null) {
            String prop = props.getProperty("MultiLRUSegmentSize");
            if (prop != null) {
                try {
                    segmentSize = Integer.parseInt(prop);
                } catch (NumberFormatException nfe) {}
            }
        }

        // create the array of LRU lists
        int segments = ((maxBuckets / segmentSize) +
                        (((maxBuckets % segmentSize) != 0) ? 1 : 0));
           lists = new LruCacheItem[segments][2];
        listsLength = new int[lists.length];
        for (int i = 0; i < lists.length; i++) {
            lists[i][LRU_HEAD] = null;
            lists[i][LRU_TAIL] = null;

            listsLength[i] = 0;
        }
    }

    /**
     * create new item
     * @param hashCode for the entry
     * @param key <code>Object</code> key
     * @param value <code>Object</code> value
     * @param size size in bytes of the item
     *
     * subclasses may override to provide their own CacheItem extensions
     * e.g. one that permits persistence.
     */
    protected CacheItem createItem(int hashCode, Object key,
                                        Object value, int size) {
        return new LruCacheItem(hashCode, key, value, size);
    }

    /**
     * remove an lru item from one of the LRU lists
     * @param the LRU segment index to trim
     * @return the item that was successfully trimmed
     */
    protected CacheItem trimLru(int segment) {
        LruCacheItem[] list = lists[segment];
        LruCacheItem l = null;

        l = list[LRU_TAIL];

        list[LRU_TAIL] = l.lPrev;
        list[LRU_TAIL].lNext = null;

        l.lPrev = null;
        listsLength[segment]--;

        l.isTrimmed = true;

        trimCount++;

        return l;
    }

    /**
     * this item is just added to the cache
     * @param item <code>CacheItem</code> that was created
     * @return a overflow item; may be null
     *
     * Cache bucket is already synchronized by the caller
     */
    protected CacheItem itemAdded(CacheItem item) {
        CacheItem overflow = null;
        if(! (item instanceof LruCacheItem))
            return null;

        LruCacheItem lc = (LruCacheItem) item;

        int index = getIndex(item.hashCode());
        int segment = (index/segmentSize);
        LruCacheItem[] list = lists[segment];

        // update the LRU
        synchronized (list) {
            if (list[LRU_HEAD] != null) {
                list[LRU_HEAD].lPrev = lc;
                lc.lNext = list[LRU_HEAD];
            }
            else
                list[LRU_TAIL] = lc;
            list[LRU_HEAD] = lc;

            listsLength[segment]++;

            if (isThresholdReached()) {
                overflow = trimLru(trimIndex);
                // go round robin for the next trim
                incrementTrimIndex();
            }
        }

        return overflow;
    }

    /**
     * this item is accessed
     * @param item <code>CacheItem</code> accessed
     *
     * Cache bucket is already synchronized by the caller
     */
    protected void itemAccessed(CacheItem item) {
        int index = getIndex(item.hashCode());
        int segment = (index/segmentSize);
        LruCacheItem[] list = lists[segment];

        if(! (item instanceof LruCacheItem))
            return;
        LruCacheItem lc = (LruCacheItem) item;

        // update the LRU list
        synchronized (list) {
            LruCacheItem prev = lc.lPrev;
            LruCacheItem next = lc.lNext;

            if (prev != null) {
                // put the item at the head of LRU list
                lc.lPrev = null;
                lc.lNext = list[LRU_HEAD];
                list[LRU_HEAD].lPrev = lc;
                list[LRU_HEAD] = lc;

                // patch up the previous neighbors
                prev.lNext = next;
                if (next != null)
                    next.lPrev = prev;
                else
                    list[LRU_TAIL] = prev;

           }
        }
    }

    /**
     * item value has been refreshed
     * @param item <code>CacheItem</code> that was refreshed
     * @param oldSize size of the previous value that was refreshed
     * Cache bucket is already synchronized by the caller
     */
    protected void itemRefreshed(CacheItem item, int oldSize) {
        itemAccessed(item);
    }

    /**
     * item value has been removed from the cache
     * @param item <code>CacheItem</code> that was just removed
     *
     * Cache bucket is already synchronized by the caller
     */
    protected void itemRemoved(CacheItem item) {
        if(! (item instanceof LruCacheItem))
            return;
        LruCacheItem l = (LruCacheItem) item;

        int index = getIndex(item.hashCode());
        int segment = (index/segmentSize);
        LruCacheItem[] list = lists[segment];

        // remove the item from the LRU list
        synchronized (list) {
            // if the item is already trimmed from the LRU list, nothing to do.
            if (l.isTrimmed)
                return;

            LruCacheItem prev = l.lPrev;
            LruCacheItem next = l.lNext;

            // patch up the neighbors and make sure head/tail are correct
            if (prev != null)
                prev.lNext = next;
            else
                list[LRU_HEAD] = next;

            if (next != null)
                next.lPrev = prev;
            else
                list[LRU_TAIL] = prev;

            listsLength[segment]--;
        }
    }

    /**
     * cache has reached threshold so trim its size. subclasses are expected
     * to provide a robust cache replacement algorithm.
     */
    protected void handleOverflow() {
    }

    int getListsLength() {
        return lists.length;
    }

    protected void incrementTrimIndex() {
        synchronized (trimIndexLk) {
            trimIndex = (trimIndex + 1) % lists.length;
        }
    }

    /**
     * get generic stats from subclasses
     */

    /**
     * get the desired statistic counter
     * @param key to corresponding stat
     * @return an Object corresponding to the stat
     * See also: Constant.java for the key
     */
    public Object getStatByName(String key) {
        Object stat = super.getStatByName(key);

        if (stat == null && key != null) {
            if (key.equals(Constants.STAT_MULTILRUCACHE_SEGMENT_SIZE))
                stat = Integer.valueOf(segmentSize);
            else if (key.equals(Constants.STAT_MULTILRUCACHE_TRIM_COUNT))
                stat = Integer.valueOf(trimCount);
            else if (key.equals(Constants.STAT_MULTILRUCACHE_SEGMENT_LIST_LENGTH)) {
                stat = new Integer[lists.length];

                for (int i = 0; i < lists.length; i++) {
                    ((Integer[])stat)[i] = Integer.valueOf(listsLength[i]);
                }
            }
        }

        return stat;
    }

    /**
     * get the stats snapshot
     * @return a Map of stats
     * See also: Constant.java for the keys
     */
    public Map getStats() {
        Map stats = super.getStats();

        stats.put(Constants.STAT_MULTILRUCACHE_SEGMENT_SIZE,
                  Integer.valueOf(segmentSize));
        for (int i = 0; i < lists.length; i++) {
            stats.put(Constants.STAT_MULTILRUCACHE_SEGMENT_LIST_LENGTH + "["
                      + i + "]:",
                      Integer.valueOf(listsLength[i]));
        }
        stats.put(Constants.STAT_MULTILRUCACHE_TRIM_COUNT,
                  Integer.valueOf(trimCount));
        return stats;
    }

    /** default CacheItem class implementation  ***/
    static class LruCacheItem extends BaseCache.CacheItem {

        // double linked LRU list
        LruCacheItem lNext;
        LruCacheItem lPrev;
        boolean isTrimmed;

        LruCacheItem(int hashCode, Object key, Object value, int size) {
            super(hashCode, key, value, size);
        }
    }
}
