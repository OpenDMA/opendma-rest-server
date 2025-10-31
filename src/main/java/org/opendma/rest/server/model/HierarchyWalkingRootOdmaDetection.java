package org.opendma.rest.server.model;

import java.util.ArrayList;
import java.util.HashSet;

import org.opendma.api.OdmaClass;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaQName;

public class HierarchyWalkingRootOdmaDetection implements RootOdmaDetection {

    @Override
    public RootOdmaClassAndAspects detect(OdmaObject obj) {
        OdmaClass odmaRootClass = obj.getOdmaClass();
        while(!odmaRootClass.getNamespace().equals("opendma")) {
            if(odmaRootClass.getSuperClass() == null || odmaRootClass.getSuperClass().getQName().equals(odmaRootClass.getQName())) {
                break;
            }
            odmaRootClass = odmaRootClass.getSuperClass();
        }
        HashSet<OdmaQName> aspectRootNamesSet = new HashSet<OdmaQName>();
        for(OdmaClass aspect :  obj.getOdmaClass().getIncludedAspects()) {
            OdmaClass aspectOdmaRootClass = aspect;
            while(!aspectOdmaRootClass.getNamespace().equals("opendma")) {
                if(aspectOdmaRootClass.getSuperClass() == null || aspectOdmaRootClass.getSuperClass().equals(aspectOdmaRootClass) || aspectOdmaRootClass.getSuperClass().getQName().equals(aspectOdmaRootClass.getQName())) {
                    break;
                }
                aspectOdmaRootClass = aspectOdmaRootClass.getSuperClass();
            }
            aspectRootNamesSet.add(aspectOdmaRootClass.getQName());
        }
        ArrayList<OdmaQName> aspectRootNames = new ArrayList<OdmaQName>(aspectRootNamesSet.size());
        aspectRootNames.addAll(aspectRootNamesSet);
        return new RootOdmaClassAndAspects(odmaRootClass.getQName(),aspectRootNames);
    }

}
