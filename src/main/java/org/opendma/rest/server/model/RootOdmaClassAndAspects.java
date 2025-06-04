package org.opendma.rest.server.model;

import java.util.List;

import org.opendma.api.OdmaQName;

public class RootOdmaClassAndAspects {

    private OdmaQName rootOdmaClassName;

    private List<OdmaQName> aspectRootOdmaNames;

    public RootOdmaClassAndAspects(OdmaQName rootOdmaClassName, List<OdmaQName> aspectRootOdmaNames) {
        super();
        this.rootOdmaClassName = rootOdmaClassName;
        this.aspectRootOdmaNames = aspectRootOdmaNames;
    }

    public OdmaQName getRootOdmaClassName() {
        return rootOdmaClassName;
    }

    public List<OdmaQName> getAspectRootOdmaNames() {
        return aspectRootOdmaNames;
    }

}
