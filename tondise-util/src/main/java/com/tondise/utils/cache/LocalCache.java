package com.tondise.utils.cache;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public enum LocalCache {
    Instance;

    // ConcurrentHashMap : thread-safe pour les lectures/écritures concurrentes
    private Map<String, CacheArgument> map = new ConcurrentHashMap<>();

    public boolean set(String key, CacheArgument value) {
        map.put(key, value);
        return true;
    }

    public <T> Optional<T> get(@NonNull String key) {
        return Optional.ofNullable(map.get(key))
                .filter(cacheArgument1 -> LocalDateTime.now().isBefore(cacheArgument1.expireAt()))
                .map(CacheArgument::value)
                .map(o -> (T) o);
    }

    /**
     * synchronized : garantit qu'un seul thread exécute le check + fetch + set à la fois.
     * Le double-check interne évite que les threads en attente relancent un fetch inutile.
     */
    public synchronized <T> Optional<T> getOrSetIfAbsent(@NonNull String key, @NonNull Supplier<CacheArgument> supplier) {
        // Double-check : un thread concurrent a peut-être déjà rafraîchi pendant l'attente du lock
        Optional<Object> getResult = get(key);
        if (getResult.isPresent()) {
            return (Optional<T>) getResult;
        }
        CacheArgument cacheArgument = supplier.get();
        set(key, cacheArgument);
        return (Optional<T>) Optional.of(cacheArgument.value());
    }

    public void invalidate(@NonNull String key) {
        map.remove(key);
    }

    public void init() {
        this.map = new ConcurrentHashMap<>();
    }
}
