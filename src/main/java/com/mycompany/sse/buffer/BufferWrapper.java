package com.mycompany.sse.buffer;

import com.mycompany.sse.bean.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper class for buffer. Handles synchronization on buffer, if necessary
 */
public class BufferWrapper {

    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferWrapper.class);

    /**
     * Data/Event buffer
     */
    private final BlockingQueue<Event> buffer;

    /**
     * Backpressure enabled. Used to decide whether to lock the buffer while calculating aggregates
     */
    private final boolean isBackpressureEnabled;

    /**
     * Constructor
     *
     * @param limit                 size of the buffer
     * @param isBackpressureEnabled Backpressure enabled. Used to decide whether to lock the buffer while calculating aggregates
     */
    public BufferWrapper(int limit, boolean isBackpressureEnabled) {
        this.buffer = new LinkedBlockingQueue<>(limit);
        this.isBackpressureEnabled = isBackpressureEnabled;
    }

    /**
     * Add json event to the buffer.
     *
     * @param messageEvent json representation of the data
     * @return whether event was added to the buffer of not
     */
    public boolean addDataToBuffer(String messageEvent) {
        Event data = Event.getEventFromJson(messageEvent);
        if (data != null) {
            boolean success = false;
            if (!isBackpressureEnabled) {
                success = addDataToBuffer(data);
            } else {
                synchronized (buffer) {
                    success = addDataToBuffer(data);
                }
            }
            return success;
        } else {
            return false;
        }
    }

    /**
     * Add event to buffer. If buffer is full, keep retrying
     *
     * @param data event data
     * @return whether event was added to the buffer of not
     */
    private boolean addDataToBuffer(Event data) {
        try {
            while (!buffer.offer(data, 10, TimeUnit.MILLISECONDS)) {
                LOGGER.warn("Waiting to the event to buffer");
            }
            return true;
        } catch (InterruptedException iex) {
            LOGGER.error("interrupted while waiting to add data to buffer. Could not add data to the buffer:{}", data);
            LOGGER.error(iex.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Removes elements from the buffer and adds them to a list
     *
     * @return list of events taken from buffer
     */
    public List<Event> pollCurrentBuffer() {
        List<Event> tempBuffer;

        if (isBackpressureEnabled) {
            synchronized (buffer) {
                tempBuffer = drainBuffer();
            }
        } else {
            tempBuffer = drainBuffer();
        }
        return tempBuffer;
    }


    /**
     * Drain the buffer and return list of events
     *
     * @return list of drained events
     */
    private List<Event> drainBuffer() {
        int size = buffer.size();
        List<Event> list = new ArrayList<>(size);
        buffer.drainTo(list, size);
        return list;
    }

    /**
     * Get size of the buffer
     *
     * @return size of the buffer
     */
    public int getSize() {
        return buffer.size();
    }

}
