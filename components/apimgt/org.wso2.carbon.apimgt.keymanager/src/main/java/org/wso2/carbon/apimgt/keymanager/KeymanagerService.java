package org.wso2.carbon.apimgt.keymanager;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.keymanager.exception.KeyManagerException;
import org.wso2.carbon.apimgt.keymanager.util.KeyManagerUtil;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FormDataParam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
        KeyManagerUtil.addUsersAndScopes();
        getData();
        OAuthApplication pubApp = new OAuthApplication();
        pubApp.setClientId("publisher");
        pubApp.setClientSecret("1234-5678-9101");
        appsByClientId.put("publisher", pubApp);
        OAuthApplication storeApp = new OAuthApplication();
        storeApp.setClientId("store");
        storeApp.setClientSecret("1234-5678-9101");
        appsByClientId.put("store", storeApp);
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        KeyManagerUtil.backUpOauthData(applications, appsByClientId);
        KeyManagerUtil.backUpTokenData();
    }

    @POST
    @Path ("/token")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    @Produces({ "application/json" })
    public Response getNewAccessToken(@FormDataParam("username") String username, @FormDataParam("password") String
            password , @FormDataParam("grant_type") String grantType, @FormDataParam("validity_period")
            String validityPeriod, @HeaderParam("Authorization")
            String authzHeader)
            throws KeyManagerException {

        if (!"client_credentials".equals(grantType) && !"password".equals(grantType)) {
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

        String[] decoded;
        decoded = KeyManagerUtil.extractCredentialsFromAuthzHeader(authzHeader);
        String clientId = decoded[0];
        String clientSecret = decoded[1];
        Long validPeriod = Long.parseLong(validityPeriod);

        if (!appsByClientId.containsKey(clientId) ||
                !appsByClientId.get(clientId).getClientSecret().equals(clientSecret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!appsByClientId.get(clientId).getClientSecret().equals(clientSecret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        OAuthTokenResponse oAuthTokenResponse = new OAuthTokenResponse();
        if ("password".equals(grantType)) {
            if (KeyManagerUtil.getLoginAccessToken(oAuthTokenResponse, username, password, validPeriod)) {
                return Response.status(Response.Status.OK).entity(oAuthTokenResponse).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        OAuthApplication oAuthApplication = appsByClientId.get(clientId);
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse();
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        if (oAuthApplication.getAccessToken() != null) {
            accessToken = oAuthApplication.getAccessToken();
            refreshToken = oAuthApplication.getRefreshToken();
        }
        tokenResponse.setToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setExpiresTimestamp(KeyManagerUtil.getExpiresTime(validPeriod));
        tokenResponse.setExpiresIn(validPeriod);
        oAuthApplication.setAccessToken(accessToken);
        oAuthApplication.setRefreshToken(refreshToken);
        appsByClientId.put(clientId, oAuthApplication);
        return Response.status(Response.Status.OK).entity(tokenResponse).build();
    }

    @POST
    @Path("/register")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response registerClient(OAuthApplication body, @HeaderParam("Authorization")
            String authzHeader) throws KeyManagerException {
        if (authzHeader == null || authzHeader.isEmpty()) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode("900401");
            errorDTO.setMessage("Unauthorized. Authorization header not provided");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
        }
        if (applications.containsKey(body.getClientName())) {
            OAuthApplication oAuthApplication = applications.get(body.getClientName());
            body.setClientId(oAuthApplication.getClientId());
            body.setClientSecret(oAuthApplication.getClientSecret());
            return Response.status(Response.Status.CREATED).entity(body).build();
        }

        String[] decoded;
        decoded = KeyManagerUtil.extractCredentialsFromAuthzHeader(authzHeader);
        String userName = decoded[0];
        String password = decoded[1];

        if (!KeyManagerUtil.validateUser(userName, password)) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setCode("900401");
            errorDTO.setMessage("Unauthorized. Wrong Authorization header provided");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
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

    @POST
    @Path("/introspect")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    @Produces({ "application/json" })
    public Response introspect(@FormParam ("token") String token, @FormParam("token_type_hint") String tokenTypeHint) {
        OAuth2IntrospectionResponse oAuth2IntrospectionResponse = new OAuth2IntrospectionResponse();
        if (tokenTypeHint == null) {
            tokenTypeHint = "bearer";
        }

        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid input\"}").build();
        }
        if (KeyManagerUtil.validateToken(token, oAuth2IntrospectionResponse)) {
            return Response.status(Response.Status.OK).entity(oAuth2IntrospectionResponse).build();
        }
        return Response.status(Response.Status.OK).entity("{\"active\":false}").build();
    }

    private static void getData() {
        applications = KeyManagerUtil.getBackedUpData("applications.data");
        appsByClientId = KeyManagerUtil.getBackedUpData("appsByClientId.data");
        KeyManagerUtil.getBackedUpTokenData("token.data");
    }
}
