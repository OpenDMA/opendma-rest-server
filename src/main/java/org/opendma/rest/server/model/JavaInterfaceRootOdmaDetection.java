package org.opendma.rest.server.model;

import java.util.ArrayList;
import java.util.List;

import org.opendma.api.OdmaAssociation;
import org.opendma.api.OdmaChoiceValue;
import org.opendma.api.OdmaClass;
import org.opendma.api.OdmaCommonNames;
import org.opendma.api.OdmaContainable;
import org.opendma.api.OdmaContainer;
import org.opendma.api.OdmaContentElement;
import org.opendma.api.OdmaDataContentElement;
import org.opendma.api.OdmaDocument;
import org.opendma.api.OdmaFolder;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaPropertyInfo;
import org.opendma.api.OdmaQName;
import org.opendma.api.OdmaReferenceContentElement;
import org.opendma.api.OdmaRepository;
import org.opendma.api.OdmaVersionCollection;

public class JavaInterfaceRootOdmaDetection implements RootOdmaDetection {

    @Override
    public RootOdmaClassAndAspects detect(OdmaObject obj) {
        return new RootOdmaClassAndAspects(detectClass(obj), detectAspects(obj));
    }
    
    private OdmaQName detectClass(OdmaObject obj) {
        if(obj instanceof OdmaRepository) {
            return OdmaCommonNames.CLASS_REPOSITORY;
        }
        if(obj instanceof OdmaClass) {
            return OdmaCommonNames.CLASS_CLASS;
        }
        if(obj instanceof OdmaPropertyInfo) {
            return OdmaCommonNames.CLASS_PROPERTYINFO;
        }
        if(obj instanceof OdmaChoiceValue) {
            return OdmaCommonNames.CLASS_CHOICEVALUE;
        }
        return OdmaCommonNames.CLASS_OBJECT;
    }
    
    private List<OdmaQName> detectAspects(OdmaObject obj) {
        ArrayList<OdmaQName> result = new ArrayList<OdmaQName>(9);
        if(obj instanceof OdmaDocument) {
            result.add(OdmaCommonNames.CLASS_DOCUMENT);
        }
        if(obj instanceof OdmaDataContentElement) {
            result.add(OdmaCommonNames.CLASS_DATACONTENTELEMENT);
        }
        if(obj instanceof OdmaReferenceContentElement) {
            result.add(OdmaCommonNames.CLASS_REFERENCECONTENTELEMENT);
        }
        if(obj instanceof OdmaContentElement) {
            if( !result.contains(OdmaCommonNames.CLASS_DATACONTENTELEMENT) && !result.contains(OdmaCommonNames.CLASS_REFERENCECONTENTELEMENT) ) {
                result.add(OdmaCommonNames.CLASS_CONTENTELEMENT);
            }
        }
        if(obj instanceof OdmaVersionCollection) {
            result.add(OdmaCommonNames.CLASS_VERSIONCOLLECTION);
        }
        if(obj instanceof OdmaFolder) {
            result.add(OdmaCommonNames.CLASS_FOLDER);
        }
        if(obj instanceof OdmaContainer) {
            if( !result.contains(OdmaCommonNames.CLASS_FOLDER) ) {
                result.add(OdmaCommonNames.CLASS_CONTAINER);
            }
        }
        if(obj instanceof OdmaContainable) {
            result.add(OdmaCommonNames.CLASS_CONTAINABLE);
        }
        if(obj instanceof OdmaAssociation) {
            result.add(OdmaCommonNames.CLASS_ASSOCIATION);
        }
        return result;
    }

}
