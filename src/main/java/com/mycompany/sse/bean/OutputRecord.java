package com.mycompany.sse.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A helper class to print the output in specified format
 */
public class OutputRecord extends GroupingKey {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputRecord.class);

    /**
     * Sps
     */
    private final int sps;

    /**
     * Public constructor
     *
     * @param entry entry of grouping key and integer count
     */
    public OutputRecord(Map.Entry<GroupingKey, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    private OutputRecord(GroupingKey key, int sps) {
        super(key.getDevice(), key.getTitle(), key.getCountry());
        this.sps = sps;
    }

    /**
     * Getter for sps
     *
     * @return sps
     */
    public int getSps() {
        return sps;
    }

    /**
     * Json representation of the object
     *
     * @return json string
     */
    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return null;
    }
}
