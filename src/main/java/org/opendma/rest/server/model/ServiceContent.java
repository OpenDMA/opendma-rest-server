package org.opendma.rest.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceContent {

    @JsonProperty("id")
    private String id;
    
    @JsonProperty("size")
    private long size;

    public ServiceContent(String id, long size) {
        super();
        this.id = id;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public long getSize() {
        return size;
    }
    
}
