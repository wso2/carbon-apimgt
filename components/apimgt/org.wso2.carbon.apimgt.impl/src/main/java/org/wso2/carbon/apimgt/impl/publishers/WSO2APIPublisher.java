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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.FileUtils;
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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;

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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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
            boolean added = addAPIToStore(api,store.getEndpoint(), store.getUsername(), httpContext,store.getDisplayName());
            if (added) {   //If API creation success,then try publishing the API
                published = publishAPIToStore(api.getId(), store.getEndpoint(), store.getUsername(), httpContext,store.getDisplayName());
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
            	deleted = deleteWSO2Store(apiId, store.getUsername(), store.getEndpoint(), httpContext,store.getDisplayName());
            	logoutFromExternalStore(store, httpContext);
            }
            return deleted;
        }
    }

    private boolean deleteWSO2Store(APIIdentifier apiId, String externalPublisher, String storeEndpoint, HttpContext httpContext,String displayName) throws APIManagementException {
        boolean deleted;
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

            }else{
                String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
                throw new APIManagementException("Error while deleting the API - "+ apiId.getApiName() +" from the external WSO2 APIStore - "+displayName  + ".Reason -"+errorMsg);
            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while deleting the API - "+ apiId.getApiName() +" from the external WSO2 APIStore - "+displayName  + "--"+e.getMessage(),e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while deleting the API - "+ apiId.getApiName() +" from the external WSO2 APIStore - "+displayName  + "--"+e.getMessage(),e);

        } catch (IOException e) {
            throw new APIManagementException("Error while deleting the API - "+ apiId.getApiName() +" from the external WSO2 APIStore - "+displayName  + "--"+e.getMessage(),e);

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
                String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
                throw new APIManagementException(" Authentication with external APIStore - "+store.getDisplayName()+ "  failed due to "+errorMsg+".API publishing to APIStore- "+store.getDisplayName()+" failed.");

            } else{
                return true;
            }

        } catch (IOException e) {
            throw new APIManagementException("Error while accessing the external store : "+ store.getDisplayName() +" : "+e.getMessage(), e);

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
                String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
                throw new APIManagementException(" Log out from external APIStore - "+store.getDisplayName()+ " failed due to -"+errorMsg);

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

    private boolean addAPIToStore(API api,String storeEndpoint,String externalPublisher, HttpContext httpContext,String displayName) throws APIManagementException {
        boolean added;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_ADD_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_ADD_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        try {
            if(api.getThumbnailUrl()!=null){
            MultipartEntity entity=getMultipartEntity(api,externalPublisher,APIConstants.API_ADD_ACTION);
            httppost.setEntity(entity);
            }
            else{
            // Request parameters and other properties.
            List<NameValuePair> params = getParamsList(api, externalPublisher, APIConstants.API_ADD_ACTION);
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            }
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            File createdTmpFile = new File("tmp/icon");//With multipart file uploading
            if(createdTmpFile.exists()){
                createdTmpFile.delete();
            }
            if (!isError) { //If API creation success
            added=true;
            }else{
            String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
            throw new APIManagementException("Error while adding the API-"+ api.getId().getApiName()+" to the external WSO2 APIStore-"+displayName+".Reason -"+errorMsg );
            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while adding the API-"+ api.getId().getApiName()+" to the external WSO2 APIStore-"+displayName  + "--"+e.getMessage(),e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while adding the API-"+ api.getId().getApiName()+" to the external WSO2 APIStore-"+displayName  + "--"+e.getMessage(),e);

        } catch (IOException e) {
            throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+displayName  + "--"+e.getMessage(),e);

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+displayName + "--"+e.getMessage(),e);
        } catch (UserStoreException e) {
        	throw new APIManagementException("Error while adding the API:"+ api.getId().getApiName()+" to the external WSO2 APIStore:"+displayName + "--"+e.getMessage(),e);
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
        	updated = updateWSO2Store(api, store.getUsername(), store.getEndpoint(), httpContext,store.getDisplayName());
        	logoutFromExternalStore(store, httpContext);
        }
        return updated;
        }
    }
    private boolean updateWSO2Store(API api, String externalPublisher, String storeEndpoint, HttpContext httpContext,String displayName) throws APIManagementException {
        boolean updated;
        HttpClient httpclient = new DefaultHttpClient();
        if(storeEndpoint.contains("/store")){
            storeEndpoint=storeEndpoint.split("store")[0]+"publisher"+APIConstants.APISTORE_ADD_URL;
        }
        else if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+APIConstants.APISTORE_ADD_URL;
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        try {
            if(api.getThumbnailUrl()!=null){
                MultipartEntity entity=getMultipartEntity(api,externalPublisher,APIConstants.API_UPDATE_ACTION);
                httppost.setEntity(entity);
            }
            else{
                // Request parameters and other properties.
                List<NameValuePair> params = getParamsList(api, externalPublisher, APIConstants.API_UPDATE_ACTION);
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            }
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            File createdTmpFile = new File("tmp/icon");//File created with multipart file uploading to store icon
            if(createdTmpFile.exists()){
                createdTmpFile.delete();
            }
            boolean isError=Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            if (!isError) {   //If API update success
                updated=true;

            } else{
                String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
                throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName +".Reason -"+errorMsg);
            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName +"--"+ e.getMessage(),e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName + "--"+e.getMessage(),e);

        } catch (IOException e) {
            throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName +"--"+ e.getMessage(),e);

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName + "--"+e.getMessage(),e);
        } catch (UserStoreException e) {
        	throw new APIManagementException("Error while updating the API- "+ api.getId().getApiName()+" in the external WSO2 APIStore- "+displayName + "--"+e.getMessage(),e);
		}
        return updated;
    }


    public boolean isAPIAvailable(API api, APIStore store) throws APIManagementException {

            boolean available = false;
            if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined. Cannot proceed with checking API availabiltiy from the APIStore - "
                		+ store.getDisplayName();
            throw new APIManagementException(msg);

            } else {
            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    		boolean authenticated = authenticateAPIM(store, httpContext);
    		if (authenticated) {
    		available = isAPIAvailableInWSO2Store(api, store.getUsername(), store.getEndpoint(), httpContext);
    		logoutFromExternalStore(store, httpContext);
    		}
    		return available;
    	}
    }

    private boolean isAPIAvailableInWSO2Store(API api, String externalPublisher, String storeEndpoint,
                                    		  HttpContext httpContext) throws APIManagementException {
        		boolean available = false;
        		HttpClient httpclient = new DefaultHttpClient();
        		if (storeEndpoint.contains("/store")) {
            	storeEndpoint = storeEndpoint.split("store")[0] + "publisher" + APIConstants.APISTORE_LIST_URL;
            	} else if (!generateEndpoint(storeEndpoint)) {
            	storeEndpoint = storeEndpoint + APIConstants.APISTORE_LIST_URL;
            	}
        		HttpPost httppost = new HttpPost(storeEndpoint);

                // Request parameters
                List<NameValuePair> paramVals = new ArrayList<NameValuePair>();
        		paramVals.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_GET_ACTION));
        		paramVals.add(new BasicNameValuePair("name", api.getId().getApiName()));
        		paramVals.add(new BasicNameValuePair("provider", externalPublisher));
        		paramVals.add(new BasicNameValuePair("version", api.getId().getVersion()));

                try {
            	httppost.setEntity(new UrlEncodedFormEntity(paramVals, "UTF-8"));
            	// Execute and get the response.
                HttpResponse response = httpclient.execute(httppost, httpContext);
            	HttpEntity entity = response.getEntity();
            	String responseString = EntityUtils.toString(entity, "UTF-8");
            	boolean isError = Boolean.parseBoolean(responseString.split(",")[0].split(":")[1].split("}")[0].trim());
            	if (!isError) { // If get API successful
                available = true;
                }
            	} catch (UnsupportedEncodingException e) {
            	throw new APIManagementException("Error while checking the API availabilty: " + api.getId().getApiName()
                             					+ " in the external WSO2 APIStore: " + storeEndpoint + e);

                } catch (ClientProtocolException e) {
            	throw new APIManagementException("Error while checking the API availabilty: " + api.getId().getApiName()
                    					+ " in the external WSO2 APIStore: " + storeEndpoint + e);

                } catch (IOException e) {
            	throw new APIManagementException("Error while checking the API availabilty: " + api.getId().getApiName()
                    					+ " in the external WSO2 APIStore: " + storeEndpoint + e);

                }
        		return available;
    }



    private boolean publishAPIToStore(APIIdentifier apiId,String storeEndpoint,String externalPublisher, HttpContext httpContext,String displayName) throws APIManagementException {
        boolean published;
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

            }else{
                String errorMsg=responseString.split(",")[1].split(":")[1].split("}")[0].trim();
                throw new APIManagementException("Error while publishing the API- "+apiId.getApiName()+" to the external WSO2 APIStore - "+displayName +".Reason -"+errorMsg);


            }
        } catch (UnsupportedEncodingException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore - "+displayName + "--"+e.getMessage(),e);

        } catch (ClientProtocolException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore - "+displayName + "--"+e.getMessage(),e);

        } catch (IOException e) {
            throw new APIManagementException("Error while publishing the API: "+apiId.getApiName()+" to the external WSO2 APIStore - "+displayName  + "--"+e.getMessage(),e);

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

    private List<NameValuePair> getParamsList(API api,String externalPublisher, String action) throws APIManagementException, UserStoreException{
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
        
        
        String tenantDomain = MultitenantUtils.getTenantDomain(
        		APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                getTenantManager().getTenantId(tenantDomain);

        params.add(new BasicNameValuePair("redirectURL", getExternalStoreRedirectURL(tenantId)));
        
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
    
    private String getExternalStoreRedirectURL(int tenantId) throws APIManagementException {
    	UserRegistry registry;
    	 String redirectURL = null;
		try {
			registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                redirectURL = element.getFirstChildWithName(new QName(APIConstants.EXTERNAL_API_STORES_STORE_URL)).getText();
			}
			return redirectURL;
		} catch (RegistryException e) {
			String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
		}
         
    }

    private MultipartEntity getMultipartEntity(API api,String externalPublisher, String action) throws org.wso2.carbon.registry.api.RegistryException, IOException {


        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        try {


        entity.addPart(APIConstants.API_ACTION, new StringBody(action));

        entity.addPart("name", new StringBody(api.getId().getApiName()));
        entity.addPart("version", new StringBody(api.getId().getVersion()));
        entity.addPart("provider", new StringBody(externalPublisher));
        entity.addPart("description", new StringBody(checkValue(api.getDescription())));
        entity.addPart("endpoint", new StringBody(checkValue(api.getUrl())));
        entity.addPart("sandbox", new StringBody(checkValue(api.getSandboxUrl())));
        entity.addPart("wsdl", new StringBody(checkValue(api.getWsdlUrl())));
        entity.addPart("wadl", new StringBody(checkValue(api.getWadlUrl())));
        entity.addPart("endpoint_config",  new StringBody(checkValue(api.getEndpointConfig())));

        URL url = new URL(getFullRegistryIconUrl(api.getThumbnailUrl()));
        File fileToUpload = new File("tmp/icon");
        if(!fileToUpload.exists()){
        fileToUpload.createNewFile();
        }
        FileUtils.copyURLToFile(url, fileToUpload);
        FileBody fileBody = new FileBody(fileToUpload, "application/octet-stream");
        entity.addPart("apiThumb", fileBody);
       // fileToUpload.delete();

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

        entity.addPart("tags", new StringBody(checkValue(tagsSet.toString())));
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
        entity.addPart("tiersCollection", new StringBody(checkValue(tiersSet.toString())));
        entity.addPart("context", new StringBody(api.getContext()));
        entity.addPart("bizOwner", new StringBody(checkValue(api.getBusinessOwner())));
        entity.addPart("bizOwnerMail", new StringBody(checkValue(api.getBusinessOwnerEmail())));
        entity.addPart("techOwnerMail", new StringBody(checkValue(api.getTechnicalOwnerEmail())));
        entity.addPart("techOwner", new StringBody(checkValue(api.getTechnicalOwner())));
        entity.addPart("visibility", new StringBody(api.getVisibility()));
        entity.addPart("roles", new StringBody(checkValue(api.getVisibleRoles())));
        entity.addPart("endpointType", new StringBody(checkValue(String.valueOf(api.isEndpointSecured()))));
        entity.addPart("epUsername", new StringBody(checkValue(api.getEndpointUTUsername())));
        entity.addPart("epPassword", new StringBody(checkValue(api.getEndpointUTPassword())));
        entity.addPart("advertiseOnly",  new StringBody("true"));

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
        entity.addPart("redirectURL",  new StringBody(checkValue(config.getFirstProperty(APIConstants.EXTERNAL_API_STORES_STORE_URL))));
        if(api.getTransports()==null){
            entity.addPart("http_checked",new StringBody(""));
            entity.addPart("https_checked", new StringBody(""));
        }else{
            String[] transports=api.getTransports().split(",");
            if(transports.length==1){
                if("https".equals(transports[0])){
                    entity.addPart("http_checked",new StringBody(""));
                    entity.addPart("https_checked", new StringBody(transports[0]));

                }else{
                    entity.addPart("https_checked",new StringBody(""));
                    entity.addPart("http_checked", new StringBody(transports[0]));
                }
            }else{
                entity.addPart("http_checked",new StringBody("http"));
                entity.addPart("https_checked", new StringBody("https"));

            }
        }
        entity.addPart("resourceCount", new StringBody(String.valueOf(api.getUriTemplates().size())));

        Iterator urlTemplate = api.getUriTemplates().iterator();
        int i=0;
        while (urlTemplate.hasNext()) {
            Object templateObject = urlTemplate.next();
            URITemplate template=(URITemplate)templateObject;
            entity.addPart("uriTemplate-" + i, new StringBody(template.getUriTemplate()));
            entity.addPart("resourceMethod-" + i, new StringBody(template.getMethodsAsString().replaceAll("\\s",",")));
            entity.addPart("resourceMethodAuthType-" + i, new StringBody(String.valueOf(template.getAuthTypeAsString().replaceAll("\\s",","))));
            entity.addPart("resourceMethodThrottlingTier-" + i, new StringBody(template.getThrottlingTiersAsString().replaceAll("\\s",",")));
            i++;
        }
        return entity;
        } catch (UnsupportedEncodingException e) {
           throw new IOException("Error while adding the API to external APIStore :",e);
        }
    }

    private static String getFullRegistryIconUrl(String postfixUrl) {
        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (postfixUrl != null && webContext != null && !webContext.equals("/")) {
            postfixUrl = webContext + postfixUrl;
        }
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        String backendHttpPort = getBackendPort("http");

        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }
        return "http://" + hostName + ":" + backendHttpPort+postfixUrl;

    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    private static String getBackendPort(String transport) {
        int port;
        String backendPort;
        ConfigurationContext context=ServiceReferenceHolder.getContextService().getServerConfigContext();

            port = CarbonUtils.getTransportProxyPort(context, transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(context, transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;

    }



}