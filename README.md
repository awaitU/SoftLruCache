# SoftLruCache
package android.support.v4.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {
    //核心数据结构
    //缓存 map 集合，为什么要用LinkedHashMap
    //因为没错取了缓存值之后，都要进行排序，以确保
    //下次移除的是最少使用的值
    private final LinkedHashMap<K, V> map;
    //当前缓存数据所占大小
    private int size;
    //缓存空间总容量（最大值）
    private int maxSize;
    //添加到缓存中的个数
    private int putCount;
    // 记录 create 的次数
    private int createCount;
    //被移除的个数
    private int evictionCount;
    //命中次数
    private int hitCount;
    //丢失次数
    private int missCount;
     /*
*
*唯一构造函数，传入最大的缓存，当传入缓存大小小于
*等于0的时候，抛出非法参数异常，其他情况下，获得
*当前最大容量的引用，map对象
     * 初始化LinkedHashMap
     * 第一个参数：initialCapacity，初始大小
     * 第二个参数：loadFactor，负载因子=0.75f
     * 第三个参数：accessOrder=true，基于访问顺序；accessOrder=false，基   	    于插入顺序
     */
    public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }

    /**官方解释：
     * 设置缓存的大小。
     * 重置最大缓存的值
     * @param maxSize The new maximum size.
     */
    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }

        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

  / * *官方解释：

    *返回{@code key}的值，如果它存在于缓存中或可以存在

   *由{@code #create}创建。如果返回一个值，则将其移动到

   *队列头部。如果一个值未被缓存且无法缓存，则返回null

   *被创建。通过 key 获取缓存值

   * /
    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            missCount++;
        }

     /*
     * 官方解释：
     * 尝试创建一个值，这可能需要很长时间，并且Map可能在create()返回的值时有所不同。如果在create()执行的时
     * 候，一个冲突的值被添加到Map，我们在Map中删除这个值，释放被创造的值。
     */

        V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }
    /*
     * 正常情况走不到这里
     * 走到这里的话 说明 实现了自定义的 create(K key) 逻辑
     * 因为默认的 create(K key) 逻辑为null
     */
        synchronized (this) {
            createCount++;
            mapValue = map.put(key, createdValue);

            if (mapValue != null) {
             /*
             * 有冲突
             * 所以 撤销 刚才的 操作
             * 将 之前相同key 的值 重新放回去
             */
                map.put(key, mapValue);
            } else {
             // 拿到键值对，计算出在容量中的相对长度，然后加上。缓存的大小改变
                size += safeSizeOf(key, createdValue);
            }
        }
          //这里没有移除，只是改变了位置
        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
           //判断缓存是否越界
            trimToSize(maxSize);
            return createdValue;
        }
    }

    //添加缓存，跟上面这个方法的 create 之后的代码一样的
    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            // 记录 put 的次数
            putCount++;
     // 通过键值对，计算出要保存对象value的大小，并更新当前缓存大小
            size += safeSizeOf(key, value);
        /*
         * 如果 之前存在key，用新的value覆盖原来的数据， 并返回 之前key 的value
         * 记录在 previous
         */
            previous = map.put(key, value);
         // 如果之前存在key，并且之前的value不为null
            if (previous != null) {
// 计算出 之前value的大小，因为前面size已经加上了新的value数据的大小，此时，需要再次更新size，减去原来value的大小
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }
//裁剪缓存容量（在当前缓存数据大小超过了总容量maxSize时，才会真正去执行LRU）
        trimToSize(maxSize);
        return previous;
    }

    //检测缓存是否越界
    public void trimToSize(int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }
     //如果没有，则返回
                if (size <= maxSize || map.isEmpty()) {
                    break;
                }
                //以下代码表示已经超出了最大范围
                Map.Entry<K, V> toEvict = map.entrySet().iterator().next();
                //移除最后一个，也就是最少使用的缓存
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= safeSizeOf(key, value);
                evictionCount++;
            }

            entryRemoved(true, key, value, null);
        }
    }

    //手动移除，用户调用
    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    //这里用户可以重写它，实现数据和内存回收操作
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {}

    protected V create(K key) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

   //这个方法要特别注意，跟我们实例化 LruCache 的 maxSize 要呼应，怎么
   //做到呼应呢，比如 maxSize 的大小为缓存的个数，这里就是 return 1就
   // ok，如果是内存的大小，如果5M，这个就不能是个数 了，这是应该是每个
  //缓存 value 的 size 大小，如果是 Bitmap，这应该是 bitmap.getByteCount();
    protected int sizeOf(K key, V value) {
        return 1;
    }

   //清空缓存
    public final void evictAll() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache.
     */
    public synchronized final int size() {
        return size;
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the maximum
     * number of entries in the cache. For all other caches, this returns the
     * maximum sum of the sizes of the entries in this cache.
     */
    public synchronized final int maxSize() {
        return maxSize;
    }

    /**
     * Returns the number of times {@link #get} returned a value that was
     * already present in the cache.
     */
    public synchronized final int hitCount() {
        return hitCount;
    }

    /**
     * Returns the number of times {@link #get} returned null or required a new
     * value to be created.
     */
    public synchronized final int missCount() {
        return missCount;
    }

    /**
     * Returns the number of times {@link #create(Object)} returned a value.
     */
    public synchronized final int createCount() {
        return createCount;
    }

    /**
     * Returns the number of times {@link #put} was called.
     */
    public synchronized final int putCount() {
        return putCount;
    }

    /**
     * Returns the number of values that have been evicted.
     */
    public synchronized final int evictionCount() {
        return evictionCount;
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(map);
    }

    @Override public synchronized final String toString() {
        int accesses = hitCount + missCount;
        int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
        return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
                maxSize, hitCount, missCount, hitPercent);
    }
}
