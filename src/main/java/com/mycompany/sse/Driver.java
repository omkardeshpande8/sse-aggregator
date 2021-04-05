package com.mycompany.sse;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.mycompany.sse.buffer.BufferWrapper;
import com.mycompany.sse.handler.BufferedEventHandler;
import com.mycompany.sse.aggregate.EventAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Driver class that starts the event listener
 */
public class Driver {

    /**
     * Driver for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);
    /**
     * Key to read property Interval in seconds from configs
     */
    public static final String INTERVAL_SECONDS = "interval.seconds";
    /**
     * Address to read server-sent events from
     */
    public static final String SERVICE_ADDRESS = "https://tweet-service.herokuapp.com/sps";
    /**
     * Key to read property is backpressure enabled from config
     */
    private static final String BACKPRESSURE_ENABLED = "backpressure.enabled";

    /**
     * Key to read property max size from config
     */
    private static final String MAX_SIZE = "max.size";

    /**
     * Read properties from config.properties file
     *
     * @return Properties
     */
    private static Properties readProperties() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.error("config.properties file not found. Using default values");
        }
        return properties;
    }

    /**
     * Entrypoint for the project
     *
     * @param args command line args
     */
    public static void main(String[] args) throws InterruptedException {

        Properties properties = readProperties();
        int interval = Integer.parseInt(properties.getProperty(INTERVAL_SECONDS, Integer.toString(1)));
        boolean backPressure = Boolean.parseBoolean(properties.getProperty(BACKPRESSURE_ENABLED, Boolean.toString(Boolean.FALSE)));
        int maxSize = Integer.parseInt(properties.getProperty(MAX_SIZE, Integer.toString(Integer.MAX_VALUE)));

        BufferWrapper bufferWrapper = new BufferWrapper(maxSize, backPressure);
        EventAggregator eventAggregator = new EventAggregator(bufferWrapper);
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(1);

        //Create event source
        EventHandler eventHandler = new BufferedEventHandler(bufferWrapper);
        URI uri = URI.create(SERVICE_ADDRESS);
        EventSource.Builder builder = new EventSource.Builder(eventHandler, uri)
                .reconnectTime(Duration.ofMillis(3000));

        //Start event source
        EventSource eventSource = builder.build();
        Thread sourceThread = new Thread(eventSource::start);
        sourceThread.setName("Event-Source-Thread");
        sourceThread.start();

        // Schedule consumer threads at fixed interval
        scheduledExecutorService.scheduleAtFixedRate(eventAggregator::aggregateAndPrint, interval, interval, TimeUnit.SECONDS);

        // Add shutdown hook to clean up source and consumer
        Runtime.getRuntime().addShutdownHook(new Cleanup(scheduledExecutorService, eventAggregator, eventSource));

        // wait for source thread to complete
        sourceThread.join();
    }
}

