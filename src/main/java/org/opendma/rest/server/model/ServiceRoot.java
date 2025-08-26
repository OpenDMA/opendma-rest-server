package org.opendma.rest.server.model;

import java.util.LinkedList;
import java.util.List;

import org.opendma.api.OdmaId;
import org.opendma.api.OdmaQName;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceRoot {

    @JsonProperty("opendmaVersion")
    private String opendmaVersion;

    @JsonProperty("serviceVersion")
    private String serviceVersion;

    @JsonProperty("repositories")
    private List<String> repositories;
    
    @JsonProperty("supportedQueryLanguages")
    private List<String> supportedQueryLanguages;

    public ServiceRoot(String opendmaVersion, String serviceVersion, List<OdmaId> repositoryIds, List<OdmaQName> supportedQueryLanguageNames) {
        super();
        this.opendmaVersion = opendmaVersion;
        this.serviceVersion = serviceVersion;
        this.repositories = new LinkedList<String>();
        for(OdmaId id : repositoryIds) {
            this.repositories.add(id.toString());
        }
        this.supportedQueryLanguages = new LinkedList<String>();
        for(OdmaQName name : supportedQueryLanguageNames) {
            this.supportedQueryLanguages.add(name.toString());
        }
    }

    public String getOpendmaVersion() {
        return opendmaVersion;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public List<String> getRepositories() {
        return repositories;
    }

}
