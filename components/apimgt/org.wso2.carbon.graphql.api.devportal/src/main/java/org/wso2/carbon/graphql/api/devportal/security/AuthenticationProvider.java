package org.wso2.carbon.graphql.api.devportal.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {


    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

        AuthenticationToken authenticationToken = (AuthenticationToken) usernamePasswordAuthenticationToken;
        String token = authenticationToken.getToken();

        WebAppAuthenticatorImpl webAppAuthenticator = new WebAppAuthenticatorImpl();
        AccessTokenInfo accessTokenInfo = null;
        String UserName = null;
        try {
            accessTokenInfo = webAppAuthenticator.getTokenMetaData(token);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        if (accessTokenInfo.isTokenValid()){
            UserName = accessTokenInfo.getEndUserName();//MultitenantUtils.getTenantDomain(accessTokenInfo.getEndUserName());
        }
        return new Tenant(UserName);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return (AuthenticationToken.class.isAssignableFrom(aClass));
    }
}
