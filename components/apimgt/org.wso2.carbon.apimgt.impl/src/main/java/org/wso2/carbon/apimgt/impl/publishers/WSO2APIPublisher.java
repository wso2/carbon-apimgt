/*
*  Copyright WSO2 Inc.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WSO2APIPublisher implements APIPublisher {
    private Log log = LogFactory.getLog(getClass());

    /**
     * The method to publish API to external WSO2 Store
     * @param api      API
     * @param store    Store
     * @return   published/not
     */

    public boolean publishToStore(API api,APIStore store) throws APIManagementException {
        boolean published = false;

        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with publishing API to the APIStore - "+store.getDisplayName();
            throw new APIManagementException(msg);
        }
        else{
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        boolean authenticated = authenticateAPIM(store,httpContext);
        if(authenticated){  //First try to login to store
            boolean added = addAPIToStore(api,store.getEndpoint(), store.getUsername(), httpContext);
            if (added) {   //If API creation success,then try publishing the API
                published = publishAPIToStore(api.getId(), store.getEndpoint(), store.getUsername(), httpContext);
            }
            logoutFromExternalStore(store, httpContext);
        }
        }
        return published;
    }

    @Override
    public boolean deleteFromStore(APIIdentifier apiId, APIStore store) throws APIManagementException {
    	boolean deleted = false;
        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with publishing API to the APIStore - " + store.getDisplayName();
            throw new APIManagementException(msg);

        } else {
            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            boolean authenticated = authenticateAPIM(store,httpContext);
            if (authenticated) {
            	deleted = deleteWSO2Store(apiId, store.getUsername(), store.getEndpoint(), httpContext);
            	logoutFromExternalStore(store, httpContext);
            }
            return deleted;
        }
    }

    private boolean deleteWSO2Store(APIIdentifier apiId, String externalPublisher, String storeEndpoint, HttpContext httpContext) throws APIManagementException {
        boolean deleted=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_DELETE_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_DELETE_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        List<NameValuePair> paramVals = new ArrayList<NameValuePair>();
        paramVals.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_REMOVE_ACTION));
        paramVals.add(new BasicNameValuePair("name", apiId.getApiName()));
        paramVals.add(new BasicNameValuePair("provider", externalPublisher));
        paramVals.add(new BasicNameValuePair("version", apiId.getVersion()));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(paramVals, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (!isError) {  //If API deletion success
            deleted=true;

            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while deleting the API : "+ apiId.getApiName() +" from the external WSO2 APIStore : "+storeEndpoint + e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while deleting the API : "+ apiId.getApiName() +" from the external WSO2 APIStore : "+storeEndpoint + e);

        } catch (IOException e) {
            throw new APIManagementException("Error while deleting the API : "+ apiId.getApiName() +" from the external WSO2 APIStore : "+storeEndpoint + e);

        }
        return deleted;
    }

    /**
     * Authenticate to external APIStore
     *
     * @param httpContext  HTTPContext
     */
    private boolean authenticateAPIM(APIStore store,HttpContext httpContext) throws APIManagementException {
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            String storeEndpoint=store.getEndpoint();
            if(store.getEndpoint().contains("/store")){
            storeEndpoint=store.getEndpoint().split("store")[0]+"publisher"+APIConstants.APISTORE_LOGIN_URL;
            }
            else if(!generateEndpoint(store.getEndpoint())){
                storeEndpoint=storeEndpoint+ APIConstants.APISTORE_LOGIN_URL;
            }
            HttpPost httppost = new HttpPost(storeEndpoint);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(APIConstants.APISTORE_LOGIN_USERNAME, store.getUsername()));
            params.add(new BasicNameValuePair(APIConstants.APISTORE_LOGIN_PASSWORD, store.getPassword()));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());


            if (isError) {
                throw new APIManagementException(" Authentication with external APIStore : "+store.getDisplayName()+ "   failed: HTTP error code : " +
                          response.getStatusLine().getStatusCode()+".Failed API publishing to APIStore: "+store.getDisplayName());

            } else{
                return true;
            }

        } catch (Exception e) {
            throw new APIManagementException("Authentication with external APIStore : "+store.getDisplayName() +" failed.", e);

        }
    }
    /**
     * Login out from external APIStore
     *
     * @param httpContext  HTTPContext
     */
    private boolean logoutFromExternalStore(APIStore store,HttpContext httpContext) throws APIManagementException {
    	try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            String storeEndpoint=store.getEndpoint();
            if(store.getEndpoint().contains("/store")){
                storeEndpoint=store.getEndpoint().split("store")[0]+"publisher"+APIConstants.APISTORE_LOGIN_URL;
            }
            else if(!generateEndpoint(store.getEndpoint())){
                storeEndpoint=storeEndpoint+ APIConstants.APISTORE_LOGIN_URL;
            }
            HttpPost httppost = new HttpPost(storeEndpoint);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_LOGOUT_ACTION));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (isError) {
                throw new APIManagementException(" Log out from external APIStore : "+store.getDisplayName()+ " failed: HTTP error code : " +
                          response.getStatusLine().getStatusCode());

            } else{
                return true;
            }

        } catch (Exception e) {
            throw new APIManagementException("Error while login out from : "+store.getDisplayName(), e);

        }
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }

    private boolean addAPIToStore(API api,String storeEndpoint,String externalPublisher, HttpContext httpContext) throws APIManagementException {
        boolean added=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_ADD_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_ADD_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        // Request parameters and other properties.
        List<NameValuePair> params =getParamsList(api, externalPublisher, APIConstants.API_ADD_ACTION);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (!isError) { //If API creation success
            added=true;
            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+storeEndpoint + e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+storeEndpoint + e);

        } catch (IOException e) {
            throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+storeEndpoint + e);

        }
        return added;
    }

    public boolean updateToStore(API api, APIStore store) throws APIManagementException {
    	boolean updated = false;
        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with publishing API to the APIStore - " + store.getDisplayName();
            throw new APIManagementException(msg);

        }
        else{
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        boolean authenticated = authenticateAPIM(store, httpContext);
        if (authenticated) {
        	updated = updateWSO2Store(api, store.getUsername(), store.getEndpoint(), httpContext);
        	logoutFromExternalStore(store, httpContext);
        }
        return updated;
        }
    }
    private boolean updateWSO2Store(API api, String externalPublisher, String storeEndpoint, HttpContext httpContext) throws APIManagementException {
        boolean updated=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_ADD_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_ADD_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        // Request parameters and other properties.
        List<NameValuePair> params = getParamsList(api, externalPublisher, APIConstants.API_UPDATE_ACTION);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (!isError) {   //If API update success
                updated=true;

            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while updating the API: "+ api.getId().getApiName()+" in the external WSO2 APIStore: "+storeEndpoint + e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while updating the API: "+ api.getId().getApiName()+" in the external WSO2 APIStore: "+storeEndpoint + e);

        } catch (IOException e) {
            throw new APIManagementException("Error while updating the API: "+ api.getId().getApiName()+" in the external WSO2 APIStore: "+storeEndpoint + e);

        }
        return updated;
    }

    private boolean publishAPIToStore(APIIdentifier apiId,String storeEndpoint,String externalPublisher, HttpContext httpContext) throws APIManagementException {
        boolean published=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_PUBLISH_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_PUBLISH_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        List<NameValuePair> paramVals = new ArrayList<NameValuePair>();
        paramVals.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_CHANGE_STATUS_ACTION));
        paramVals.add(new BasicNameValuePair("name", apiId.getApiName()));
        paramVals.add(new BasicNameValuePair("provider", externalPublisher));
        paramVals.add(new BasicNameValuePair("version", apiId.getVersion()));
        paramVals.add(new BasicNameValuePair("status", APIConstants.PUBLISHED));
        paramVals.add(new BasicNameValuePair("publishToGateway", "true"));
        paramVals.add(new BasicNameValuePair("deprecateOldVersions", "false"));
        paramVals.add(new BasicNameValuePair("requireResubscription", "false"));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(paramVals, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (!isError) {  //If API publishing success
                published=true;

            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore : "+storeEndpoint + e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore : "+storeEndpoint + e);

        } catch (IOException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore : "+storeEndpoint + e);

        }
        return published;
    }

    private boolean generateEndpoint(String inputEndpoint) {
        boolean isAbsoluteEndpoint=false;
        if(inputEndpoint.contains("/site/block/")) {
            isAbsoluteEndpoint=true;
        }
        return isAbsoluteEndpoint;
    }

    private List<NameValuePair> getParamsList(API api,String externalPublisher, String action){
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(APIConstants.API_ACTION,action));
        params.add(new BasicNameValuePair("name", api.getId().getApiName()));
        params.add(new BasicNameValuePair("version", api.getId().getVersion()));
        params.add(new BasicNameValuePair("provider", externalPublisher));
        params.add(new BasicNameValuePair("description", api.getDescription()));
        params.add(new BasicNameValuePair("endpoint", api.getUrl()));
        params.add(new BasicNameValuePair("sandbox", api.getSandboxUrl()));
        params.add(new BasicNameValuePair("wsdl", api.getWadlUrl()));
        params.add(new BasicNameValuePair("wadl", api.getWsdlUrl()));
        params.add(new BasicNameValuePair("endpoint_config", api.getEndpointConfig()));

        StringBuilder tagsSet = new StringBuilder("");

        Iterator it = api.getTags().iterator();
        int j = 0;
        while (it.hasNext()) {
            Object tagObject = it.next();
            tagsSet.append((String) tagObject);
            if (j != api.getTags().size() - 1) {
                tagsSet.append(",");
            }
            j++;
        }
        params.add(new BasicNameValuePair("tags", checkValue(tagsSet.toString())));

        StringBuilder tiersSet = new StringBuilder("");
        Iterator tier = api.getAvailableTiers().iterator();
        int k = 0;
        while (tier.hasNext()) {
            Object tierObject = tier.next();
            Tier availTier=(Tier) tierObject;
            tiersSet.append(availTier.getName());
            if (k != api.getAvailableTiers().size() - 1) {
                tiersSet.append(",");
            }
            k++;
        }
        params.add(new BasicNameValuePair("tiersCollection", checkValue(tiersSet.toString())));
        params.add(new BasicNameValuePair("context", api.getContext()));
        params.add(new BasicNameValuePair("bizOwner", api.getBusinessOwner()));
        params.add(new BasicNameValuePair("bizOwnerMail", api.getBusinessOwnerEmail()));
        params.add(new BasicNameValuePair("techOwner", api.getTechnicalOwner()));
        params.add(new BasicNameValuePair("techOwnerMail", api.getTechnicalOwnerEmail()));
        params.add(new BasicNameValuePair("visibility", api.getVisibility()));
        params.add(new BasicNameValuePair("roles", api.getVisibleRoles()));
        params.add(new BasicNameValuePair("endpointType", String.valueOf(api.isEndpointSecured())));
        params.add(new BasicNameValuePair("epUsername", api.getEndpointUTUsername()));
        params.add(new BasicNameValuePair("epPassword", api.getEndpointUTPassword()));
        
        //Setting current API provider as the owner of the externally publishing API
        params.add(new BasicNameValuePair("apiOwner", api.getId().getProviderName()));
        params.add(new BasicNameValuePair("advertiseOnly", "true"));
        
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        params.add(new BasicNameValuePair("redirectURL", config.getFirstProperty(APIConstants.EXTERNAL_API_STORES_STORE_URL)));
        
        if(api.getTransports()==null){
            params.add(new BasicNameValuePair("http_checked",null));
            params.add(new BasicNameValuePair("https_checked",null));
        }else{
            String[] transports=api.getTransports().split(",");
            if(transports.length==1){
                if("https".equals(transports[0])){
                    params.add(new BasicNameValuePair("http_checked",null));
                    params.add(new BasicNameValuePair("https_checked",transports[0]));
                }else{
                    params.add(new BasicNameValuePair("https_checked",null));
                    params.add(new BasicNameValuePair("http_checked",transports[0]));
                }
            }else{
                params.add(new BasicNameValuePair("http_checked", "http"));
                params.add(new BasicNameValuePair("https_checked", "https"));
            }
        }
        params.add(new BasicNameValuePair("resourceCount", String.valueOf(api.getUriTemplates().size())));
        Iterator urlTemplate = api.getUriTemplates().iterator();
        int i=0;
        while (urlTemplate.hasNext()) {
            Object templateObject = urlTemplate.next();
            URITemplate template=(URITemplate)templateObject;
            params.add(new BasicNameValuePair("uriTemplate-" + i, template.getUriTemplate()));
            params.add(new BasicNameValuePair("resourceMethod-" + i, template.getMethodsAsString().replaceAll("\\s",",")));
            params.add(new BasicNameValuePair("resourceMethodAuthType-" + i, template.getAuthTypeAsString().replaceAll("\\s",",")));
            params.add(new BasicNameValuePair("resourceMethodThrottlingTier-" + i, template.getThrottlingTiersAsString().replaceAll("\\s",",")));
            i++;
        }
        return params;
    }


}