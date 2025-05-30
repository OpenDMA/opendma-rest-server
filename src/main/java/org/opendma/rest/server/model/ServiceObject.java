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

    public ServiceObject(OdmaObject obj, IncludeListSpec includeListSpec) {
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
        Iterator<OdmaProperty> availableProperties = obj.availableProperties();
        OdmaQName[] includedPropertyNames = includeListSpec.getIncludedPropertyNames();
        if(availableProperties != null && (includedPropertyNames.length == 0 || includeListSpec.isDefaultIncluded()) && !includeListSpec.hasWildcards()) {
            // performance optimization supported
            if(includedPropertyNames.length > 0) {
                // we have includes in addition to the default set
                obj.prepareProperties(includedPropertyNames, false);
            }
            while(availableProperties.hasNext()) {
                OdmaProperty prop = availableProperties.next();
                String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId()));
            }
            this.complete = obj.availablePropertiesComplete();
        } else {
            // performance optimization not supported or we need to handle wildcards
            this.complete = true;
            HashSet<OdmaQName> includedProps = new HashSet<OdmaQName>();
            for(OdmaPropertyInfo pi : obj.getOdmaClass().getProperties()) {
                OdmaQName propName = pi.getQName();
                if(includedPropertyNames.length > 0 && !includeListSpec.isPropertyIncluded(propName)) {
                    this.complete = false;
                    continue;
                }
                try {
                    OdmaProperty prop = obj.getProperty(propName);
                    String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                    boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                    this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId()));
                    includedProps.add(propName);
                } catch(OdmaPropertyNotFoundException pnfe) {
                    this.complete = false;
                }
            }
            if(includeListSpec.isDefaultIncluded() && availableProperties != null) {
                // combination of wildcards and default
                while(availableProperties.hasNext()) {
                    OdmaProperty prop = availableProperties.next();
                    if(includedProps.contains(prop.getName())) {
                        continue;
                    }
                    String nextToken = prop.getType() == OdmaType.REFERENCE && prop.isMultiValue() ? includeListSpec.getNextToken(prop.getName()) : null;
                    boolean enforceResolved = includeListSpec.isPropertyIncluded(prop.getName());
                    this.properties.add(new ServiceProperty(prop, nextToken, enforceResolved, includeListSpec, obj.getId()));
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
