package org.wso2.carbon.apimgt.api;

public interface NewPostLoginExecutor extends LoginPostExecutor{

    /**
     * get groups of user for the purpose of sharing applications and subscriptions among other users who are in
     * the  same group.
     *
     * @param response login response
     * @return String Array containing all the groups
     */
    String[] getGroupingIdentifierList(String response);
}
