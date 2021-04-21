package org.wso2.carbon.graphql.api.devportal.security;

//import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class AuthenticationContext {

    private static final String ANONYMOUS_USER = "__wso2.am.anon__";

    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;


    public static String  getLoggedInUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username =  authentication.getName();
        if(username!=null){
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
               //when the username is an email in supertenant, it has at least 2 occurrences of '@'
                long count = username.chars().filter(ch -> ch == '@').count();
               //in the case of email, there will be more than one '@'
                boolean isEmailUsernameEnabled = Boolean.parseBoolean(CarbonUtils.getServerConfiguration().
                       getFirstProperty("EnableEmailUserName"));
                if (isEmailUsernameEnabled || (username.endsWith(SUPER_TENANT_SUFFIX) && count <= 1)) {
                    username = MultitenantUtils.getTenantAwareUsername(username);
                }
            }
        }else{
            username = ANONYMOUS_USER;

        }
        return username;

    }

    public static String getLoggedInTenanDomain(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username =  authentication.getName();
        String tenantDomain = null;
        if(username!=null){
            tenantDomain = MultitenantUtils.getTenantDomain(username);
        }else{
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        return tenantDomain;
    }
}
