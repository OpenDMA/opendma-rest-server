package org.opendma.rest.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendma.api.OdmaQName;
import org.opendma.rest.server.IncludeSpecParser.IncludeSpec;

public class IncludeListSpec {

    private boolean includeDefault = false;
    private boolean hasWildcards = false;
    private Map<OdmaQName, String> includeSpecMap = new HashMap<OdmaQName, String>();
    
    public IncludeListSpec(List<IncludeSpec> includeList) {
        for(IncludeSpec includeSpec : includeList) {
            if("default".equals(includeSpec.propertySpec)) {
                includeDefault =  true;
                continue;
            }
            try {
                OdmaQName qn = OdmaQName.fromString(includeSpec.propertySpec);
                if(qn.getNamespace().equals("*") || qn.getName().equals("*")) {
                    hasWildcards = true;
                }
                includeSpecMap.put(qn, includeSpec.nextTokenPrefix);
            } catch(IllegalArgumentException iae) {
                ; // ignore
            }
        }
    }
    
    public boolean isDefaultIncluded() {
        return includeDefault;
    }
    
    public boolean hasWildcards() {
        return hasWildcards;
    }
    
    public OdmaQName[] getIncludedPropertyNames() {
        return includeSpecMap.keySet().toArray(new OdmaQName[includeSpecMap.keySet().size()]);
    }
    
    public boolean isPropertyIncluded(OdmaQName propName) {
        if(includeSpecMap.containsKey(propName)) {
            return true;
        }
        if(hasWildcards) {
            for(OdmaQName qn : includeSpecMap.keySet()) {
                if(qn.getNamespace().equals("*") && qn.getName().equals("*")) {
                    return true;
                }
                if(qn.getNamespace().equals("*") && qn.getName().equals(propName.getName())) {
                    return true;
                }
                if(qn.getNamespace().equals(propName.getNamespace()) && qn.getName().equals("*")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public String getNextToken(OdmaQName propName) {
        return includeSpecMap.get(propName);
    }
    
}
