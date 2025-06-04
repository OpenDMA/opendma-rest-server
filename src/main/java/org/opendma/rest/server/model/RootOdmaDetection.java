package org.opendma.rest.server.model;

import org.opendma.api.OdmaObject;

public interface RootOdmaDetection {
    
    RootOdmaClassAndAspects detect(OdmaObject obj);

}
