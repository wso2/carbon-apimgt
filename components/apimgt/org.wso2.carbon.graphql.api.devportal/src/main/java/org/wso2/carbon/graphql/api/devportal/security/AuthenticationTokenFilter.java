package org.wso2.carbon.graphql.api.devportal.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

    public AuthenticationTokenFilter() {
        super("/graphql/**");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {

        String header = httpServletRequest.getHeader("Authorization");


        AuthenticationToken token = null;
        if (header == null ){
            token = new AuthenticationToken(null); // for the ANONYMOUS_USER
        }else if (header!=null & header.startsWith("Bearer ")){
            String authenticationToken = header.substring(7);
            token =new AuthenticationToken(authenticationToken);
        }


        return getAuthenticationManager().authenticate(token);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
