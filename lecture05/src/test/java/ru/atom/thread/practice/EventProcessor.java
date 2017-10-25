package ru.atom.thread.practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @author apomosov
 * @since 15.03.17
 */
public class EventProcessor {

    static Thread currentThread;

    public static void produceEvents(List<EventProducer> eventProducers) {
        for (EventProducer producer : eventProducers) {
            currentThread = new Thread(producer);
            currentThread.start();
        }
    }

    public static long countTotalNumberOfGoodEvents() {
        if (currentThread != null) try {
            currentThread.join();
        } catch (java.lang.InterruptedException e) {}
        return EventQueue.getNumberOfEvents(Event.EventType.GOOD);
    }

    public static long countTotalNumberOfBadEvents() {
        if (currentThread != null) try {
            currentThread.join();
        } catch (java.lang.InterruptedException e) {}
        return EventQueue.getNumberOfEvents(Event.EventType.BAD);
    }
}
