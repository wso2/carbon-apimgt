package org.wso2.carbon.apimgt.impl.internal;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.apimgt.impl.AuthenticationException;

/**
 * Setting the trasnport authenticator for carbon.
 *
 * @author chamath
 */
public class ServiceAuthenticator {

    private static ServiceAuthenticator instance = null;
    private String accessUsername = null;
    private String accessPassword = null;

    private ServiceAuthenticator() {
    }

    public static ServiceAuthenticator getInstance() {

        if (instance != null) {
            return instance;
        } else {
            instance = new ServiceAuthenticator();
            return instance;
        }
    }

    public void authenticate(ServiceClient client) throws AuthenticationException {

        if (accessUsername != null && accessPassword != null) {
            Options option = client.getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(accessUsername);
            auth.setPassword(accessPassword);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);

        } else {
            throw new AuthenticationException("Authentication username or password not set");
        }
    }

    public void setAccessUsername(String accessUsername) {
        this.accessUsername = accessUsername;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }

}
