package com.test.oes.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.test.oes.entity.Message;
import com.test.oes.entity.Replay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MessageCacheFacade {

    private static final String CACHE_MESSAGE = "message";

    private final CacheProperties cacheProperties;
    private final LocalCacheRegistry localCacheRegistry;
    private final JsonRedisStore jsonRedisStore;
    private final CacheMetrics cacheMetrics;
    private final AtomicLong localFeedVersion = new AtomicLong(1L);

    public Page<Message> getMessagePage(Integer page, Integer size, Supplier<Page<Message>> loader) {
        String key = CacheKeys.messagePage(currentFeedVersion(), page, size);
        Page<Message> localValue = localCacheRegistry.messagePage().getIfPresent(key);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "miss");
        Page<Message> redisValue = jsonRedisStore.get(CACHE_MESSAGE, key, new TypeReference<Page<Message>>() {
        });
        if (redisValue != null) {
            localCacheRegistry.messagePage().put(key, redisValue);
            return redisValue;
        }
        Page<Message> loaded = cacheMetrics.recordLoad(CACHE_MESSAGE, "db", loader::get);
        localCacheRegistry.messagePage().put(key, loaded);
        jsonRedisStore.set(CACHE_MESSAGE, key, loaded, cacheProperties.getMessage().getRedisTtl());
        return loaded;
    }

    public Message getMessageDetail(Integer messageId, Supplier<Message> loader) {
        Message localValue = localCacheRegistry.messageDetail().getIfPresent(messageId);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "miss");
        Message redisValue = jsonRedisStore.get(CACHE_MESSAGE, CacheKeys.messageDetail(messageId), Message.class);
        if (redisValue != null) {
            localCacheRegistry.messageDetail().put(messageId, redisValue);
            return redisValue;
        }
        Message loaded = cacheMetrics.recordLoad(CACHE_MESSAGE, "db", loader::get);
        if (loaded != null) {
            localCacheRegistry.messageDetail().put(messageId, loaded);
            jsonRedisStore.set(CACHE_MESSAGE, CacheKeys.messageDetail(messageId), loaded, cacheProperties.getMessage().getRedisTtl());
        }
        return loaded;
    }

    public List<Replay> getMessageReplies(Integer messageId, Supplier<List<Replay>> loader) {
        List<Replay> localValue = localCacheRegistry.messageReplies().getIfPresent(messageId);
        if (localValue != null) {
            cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "hit");
            return localValue;
        }
        cacheMetrics.recordRequest(CACHE_MESSAGE, "local", "miss");
        List<Replay> redisValue = jsonRedisStore.get(CACHE_MESSAGE, CacheKeys.messageReplies(messageId), new TypeReference<List<Replay>>() {
        });
        if (redisValue != null) {
            localCacheRegistry.messageReplies().put(messageId, redisValue);
            return redisValue;
        }
        List<Replay> loaded = cacheMetrics.recordLoad(CACHE_MESSAGE, "db", loader::get);
        localCacheRegistry.messageReplies().put(messageId, loaded);
        jsonRedisStore.set(CACHE_MESSAGE, CacheKeys.messageReplies(messageId), loaded, cacheProperties.getMessage().getRedisTtl());
        return loaded;
    }

    public void bumpMessageFeedVersion() {
        localFeedVersion.incrementAndGet();
        jsonRedisStore.increment(CACHE_MESSAGE, CacheKeys.messageFeedVersion());
        localCacheRegistry.messagePage().invalidateAll();
    }

    public void evictMessageDetail(Integer messageId) {
        localCacheRegistry.messageDetail().invalidate(messageId);
        jsonRedisStore.delete(CACHE_MESSAGE, CacheKeys.messageDetail(messageId));
    }

    public void evictMessageReplies(Integer messageId) {
        localCacheRegistry.messageReplies().invalidate(messageId);
        jsonRedisStore.delete(CACHE_MESSAGE, CacheKeys.messageReplies(messageId));
    }

    private String currentFeedVersion() {
        String version = jsonRedisStore.getRaw(CACHE_MESSAGE, CacheKeys.messageFeedVersion());
        if (version != null && !version.isBlank()) {
            return version;
        }
        return String.valueOf(localFeedVersion.get());
    }
}
