package ru.atom.thread.practice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author apomosov
 * @since 15.03.17
 */
public class EventQueue {
    private static BlockingQueue<Event> instance = new LinkedBlockingQueue<>();

    public static BlockingQueue<Event> getInstance() {
        return instance;
    }

    public static long getNumberOfEvents(Event.EventType type) {
        long matchingEvents = 0;
        for (Event event : instance) {
            if (event.getEventType() == type)
                matchingEvents++;
        }
        return matchingEvents;
    }
}
