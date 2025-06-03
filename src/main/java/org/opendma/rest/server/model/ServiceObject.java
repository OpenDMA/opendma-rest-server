package org.opendma.rest.server.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opendma.api.OdmaClass;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaPropertyInfo;
import org.opendma.api.OdmaQName;
import org.opendma.api.OdmaType;
import org.opendma.exceptions.OdmaPropertyNotFoundException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ServiceObject {

    @JsonProperty("id")
    private String id;

    @JsonProperty("rootOdmaClassName")
    private String rootOdmaClassName;

    @JsonProperty("aspectRootOdmaNames")
    private List<String> aspectRootOdmaNames;

    @JsonProperty("properties")
    private List<ServiceProperty> properties;

    @JsonProperty("complete")
    private Boolean complete;

    public ServiceObject(OdmaObject obj, IncludeListSpec includeListSpec, boolean rescanPreparedProperties) {
        this.id = obj.getId().toString();
        OdmaClass odmaRootClass = obj.getOdmaClass();
        while(!odmaRootClass.getNamespace().equals("opendma")) {
            if(odmaRootClass.getSuperClass() == null || odmaRootClass.getSuperClass().getQName().equals(odmaRootClass.getQName())) {
                break;
            }
            odmaRootClass = odmaRootClass.getSuperClass();
        }
        this.rootOdmaClassName = odmaRootClass.getQName().toString();
        HashSet<OdmaQName> aspectRootNames = new HashSet<OdmaQName>();
        for(OdmaClass aspect :  obj.getOdmaClass().getAspects()) {
            OdmaClass aspectOdmaRootClass = aspect;
            while(!aspectOdmaRootClass.getNamespace().equals("opendma")) {
                if(aspectOdmaRootClass.getSuperClass() == null || aspectOdmaRootClass.getSuperClass().equals(aspectOdmaRootClass) || aspectOdmaRootClass.getSuperClass().getQName().equals(aspectOdmaRootClass.getQName())) {
                    break;
                }
                aspectOdmaRootClass = aspectOdmaRootClass.getSuperClass();
            }
            aspectRootNames.add(aspectOdmaRootClass.getQName());
        }
        this.aspectRootOdmaNames = new ArrayList<String>(aspectRootNames.size());
        for(OdmaQName qn : aspectRootNames) {
            aspectRootOdmaNames.add(qn.toString());
        }
        this.properties = new LinkedList<ServiceProperty>();
        ////////////////////////////////////
        // IMPORTANT
        ////////////////////////////////////
        // This code is highly optimised to avoid any unnecessary class to obj.getOdmaClass() and obj.getOdmaClass().getProperties()
        // which could trigger round-trips to the backend system.
        ////////////////////////////////////
        if(includeListSpec == null) {
            // the service has been called without the `include=` query parameter
            Iterator<OdmaProperty> availableProperties = obj.availableProperties();
            if(availableProperties != null) {
                // the adaptor provides a list of immediately available properties. Just send these.
                while(availableProperties.hasNext()) {
                    OdmaProperty prop = availableProperties.next();
                    String nextToken = null;
                    boolean enforceResolved = false;
                    this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                }
                this.complete = obj.availablePropertiesComplete();
            } else {
                // the adaptor does not help us. We need to iterate over all OdmaPropertyInfo objects of the class and just send all properties
                this.complete = true;
                for(OdmaPropertyInfo pi : obj.getOdmaClass().getProperties()) {
                    OdmaQName propName = pi.getQName();
                    try {
                        OdmaProperty prop = obj.getProperty(propName);
                        String nextToken = null;
                        boolean enforceResolved = false;
                        this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                    } catch(OdmaPropertyNotFoundException pnfe) {
                        this.complete = false;
                    }
                }
            }
        } else {
            // includeListSpec != null
            if(!includeListSpec.hasWildcards()) {
                // we do not have any wildcards in the `include=` list. this allows us to optimise this section
                OdmaQName[] includedPropertyNames = includeListSpec.getIncludedPropertyNames();
                if(includedPropertyNames.length > 0) {
                    obj.prepareProperties(includedPropertyNames, false);
                }
                Iterator<OdmaProperty> availableProperties = obj.availableProperties();
                if(availableProperties != null && includeListSpec.isDefaultIncluded()) {
                    HashSet<OdmaQName> includedProps = new HashSet<OdmaQName>();
                    // just send all available properties. we have prepared everything on the include list. if they exist, they will be part of `availableProperties`
                    while(availableProperties.hasNext()) {
                        OdmaProperty prop = availableProperties.next();
                        String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                        boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                        this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                        includedProps.add(prop.getName());
                    }
                    this.complete = obj.availablePropertiesComplete();
                    // sanity check
                    if(rescanPreparedProperties) {
                        for(OdmaQName propName : includedPropertyNames) {
                            if(!includedProps.contains(propName)) {
                                // at this point, it is possible that the `prepareProperties` call failed or that this property actually does not exist
                                try {
                                    OdmaProperty prop = obj.getProperty(propName);
                                    String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                                    boolean enforceResolved = true;
                                    this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                                    // if there hasn't been an OdmaPropertyNotFoundException, it means that we found a property that has not been prepared properly. We cannot trust availablePropertiesComplete()
                                    this.complete = false;
                                } catch(OdmaPropertyNotFoundException pnfe) {
                                    // silently ignored
                                }
                            }
                        }
                    }
                } else {
                    // go through the include list and just send each single property
                    for(OdmaQName propName : includedPropertyNames) {
                        try {
                            OdmaProperty prop = obj.getProperty(propName);
                            String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                            boolean enforceResolved = true;
                            this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                        } catch(OdmaPropertyNotFoundException pnfe) {
                            // silently ignored
                        }
                    }
                    this.complete = false;
                }
            } else {
                // includeListSpec.hasWildcards()
                // we have wildcards. this requires us to iterate over all OdmaPropertyInfo objects of the class and check if they are included
                this.complete = false;
                HashSet<OdmaQName> includedProps = new HashSet<OdmaQName>();
                Iterator<OdmaProperty> availableProperties = obj.availableProperties();
                if(availableProperties != null && includeListSpec.isDefaultIncluded())
                {
                    // we also need to include the default set
                    while(availableProperties.hasNext()) {
                        OdmaProperty prop = availableProperties.next();
                        String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                        boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                        this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                        includedProps.add(prop.getName());
                    }
                    this.complete = obj.availablePropertiesComplete();
                }
                if(this.complete)
                {
                    // special case: we are done.
                    // the `availableProperties` we have sent  are complete. there are no  other properties that could be add through the include list
                }
                else
                {
                    // since we have wildcards, we need to iterate over all OdmaPropertyInfo objects
                    this.complete = true;
                    for(OdmaPropertyInfo pi : obj.getOdmaClass().getProperties()) {
                        OdmaQName propName = pi.getQName();
                        if(includedProps.contains(propName)) {
                            // we already have this one as part of the `availableProperties`
                            continue;
                        }
                        if(includeListSpec.isDefaultIncluded() && availableProperties == null) {
                            // special case: we have the default set included.
                            // In the case of `availableProperties == null` this means "include everything" (see above).
                        } else {
                            if(!includeListSpec.isPropertyIncluded(propName)) {
                                this.complete = false;
                                continue;
                            }
                        }
                        try {
                            OdmaProperty prop = obj.getProperty(propName);
                            String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                            boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                            this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId(), rescanPreparedProperties));
                            includedProps.add(propName);
                        } catch(OdmaPropertyNotFoundException pnfe) {
                            this.complete = false;
                        }
                    }
                }
            }
        }
    }

    public ServiceObject(OdmaId objId) {
        this.id = objId.toString();
    }

    public String getId() {
        return id;
    }

    public String getRootOdmaClassName() {
        return rootOdmaClassName;
    }

    public List<String> getAspectRootOdmaNames() {
        return aspectRootOdmaNames;
    }

    public List<ServiceProperty> getProperties() {
        return properties;
    }

    public Boolean isComplete() {
        return complete;
    }

}
