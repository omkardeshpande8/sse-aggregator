package com.mycompany.sse.aggregate;

import com.mycompany.sse.bean.Event;
import com.mycompany.sse.bean.GroupingKey;
import com.mycompany.sse.bean.OutputRecord;
import com.mycompany.sse.buffer.BufferWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The aggregator class that consumes events from the buffer and aggregates them.
 */
public class EventAggregator {

    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventAggregator.class);

    /**
     * Buffer wrapper from which the events are consumed
     */
    private final BufferWrapper bufferWrapper;


    /**
     * Constructor
     *
     * @param bufferWrapper buffer wrapper shared with the event source
     */
    public EventAggregator(BufferWrapper bufferWrapper) {
        this.bufferWrapper = bufferWrapper;
    }

    /**
     * Aggregates the buffer and prints the result
     */
    public void aggregateAndPrint() {
        Map<GroupingKey, Integer> map = aggregate();
        if (map != null) {
            map.entrySet().stream()
                    .map(OutputRecord::new)
                    .map(OutputRecord::toString)
                    .forEach(LOGGER::info);
        }
    }

    /**
     * First, poll/drain the current buffer to a temp list and
     * generate counts by the grouping key from this list.
     *
     * @return Map<GroupingKey, Integer> grouping key and count is value
     */
    public Map<GroupingKey, Integer> aggregate() {
        List<Event> tempBuffer = bufferWrapper.pollCurrentBuffer();

        Map<GroupingKey, Integer> map = null;
        if(!tempBuffer.isEmpty()) {
            map = getCounts(tempBuffer);
            printBufferStats(tempBuffer.size());
        }
        return map;
    }

    /**
     * Generates counts by the grouping key for the events in the input list
     *
     * @param events list of events
     * @return Map<GroupingKey, Integer> grouping key and count is value
     */
    private Map<GroupingKey, Integer> getCounts(List<Event> events) {
        return events.stream()
                .filter(Objects::nonNull)
                .filter(event -> Event.Sev.SUCCESS.equals(event.getSev()))
                .map(event -> new GroupingKey(event.getDevice(), event.getTitle(), event.getCountry()))
                .collect(Collectors.toMap(Function.identity(), groupingKey -> 1, Integer::sum));
    }

    /**
     * Print size of buffer and processed size.
     * Buffer will always be empty when backpressure is enabled.
     *
     * @param processedSize number of processed records
     */
    private void printBufferStats(int processedSize) {
        LOGGER.info("Processed events: {}, buffer size:{} ", processedSize, bufferWrapper.getSize());
    }
}
