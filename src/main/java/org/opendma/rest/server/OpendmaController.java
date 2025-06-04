package org.opendma.rest.server;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.opendma.api.OdmaContent;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaQName;
import org.opendma.api.OdmaRepository;
import org.opendma.api.OdmaSession;
import org.opendma.api.OdmaSessionProvider;
import org.opendma.exceptions.OdmaException;
import org.opendma.exceptions.OdmaInvalidDataTypeException;
import org.opendma.exceptions.OdmaObjectNotFoundException;
import org.opendma.exceptions.OdmaPropertyNotFoundException;
import org.opendma.rest.server.model.Base64Coder;
import org.opendma.rest.server.model.IncludeListSpec;
import org.opendma.rest.server.model.ServiceObject;
import org.opendma.rest.server.model.ServiceRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opendma")
public class OpendmaController {


    private final OdmaSessionProvider sessionProvider;
    
    @Value("${rescanPreparedProperties:false}")
    private boolean rescanPreparedProperties;

    @Autowired
    public OpendmaController(OdmaSessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }
    
    @GetMapping(value = "/", produces = {"application/json"})
    ResponseEntity<ServiceRoot> serviceRoot(HttpServletRequest httpRequest) {

        OdmaSession session;
        try {
            session = getSessionForRequest(httpRequest);
        } catch (OdmaException e) {
            session = null;
        }
        if(session == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("WWW-Authenticate", "Basic realm=\"OpenDMA REST Service\"");
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }
        try {
            ServiceRoot serviceRoot = new ServiceRoot("0.7.0", "0.1.0", session.getRepositoryIds());
            return new ResponseEntity<ServiceRoot>(serviceRoot, HttpStatus.OK);
        } finally {
            session.close();
        }

    }

    @GetMapping(value = "/obj/{repoid}", produces = {"application/json"})
    ResponseEntity<ServiceObject> repository(
            @PathVariable("repoid") String repoId,
            @Valid @RequestParam(value = "include", required = false) String include,
            HttpServletRequest httpRequest) {

        IncludeListSpec includeListSpec = include == null ? null  : new IncludeListSpec(IncludeSpecParser.parse(include));
        OdmaSession session;
        try {
            session = getSessionForRequest(httpRequest);
        } catch (OdmaException e) {
            session = null;
        }
        if(session == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("WWW-Authenticate", "Basic realm=\"OpenDMA REST Service\"");
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }
        try {
            OdmaRepository repo;
            try {
                repo = session.getRepository(new OdmaId(repoId));
            } catch (OdmaObjectNotFoundException e) {
                return new ResponseEntity<ServiceObject>(HttpStatus.NOT_FOUND);
            }
            ServiceObject serviceRoot = new ServiceObject(repo, includeListSpec, rescanPreparedProperties);
            return new ResponseEntity<ServiceObject>(serviceRoot, HttpStatus.OK);
        } finally {
            session.close();
        }

    }

    @GetMapping(value = "/obj/{repoid}/{objid}", produces = {"application/json"})
    ResponseEntity<ServiceObject> object(
            @PathVariable("repoid") String repoId,
            @PathVariable("objid") String objId,
            @Valid @RequestParam(value = "include", required = false) String include,
            HttpServletRequest httpRequest) {

        IncludeListSpec includeListSpec = include == null ? null  : new IncludeListSpec(IncludeSpecParser.parse(include));
        OdmaSession session;
        try {
            session = getSessionForRequest(httpRequest);
        } catch (OdmaException e) {
            session = null;
        }
        if(session == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("WWW-Authenticate", "Basic realm=\"OpenDMA REST Service\"");
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }
        try {
            OdmaObject obj;
            try {
                obj = session.getObject(new OdmaId(repoId), new OdmaId(objId), null);
            } catch (OdmaObjectNotFoundException e) {
                return new ResponseEntity<ServiceObject>(HttpStatus.NOT_FOUND);
            }
            ServiceObject serviceRoot = new ServiceObject(obj, includeListSpec, rescanPreparedProperties);
            return new ResponseEntity<ServiceObject>(serviceRoot, HttpStatus.OK);
        } finally {
            session.close();
        }

    }

    @GetMapping(value = "/bin/{repoid}/{contentid}", produces = {"application/octet-stream"})
    ResponseEntity<InputStreamResource> content(
            @PathVariable("repoid") String repoId,
            @PathVariable("contentid") String contentId,
            HttpServletRequest httpRequest) {

        OdmaSession session;
        try {
            session = getSessionForRequest(httpRequest);
        } catch (OdmaException e) {
            session = null;
        }
        if(session == null) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.UNAUTHORIZED);
        }
        
        List<String> contentIdParts = SafeSplitter.split(contentId);
        if(contentIdParts.size() != 3) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
        }
        String objId = contentIdParts.get(0);
        OdmaQName propName;
        try {
            propName = OdmaQName.fromString(contentIdParts.get(1));
        } catch(IllegalArgumentException iae) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
        }
        int pos;
        try {
            pos = Integer.parseInt(contentIdParts.get(2));
        } catch(NumberFormatException nfe) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
        }
        if(pos < 0 || pos > 1024) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
        }
        
        try {
            OdmaObject obj;
            OdmaProperty prop;
            OdmaContent content;
            try {
                obj = session.getObject(new OdmaId(repoId), new OdmaId(objId), null);
                prop = obj.getProperty(propName);
                if(prop.isMultiValue()) {
                    content = prop.getContentList().get(pos);
                } else {
                    content = prop.getContent();
                }
            } catch (OdmaObjectNotFoundException | OdmaPropertyNotFoundException | IndexOutOfBoundsException e) {
                return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
            } catch (OdmaInvalidDataTypeException e) {
                return new ResponseEntity<InputStreamResource>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            InputStreamResource  resource = new InputStreamResource(content.getStream());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(content.getSize())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch(Exception e) {
            return new ResponseEntity<InputStreamResource>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            session.close();
        }

    }
    
    private OdmaSession getSessionForRequest(HttpServletRequest httpRequest) throws OdmaException {
        
        String authHeader = httpRequest.getHeader("Authorization");
        
        if (authHeader == null) {
            return sessionProvider.getSession();
        }
        
        if(authHeader.toLowerCase().startsWith("basic ")) {
            String base64Credentials = authHeader.substring(6).trim();
            byte[] credDecoded = Base64Coder.decode(base64Credentials);
            // byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            if (values.length != 2) {
                return null;
            }
            return sessionProvider.getSessionForAccount(values[0], values[1]);
        }
        
        if(authHeader.toLowerCase().startsWith("bearer ")) {
            String bearerToken = authHeader.substring(7).trim();
            return sessionProvider.getSessionWithToken(new OdmaQName("http","Bearer"), bearerToken);
        }
        
        return null;
    }

}
