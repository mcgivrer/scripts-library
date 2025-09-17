package {{BASE_PACKAGE}}.utils;

import java.util.LinkedList;

/**
 * A simple circular queue implementation using LinkedList.
 */
public class CircularQueue<E> extends LinkedList<E> {
    private final int capacity;

    /**
     * Creates a new CircularQueue with the specified capacity.
     */
    public CircularQueue(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        if (size() >= capacity)
            removeFirst();
        return super.add(e);
    }
}
