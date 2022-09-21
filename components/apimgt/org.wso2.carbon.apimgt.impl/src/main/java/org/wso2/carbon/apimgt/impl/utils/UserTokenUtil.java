package org.wso2.carbon.apimgt.impl.utils;

/**
 * This Util can be used to get access token sent from UI.
 */
public class UserTokenUtil {


    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<String>();

    private UserTokenUtil() {
    }

    public static String getToken() {
        return tokenThreadLocal.get();
    }

    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }

    public static void clear(){tokenThreadLocal.remove();}

}
