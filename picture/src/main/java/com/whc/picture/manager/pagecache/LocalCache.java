package com.whc.picture.manager.pagecache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LocalCache extends PagePictureCacheTemplate {

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder()
                    .initialCapacity(1024)  // 初始容量，可以让系统一启动就分配一些内存空间，提高缓存启动效率
                    .maximumSize(10000L) // 最大存储数量
                    // 写缓存之后 5 分钟移除，中间有访问的话不会被重置
                    // 如果场景是每次访问都需要将缓存失效时间重置为 从0 开始的话 要使用 expireAfterAccess
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    /*
                        这里指的是 当项目启动时和 缓存失效后调用 createExpensiveGraph() 方法给缓存进行刷新
                        .build(key -> createExpensiveGraph(key));
                     */
                    .build(); // 这里我们是随用随写，不用定期去更新


    @Override
    public String getCacheValueByKey(String catchKey) {
        // 从本地缓存
        return LOCAL_CACHE.getIfPresent(catchKey);
    }

    @Override
    public void setCache(String catchKey, String cacheValue) {
        // 写入本地缓存
        LOCAL_CACHE.put(catchKey, cacheValue);
    }

}
