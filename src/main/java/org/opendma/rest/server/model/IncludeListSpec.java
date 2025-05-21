package org.opendma.rest.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendma.api.OdmaQName;
import org.opendma.rest.server.IncludeSpecParser.IncludeSpec;

public class IncludeListSpec {

    private boolean includeDefault = false;
    private Map<OdmaQName, String> includeSpecMap = new HashMap<OdmaQName, String>();
    
    public IncludeListSpec(List<IncludeSpec> includeList) {
        for(IncludeSpec includeSpec : includeList) {
            if("default".equals(includeSpec.propertySpec)) {
                includeDefault =  true;
                continue;
            }
            try {
                OdmaQName  qn = OdmaQName.fromString(includeSpec.propertySpec);
                includeSpecMap.put(qn, includeSpec.nextTokenPrefix);
            } catch(IllegalArgumentException iae) {
                ; // ignore
            }
        }
    }
    
    public boolean isDefaultIncluded() {
        return includeDefault;
    }
    
    public OdmaQName[] getIncludedPropertyNames() {
        return includeSpecMap.keySet().toArray(new OdmaQName[includeSpecMap.keySet().size()]);
    }
    
    public boolean isPropertyIncluded(OdmaQName propName) {
        return includeSpecMap.containsKey(propName);
    }
    
    public String getNextToken(OdmaQName propName) {
        return includeSpecMap.get(propName);
    }
    
}
