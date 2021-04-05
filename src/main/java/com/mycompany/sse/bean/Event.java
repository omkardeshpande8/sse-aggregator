package com.mycompany.sse.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold event object
 */
public class Event {

    /**
     * Objectmapper for json parsing
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Logger for the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);

    /**
     * Enum Sev
     */
    public enum Sev {
        @JsonProperty("success")
        SUCCESS,
        @JsonProperty("error")
        ERROR
    }

    /**
     * Device
     */
    private String device;

    /**
     * Sev
     */
    private Sev sev;

    /**
     * Title
     */
    private String title;

    /**
     * Country
     */
    private String country;

    /**
     * Timestamp
     */
    private long time;

    /**
     * Getter for device
     *
     * @return string device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Getter for sev
     *
     * @return sev sev
     */
    public Sev getSev() {
        return sev;
    }

    /**
     * Getter for title
     *
     * @return string title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for country
     *
     * @return string country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Getter for time
     *
     * @return long time
     */
    public long getTime() {
        return time;
    }

    /**
     * Parse json event and return an instance of class Event
     * Returns null if json parsing fails. The JsonProcessingException is not thrown
     *
     * @param event Json representation of the string
     * @return instance of class event
     */
    public static Event getEventFromJson(String event) {
        try {
            return objectMapper.readValue(event, Event.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return null;
    }
}

