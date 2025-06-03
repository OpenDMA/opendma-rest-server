package org.opendma.rest.server.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.opendma.api.OdmaContent;
import org.opendma.api.OdmaGuid;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaPageIterator;
import org.opendma.api.OdmaPagingIterable;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaQName;
import org.opendma.api.OdmaType;
import org.opendma.rest.server.SafeSplitter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ServiceProperty {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("multiValue")
    private boolean multiValue;

    @JsonProperty("readOnly")
    private boolean readOnly;

    @JsonProperty("resolved")
    private boolean resolved;

    @JsonProperty("value")
    private Object value;
    
    private int pageSizeWithoutPagingSupport = Integer.MAX_VALUE;
    
    private boolean rescanPreparedProperties = false;

    public ServiceProperty(OdmaProperty prop, String startToken, boolean enforceResolved, IncludeListSpec includeListSpec, OdmaId objId, boolean rescanPreparedProperties) {
        this.name = prop.getName().toString();
        this.type = prop.getType().toString();
        this.multiValue = prop.isMultiValue();
        this.readOnly = prop.isReadOnly();
        this.resolved = prop.isResolved() || enforceResolved;
        if(this.resolved) {
            this.value = prop.isMultiValue() ? convertMultiValue(prop.getName(), prop.getType(), prop.getValue(), startToken, includeListSpec, objId) : convertSingleValue(prop.getName(), prop.getType(), prop.getValue(), includeListSpec, objId, 0);
        }
        this.rescanPreparedProperties = rescanPreparedProperties;
    }
    
    private Object convertMultiValue(OdmaQName name, OdmaType type, Object value, String startToken, IncludeListSpec includeListSpec, OdmaId objId) {
        if(value == null) {
            throw new RuntimeException("multi-valued property must not be NULL");
        }
        if(type == OdmaType.REFERENCE) {
            List<Object> items = new LinkedList<Object>();
            String next = null;
            @SuppressWarnings("unchecked")
            Iterable<OdmaObject> refs = (Iterable<OdmaObject>)value;
            if(refs instanceof OdmaPagingIterable<?>) {
                OdmaPagingIterable<OdmaObject> pagingRefs = (OdmaPagingIterable<OdmaObject>)refs;
                OdmaPageIterator<OdmaObject> pageIterator = pagingRefs.pageIterator();
                if(startToken != null) {
                    pageIterator.goToMark(startToken);
                }
                for(OdmaObject ref : pageIterator.getPage()) {
                    items.add(convertSingleValue(name, type, ref, includeListSpec, null, -1));
                }
                next = pageIterator.nextPageMark();
            } else {
                int pos = 0;
                Iterator<OdmaObject> itObj = refs.iterator();
                if(startToken != null) {
                    int skip;
                    try {
                        skip = Integer.parseInt(startToken);
                    } catch(NumberFormatException nfe) {
                        skip = 0;
                    }
                    for(int i = 0; i < skip; i++) {
                        try {
                            itObj.next();
                            pos++;
                        } catch(NoSuchElementException nsee) {
                            break;
                        }
                    }
                }
                int count = 0;
                while(itObj.hasNext()) {
                    items.add(convertSingleValue(name, type, itObj.next(), includeListSpec, null, -1));
                    count++;
                    pos++;
                    if(count >= pageSizeWithoutPagingSupport) {
                        break;
                    }
                }
                if(itObj.hasNext()) {
                    next = Integer.toString(pos);
                }
            }
            return new ServiceReferenceEnumeration(items, next);
        } else {
            List<Object> result = new LinkedList<Object>();
            int pos = 0;
            @SuppressWarnings("unchecked")
            List<Object> values = (List<Object>)value;
            for(Object obj : values) {
                result.add(convertSingleValue(name, type, obj, includeListSpec, objId, pos++));
            }
            return result;
        }
    }
    
    private Object convertSingleValue(OdmaQName name, OdmaType type, Object value, IncludeListSpec includeListSpec, OdmaId objId, int pos) {
        if(value == null) {
            return null;
        }
        switch(type) {
            case BLOB :
                return Base64Coder.encode((byte[])value);
            case BOOLEAN :
                return ((Boolean)value).booleanValue() ? "true" : "false";
            case CONTENT :
                OdmaContent content = (OdmaContent)value;
                return new ServiceContent(SafeSplitter.encode(objId.toString())+";"+SafeSplitter.encode(name.toString())+";"+Integer.toString(pos), content.getSize());
            case DATETIME :
                return DATETIME_FORMAT.get().format((Date)value);
            case DOUBLE :
                return Double.toString((Double)value);
            case FLOAT :
                return Float.toString((Float)value);
            case GUID :
                OdmaGuid guid = (OdmaGuid)value;
                return new ServiceGuid(guid.getObjectId().toString(), guid.getRepositoryId().toString());
            case ID :
                return ((OdmaId)value).toString();
            case INTEGER :
                return Integer.toString((Integer)value);
            case LONG :
                return Long.toString((Long)value);
            case REFERENCE :
                OdmaObject refObj = (OdmaObject)value;
                if(refObj.isEmbeddingRecommended()) {
                    return new ServiceObject(refObj, includeListSpec, rescanPreparedProperties);
                } else {
                    return new ServiceObject(refObj.getId());
                }
            case SHORT :
                return Short.toString((Short)value);
            case STRING :
                return ((String)value);
            default :
                throw new IllegalArgumentException("Unknown OdmaType value: "+type.toString());
        }
    }
    
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override protected SimpleDateFormat initialValue() {
            SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            result.setTimeZone(TimeZone.getTimeZone("UTC"));
            result.setLenient(false);
            return result;
        }
    };

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

}
