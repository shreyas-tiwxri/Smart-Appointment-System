package service;

import model.Appointment;

import java.util.*;

/**
 * Prioritizes patient appointments using Java PriorityQueue and Comparator.
 * Emergency and urgent cases jump ahead in the queue.
 */
public class TriageQueueService {

    /**
     * Comparator ordering appointments by triage rank first, then scheduled timestamp.
     */
    private static final Comparator<Appointment> TRIAGE_COMPARATOR = (a1, a2) -> {
        int rankDiff = Integer.compare(a1.getPriority().getRank(), a2.getPriority().getRank());
        if (rankDiff != 0) {
            return rankDiff;
        }
        return a1.getDateTime().compareTo(a2.getDateTime());
    };

    private final PriorityQueue<Appointment> queue;

    public TriageQueueService() {
        this.queue = new PriorityQueue<>(TRIAGE_COMPARATOR);
    }

    public synchronized void enqueue(Appointment appointment) {
        if (appointment != null) {
            queue.offer(appointment);
        }
    }

    public synchronized void enqueueAll(Collection<Appointment> appointments) {
        if (appointments != null) {
            for (Appointment a : appointments) {
                enqueue(a);
            }
        }
    }

    public synchronized Appointment dequeue() {
        return queue.poll();
    }

    public synchronized Appointment peek() {
        return queue.peek();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns an ordered snapshot of prioritized appointments without modifying the queue.
     */
    public synchronized List<Appointment> getOrderedQueue() {
        List<Appointment> list = new ArrayList<>(queue);
        list.sort(TRIAGE_COMPARATOR);
        return Collections.unmodifiableList(list);
    }
}
