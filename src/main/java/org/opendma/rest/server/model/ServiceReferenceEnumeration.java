package org.opendma.rest.server.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceReferenceEnumeration {
    
    @JsonProperty("items")
    private List<Object> items;
    
    @JsonProperty("next")
    private String next;

    public ServiceReferenceEnumeration(List<Object> items, String next) {
        super();
        this.items = items;
        this.next = next;
    }

    public List<Object> getItems() {
        return items;
    }

    public String getNext() {
        return next;
    }

}
