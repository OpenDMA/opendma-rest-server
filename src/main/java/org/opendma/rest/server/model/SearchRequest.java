package org.opendma.rest.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchRequest {
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("query")
    private String query;

    public SearchRequest() {
        super();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
