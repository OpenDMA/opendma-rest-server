package org.opendma.rest.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceGuid {

    @JsonProperty("objectId")
    private String objectId;

    @JsonProperty("repositoryId")
    private String repositoryId;

    public ServiceGuid(String objectId, String repositoryId) {
        super();
        this.objectId = objectId;
        this.repositoryId = repositoryId;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }
    
}
