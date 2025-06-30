package com.whc.picture.manager.pagecache;

import cn.hutool.core.util.RandomUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCache extends PagePictureCacheTemplate {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public String getCacheValueByKey(String catchKey) {
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        // 从 Redis 缓存中查询
        return valueOps.get(catchKey);
    }

    @Override
    public void setCache(String catchKey, String cacheValue) {
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        // 设置缓存过期时间 5-10 分钟，防止缓存雪崩
        /*
            https://www.mianshiya.com/question/1780933295672946690?screen=full
            为什么设置随机时间会防止缓存雪崩？
            答：缓存雪崩本质上是由于大量缓存同时过期而导致同一时间内多个请求去数据库查询，导致数据库压力过大而崩溃的一个现象，
                本质上是因为key过期而导致的，那么我们就可以将key的过期时间打散，这样即使key过期也不会导致过多的请求打到数据库
            缓存击穿：某些热点数据（假设qps 在 1000万）在缓存过期后，大量请求直接打到数据库，导致数据库崩溃
                解决方法：
                1. 设置热点数据的超长过期时间
                2. 使用互斥锁，如 Redisson 或 本地synchronized 锁,来控制一个一个请求进行排队，第一个请求先去查询数据库，并将结果添加到缓存
                    后续请求就能去缓存获取。本质上就是控制缓存刷新
            缓存穿透：用户频繁请求不存在的 key，导致大量请求直接触发数据库查询
                解决方案:对无效查询结果也进行缓存（如设置空值缓存)，或者使用布隆过滤器。

            什么是布隆过滤器？如何在redis中实现
                https://www.mianshiya.com/question/1780933295689723905

性能

         */
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        valueOps.set(catchKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
    }

}
