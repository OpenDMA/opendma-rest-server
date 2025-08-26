package org.opendma.rest.server.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResponse {
    
    @JsonProperty("items")
    private List<ServiceObject> items;

    public SearchResponse(List<ServiceObject> items) {
        super();
        this.items = items;
    }

    public List<ServiceObject> getItems() {
        return items;
    }

}
