package com.mycompany.sse.handler;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import com.mycompany.sse.buffer.BufferWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler implementation that reads events from text/event-stream and adds it to the buffer
 */
public class BufferedEventHandler implements EventHandler {

    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedEventHandler.class);

    /**
     * Buffer wrapper to which events are added
     */
    private final BufferWrapper bufferWrapper;

    /**
     * Constructor
     *
     * @param bufferWrapper instance of buffer wrapper shared with event aggregator
     */
    public BufferedEventHandler(BufferWrapper bufferWrapper) {
        this.bufferWrapper = bufferWrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen() {
        LOGGER.info("onOpen called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClosed() {
        LOGGER.info("onClosed called");
    }

    /**
     * Gets invoked on message event. The event is added to the buffer
     *
     * @param event        event
     * @param messageEvent messageEvent
     */
    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        bufferWrapper.addDataToBuffer(messageEvent.getData());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onComment(String comment) {
        LOGGER.info("onComment called:{}", comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Throwable t) {
        LOGGER.error("onError called:{}", t.getLocalizedMessage());
    }

}
