package org.opendma.rest.server;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.opendma.api.OdmaSession;

@WebListener
public class OdmaSessionCleanupListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        OdmaSession session = (OdmaSession) se.getSession().getAttribute("odmaSession");
        if (session != null) {
            session.close();
        }
    }

}