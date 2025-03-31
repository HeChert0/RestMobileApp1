package app.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LruCache<K, V> {
    private final Logger logger = LoggerFactory.getLogger(LruCache.class);
    private final Lock lock = new ReentrantLock();
    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private Node<K, V> head;
    private Node<K, V> tail;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
        this.capacity = capacity;
        this.cache = new HashMap<>();
        logger.info("Initialized LruCache with capacity {}", capacity);
    }

    public V get(K key) {
        lock.lock();
        try {
            size();

            Node<K, V> node = cache.get(key);
            if (node == null) {
                logger.debug("Cache miss for key {}", key);
                return null;
            }
            moveToHead(node);
            logger.debug("Cache hit for key {}. Returning value: {}", key, node.value);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            size();

            Node<K, V> node = cache.get(key);
            if (node != null) {
                node.value = value;
                moveToHead(node);
                logger.debug("Updated existing key {} with new value {}", key, value);
            } else {
                node = new Node<>(key, value);
                cache.put(key, node);
                addToHead(node);
                logger.debug("Inserted key {} with value {}", key, value);
                if (cache.size() > capacity) {
                    logger.debug("Cache capacity exceeded. Removing tail element.");
                    removeTail();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public V remove(K key) {
        lock.lock();
        try {
            Node<K, V> node = cache.remove(key);
            if (node == null) {
                logger.debug("Attempted to remove key {} but it was not found", key);
                return null;
            }
            removeNode(node);
            logger.debug("Removed key {} with value {}", key, node.value);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            int size = cache.size();
            logger.debug("Current cache size: {}", size);
            return size;
        } finally {
            lock.unlock();
        }
    }

    private void moveToHead(Node<K, V> node) {
        if (node == head) {
            return;
        }
        removeNode(node);
        addToHead(node);
        logger.debug("Moved key {} to head", node.key);
    }

    private void addToHead(Node<K, V> node) {
        node.prev = null;
        node.next = head;
        if (head != null) {
            head.prev = node;
        }
        head = node;
        if (tail == null) {
            tail = head;
        }
        logger.debug("Added key {} to head", node.key);
    }

    private void removeNode(Node<K, V> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        logger.debug("Removed node with key {}", node.key);
    }

    private void removeTail() {
        if (tail == null) {
            return;
        }
        logger.debug("Removing tail key {} from cache", tail.key);
        cache.remove(tail.key);
        removeNode(tail);
    }
}
