package com.mycompany.sse;

import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.ReadyState;
import com.mycompany.sse.aggregate.EventAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Cleanup extends Thread {

    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Cleanup.class);

    private final ExecutorService executorService;
    private final EventAggregator eventAggregator;
    private final EventSource eventSource;

    /**
     * Constructor
     *
     * @param executorService ExecutorService
     * @param eventAggregator ScheduledEventAggregator
     * @param eventSource     EventSource
     */
    public Cleanup(ExecutorService executorService, EventAggregator eventAggregator, EventSource eventSource) {
        this.eventAggregator = eventAggregator;
        this.executorService = executorService;
        this.eventSource = eventSource;
        this.setName("Shutdown-Cleanup");
    }

    /**
     * Invoked on shutdown. stops event source, executor service and
     * processes the events in the buffer
     */
    @Override
    public void run() {
        LOGGER.info("ShutdownHook called");

        // Close the eventsource if it's not closed already
        if (!eventSource.getState().equals(ReadyState.CLOSED)) {
            eventSource.close();
        }

        // shutdown executor service
        try {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getLocalizedMessage());
        } finally {
            //aggregate remaining event in buffer
            LOGGER.info("Processing buffer");
            eventAggregator.aggregateAndPrint();
            // if the buffer was full, it just got freed. There might be an outstanding record. Process it.
            eventAggregator.aggregateAndPrint();
        }
    }
}
