package org.wso2.carbon.apimgt.keymanager;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Microservice;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This class is used to mock the key manager apis.
 *
 */
@Component (
        name = "org.wso2.carbon.apimgt.keymanager.KeymanagerService",
        service = Microservice.class,
        immediate = true
)
@Path("/keyserver")
public class KeymanagerService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(KeymanagerService.class);

    private static Map<String, OAuthApplication> applications = new HashMap<>();
    private static Map<String, OAuthApplication> appsByClientId = new HashMap<>();

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    @POST
    @Path ("/token")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response getNewAccessToken(OAuthRequest body, @HeaderParam("Authorization") String authzHeader) {

        if (!"client_credentials".equals(body.getGrantType())) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode("900501");
            errorDTO.setMessage("Unsupported Grant Type");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }

        if (authzHeader == null || authzHeader.isEmpty()) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode("900401");
            errorDTO.setMessage("Unauthorized. Authorization header not provided");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
        }

        String[] decoded = new String[0];
        try {
            decoded = new String(Base64.getDecoder().decode(authzHeader.getBytes("UTF-8")), "UTF-8").split(":");
        } catch (UnsupportedEncodingException e) {
            log.error("Error getting header");
        }
        String clientId = decoded[0];
        String clientSecret = decoded[1];

        if (!appsByClientId.containsKey(clientId) ||
                !appsByClientId.get(clientId).getClientSecret().equals(clientSecret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!appsByClientId.get(clientId).getClientSecret().equals(clientSecret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        OAuthTokenResponse tokenResponse = new OAuthTokenResponse();
        tokenResponse.setToken(UUID.randomUUID().toString());
        tokenResponse.setRefreshToken(UUID.randomUUID().toString());
        return Response.status(Response.Status.OK).entity(tokenResponse).build();
    }

    @POST
    @Path("/register")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response registerClient(OAuthApplication body) {
        if (applications.containsKey(body.getClientName())) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode("900409");
            errorDTO.setMessage("Client already exists with name " + body.getClientName());
            return Response.status(Response.Status.CONFLICT).entity(errorDTO).
                    build();
        }
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        body.setClientId(clientId);
        body.setClientSecret(clientSecret);
        applications.put(body.getClientName(), body);
        appsByClientId.put(clientId, body);

        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @GET
    @Path("/register/{clientId}")
    @Produces({ "application/json" })
    public Response clientRead(@PathParam("clientId") String clientId) {
        if (appsByClientId.containsKey(clientId)) {
            return Response.status(Response.Status.OK).entity(appsByClientId.get(clientId)).build();
        }

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode("900404");
        errorDTO.setMessage("Client not found with id " + clientId);
        return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
    }
}
