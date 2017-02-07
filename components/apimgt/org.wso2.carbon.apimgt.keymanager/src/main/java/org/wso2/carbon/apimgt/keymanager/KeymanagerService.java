package org.wso2.carbon.apimgt.keymanager;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * This method authenticate the user.
 *
 */
@Component (
        name = "org.wso2.carbon.apimgt.keymanager.KeymanagerService",
        service = Microservice.class,
        immediate = true
)
@Path("/oauth2")
public class KeymanagerService implements Microservice {

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    @GET
    @Path ("/token")
    public String getNewAccessToken() {
        return "";
    }
}
