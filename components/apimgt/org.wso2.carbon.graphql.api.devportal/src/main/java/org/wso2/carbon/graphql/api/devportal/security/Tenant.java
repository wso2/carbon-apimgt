package org.wso2.carbon.graphql.api.devportal.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class Tenant implements UserDetails {

    //private String userName;
    private String tenantDomain;
    //private String password;
    //private Collection<? extends GrantedAuthority> authorities;


    public Tenant(String tenantDomain){//, List<GrantedAuthority> grantedAuthorities) {

//        this.userName = userName;
//        this.password = password;
        this.tenantDomain= tenantDomain;
        //this.authorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return tenantDomain;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    public String getUserName() {
        return null;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }
}
