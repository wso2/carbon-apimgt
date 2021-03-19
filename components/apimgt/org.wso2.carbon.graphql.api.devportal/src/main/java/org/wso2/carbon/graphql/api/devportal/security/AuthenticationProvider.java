package org.wso2.carbon.graphql.api.devportal.security;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl;

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
        if(token!=null){
            try {
                accessTokenInfo = webAppAuthenticator.getTokenMetaData(token);
                if (accessTokenInfo.isTokenValid()){
                    UserName = accessTokenInfo.getEndUserName();
                }else{
                    throw new AuthenticationException("Invalid Access Token") {
                        @Override
                        public String getMessage() {
                            return super.getMessage();
                        }
                    };
                }
            } catch (APIManagementException e) {
                //e.printStackTrace();
            }
        }
        return new User(UserName);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return (AuthenticationToken.class.isAssignableFrom(aClass));
    }
}
