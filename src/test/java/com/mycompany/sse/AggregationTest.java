package com.mycompany.sse;

import com.mycompany.sse.aggregate.EventAggregator;
import com.mycompany.sse.bean.GroupingKey;
import com.mycompany.sse.buffer.BufferWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AggregationTest {

    private BufferWrapper bufferWrapper;

    /**
     * Read the events from the file
     *
     * @throws IOException if file is not present at src/test/resources/data.txt
     */
    @BeforeEach
    public void setUp() throws IOException {
        String path = "src/test/resources/data.txt";
        bufferWrapper = new BufferWrapper(Integer.MAX_VALUE, false);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            for (String line; (line = br.readLine()) != null; ) {
                bufferWrapper.addDataToBuffer(line);
            }
        }
    }

    /**
     * Basic correctness test
     */
    @Test
    @DisplayName("Correctness test")
    public void TestCorrectNess() {
        assertEquals(bufferWrapper.getSize(), 3596);

        EventAggregator aggregator = new EventAggregator(bufferWrapper);
        Map<GroupingKey, Integer> aggregate = aggregator.aggregate();

        assertEquals(aggregate.get(new GroupingKey
                ("xbox_360", "narcos", "CA")), 8);
        assertEquals(bufferWrapper.getSize(), 0);
    }

    /**
     * Test the error records are filtered out
     */
    @Test
    @DisplayName("Filter out test")
    public void TestFiltering() {
        EventAggregator aggregator = new EventAggregator(bufferWrapper);
        Map<GroupingKey, Integer> aggregate = aggregator.aggregate();

        // test if "sev":"error" records get filtered out
        assertNotEquals(aggregate.get(new GroupingKey
                ("ps3", "orange is the new black", "IND")), 16);
        assertNotEquals(aggregate.values().stream()
                .reduce(0, Integer::sum), 3596);
    }

}