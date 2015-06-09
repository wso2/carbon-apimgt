/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hostobjects;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.*;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.apimgt.keymgt.client.APIAuthenticationServiceClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants.ApplicationStatus;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.*;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class APIStoreHostObject extends ScriptableObject {

    private static final long serialVersionUID = -3169012616750937045L;
    private static final Log log = LogFactory.getLog(APIStoreHostObject.class);
    private static final String hostObjectName = "APIStore";
    private static final String httpPort = "mgt.transport.http.port";
    private static final String httpsPort = "mgt.transport.https.port";
    private static final String hostName = "carbon.local.ip";

    private APIConsumer apiConsumer;

    private String username;

    // The zero-argument constructor used for create instances for runtime
    public APIStoreHostObject() throws APIManagementException {
        //apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
    }

    public APIStoreHostObject(String loggedUser) throws APIManagementException {
        if (loggedUser != null) {
            this.username = loggedUser;
            apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
        } else {
            apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
        }
    }

    public static void jsFunction_loadRegistryOfTenant(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj) {
        if (!isStringArray(args)) {
            return;
        }

        String tenantDomain = args[0].toString();
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error("Could not load tenant registry. Error while getting tenant id from tenant domain " +
                        tenantDomain);
            }
        }

    }
    
	/**
	 * load axis configuration for the tenant
	 * 
	 * @param cx
	 * @param thisObj
	 * @param args
	 * @param funObj
	 */
	public static void jsFunction_loadAxisConfigOfTenant(Context cx, Scriptable thisObj,
	                                                     Object[] args, Function funObj) {
		if (!isStringArray(args)) {
			return;
		}

		String tenantDomain = args[0].toString();
		if (tenantDomain != null &&
		    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
			APIUtil.loadTenantConfig(tenantDomain);
		}

	}
        			    
    

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, APIManagementException {

        if (args != null && args.length != 0) {
            String username = (String) args[0];
            return new APIStoreHostObject(username);
        }
        return new APIStoreHostObject(null);
    }

    private static String getUsernameFromObject(Scriptable obj) {
        return ((APIStoreHostObject) obj).getUsername();
    }

    private static APIConsumer getAPIConsumer(Scriptable thisObj) {
        return ((APIStoreHostObject) thisObj).getApiConsumer();
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    private static APIAuthenticationServiceClient getAPIKeyManagementClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        if (username == null || password == null) {
            handleException("Authentication credentials for API key manager unspecified");
        }

        try {
            return new APIAuthenticationServiceClient(url, username, password);
        } catch (Exception e) {
            handleException("Error while initializing the subscriber key management client", e);
            return null;
        }
    }

    public static NativeArray jsFunction_getFirstAccessTime(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }

        List<String> list = null;
        if (args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String subscriberName = (String) args[0];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getFirstAccessTime(subscriberName, 1);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for StoreAPIUsage", e);
        }
        NativeObject row = new NativeObject();

        if (!list.isEmpty()) {
            row.put("year", row, list.get(0).toString());
            row.put("month", row, list.get(1).toString());
            row.put("day", row, list.get(2).toString());
            myn.put(0, myn, row);
        }

        return myn;
    }

    public static NativeArray jsFunction_getAppApiCallType(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }

        List<AppCallTypeDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String subscriberName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getAppApiCallType(subscriberName, fromDate, toDate, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }

        int i = 0;
        if (it != null) {
            // Sort API Usage by Application Name
            NativeObject perAPICallType;

            Map<String, NativeArray> appCallTypeUsageMap = new HashMap<String, NativeArray>();

            while (it.hasNext()) {
                AppCallTypeDTO appCallTypeDTO = (AppCallTypeDTO) it.next();
                NativeArray callType = new NativeArray(0);
                perAPICallType = new NativeObject();


                List<String> callTypeList = appCallTypeDTO.getCallType();
                int j = 0;
                for (String type : callTypeList) {
                    callType.put(j, callType, type);
                    j++;

                }

                perAPICallType.put("apiName", perAPICallType, appCallTypeDTO.getApiName());
                perAPICallType.put("callType", perAPICallType, callType);


                if (appCallTypeUsageMap.containsKey(appCallTypeDTO.getappName()) && appCallTypeUsageMap != null) {
                    NativeArray apiCallType = appCallTypeUsageMap.get(appCallTypeDTO.getappName());

                    apiCallType.put(apiCallType.size(), apiCallType, perAPICallType);
                } else {

                    NativeArray apiCalltype = new NativeArray(0);
                    apiCalltype.put(0, apiCalltype, perAPICallType);
                    appCallTypeUsageMap.put(appCallTypeDTO.getappName(), apiCalltype);
                }
            }

            for (Map.Entry entry : appCallTypeUsageMap.entrySet()) {
                NativeObject row = new NativeObject();
                row.put("appName", row, entry.getKey());
                row.put("apiCallTypeArray", row, entry.getValue());

                myn.put(i, myn, row);
                i++;
            }

        }
        return myn;
    }

    public static NativeArray jsFunction_getPerAppAPIFaultCount(Context cx, Scriptable thisObj,
                                                                Object[] args, Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }

        List<APIResponseFaultCountDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String subscriberName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getPerAppFaultCount(subscriberName, fromDate, toDate, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for faultCount", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }

        int i = 0;
        if (it != null) {
            // Sort API Usage by Application Name
            NativeObject perAPICount;

            Map<String, NativeArray> faultCountMap = new HashMap<String, NativeArray>();

            while (it.hasNext()) {
                APIResponseFaultCountDTO faultCount = (APIResponseFaultCountDTO) it.next();
                perAPICount = new NativeObject();

                perAPICount.put("apiName", perAPICount, faultCount.getApiName());
                perAPICount.put("count", perAPICount, faultCount.getCount());

                if (faultCountMap.containsKey(faultCount.getappName())) {
                    NativeArray faultCountList = faultCountMap.get(faultCount.getappName());

                    faultCountList.put(faultCountList.size(), faultCountList, perAPICount);
                } else {

                    NativeArray faultCountList = new NativeArray(0);
                    faultCountList.put(0, faultCountList, perAPICount);
                    faultCountMap.put(faultCount.getappName(), faultCountList);
                }
            }

            for (Map.Entry entry : faultCountMap.entrySet()) {
                NativeObject row = new NativeObject();
                row.put("appName", row, entry.getKey());
                row.put("apiCountArray", row, entry.getValue());

                myn.put(i, myn, row);
                i++;
            }

        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUsage(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }

        List<APIUsageDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String subscriberName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.perAppPerAPIUsage(subscriberName, fromDate, toDate, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }

        int i = 0;
        if (it != null) {
            // Sort API Usage by Application Name
            NativeObject perAPICount;

            Map<String, NativeArray> appAPIUsageMap = new HashMap<String, NativeArray>();

            while (it.hasNext()) {
                APIUsageDTO apiUsage = (APIUsageDTO) it.next();
                perAPICount = new NativeObject();

                perAPICount.put("apiName", perAPICount, apiUsage.getApiName());
                perAPICount.put("count", perAPICount, apiUsage.getCount());
                String APPNAME = apiUsage.getappName();

                if (appAPIUsageMap.containsKey(apiUsage.getappName()) && appAPIUsageMap != null) {
                    NativeArray appCountList = appAPIUsageMap.get(apiUsage.getappName());

                    appCountList.put(appCountList.size(), appCountList, perAPICount);
                } else {

                    NativeArray appCountList = new NativeArray(0);
                    appCountList.put(0, appCountList, perAPICount);
                    appAPIUsageMap.put(apiUsage.getappName(), appCountList);
                }
            }

            for (Map.Entry entry : appAPIUsageMap.entrySet()) {
                NativeObject row = new NativeObject();
                row.put("appName", row, entry.getKey());
                row.put("apiCountArray", row, entry.getValue());

                myn.put(i, myn, row);
                i++;
            }

        }
        return myn;
    }

    public static NativeArray jsFunction_getTopAppUsers(Context cx, Scriptable thisObj,
                                                        Object[] args, Function funObj)
            throws APIManagementException {
        List<AppUsageDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }
        String subscriberName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getTopAppUsers(subscriberName, fromDate, toDate, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }

        int i = 0;
        if (it != null) {
            // Sort API Usage by Application Name
            NativeObject userCount;


            List<String> appNames = new ArrayList<String>();
            List<NativeArray> appUsageList = new ArrayList<NativeArray>();

            while (it.hasNext()) {
                AppUsageDTO appUsageDTO = (AppUsageDTO) it.next();
                userCount = new NativeObject();


                userCount.put("user", userCount, appUsageDTO.getUserid());
                userCount.put("count", userCount, appUsageDTO.getCount());


                if (appNames.contains(appUsageDTO.getappName())) {

                    int index = appNames.indexOf(appUsageDTO.getappName());
                    NativeArray userCountList = appUsageList.get(index);

                    userCountList.put(userCountList.size(), userCountList, userCount);
                } else {
                    appNames.add(appUsageDTO.getappName());
                    NativeArray userCountList = new NativeArray(0);
                    userCountList.put(0, userCountList, userCount);
                    appUsageList.add(userCountList);

                }
            }

            for (String appName : appNames) {
                NativeObject row = new NativeObject();
                row.put("appName", row, appName);
                row.put("userCountArray", row, appUsageList.get(i));

                myn.put(i, myn, row);
                i++;
            }

        }
        return myn;
    }

    public static NativeArray jsFunction_getPerAppSubscribers(Context cx, Scriptable thisObj,
                                                              Object[] args, Function funObj)
            throws APIManagementException {
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }

        List<AppRegisteredUsersDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        String subscriberName = (String) args[0];
        String fromDate = (String) args[1];
        String toDate = (String) args[2];
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getAppRegisteredUsers(subscriberName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }

        int i = 0;
        if (it != null) {
            // Sort API Usage by Application Name


            Map<String, NativeArray> appUsersMap = new HashMap<String, NativeArray>();

            while (it.hasNext()) {
                AppRegisteredUsersDTO appUser = (AppRegisteredUsersDTO) it.next();


                if (appUsersMap.containsKey(appUser.getappName()) && appUsersMap != null) {
                    NativeArray userList = appUsersMap.get(appUser.getappName());

                    userList.put(userList.size(), userList, appUser.getUser());
                } else {

                    NativeArray userList = new NativeArray(0);
                    userList.put(0, userList, appUser.getUser());
                    appUsersMap.put(appUser.getappName(), userList);
                }
            }

            for (Map.Entry entry : appUsersMap.entrySet()) {
                NativeObject row = new NativeObject();
                row.put("appName", row, entry.getKey());
                row.put("userArray", row, entry.getValue());

                myn.put(i, myn, row);
                i++;
            }

        }
        return myn;
    }

    public static String jsFunction_getAuthServerURL(Context cx, Scriptable thisObj,
                                                     Object[] args, Function funObj) throws APIManagementException {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }
        return url;
    }

    public static String jsFunction_getHTTPsURL(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj)
            throws APIManagementException {

        String hostName = null;
        if (args != null && isStringArray(args)) {
            hostName = (String) args[0];
            URI uri;
            try {
                uri = new URI(hostName);
                hostName = uri.getHost();
            } catch (URISyntaxException e) {
                //ignore
            }
        }

        if (hostName == null) {
            hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        }
        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }
        String backendHttpsPort = HostObjectUtils.getBackendPort("https");
        return "https://" + hostName + ":" + backendHttpsPort;

    }

    public static String jsFunction_getHTTPURL(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws APIManagementException {
        return "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort);
    }

    /*
     * getting key for API subscriber args[] list String subscriberID, String
	 * api, String apiVersion, String Date
	 */
    public static String jsFunction_getKey(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && isStringArray(args)) {
            APIInfoDTO apiInfo = new APIInfoDTO();
            apiInfo.setProviderId((String) args[0]);
            apiInfo.setApiName((String) args[1]);
            apiInfo.setVersion((String) args[2]);
            apiInfo.setContext((String) args[3]);
            try {
                SubscriberKeyMgtClient keyMgtClient = HostObjectUtils.getKeyManagementClient();
                return keyMgtClient.getAccessKey((String) args[5], apiInfo, (String) args[4], (String) args[6], (String) args[7]);
            } catch (Exception e) {
                String msg = "Error while obtaining access tokens";
                handleException(msg, e);
                return null;
            }
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

    /**
     * This method is responsible to create oAuth Application and Application keys for a given APIM application
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of properties and for
     *                performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @return this will return response of oAuthApplication registration.
     * @throws ScriptException
     * @throws APIManagementException
     * @throws ParseException
     */
    public static NativeObject jsFunction_getApplicationKey(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        if (args != null && args.length != 0) {
            NativeArray accessAllowDomainsArr = (NativeArray) args[4]; // args[4] is not mandatory
            String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
            for (Object domain : accessAllowDomainsArr.getIds()) {
                int index = (Integer) domain;
                accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
            }
            try {
                String validityPeriod = (String) args[5];
	            String scopes = (String) args[7];
	            String username = String.valueOf(args[0]);
	            String tenantDomain = MultitenantUtils.getTenantDomain(username);
	            int tenantId =
			            ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
			                                  .getTenantId(tenantDomain);

                if (null == validityPeriod || validityPeriod.isEmpty()) { // In case a validity period is unspecified
                    long defaultValidityPeriod = getApplicationAccessTokenValidityPeriodInSeconds();

                    if (defaultValidityPeriod < 0) {
                        validityPeriod = String.valueOf(Long.MAX_VALUE);
                    }
                    else {
                        validityPeriod = String.valueOf(defaultValidityPeriod);
                    }
                }

                String jsonParams = null;
                if(args.length == 10){
                    jsonParams = (String) args[9];
                }else{
                    jsonParams = null;
                }


	            //checking for authorized scopes
	            Set<Scope> scopeSet = new LinkedHashSet<Scope>();
	            List<Scope> authorizedScopes = new ArrayList<Scope>();
	            String authScopeString;
	            APIConsumer apiConsumer = getAPIConsumer(thisObj);
	            if (scopes != null && scopes.length() != 0 &&
	                !scopes.equals(APIConstants.OAUTH2_DEFAULT_SCOPE)) {
		            scopeSet.addAll(apiConsumer.getScopesByScopeKeys(scopes, tenantId));
		            authorizedScopes = getAllowedScopesForUserApplication(username, scopeSet);
	            }

	            if (!authorizedScopes.isEmpty()) {
		            StringBuilder scopeBuilder = new StringBuilder();
		            for (Scope scope : authorizedScopes) {
			            scopeBuilder.append(scope.getKey()).append(" ");
		            }
		            authScopeString = scopeBuilder.toString();
	            } else {
		            authScopeString = APIConstants.OAUTH2_DEFAULT_SCOPE;
	            }

	            String applicationName = (String) args[1];
                String tokenType = (String) args[2];
                String callbackUrl = (String) args[3]; 
                String groupingId = (String)args[8];
        
                Map<String, Object> keyDetails = getAPIConsumer(thisObj).requestApprovalForApplicationRegistration(
                        username, applicationName, tokenType, callbackUrl,
		                accessAllowDomainsArray, validityPeriod, authScopeString, groupingId,
                        jsonParams);

                NativeObject row = new NativeObject();
                String authorizedDomains = "";
                boolean first = true;
                for (String anAccessAllowDomainsArray : accessAllowDomainsArray) {
                    if (first) {
                        authorizedDomains = anAccessAllowDomainsArray;
                        first = false;
                    } else {
                        authorizedDomains = authorizedDomains + ", " + anAccessAllowDomainsArray;
                    }
                }

                Set<Map.Entry<String, Object>> entries = keyDetails.entrySet();

                for (Map.Entry<String, Object> entry : entries) {
                    row.put(entry.getKey(), row, entry.getValue());
                }

                boolean isRegenarateOptionEnabled = true;
                if (getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                    isRegenarateOptionEnabled = false;
                }
                row.put("enableRegenarate", row, isRegenarateOptionEnabled);
                row.put("accessallowdomains", row, authorizedDomains);
                return row;
            } catch (Exception e) {
                String msg = "Error while obtaining the application access token for the application:" + args[1];
                log.error(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }


    /**
     * This method is responsible for update given oAuthApplication.
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of
     *                properties and for performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @return this will return response of oAuthApplication registration.
     * @throws ScriptException
     * @throws APIManagementException
     * @throws ParseException
     */
    public static NativeObject jsFunction_updateAuthClient(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)
            throws ScriptException, APIManagementException, ParseException {

        if (args != null && args.length != 0) {
            NativeArray accessAllowDomainsArr = (NativeArray) args[4]; // args[4] is not mandatory
            String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
            for (Object domain : accessAllowDomainsArr.getIds()) {
                int index = (Integer) domain;
                accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
            }
            try {
                String validityPeriod = (String) args[5];
                String scopes = (String) args[7];
                String username = String.valueOf(args[0]);
                String tenantDomain = MultitenantUtils.getTenantDomain(username);
                int tenantId =
                        ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                .getTenantId(tenantDomain);

                if (null == validityPeriod || validityPeriod.isEmpty()) { // In case a validity period is unspecified
                    long defaultValidityPeriod = getApplicationAccessTokenValidityPeriodInSeconds();

                    if (defaultValidityPeriod < 0) {
                        validityPeriod = String.valueOf(Long.MAX_VALUE);
                    } else {
                        validityPeriod = String.valueOf(defaultValidityPeriod);
                    }
                }

                String jsonParams = null;
                if (args.length == 10) {
                    jsonParams = (String) args[9];
                } else {
                    jsonParams = null;
                }


                //checking for authorized scopes
                Set<Scope> scopeSet = new LinkedHashSet<Scope>();
                List<Scope> authorizedScopes = new ArrayList<Scope>();
                String authScopeString;
                APIConsumer apiConsumer = getAPIConsumer(thisObj);
                if (scopes != null && scopes.length() != 0 &&
                    !scopes.equals(APIConstants.OAUTH2_DEFAULT_SCOPE)) {
                    scopeSet.addAll(apiConsumer.getScopesByScopeKeys(scopes, tenantId));
                    authorizedScopes = getAllowedScopesForUserApplication(username, scopeSet);
                }

                if (!authorizedScopes.isEmpty()) {
                    StringBuilder scopeBuilder = new StringBuilder();
                    for (Scope scope : authorizedScopes) {
                        scopeBuilder.append(scope.getKey()).append(" ");
                    }
                    authScopeString = scopeBuilder.toString();
                } else {
                    authScopeString = APIConstants.OAUTH2_DEFAULT_SCOPE;
                }

                String applicationName = (String) args[1];
                String tokenType = (String) args[2];
                String callbackUrl = (String) args[3];
                String groupingId = (String) args[8];

                OAuthApplicationInfo applicationInfo = getAPIConsumer(thisObj).updateAuthClient(
                        username, applicationName, tokenType, callbackUrl,
                        accessAllowDomainsArray, validityPeriod, authScopeString, groupingId,
                        jsonParams);

                NativeObject row = new NativeObject();

                if (applicationInfo != null) {
                    row.put(APIConstants.FrontEndParameterNames.CONSUMER_KEY, row, applicationInfo.getClientId());
                    row.put(APIConstants.FrontEndParameterNames.CONSUMER_SECRET, row, applicationInfo.getClientSecret());
                    row.put(APIConstants.FrontEndParameterNames.CALLBACK_URL, row, applicationInfo.getCallBackURL());
                    row.put(APIConstants.FrontEndParameterNames.CLIENT_DETAILS, row, applicationInfo.getJsonString());
                }

                return row;
            } catch (Exception e) {
                String msg = "Error while obtaining the application access token for the application:" + args[1];
                log.error(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

    /**
     * This method is responsible for deleting oAuthApplication by consumerKey.
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of
     *                properties and for performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @throws ScriptException
     * @throws APIManagementException
     * @throws ParseException
     */
    public static void jsFunction_deleteAuthApplication(Context cx, Scriptable thisObj,
                                                        Object[] args, Function funObj)
            throws ScriptException, APIManagementException, ParseException {

        if (args != null && args.length != 0) {
            NativeObject argsData = (NativeObject) args[0];
            //consumer key of oAuthApplication
            String consumerKey = (String) argsData.get("consumerKey", argsData);
            //delete oAuthApplication
            getAPIConsumer(thisObj).deleteOAuthApplication(consumerKey);
        } else {
            handleException("Invalid input parameters given while trying to delete auth application.");
        }
    }

    /**
     * This method is responsible semi-manual client registration.
     *
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of
     *                properties and for performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @throws ScriptException
     * @throws APIManagementException
     * @throws ParseException
     */
    public static void jsFunction_mapExistingOauthClient(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws ScriptException, APIManagementException, ParseException {
        if (args != null && args.length != 0) {

            try {
                NativeObject apiData = (NativeObject) args[0];

                //this parameter will hold oAuthApplication properties that required to create new oAuthApplication.
                String jsonString = (String) apiData.get("jsonParams", apiData);
                //logged in user name.
                String userName = (String) apiData.get("username", apiData);
                //this is consumer key of the oAuthApplication.
                String clientId = (String) apiData.get("client_id", apiData);
                //APIM application name.
                String applicationName = (String) apiData.get("applicationName", apiData);

                String keyType = (String) apiData.get("keytype", apiData);
                String authorizedDomains = (String) apiData.get("authorizedDomains", apiData);
                //this map will hold response that we are getting from Application registration process.
                Map<String, Object> keyDetails;
                getAPIConsumer(thisObj).mapExistingOAuthClient(jsonString, userName, clientId, applicationName,
                                                               keyType, new String[]{"ALL"});

            } catch (Exception e) {
                handleException("Error while obtaining the application access token for the application" + e
                        .getMessage(), e);
            }
        } else {
            handleException("Invalid input parameters.");
        }

    }


    /**
     * This method is responsible delete records from application registration table and key mapping table
     * If user had wrong inputs and he is unable to continue using create key method he has to delete it and re-create.
     *
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of
     *                properties and for performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @throws ScriptException
     * @throws APIManagementException
     * @throws ParseException
     */
    public static void jsFunction_deleteFromApplicationRegistration(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws ScriptException, APIManagementException, ParseException {
        if (args != null && args.length != 0) {

            try {

                String applicationId = (String) args[0];
                String keyType = (String) args[1];

                //this map will hold response that we are getting from Application registration process.
                Map<String, Object> keyDetails;
                getAPIConsumer(thisObj).deleteFromApplicationRegistration(applicationId, keyType);

            } catch (Exception e) {
                handleException("Error while obtaining the application access token for the application" + e
                        .getMessage(), e);
            }
        } else {
            handleException("Invalid input parameters.");
        }

    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {
        if (args == null || args.length == 0 || !isStringArray(args)) {
            handleException("Invalid input parameters for the login method");
        }

        String username = (String) args[0];
        String password = (String) args[1];

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        NativeObject row = new NativeObject();

        try {
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            //update permission cache before validate user
            int tenantId =  ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            PermissionUpdateUtil.updatePermissionTree(tenantId);

            String host = new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Login failed.Please recheck the username and password and try again.");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            String usernameWithDomain = APIUtil.getLoggedInUserInfo(sessionCookie, url).getUserName();
            usernameWithDomain = APIUtil.setDomainNameToUppercase(usernameWithDomain);

            boolean isSuperTenant = false;

            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                isSuperTenant = true;
            } else {
                usernameWithDomain = usernameWithDomain + "@" + tenantDomain;
            }

            boolean authorized =
                    APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_SUBSCRIBE);
            boolean displayPublishUrlFromStore = false;
            if (config != null) {
                displayPublishUrlFromStore = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SHOW_API_PUBLISHER_URL_FROM_STORE));
            }
            boolean loginUserHasPublisherAccess = false;
            if (displayPublishUrlFromStore) {
                loginUserHasPublisherAccess = APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_CREATE) ||
                        APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_PUBLISH);
            }


            if (authorized) {
                //We will clear recently added API cache when logged in.
                HostObjectUtils.invalidateRecentlyAddedAPICache(username);
                row.put("user", row, usernameWithDomain);
                row.put("sessionId", row, sessionCookie);
                row.put("isSuperTenant", row, isSuperTenant);
                row.put("error", row, false);
                row.put("hasPublisherAccess", row, loginUserHasPublisherAccess);
            } else {
                handleException("Login failed.Insufficient Privileges.");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        }

        return row;
    }

    /**
     * This method is used to update the permission cache from jaggery side. user name should be passed as a parameter
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return
     * @throws APIManagementException
     */
    public static boolean jsFunction_updatePermissionCache(Context cx, Scriptable thisObj,
                                                           Object[] args, Function funObj)throws APIManagementException {
        if (args==null || args.length == 0) {
            handleException("Invalid input parameters to the login method");
        }

        boolean updated=false;
        try{
            String username = (String) args[0];

            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId =  ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            PermissionUpdateUtil.updatePermissionTree(tenantId);
            updated = true;
        } catch (Exception e) {
            log.error("Error while updating permissions", e);
        }
        return updated;
    }

    /**
     * Given a base 64 encoded username:password string,
     * this method checks if said user has enough privileges to advance a workflow.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     * @throws WorkflowException
     */
    public static NativeObject jsFunction_validateWFPermission(Context cx, Scriptable thisObj,
                                                               Object[] args, Function funObj) throws ScriptException,
            APIManagementException {
        if (args == null || args.length == 0 || !isStringArray(args)) {
            throw new APIManagementException("Invalid input parameters for authorizing workflow progression.");
        }

        NativeObject row = new NativeObject();

        String reqString = (String) args[0];
        String authType = reqString.split("\\s+")[0];
        String encodedString = reqString.split("\\s+")[1];
        if (!HttpTransportProperties.Authenticator.BASIC.equals(authType)) {
            //throw new APIManagementException("Invalid Authorization Header Type");
            row.put("error", row, true);
            row.put("statusCode", row, 401);
            row.put("message", row, "Invalid Authorization Header Type");
            return row;
        }

        byte[] decoded = Base64.decodeBase64(encodedString.getBytes());

        String decodedString = new String(decoded);

        if (decodedString.isEmpty() || !decodedString.contains(":")) {
            //throw new APIManagementException("Invalid number of arguments. Please provide a valid username and password.");
            row.put("error", row, true);
            row.put("statusCode", row, 401);
            row.put("message", row, "Invalid Authorization Header Value");
            return row;
        }

        String username = decodedString.split(":")[0];
        String password = decodedString.split(":")[1];

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        //String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        //if (url == null) {
        //    throw new APIManagementException("API key manager URL unspecified");
        //}

        try {

            RealmService realmService = OAuthComponentServiceHolder.getRealmService();

            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(MultitenantUtils.getTenantDomain(username));

            org.wso2.carbon.user.api.UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            Boolean authStatus = userStoreManager.authenticate(username, password);

            if (!authStatus) {
                //throw new WorkflowException("Please recheck the username and password and try again.");
                row.put("error", row, true);
                row.put("statusCode", row, 401);
                row.put("message", row, "Authentication Failure. Please recheck username and password");
                return row;
            }

            String tenantDomain = MultitenantUtils.getTenantDomain(username);

            String usernameWithDomain = APIUtil.setDomainNameToUppercase(username);

            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                usernameWithDomain = usernameWithDomain + "@" + tenantDomain;
            }

            boolean authorized = APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_WORKFLOWADMIN);

            if (authorized) {
                row.put("error", row, false);
                row.put("statusCode", row, 200);
                row.put("message", row, "Authorization Successful");
                return row;
            } else {
                //handleException("Login failed! Insufficient Privileges.");
                row.put("error", row, true);
                row.put("statusCode", row, 403);
                row.put("message", row, "Forbidden. User not authorized to perform action");
                return row;
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("statusCode", row, 500);
            row.put("message", row, e.getMessage());
            return row;
        }
    }

    public static NativeArray jsFunction_getTopRatedAPIs(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getTopRatedAPIs(limit);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting Top Rated APIs Information", e);
                return myn;
            } catch (Exception e) {
                log.error("Error while getting Top Rated APIs Information", e);
                return myn;
            }
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("description", row, api.getDescription());
                row.put("rates", row, api.getRating());
                myn.put(i, myn, row);
                i++;
            }

        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_getRecentlyAddedAPIs(Context cx,
                                                              Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args != null && args.length != 0) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            String requestedTenantDomain = (String) args[1];
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            if (requestedTenantDomain == null){
                requestedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            try {
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
                }
                apiSet = apiConsumer.getRecentlyAddedAPIs(limit, requestedTenantDomain);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting Recently Added APIs Information", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting Recently Added APIs Information", e);
                return apiArray;
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("isAdvertiseOnly",currentApi,api.isAdvertiseOnly());
                if(api.isAdvertiseOnly()){
                    currentApi.put("owner",currentApi,APIUtil.replaceEmailDomainBack(api.getApiOwner()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }

        }// end of the if
        return apiArray;
    }

    

    public static NativeObject jsFunction_searchPaginatedAPIsByType(Context cx,
                                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        NativeObject resultObj = new NativeObject();
        Map<String, Object> result = new HashMap<String, Object>();
        if (args != null && args.length > 3) {
            String searchValue = (String) args[0];
            String tenantDomain = (String) args[1];
            int start = Integer.parseInt((String) args[2]);
            int end = Integer.parseInt((String) args[3]);
            String searchTerm;
            String searchType = null;
            Set<API> apiSet = null;
            boolean noSearchTerm = false;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                } else {
                	tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

                }
                if (searchValue.contains(":")) {
                    if (searchValue.split(":").length > 1) {
                        searchType = searchValue.split(":")[0];
                        searchTerm = searchValue.split(":")[1];
                        if( !APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchType)){
                        if (!searchTerm.endsWith("*")) {
                            searchTerm = searchTerm + "*";
                        }if (!searchTerm.startsWith("*")) {
                            searchTerm = "*"+searchTerm ;
                        }
                        }
                        result = apiConsumer.searchPaginatedAPIs(searchTerm, searchType, tenantDomain, start, end);
                    } else {
                        noSearchTerm = true;
                    }

                } else {
                    if (!searchValue.endsWith("*")) {
                        searchValue = searchValue + "*";
                    }if (!searchValue.startsWith("*")) {
                        searchValue = "*"+searchValue ;
                    }
                    result = apiConsumer.searchPaginatedAPIs(searchValue, "Name", tenantDomain, start, end);
                }

            } catch (APIManagementException e) {
                log.error("Error while searching APIs by type", e);
                return resultObj;
            } catch (Exception e) {
                log.error("Error while searching APIs by type", e);
                return resultObj;
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (noSearchTerm) {
                throw new APIManagementException("Search term is missing. Try again with valid search query.");
            }
            if (result != null) {
            	if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchType)) {
            		Map<Documentation, API> apiDocMap = new HashMap<Documentation, API>();
            		apiDocMap = (Map<Documentation, API>)result.get("apis");
            		if (apiDocMap != null) {
            			int i = 0;
            			for (Map.Entry<Documentation, API> entry : apiDocMap.entrySet()) {
            				Documentation doc = entry.getKey();
            				API api = entry.getValue();
            				APIIdentifier apiIdentifier = api.getId();
            				
            				NativeObject currentApi = new NativeObject();
            				
            				currentApi.put("name", currentApi, apiIdentifier.getApiName());
	                        currentApi.put("provider", currentApi,
	                                APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
	                        currentApi.put("version", currentApi,
	                                apiIdentifier.getVersion());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("rates", currentApi, api.getRating());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("endpoint", currentApi, api.getUrl());
	                        if (api.getThumbnailUrl() == null) {
	                            currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
	                        } else {
	                            currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
	                        }
	                        currentApi.put("visibility", currentApi, api.getVisibility());
	                        currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("docName", currentApi, doc.getName());
	                        currentApi.put("docSummary", currentApi, doc.getSummary());
	                        currentApi.put("docSourceURL", currentApi, doc.getSourceUrl());
	                        currentApi.put("docFilePath", currentApi, doc.getFilePath());
	
	                        apiArray.put(i, apiArray, currentApi);
            				i++;
            			}
            			resultObj.put("apis", resultObj, apiArray);
	                    resultObj.put("totalLength", resultObj, result.get("length"));
            		}
            		
            	} else {
	                apiSet = (Set<API>) result.get("apis");
	                if (apiSet != null) {
	                    Iterator it = apiSet.iterator();
	                    int i = 0;
	                    while (it.hasNext()) {
	
	                        NativeObject currentApi = new NativeObject();
	                        Object apiObject = it.next();
	                        API api = (API) apiObject;
	                        APIIdentifier apiIdentifier = api.getId();
	                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
	                        currentApi.put("provider", currentApi,
	                                APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
	                        currentApi.put("version", currentApi,
	                                apiIdentifier.getVersion());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("rates", currentApi, api.getRating());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("endpoint", currentApi, api.getUrl());
	                        if (api.getThumbnailUrl() == null) {
	                            currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
	                        } else {
	                            currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
	                        }
	                        currentApi.put("visibility", currentApi, api.getVisibility());
	                        currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
	                        currentApi.put("description", currentApi, api.getDescription());
	                        currentApi.put("isAdvertiseOnly", currentApi, api.isAdvertiseOnly());
	                        currentApi.put("apiOwner", currentApi, api.getApiOwner());
	                        
	                        apiArray.put(i, apiArray, currentApi);
	                        i++;
	                    }
	                    resultObj.put("apis", resultObj, apiArray);
	                    resultObj.put("totalLength", resultObj, result.get("length"));
	                }
            }
            }

        }// end of the if
        return resultObj;
    }

    public static boolean jsFunction_isSelfSignupEnabled() {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        return Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
    }
    
    public static boolean jsFunction_isSelfSignupEnabledForTenant(Context cx,
        Scriptable thisObj, Object[] args, Function funObj) {
    	
    	boolean status = false;
    	if (!isStringArray(args)) {
            return status;
        }

        String tenantDomain = args[0].toString();
        try {
	        UserRegistrationConfigDTO signupConfig =
	        		SelfSignUpUtil.getSignupConfiguration(tenantDomain);
            if (signupConfig != null) {
                status = signupConfig.isSignUpEnabled();
            }
        } catch (APIManagementException e) {
	       log.error("error while loading configuration from registry", e);
        }
    	
		return status;
    	
    }

    public static NativeArray jsFunction_getAPIsWithTag(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String tagName = args[0].toString();
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getAPIsWithTag(tagName);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting APIs With Tag Information", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return apiArray;
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                            APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                            apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi,
                                "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi,
                                APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    currentApi.put("description", currentApi, api.getDescription());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
            }

        }// end of the if
        return apiArray;
    }

    public static NativeObject jsFunction_getPaginatedAPIsWithTag(Context cx,
                                                                  Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        NativeObject resultObj = new NativeObject();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (args != null && isStringArray(args)) {
            String tagName = args[0].toString();
            int start = Integer.parseInt(args[1].toString());
            int end = Integer.parseInt(args[2].toString());
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                resultMap = apiConsumer.getPaginatedAPIsWithTag(tagName, start, end);
                apiSet = (Set<API>) resultMap.get("apis");
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting APIs With Tag Information", e);
                return resultObj;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return resultObj;
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                            APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                            apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi,
                                "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi,
                                APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    currentApi.put("description", currentApi, api.getDescription());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
                resultObj.put("apis", resultObj, apiArray);
                resultObj.put("totalLength", resultObj, resultMap.get("length"));
            }

        }// end of the if
        return resultObj;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                Set<API> apiSet = apiConsumer.getTopRatedAPIs(limit);
                if (apiSet != null) {
                    Iterator it = apiSet.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        NativeObject currentApi = new NativeObject();
                        Object apiObject = it.next();
                        API api = (API) apiObject;
                        APIIdentifier apiIdentifier = api.getId();
                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
                        currentApi.put("provider", currentApi,
                                apiIdentifier.getProviderName());
                        currentApi.put("version", currentApi,
                                apiIdentifier.getVersion());
                        currentApi.put("description", currentApi, api.getDescription());
                        currentApi.put("rates", currentApi, api.getRating());
                        apiArray.put(i, apiArray, currentApi);
                        i++;
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error while getting API list", e);
                return apiArray;
            }
        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getAllTags(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray tagArray = new NativeArray(0);
        Set<Tag> tags;
        String tenantDomain = null;
        if (args != null && isStringArray(args)) {
            tenantDomain = args[0].toString();
        }
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            tags = apiConsumer.getAllTags(tenantDomain);
        } catch (APIManagementException e) {
            log.error("Error from registry while getting API tags.", e);
            return tagArray;
        } catch (Exception e) {
            log.error("Error while getting API tags", e);
            return tagArray;
        }
        if (tags != null) {
            Iterator tagsI = tags.iterator();
            int i = 0;
            while (tagsI.hasNext()) {

                NativeObject currentTag = new NativeObject();
                Object tagObject = tagsI.next();
                Tag tag = (Tag) tagObject;

                currentTag.put("name", currentTag, tag.getName());
                currentTag.put("count", currentTag, tag.getNoOfOccurrences());

                tagArray.put(i, tagArray, currentTag);
                i++;
            }
        }

        return tagArray;
    }

    public static NativeArray jsFunction_getTagsWithAttributes(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws APIManagementException {


        NativeArray tagArray = new NativeArray(0);
        Set<Tag> tags;
        String tenantDomain = null;

        if (args != null && isStringArray(args)) {
            tenantDomain = args[0].toString();
        }

        APIConsumer apiConsumer = getAPIConsumer(thisObj);

        try {
            tags = apiConsumer.getTagsWithAttributes(tenantDomain);
        } catch (APIManagementException e) {
            log.error("Error from registry while getting API tags.", e);
            return tagArray;
        } catch (Exception e) {
            log.error("Error while getting API tags", e);
            return tagArray;
        }
        if (tags != null) {
            Iterator tagsI = tags.iterator();
            int i = 0;
            while (tagsI.hasNext()) {

                NativeObject currentTag = new NativeObject();
                Object tagObject = tagsI.next();
                Tag tag = (Tag) tagObject;

                currentTag.put("name", currentTag, tag.getName());
                currentTag.put("description", currentTag, tag.getDescription());
                currentTag.put("isThumbnailExists", currentTag, tag.isThumbnailExists());
                currentTag.put("count", currentTag, tag.getNoOfOccurrences());

                tagArray.put(i, tagArray, currentTag);
                i++;
            }
        }

        return tagArray;
    }

    public static NativeArray jsFunction_getAllPublishedAPIs(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        Set<API> apiSet;
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain;
            if (args != null) {
                tenantDomain = (String) args[0];
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiSet = apiConsumer.getAllPublishedAPIs(tenantDomain);

        } catch (APIManagementException e) {
            log.error("Error from Registry API while getting API Information", e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting API Information", e);
            return myn;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        if (apiSet != null) {
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("context", row, api.getContext());
                row.put("status", row, "Deployed"); // api.getStatus().toString()
                if (api.getThumbnailUrl() == null) {
                    row.put("thumbnailurl", row, "images/api-default.png");
                } else {
                    row.put("thumbnailurl", row, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                row.put("visibility", row, api.getVisibility());
                row.put("visibleRoles", row, api.getVisibleRoles());
                row.put("description", row, api.getDescription());
                String apiOwner = api.getApiOwner();
                if (apiOwner == null) {
                    apiOwner = APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                }
                row.put("apiOwner", row, apiOwner);
                row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }
    
    public static NativeObject jsFunction_getAllPaginatedPrototypedAPIs(Context cx,
            							Scriptable thisObj, Object[] args, Function funObj)
            										throws ScriptException, APIManagementException {
    	APIConsumer apiConsumer = getAPIConsumer(thisObj);
    	String tenantDomain;
    	boolean retuenAPItags = false;
        if (args[0] != null) {
        	tenantDomain = (String) args[0];
        } else {
        	tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
            
        int start = Integer.parseInt((String) args[1]);
        int end = Integer.parseInt((String) args[2]);
        
        if (args.length > 3 && args[3] != null) {
            retuenAPItags = Boolean.parseBoolean((String) args[3]);
        }
            
        return getPaginatedAPIsByStatus(apiConsumer, tenantDomain, start, end, APIConstants.PROTOTYPED, retuenAPItags);
    	
    }

    public static NativeObject jsFunction_getAllPaginatedPublishedAPIs(Context cx,
                                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
    	
    	APIConsumer apiConsumer = getAPIConsumer(thisObj);
    	String tenantDomain;
    	boolean returnAPItags = false;
        if (args[0] != null) {
        	tenantDomain = (String) args[0];
        } else {
        	tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        int start = Integer.parseInt((String) args[1]);
        int end = Integer.parseInt((String) args[2]);
        
        if (args.length > 3 && args[3] != null) {
            returnAPItags = Boolean.parseBoolean((String) args[3]);
        }
            
        return getPaginatedAPIsByStatus(apiConsumer, tenantDomain, start, end, APIConstants.PUBLISHED, returnAPItags);
            
            
    }
    
    private static NativeObject getPaginatedAPIsByStatus(APIConsumer apiConsumer, String tenantDomain, int start, 
    		int end, String status, boolean returnAPItags) {
    	
    	Set<API> apiSet;
        Map<String, Object> resultMap;
        NativeArray myn = new NativeArray(0);
        NativeObject result = new NativeObject();
        
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);

            }
            resultMap = apiConsumer.getAllPaginatedAPIsByStatus(tenantDomain, start, end, status, returnAPItags);

        } catch (APIManagementException e) {
            log.error("Error from Registry API while getting API Information", e);
            return result;
        } catch (Exception e) {
            log.error("Error while getting API Information", e);
            return result;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();


        }
        if (resultMap != null) {
            apiSet = (Set<API>) resultMap.get("apis");
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("context", row, api.getContext());
                    row.put("status", row, "Deployed"); // api.getStatus().toString()
                    if (api.getThumbnailUrl() == null) {
                        row.put("thumbnailurl", row, "images/api-default.png");
                    } else {
                        row.put("thumbnailurl", row, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    row.put("visibility", row, api.getVisibility());
                    row.put("visibleRoles", row, api.getVisibleRoles());
                    row.put("description", row, api.getDescription());
                    String apiOwner = APIUtil.replaceEmailDomainBack(api.getApiOwner());
                    if (apiOwner == null) {
                        apiOwner = APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                    }
                    row.put("apiOwner", row, apiOwner);
                    row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                    row.put("apiBusinessOwner", row, api.getBusinessOwner());
                    
                    NativeArray tierArr = new NativeArray(0);
                    Set<Tier> tierSet = api.getAvailableTiers();
                    if (tierSet != null) {
                        Iterator tierIt = tierSet.iterator();
                        int j = 0;
                        while (tierIt.hasNext()) {
                            Object tierObject = tierIt.next();
                            Tier tier = (Tier) tierObject;
                            tierArr.put(j, tierArr, tier.getName());
                            j++;
                        }
                    }
                    row.put("tiers", row, tierArr);
                    
                    if (returnAPItags) {                    
                        StringBuilder tagsSet = new StringBuilder("");
                        for (int k = 0; k < api.getTags().toArray().length; k++) {
                            tagsSet.append(api.getTags().toArray()[k].toString());
                            if (k != api.getTags().toArray().length - 1) {
                                tagsSet.append(",");
                            }
                        }
                        row.put("tags", row, tagsSet.toString());
                    }
                    myn.put(i, myn, row);
                    i++;
                }
                result.put("apis", result, myn);
                result.put("totalLength", result, resultMap.get("totalLength"));

            }
        }
        return result;
    }
    
    /**
     * Returns all the Gateway Endpoint URLs of a given API
     * 
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return List of Gateway Endpoint URLs of the API
     * @throws ScriptException
     * @throws APIManagementException
     */
    public static NativeArray jsFunction_getAPIEndpointURLs(Context cx, Scriptable thisObj, Object[] args,
                                                            Function funObj) throws ScriptException,
                                                                            APIManagementException {
        String providerName;
        String apiName;
        String version;
        String userName;

        NativeArray myn = new NativeArray(0);

        if (args != null && args.length > 3) {
            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            apiName = (String) args[1];
            version = (String) args[2];
            userName = (String) args[3];

            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            API api = apiConsumer.getAPI(apiIdentifier);

            Map<String, String> domains = new HashMap<String, String>();

            domains = apiConsumer.getTenantDomainMappings(MultitenantUtils.getTenantDomain(userName));
            if (domains != null && domains.size() > 0) {
                int index = 0;
                Iterator it = domains.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String domainValue = (String) pair.getValue();
                    if (domainValue.endsWith("/")) {
                        domainValue = domainValue.substring(0, domainValue.length() - 1);
                    }
                    String contextWithoutTenant =
                                                  api.getContext()
                                                     .replace("/t/" + MultitenantUtils.getTenantDomain(userName), "");
                    myn.put(index, myn, domainValue + contextWithoutTenant);
                    if (api.isDefaultVersion()) {
                        contextWithoutTenant = contextWithoutTenant.replace(version + "/", "");
                        myn.put(++index, myn, domainValue + contextWithoutTenant);
                    }
                    index++;
                }
            } else {
                JSONObject environmentsObject = getEnvironmentsOfAPI(api);
                JSONObject productionEnvironmentObjects = (JSONObject) environmentsObject.get("production");
                JSONObject sandboxEnvironmentObjects = (JSONObject) environmentsObject.get("sandbox");
                JSONObject hybridEnvironmentObjects = (JSONObject) environmentsObject.get("hybrid");
                int envCount = 0;
                if (productionEnvironmentObjects != null && !productionEnvironmentObjects.isEmpty()) {
                 envCount = createAPIEndpointsPerType(productionEnvironmentObjects, api, version, myn, envCount, "production");
                }
                if (sandboxEnvironmentObjects != null && !sandboxEnvironmentObjects.isEmpty()) {
                    envCount = createAPIEndpointsPerType(sandboxEnvironmentObjects, api, version, myn, envCount, "sandbox");
                }
                if (hybridEnvironmentObjects != null && !hybridEnvironmentObjects.isEmpty()) {
                    envCount = createAPIEndpointsPerType(hybridEnvironmentObjects, api, version, myn, envCount, "hybrid");
                }
            }

        }
        return myn;
    }

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String providerName;
        String apiName;
        String version;
        String username = null;
        boolean isSubscribed = false;
        NativeArray myn = new NativeArray(0);
        if (args != null && args.length != 0) {

            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            apiName = (String) args[1];
            version = (String) args[2];
            if (args[3] != null) {
                username = (String) args[3];
            }
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            API api;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }

                api = apiConsumer.getAPI(apiIdentifier);
                if (username != null) {
                    //TODO @sumedha : remove hardcoded tenant Id
                    isSubscribed = apiConsumer.isSubscribed(apiIdentifier, username);
                }

                if (api != null) {
                    NativeObject row = new NativeObject();
                    apiIdentifier = api.getId();
                    row.put("name", row, apiIdentifier.getApiName());
                    row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("description", row, api.getDescription());
                    row.put("rates", row, api.getRating());
                    row.put("endpoint", row, api.getUrl());
                    row.put("wsdl", row, api.getWsdlUrl());
                    row.put("wadl", row, api.getWadlUrl());
                    DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z");
                    String dateFormatted = dateFormat.format(api.getLastUpdated());
                    row.put("updatedDate", row, dateFormatted);
                    row.put("context", row, api.getContext());
                    row.put("status", row, api.getStatus().getStatus());

                    String user = getUsernameFromObject(thisObj);
                    if (user != null) {
                        int userRate = apiConsumer.getUserRating(apiIdentifier, user);
                        row.put("userRate", row, userRate);
                    }
                    row.put("serverURL", row, getEnvironmentsOfAPI(api).toJSONString());
                    NativeArray tierArr = new NativeArray(0);
                    Set<Tier> tierSet = api.getAvailableTiers();
                    if (tierSet != null) {
                        Iterator it = tierSet.iterator();
                        int j = 0;

                        while (it.hasNext()) {
                            NativeObject tierObj = new NativeObject();
                            Object tierObject = it.next();
                            Tier tier = (Tier) tierObject;
                            tierObj.put("tierName", tierObj, tier.getName());
                            tierObj.put("tierDisplayName", tierObj, tier.getDisplayName());
                            tierObj.put("tierDescription", tierObj,
                                    tier.getDescription() != null ? tier.getDescription() : "");
                            if (tier.getTierAttributes() != null) {
                                Map<String, Object> attributes;
                                attributes = tier.getTierAttributes();
                                String attributesList = "";
                                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                                    attributesList += attribute.getKey() + "::" + attribute.getValue() + ",";

                                }
                                tierObj.put("tierAttributes", tierObj, attributesList);
                            }
                            tierArr.put(j, tierArr, tierObj);
                            j++;

                        }
                    }
                    row.put("tiers", row, tierArr);
                    row.put("subscribed", row, isSubscribed);
                    if (api.getThumbnailUrl() == null) {
                        row.put("thumbnailurl", row, "images/api-default.png");
                    } else {
                        row.put("thumbnailurl", row, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    row.put("bizOwner", row, api.getBusinessOwner());
                    row.put("bizOwnerMail", row, api.getBusinessOwnerEmail());
                    row.put("techOwner", row, api.getTechnicalOwner());
                    row.put("techOwnerMail", row, api.getTechnicalOwnerEmail());
                    row.put("visibility", row, api.getVisibility());
                    row.put("visibleRoles", row, api.getVisibleRoles());

                    Set<URITemplate> uriTemplates = api.getUriTemplates();
                    List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
                    if (uriTemplates.size() != 0) {
                        NativeArray uriTempArr = new NativeArray(uriTemplates.size());
                        Iterator i = uriTemplates.iterator();

                        while (i.hasNext()) {
                            List<String> utArr = new ArrayList<String>();
                            URITemplate ut = (URITemplate) i.next();
                            utArr.add(ut.getUriTemplate());
                            utArr.add(ut.getMethodsAsString().replaceAll("\\s", ","));
                            utArr.add(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                            utArr.add(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                            NativeArray utNArr = new NativeArray(utArr.size());
                            for (int p = 0; p < utArr.size(); p++) {
                                utNArr.put(p, utNArr, utArr.get(p));
                            }
                            uriTemplatesArr.add(utNArr);
                        }

                        for (int c = 0; c < uriTemplatesArr.size(); c++) {
                            uriTempArr.put(c, uriTempArr, uriTemplatesArr.get(c));
                        }

                        myn.put(1, myn, uriTempArr);
                    }
                    row.put("uriTemplates", row, uriTemplatesArr.toString());
                    String apiOwner = api.getApiOwner();
                    if (apiOwner == null) {
                        apiOwner = APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
                    }
                    row.put("apiOwner", row, apiOwner);
                    row.put("isAdvertiseOnly", row, api.isAdvertiseOnly());
                    row.put("redirectURL", row, api.getRedirectURL());

                    row.put("subscriptionAvailability", row, api.getSubscriptionAvailability());
                    row.put("subscriptionAvailableTenants", row, api.getSubscriptionAvailableTenants());
                    row.put("isDefaultVersion",row,api.isDefaultVersion());
                    row.put("transports",row,api.getTransports());

                    myn.put(0, myn, row);
                }


            } catch (APIManagementException e) {
                handleException("Error from Registry API while getting get API Information on " + apiName, e);

            } catch (Exception e) {
                handleException(e.getMessage(), e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return myn;
    }
    
    /**
     * Returns all the HTTPs Gateway Endpoint URLs of all the Gateway Endpoints
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return List of HTTPs Gateway Endpoint URLs
     * @throws ScriptException
     * @throws APIManagementException
     */
    public static NativeArray jsFunction_getHTTPsGatewayEndpointURLs(Context cx, Scriptable thisObj,
                                         Object[] args, Function funObj) throws ScriptException,
                                         APIManagementException {
    	NativeArray myn = new NativeArray(0);
    	
    	APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        
        int index = 0;
        for (Environment environment : environments.values()) {
        	String apiGatewayEndpoints = environment.getApiGatewayEndpoint();

        	List<String> urlsList = new ArrayList<String>();
        	urlsList.addAll(Arrays.asList(apiGatewayEndpoints.split(",")));
        	ListIterator<String> it = urlsList.listIterator();
        	
        	while (it.hasNext()) {
        		String url = it.next();
                if (url != null && url.startsWith("https:")) {
                	myn.put(index, myn, url);
                	index ++;
                }
        	}
        }
        
        return myn;
    }

    private static String filterUrlsByTransport(List<String> urlsList, List<String> transportList,
                                                String transportName) {
        String endpointUrl = "";
        if (transportList.contains(transportName)) {
       for (String env : urlsList){
                if (env.startsWith(transportName + ":")) {
                    endpointUrl = env;
                }
            }
        }
        return endpointUrl;
    }

    public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
            APIManagementException {

        String username = null;
        if (args != null && args.length != 0) {
            String providerName = (String) args[0];
            String apiName = (String) args[1];
            String version = (String) args[2];
            if (args[3] != null) {
                username = (String) args[3];
            }
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            return username != null && apiConsumer.isSubscribed(apiIdentifier, username);
        } else {
            throw new APIManagementException("No input username value.");
        }
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        boolean isTenantFlowStarted = false;
        List<Documentation> doclist = null;
        String providerName = APIUtil.replaceEmailDomain((String) args[0]);
        String apiName = (String) args[1];
        String version = (String) args[2];
        String username = (String) args[3];
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        NativeArray myn = new NativeArray(0);
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !org.wso2.carbon.utils.multitenancy.MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            doclist = apiConsumer.getAllDocumentation(apiIdentifier, username);
            if (doclist != null) {
                Iterator it = doclist.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object docObject = it.next();
                    Documentation documentation = (Documentation) docObject;
                    Object objectSourceType = documentation.getSourceType();
                    String strSourceType = objectSourceType.toString();
                    row.put("name", row, documentation.getName());
                    row.put("sourceType", row, strSourceType);
                    row.put("summary", row, documentation.getSummary());
                    String content;
                    if (strSourceType.equals("INLINE")) {
                        content = apiConsumer.getDocumentationContent(apiIdentifier, documentation.getName());
                        row.put("content", row, content);
                    }
                    row.put("sourceUrl", row, documentation.getSourceUrl());
                    row.put("filePath", row, documentation.getFilePath());
                    DocumentationType documentationType = documentation.getType();
                    row.put("type", row, documentationType.getType());

                    if (documentationType == DocumentationType.OTHER) {
                        row.put("otherTypeName", row, documentation.getOtherTypeName());
                    }

                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (APIManagementException e) {
            handleException("Error from Registry API while getting All Documentation on " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting All Documentation " + apiName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        Comment[] commentlist = new Comment[0];
        String providerName = "";
        String apiName = "";
        String version = "";
        if (args != null && args.length != 0) {
            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            apiName = (String) args[1];
            version = (String) args[2];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName,
                version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            commentlist = apiConsumer.getComments(apiIdentifier);
        } catch (APIManagementException e) {
            handleException("Error from registry while getting  comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting comments for " + apiName, e);
        }

        int i = 0;
        if (commentlist != null) {
            for (Comment n : commentlist) {
                NativeObject row = new NativeObject();
                row.put("userName", row, n.getUser());
                row.put("comment", row, n.getText());
                row.put("createdTime", row, n.getCreatedTime().getTime());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;

    }

    public static NativeArray jsFunction_addComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String commentStr = "";
        if (args != null && args.length != 0 && isStringArray(args)) {
            providerName = APIUtil.replaceEmailDomain((String) args[0]);
            apiName = (String) args[1];
            version = (String) args[2];
            commentStr = (String) args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.addComment(apiIdentifier, commentStr, getUsernameFromObject(thisObj));
        } catch (APIManagementException e) {
            handleException("Error from registry while adding comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while adding comments for " + apiName, e);
        }

        int i = 0;
        NativeObject row = new NativeObject();
        row.put("userName", row, providerName);
        row.put("comment", row, commentStr);
        myn.put(i, myn, row);

        return myn;
    }

    public static String jsFunction_addSubscription(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            return "";
        }

        APIConsumer apiConsumer = getAPIConsumer(thisObj);

        String providerName = (String) args[0];
        providerName = APIUtil.replaceEmailDomain(providerName);
        String apiName = (String) args[1];
        String version = (String) args[2];
        String tier = (String) args[3];
        int applicationId = ((Number) args[4]).intValue();
        String userId = (String) args[5];
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

	        /* Validation for allowed throttling tiers*/
            API api = apiConsumer.getAPI(apiIdentifier);
            Set<Tier> tiers = api.getAvailableTiers();

            Iterator<Tier> iterator = tiers.iterator();
            boolean isTierAllowed = false;
            List<String> allowedTierList = new ArrayList<String>();
            while (iterator.hasNext()) {
                Tier t = iterator.next();
                if (t.getName() != null && (t.getName()).equals(tier)) {
                    isTierAllowed = true;
                }
                allowedTierList.add(t.getName());
            }
            if (!isTierAllowed) {
                throw new APIManagementException("Tier " + tier + " is not allowed for API " + apiName + "-" + version + ". Only "
                        + Arrays.toString(allowedTierList.toArray()) + " Tiers are alllowed.");
            }
            if (apiConsumer.isTierDeneid(tier)) {
                throw new APIManagementException("Tier " + tier + " is not allowed for user " + userId);
            }
	    	/* Tenant based validation for subscription*/
            String userDomain = MultitenantUtils.getTenantDomain(userId);
            boolean subscriptionAllowed = false;
            if (!userDomain.equals(tenantDomain)) {
                String subscriptionAvailability = api.getSubscriptionAvailability();
                if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                    subscriptionAllowed = true;
                } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                    String subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
                    String allowedTenants[] = null;
                    if (subscriptionAllowedTenants != null) {
                        allowedTenants = subscriptionAllowedTenants.split(",");
                        if (allowedTenants != null) {
                            for (String tenant : allowedTenants) {
                                if (tenant != null && userDomain.equals(tenant.trim())) {
                                    subscriptionAllowed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                subscriptionAllowed = true;
            }
            if (!subscriptionAllowed) {
                throw new APIManagementException("Subscription is not allowed for " + userDomain);
            }
            apiIdentifier.setTier(tier);
            String subsStatus = apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
            return subsStatus;
        } catch (APIManagementException e) {
            handleException("Error while adding subscription for user: " + userId + ". Reason: " + e.getMessage(), e);
            return null;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public static boolean jsFunction_addAPISubscription(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj) throws APIManagementException {
        if (!isStringArray(args)) {
            throw new APIManagementException("Invalid input parameters for AddAPISubscription method");
        }

        String providerName = APIUtil.replaceEmailDomain(args[0].toString());
        String apiName = args[1].toString();
        String version = args[2].toString();
        String tier = args[3].toString();
        String applicationName = ((String) args[4]);
        String userId = args[5].toString();
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            int applicationId = APIUtil.getApplicationId(applicationName, userId);
            apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
        } catch (APIManagementException e) {
            handleException("Error while adding the subscription for user: " + userId, e);
        }
        return true;
    }

    public static boolean jsFunction_removeSubscriber(Context cx,
                                                      Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String application = "";
        String userId = "";
        if (args != null && args.length != 0) {
            providerName = (String) args[0];
            apiName = (String) args[1];
            version = (String) args[2];
            application = (String) args[3];
            userId = (String) args[4];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setApplicationId(application);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscriber(apiIdentifier, userId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while removing subscriber: " + userId, e);
            return false;
        }

    }

    public static NativeArray jsFunction_rateAPI(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && args.length != 0) {
            String providerName = APIUtil.replaceEmailDomain((String) args[0]);
            String apiName = (String) args[1];
            String version = (String) args[2];
            String rateStr = (String) args[3];
            int rate;
            try {
                rate = Integer.parseInt(rateStr.substring(0, 1));
            } catch (NumberFormatException e) {
                log.error("Rate must to be number " + rateStr, e);
                return myn;
            } catch (Exception e) {
                log.error("Error from while Rating API " + rateStr, e);
                return myn;
            }
            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
                switch (rate) {
                    //Below case 0[Rate 0] - is to remove ratings from a user
                    case 0: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ZERO, user);
                        break;
                    }
                    case 1: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ONE, user);
                        break;
                    }
                    case 2: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_TWO, user);
                        break;
                    }
                    case 3: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_THREE, user);
                        break;
                    }
                    case 4: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FOUR, user);
                        break;
                    }
                    case 5: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FIVE, user);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Can't handle " + rate);
                    }

                }
            } catch (APIManagementException e) {
                log.error("Error while Rating API " + apiName
                        + e);
                return myn;
            } catch (Exception e) {
                log.error("Error while Rating API " + apiName + e);
                return myn;
            }

            NativeObject row = new NativeObject();
            row.put("name", row, apiName);
            row.put("provider", row, APIUtil.replaceEmailDomainBack(providerName));
            row.put("version", row, version);
            row.put("rates", row, rateStr);
            row.put("newRating", row, Float.toString(apiConsumer.getAPI(apiId).getRating()));
            myn.put(0, myn, row);

        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_removeAPIRating(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && args.length != 0) {
            String providerName = APIUtil.replaceEmailDomain((String) args[0]);
            String apiName = (String) args[1];
            String version = (String) args[2];
            float rating = 0;
            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);

                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(user));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }

                apiConsumer.removeAPIRating(apiId, user);
                rating = apiConsumer.getAPI(apiId).getRating();

            } catch (APIManagementException e) {
                throw new APIManagementException("Error while remove User Rating of the API " + apiName
                        + e);

            } catch (Exception e) {
                throw new APIManagementException("Error while remove User Rating of the API  " + apiName + e);

            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
            NativeObject row = new NativeObject();
            row.put("newRating", row, Float.toString(rating));
            myn.put(0, myn, row);
        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && args.length != 0) {
            String providerName = (String) args[0];
            String apiName = (String) args[1];
            String version = (String) args[2];
            String user = (String) args[3];
            String groupingId = (String) args[4];

            APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
            Subscriber subscriber = new Subscriber(user);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> apis = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier, groupingId);
            int i = 0;
            if (apis != null) {
                for (SubscribedAPI api : apis) {
                    NativeObject row = new NativeObject();
                    row.put("application", row, api.getApplication().getName());
                    row.put("applicationId", row, api.getApplication().getId());
                    row.put("prodKey", row, getKey(api, APIConstants.API_KEY_TYPE_PRODUCTION));
                    row.put("sandboxKey", row, getKey(api, APIConstants.API_KEY_TYPE_SANDBOX));
                    ArrayList<APIKey> keys = (ArrayList<APIKey>) api.getApplication().getKeys();
                    for(APIKey key : keys){
                        row.put(key.getType()+"_KEY", row, key.getAccessToken());
                    }
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

    private static APIKey getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getKeyOfType(List<APIKey> apiKeys, String keyType) {
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }

    public static NativeObject jsFunction_createApplicationKeys(Context cx,
                                                                Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        if (args != null && args.length > 7) {
            try {
              
                String userId = (String) args[0];
                String applicationName =(String) args[1];
                String tokenType = (String) args[2];
                String tokenScope = (String) args[6];
                String groupingId = (String) args[7];
                Map<String, String> keyDetails = getAPIConsumer(thisObj).completeApplicationRegistration(userId, 
                                                                   applicationName, tokenType, tokenScope, groupingId);
                NativeObject object = new NativeObject();

                if (keyDetails != null) {
                    Iterator<Map.Entry<String, String>> entryIterator = keyDetails.entrySet().iterator();
                    Map.Entry<String, String> entry = null;
                    while (entryIterator.hasNext()) {
                        entry = entryIterator.next();
                        object.put(entry.getKey(), object, entry.getValue());
                    }
                    boolean isRegenarateOptionEnabled = true;
                    if (getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                        isRegenarateOptionEnabled = false;
                    }
                    object.put("enableRegenarate", object, isRegenarateOptionEnabled);

                }

                return object;
            } catch (APIManagementException e) {
                String msg = "Error while obtaining the application access token for the application:" + args[1];
                log.error(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

	private static List<Scope> getAllowedScopesForUserApplication(String username,
	                                                              Set<Scope> reqScopeSet) {
        String[] userRoles = null;
        org.wso2.carbon.user.api.UserStoreManager userStoreManager = null;

        List<Scope> authorizedScopes = new ArrayList<Scope>();
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                 .getTenantId(MultitenantUtils.getTenantDomain(username));
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            // Log and return since we do not want to stop issuing the token in
            // case of scope validation failures.
            log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
        }

		List<String> userRoleList = new ArrayList<String>(Arrays.asList(userRoles));

		//Iterate the requested scopes list.
		for (Scope scope : reqScopeSet) {
			//Get the set of roles associated with the requested scope.
			String roles = scope.getRoles();

			//If the scope has been defined in the context of the App and if roles have been defined for the scope
			if (roles != null && roles.length() != 0) {
				List<String> roleList =
						new ArrayList<String>(Arrays.asList(roles.replaceAll(" ", "").split(",")));
				//Check if user has at least one of the roles associated with the scope
				roleList.retainAll(userRoleList);
				if (!roleList.isEmpty()) {
					authorizedScopes.add(scope);
				}
			}
		}

		return authorizedScopes;
	}

	private static String getScopeNamesbyKey(String scopeKey, Set<Scope> availableScopeSet) {
		//convert scope keys to names
		StringBuilder scopeBuilder = new StringBuilder("");
		String prodKeyScope;

		if (scopeKey.equals(APIConstants.OAUTH2_DEFAULT_SCOPE)) {
			scopeBuilder.append("Default  ");
		} else {
			List<String> inputScopeList = new ArrayList<String>(Arrays.asList(scopeKey.split(" ")));
			String scopeName = "";
			for (String inputScope : inputScopeList) {
				for (Scope availableScope : availableScopeSet) {
					if (availableScope.getKey().equals(inputScope)) {
						scopeName = availableScope.getName();
						break;
					}
				}

                if(scopeName != null && !scopeName.isEmpty()) {
                    scopeBuilder.append(scopeName);
                    scopeBuilder.append(", ");
                    scopeName = "";
                }
			}
		}
		prodKeyScope = scopeBuilder.toString();
        if(prodKeyScope.length() > 1) {
            prodKeyScope = prodKeyScope.substring(0, prodKeyScope.length() - 2);
        }
		return prodKeyScope;
	}

    public static NativeObject jsFunction_getAllSubscriptions(Context cx,
                                                              Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args == null || args.length == 0 || !isStringArray(args)) {
            return null;
        }

        NativeArray applicationList = new NativeArray(0);
        Integer subscriptionCount = 0;
        NativeObject result = new NativeObject();
        boolean isTenantFlowStarted = false;

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        try {
            String username = args[0].toString();
            String appName = args[1].toString();
            String groupingId = null;
            if (args.length > 4 && args[4] != null) {
                groupingId = (String) args[4];
            }
            int startSubIndex = Integer.parseInt(args[2].toString());
            int endSubIndex = Integer.parseInt(args[3].toString());

            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (tenantDomain != null &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications;
            applications = apiConsumer.getApplications(new Subscriber(username), groupingId);

            if (applications != null) {
                int i = 0;
                for (Application application : applications) {

                    long startLoop = 0;
                    if (log.isDebugEnabled()) {
                        startLoop = System.currentTimeMillis();
                    }

                    NativeArray apisArray = new NativeArray(0);
                    Set<Scope> scopeSet = new LinkedHashSet<Scope>();
                    NativeArray scopesArray = new NativeArray(0);

                    if (((appName == null || appName.isEmpty()) && i == 0) ||
                        appName.equals(application.getName())) {

                        //get Number of subscriptions for the given application by the subscriber.
                        subscriptionCount = apiConsumer.getSubscriptionCount(subscriber, application.getName(), groupingId);
                        //get subscribed APIs set as per the starting and ending indexes for application.
                        Set<SubscribedAPI> subscribedAPIs;
                        subscribedAPIs = apiConsumer.getPaginatedSubscribedAPIs(subscriber, application.getName(), startSubIndex, endSubIndex, groupingId);

                        List<APIIdentifier> identifiers = new ArrayList<APIIdentifier>();
                        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                            addAPIObj(subscribedAPI, apisArray, thisObj, application);
                            identifiers.add(subscribedAPI.getApiId());

                        }

                        if (!identifiers.isEmpty()) {
                            //get scopes for subscribed apis
                            scopeSet = apiConsumer.getScopesBySubscribedAPIs(identifiers);
                            for (Scope scope : scopeSet) {
                                NativeObject scopeObj = new NativeObject();
                                scopeObj.put("scopeKey", scopeObj, scope.getKey());
                                scopeObj.put("scopeName", scopeObj, scope.getName());
                                scopesArray.put(scopesArray.getIds().length, scopesArray, scopeObj);
                            }
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("getSubscribedAPIs loop took : " +
                                  (System.currentTimeMillis() - startLoop) + "ms");
                    }


                    if (ApplicationStatus.APPLICATION_APPROVED.equals(application.getStatus())) {
                        NativeObject appObj = new NativeObject();
                        appObj.put("id", appObj, application.getId());
                        appObj.put("name", appObj, application.getName());
                        appObj.put("callbackUrl", appObj, application.getCallbackUrl());
                        APIKey prodKey = getAppKey(application, APIConstants.API_KEY_TYPE_PRODUCTION);

                        OAuthApplicationInfo prodApp = application.getOAuthApp("PRODUCTION");
                        JSONParser parser = new JSONParser();
                        JSONObject jsonObject = null;

                        String prodKeyScope = "";
                        if (prodKey != null && prodKey.getTokenScope() != null) {
                            //convert scope keys to names
                            prodKeyScope = getScopeNamesbyKey(prodKey.getTokenScope(), scopeSet);
                        }

                        boolean prodEnableRegenarateOption = true;

                        // When the Production Access Token is successfully created.
                        if (prodKey != null && prodKey.getAccessToken() != null && prodApp != null) {
                            String jsonString = prodApp.getJsonString();

                            String prodConsumerKey = prodApp.getClientId();
                            String prodConsumerSecret = prodApp.getClientSecret();
                            appObj.put("prodKey", appObj, prodKey.getAccessToken());

                            appObj.put("prodKeyScope", appObj, prodKeyScope);
                            appObj.put("prodKeyScopeValue", appObj, prodKey.getTokenScope());
                            appObj.put("prodConsumerKey", appObj, prodConsumerKey);
                            appObj.put("prodConsumerSecret", appObj, prodConsumerSecret);
                            appObj.put("prodJsonString", appObj, jsonString);


                            appObj.put("prodAuthorizedDomains", appObj, prodKey.getAuthorizedDomains());

                            if (isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                                appObj.put("prodValidityTime", appObj, -1);
                                prodEnableRegenarateOption = false;
                            } else {
                                appObj.put("prodValidityTime", appObj, prodKey.getValidityPeriod());
                            }
                            appObj.put("prodRegenerateOption", appObj, prodEnableRegenarateOption);
                            appObj.put("prodKeyState", appObj, prodKey.getState());
                        } // Prod Token is not generated, but consumer key & secret is available
                        else if (prodKey != null && prodApp != null) {
                            String jsonString = prodApp.getJsonString();
                            String prodConsumerKey = prodApp.getClientId();
                            String prodConsumerSecret = prodApp.getClientSecret();

                            appObj.put("prodKey", appObj, null);
                            appObj.put("prodKeyScope", appObj, null);
                            appObj.put("prodKeyScopeValue", appObj, null);
                            appObj.put("prodConsumerKey", appObj, prodConsumerKey);
                            appObj.put("prodConsumerSecret", appObj, prodConsumerSecret);
                            appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                            appObj.put("prodAuthorizedDomains", appObj, null);
                            appObj.put("prodJsonString", appObj, jsonString);
                            if (isApplicationAccessTokenNeverExpire(
                                    getApplicationAccessTokenValidityPeriodInSeconds())) {
                                appObj.put("prodValidityTime", appObj, -1);
                            } else {
                                appObj.put("prodValidityTime", appObj,
                                           getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                            appObj.put("prodKeyState", appObj, prodKey.getState());
                        } else {
                            appObj.put("prodKey", appObj, null);
                            appObj.put("prodKeyScope", appObj, null);
                            appObj.put("prodKeyScopeValue", appObj, null);
                            appObj.put("prodConsumerKey", appObj, null);
                            appObj.put("prodConsumerSecret", appObj, null);
                            appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                            appObj.put("prodAuthorizedDomains", appObj, null);
                            if (isApplicationAccessTokenNeverExpire(
                                    getApplicationAccessTokenValidityPeriodInSeconds())) {
                                appObj.put("prodValidityTime", appObj, -1);
                            } else {
                                appObj.put("prodValidityTime", appObj,
                                           getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                            if(prodKey != null) {
                                if (prodKey.getState() != null) {
                                    appObj.put("prodKeyState", appObj, prodKey.getState());
                                }
                            }
                            appObj.put("prodJsonString", appObj, null);

                        }

                        APIKey sandboxKey = getAppKey(application, APIConstants.API_KEY_TYPE_SANDBOX);

                        OAuthApplicationInfo sandApp = application.getOAuthApp("SANDBOX");
                        boolean sandEnableRegenarateOption = true;


                        String sandKeyScope = "";
                        if (sandboxKey != null && sandboxKey.getTokenScope() != null) {
                            //convert scope keys to names
                            sandKeyScope = getScopeNamesbyKey(sandboxKey.getTokenScope(), scopeSet);
                        }


                        if (sandboxKey != null && sandboxKey.getConsumerKey() != null && sandApp != null) {
                            String jsonString = sandApp.getJsonString();

                            String sandboxConsumerKey = sandApp.getClientId();
                            String sandboxConsumerSecret = sandApp.getClientSecret();
                            appObj.put("sandboxKey", appObj, sandboxKey.getAccessToken());

                            appObj.put("sandKeyScope", appObj, sandKeyScope);
                            appObj.put("sandKeyScopeValue", appObj, sandboxKey.getTokenScope());
                            appObj.put("sandboxConsumerKey", appObj, sandboxConsumerKey);
                            appObj.put("sandboxConsumerSecret", appObj, sandboxConsumerSecret);
                            appObj.put("sandboxKeyState", appObj, sandboxKey.getState());
                            appObj.put("sandboxJsonString", appObj, jsonString);

                            appObj.put("sandboxAuthorizedDomains", appObj, sandboxKey.getAuthorizedDomains());
                            if (isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                                appObj.put("sandValidityTime", appObj, -1);
                                sandEnableRegenarateOption = false;
                            } else {
                                appObj.put("sandValidityTime", appObj, sandboxKey.getValidityPeriod());
                            }
                            appObj.put("sandRegenarateOption", appObj, sandEnableRegenarateOption);
                        } else if (sandboxKey != null && sandApp != null) {
                            String jsonString = sandApp.getJsonString();
                            String sandboxConsumerKey = sandApp.getClientId();
                            String sandboxConsumerSecret = sandApp.getClientSecret();
                            appObj.put("sandboxKey", appObj, null);
                            appObj.put("sandKeyScope", appObj, null);
                            appObj.put("sandKeyScopeValue", appObj, null);
                            appObj.put("sandboxConsumerKey", appObj, sandboxConsumerKey);
                            appObj.put("sandboxConsumerSecret", appObj, sandboxConsumerSecret);
                            appObj.put("sandRegenarateOption", appObj, sandEnableRegenarateOption);
                            appObj.put("sandboxAuthorizedDomains", appObj, null);
                            appObj.put("sandboxKeyState", appObj, sandboxKey.getState());
                            appObj.put("sandboxJsonString", appObj, jsonString);
                            if (isApplicationAccessTokenNeverExpire(
                                    getApplicationAccessTokenValidityPeriodInSeconds())) {
                                appObj.put("sandValidityTime", appObj, -1);
                            } else {
                                appObj.put("sandValidityTime", appObj,
                                           getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                        } else {
                            appObj.put("sandboxKey", appObj, null);
                            appObj.put("sandKeyScope", appObj, null);
                            appObj.put("sandKeyScopeValue", appObj, null);
                            appObj.put("sandboxConsumerKey", appObj, null);
                            appObj.put("sandboxConsumerSecret", appObj, null);
                            appObj.put("sandRegenarateOption", appObj, sandEnableRegenarateOption);
                            appObj.put("sandboxAuthorizedDomains", appObj, null);
                            appObj.put("sandboxJsonString", appObj, null);
                            if (isApplicationAccessTokenNeverExpire(
                                    getApplicationAccessTokenValidityPeriodInSeconds())) {
                                appObj.put("sandValidityTime", appObj, -1);
                            } else {
                                appObj.put("sandValidityTime", appObj,
                                           getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                            if (sandboxKey != null) {
                                if (sandboxKey.getState() != null) {
                                    appObj.put("sandboxKeyState", appObj, sandboxKey.getState());
                                }
                            }
                        }


                        if (appName == null || appName.isEmpty() || appName.equals(application.getName())) {

                            startLoop = 0;
                            if (log.isDebugEnabled()) {
                                startLoop = System.currentTimeMillis();
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("getSubscribedAPIs loop took : " +
                                          (System.currentTimeMillis() - startLoop) + "ms");
                            }
                        }
                        appObj.put("subscriptions", appObj, apisArray);
                        appObj.put("scopes", appObj, scopesArray);

                        applicationList.put(i++, applicationList, appObj);
                        result.put("applications", result, applicationList);
                        result.put("totalLength", result, subscriptionCount);
                    }
                }
            }
        } catch (APIManagementException e) {
            handleException("Error while obtaining application data", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("jsFunction_getMySubscriptionDetail took : " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return result;
    }

    private static void addAPIObj(SubscribedAPI subscribedAPI, NativeArray apisArray,
                                  Scriptable thisObj, Application appObject) throws APIManagementException {
        NativeObject apiObj = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        try {
            API api = apiConsumer.getAPIInfo(subscribedAPI.getApiId());
            apiObj.put("name", apiObj, subscribedAPI.getApiId().getApiName());
            apiObj.put("provider", apiObj, APIUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
            apiObj.put("version", apiObj, subscribedAPI.getApiId().getVersion());
            apiObj.put("status", apiObj, api.getStatus().toString());
            apiObj.put("tier", apiObj, subscribedAPI.getTier().getDisplayName());
            apiObj.put("subStatus", apiObj, subscribedAPI.getSubStatus());
            apiObj.put("thumburl", apiObj, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
            apiObj.put("context", apiObj, api.getContext());
            //Read key from the appObject
            APIKey prodKey = getAppKey(appObject, APIConstants.API_KEY_TYPE_PRODUCTION);
            if (prodKey != null) {
                apiObj.put("prodKey", apiObj, prodKey.getAccessToken());
                apiObj.put("prodConsumerKey", apiObj, prodKey.getConsumerKey());
                apiObj.put("prodConsumerSecret", apiObj, prodKey.getConsumerSecret());
                apiObj.put("prodAuthorizedDomains", apiObj, prodKey.getAuthorizedDomains());
                if (isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                    apiObj.put("prodValidityTime", apiObj, -1);
                } else {
                    apiObj.put("prodValidityTime", apiObj, prodKey.getValidityPeriod());
                }
                //apiObj.put("prodValidityRemainingTime", apiObj, apiMgtDAO.getApplicationAccessTokenRemainingValidityPeriod(prodKey.getAccessToken()));
            } else {
                apiObj.put("prodKey", apiObj, null);
                apiObj.put("prodConsumerKey", apiObj, null);
                apiObj.put("prodConsumerSecret", apiObj, null);
                apiObj.put("prodAuthorizedDomains", apiObj, null);
                if (isApplicationAccessTokenNeverExpire(getApplicationAccessTokenValidityPeriodInSeconds())) {
                    apiObj.put("prodValidityTime", apiObj, -1);
                } else {
                    apiObj.put("prodValidityTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                }
                // apiObj.put("prodValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }

            APIKey sandboxKey = getAppKey(appObject, APIConstants.API_KEY_TYPE_SANDBOX);
            if (sandboxKey != null) {
                apiObj.put("sandboxKey", apiObj, sandboxKey.getAccessToken());
                apiObj.put("sandboxConsumerKey", apiObj, sandboxKey.getConsumerKey());
                apiObj.put("sandboxConsumerSecret", apiObj, sandboxKey.getConsumerSecret());
                apiObj.put("sandAuthorizedDomains", apiObj, sandboxKey.getAuthorizedDomains());
                if (isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                    apiObj.put("sandValidityTime", apiObj, -1);
                } else {
                    apiObj.put("sandValidityTime", apiObj, sandboxKey.getValidityPeriod());
                }
                //apiObj.put("sandValidityRemainingTime", apiObj, apiMgtDAO.getApplicationAccessTokenRemainingValidityPeriod(sandboxKey.getAccessToken()));
            } else {
                apiObj.put("sandboxKey", apiObj, null);
                apiObj.put("sandboxConsumerKey", apiObj, null);
                apiObj.put("sandboxConsumerSecret", apiObj, null);
                apiObj.put("sandAuthorizedDomains", apiObj, null);
                if (getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                    apiObj.put("sandValidityTime", apiObj, -1);
                } else {
                    apiObj.put("sandValidityTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                }
                // apiObj.put("sandValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }
            apiObj.put("hasMultipleEndpoints", apiObj, String.valueOf(api.getSandboxUrl() != null));
            apisArray.put(apisArray.getIds().length, apisArray, apiObj);
        } catch (APIManagementException e) {
            log.error("Error while obtaining application metadata", e);
        }
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && isStringArray(args)) {
            NativeObject user = new NativeObject();
            String userName = args[0].toString();
            Subscriber subscriber = null;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                subscriber = apiConsumer.getSubscriber(userName);
            } catch (APIManagementException e) {
                handleException("Error while getting Subscriber", e);
            } catch (Exception e) {
                handleException("Error while getting Subscriber", e);
            }

            if (subscriber != null) {
                user.put("name", user, subscriber.getName());
                user.put("id", user, subscriber.getId());
                user.put("email", user, subscriber.getEmail());
                user.put("subscribedDate", user, subscriber.getSubscribedDate());
                return user;
            }
        }
        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException, UserStoreException {

        if (args != null && isStringArray(args)) {
            Subscriber subscriber = new Subscriber((String) args[0]);
            String groupId = (String) args[1];
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(MultitenantUtils.getTenantDomain((String) args[0]));
                subscriber.setTenantId(tenantId);
                apiConsumer.addSubscriber(subscriber, groupId);
            } catch (APIManagementException e) {
                handleException("Error while adding the subscriber" + subscriber.getName(), e);
                return false;
            } catch (Exception e) {
                handleException("Error while adding the subscriber" + subscriber.getName(), e);
                return false;
            }
            return true;
        }
        return false;
    }

    public static NativeArray jsFunction_getApplications(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String username = args[0].toString();
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications;
            String groupId="";
            if(args.length >1 && args[1] != null){
            	 groupId = args[1].toString();
            }
            applications = apiConsumer.getApplications(new Subscriber(username), groupId);
            Subscriber subscriber = new Subscriber(username);

            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    int subscriptionCount = apiConsumer.getSubscriptionCount(subscriber,application.getName(),groupId);
                    NativeObject row = new NativeObject();
                    row.put("name", row, application.getName());
                    row.put("tier", row, application.getTier());
                    row.put("id", row, application.getId());
                    row.put("callbackUrl", row, application.getCallbackUrl());
                    row.put("status", row, application.getStatus());
                    row.put("description", row, application.getDescription());
                    row.put("apiCount", row, subscriptionCount);
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

    /**
     * This method helps to get an APIM application by given name.
     * @param cx      will be used to store information about the executing of the script.
     *                This is a object of org.mozilla.javascript.Context package.
     * @param thisObj Object of Scriptable interface provides for the management of
     *                properties and for performing conversions.
     * @param args    this will contain parameter list from jag files.
     * @param funObj  this object  provides for calling functions and constructors.
     * @return this will return response of oAuthApplication registration.
     * @throws ScriptException
     * @throws APIManagementException
     *
     */
    public static NativeObject jsFunction_getApplicationByName(Context cx,
                                                               Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        if (args != null) {
            String userId = (String) args[0];
            String applicationName = (String) args[1];
            String groupId = (String) args[2];
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application application = apiConsumer.getApplicationsByName(userId, applicationName,groupId);
            if (application != null) {
                NativeObject row = new NativeObject();

                row.put("name", row, application.getName());
                row.put("tier", row, application.getTier());
                row.put("id", row, application.getId());
                row.put("callbackUrl", row, application.getCallbackUrl());
                row.put("status", row, application.getStatus());
                row.put("description", row, application.getDescription());

                return row;
            }
        }
        return null;
    }

    public static String jsFunction_addApplication(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        String status = null;
        if (args != null && args.length >= 4 && isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            String tier = (String) args[2];
            String callbackUrl = (String) args[3];
            String description = (String) args[4];
            String groupId = null;
            if (args.length > 5 && args[5] != null) {
                groupId = (String) args[5];
            }

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Subscriber subscriber = new Subscriber(username);

            Application application = new Application(name, subscriber);
            application.setTier(tier);
            application.setCallbackUrl(callbackUrl);
            application.setDescription(description);
            if (groupId != null) {
                application.setGroupId(groupId);
            }

            status = apiConsumer.addApplication(application, username);
            return status;
        }

        return status;
    }

    public static boolean jsFunction_sleep(Context cx,
                                           Scriptable thisObj, Object[] args, Function funObj) {
        if (isStringArray(args)) {
            String millis = (String) args[0];
            try {
                Thread.sleep(Long.valueOf(millis));
            } catch (InterruptedException e) {
                log.error("Sleep Thread Interrupted");
                return false;
            }
        }
        return true;
    }

    public static boolean jsFunction_removeApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && args.length > 2 && isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            String groupingId = (String) args[2];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps;
           	apps = apiConsumer.getApplications(subscriber, groupingId);
            
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    apiConsumer.removeApplication(app);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getSubscriptionsByApplication(Context cx,
                                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args != null && isStringArray(args)) {
            String applicationName = (String) args[0];
            String username = (String) args[1];
            String groupingId = (String) args[2];
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }

                //check whether application exist prior to get subscription
                if (!APIUtil.isApplicationExist(username, applicationName, groupingId)) {
                    handleException("Application " + applicationName + " does not exist for user " +
                            "" + username);
                }

                Subscriber subscriber = new Subscriber(username);
                APIConsumer apiConsumer = getAPIConsumer(thisObj);
                Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber, applicationName, null);

                int i = 0;
                for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                    API api = apiConsumer.getAPI(subscribedAPI.getApiId());
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, subscribedAPI.getApiId().getApiName());
                    row.put("apiVersion", row, subscribedAPI.getApiId().getVersion());
                    row.put("apiProvider", row, APIUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
                    row.put("description", row, api.getDescription());
                    row.put("subscribedTier", row, subscribedAPI.getTier().getName());
                    row.put("status", row, api.getStatus().getStatus());
                    myn.put(i, myn, row);
                    i++;
                }
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_updateApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && args.length > 5 && isStringArray(args)) {
            String name = (String) args[0];
            String oldName = (String) args[1];
            String username = (String) args[2];
            String tier = (String) args[3];
            String callbackUrl = (String) args[4];
            String description = (String) args[5];
            String groupingId = null;
            if(args.length > 6 && args[6] != null){
                groupingId = (String) args[6];
            }
           
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps;
           	apps = apiConsumer.getApplications(subscriber, groupingId);
           if (apps == null || apps.length == 0) {
                return false;
            }

            Map appsMap = new HashMap();
            for (Application app : apps) {
                appsMap.put(app.getName(), app);
            }
            
            // check whether there is an app with same name
            if (!name.equals(oldName) && appsMap.containsKey(name)) {
                return false;
            }

            for (Application app : apps) {
                if (app.getName().equals(oldName)) {
                    Application application = new Application(name, subscriber);
                    application.setId(app.getId());
                    application.setTier(tier);
                    application.setCallbackUrl(callbackUrl);
                    application.setDescription(description);
                    apiConsumer.updateApplication(application);
                    return true;
                }
            }
        }

        return false;
    }

    public static NativeObject jsFunction_resumeWorkflow(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, WorkflowException {

        NativeObject row = new NativeObject();

        if (args != null && isStringArray(args)) {

            String workflowReference = (String) args[0];
            String status = (String) args[1];
            String description = null;
            if (args.length > 2 && args[2] != null) {
                description = (String) args[2];
            }

            ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

            boolean isTenantFlowStarted = false;

            try {
                if (workflowReference != null) {
                    WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReference);
                    String tenantDomain = workflowDTO.getTenantDomain();
                    if (tenantDomain != null && !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        isTenantFlowStarted = true;
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    }

                    if (workflowDTO == null) {
                        log.error("Could not find workflow for reference " + workflowReference);
                        row.put("error", row, true);
                        row.put("statusCode", row, 500);
                        row.put("message", row, "Could not find workflow for reference " + workflowReference);
                        return row;
                    }

                    workflowDTO.setWorkflowDescription(description);
                    workflowDTO.setStatus(WorkflowStatus.valueOf(status));

                    String workflowType = workflowDTO.getWorkflowType();
                    WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance()
                            .getWorkflowExecutor(workflowType);

                    workflowExecutor.complete(workflowDTO);
                    row.put("error", row, false);
                    row.put("statusCode", row, 200);
                    row.put("message", row, "Invoked workflow completion successfully.");
                }
            } catch (IllegalArgumentException e) {
                row.put("error", row, true);
                row.put("statusCode", row, 500);
                row.put("message", row, "Illegal argument provided. Valid values for status are APPROVED and REJECTED.");
            } catch (APIManagementException e) {
                row.put("error", row, true);
                row.put("statusCode", row, 500);
                row.put("message", row, "Error while resuming workflow. " + e.getMessage());
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return row;
    }

    public static boolean jsFunction_updateApplicationTier(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && isStringArray(args)) {
            String name = (String) args[0];
            String tier = (String) args[1];
            String username = (String) args[2];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber, null);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    app.setTier(tier);
                    apiConsumer.updateApplication(app);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content = null;
        NativeArray myn = new NativeArray(0);


        if (args != null && isStringArray(args)) {
        	providerName = (String) args[0];
            apiName = (String) args[1];
            version = (String) args[2];
            docName = (String) args[3];
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
                if (tenantDomain != null &&
                    !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
            	providerName = APIUtil.replaceEmailDomain(URLDecoder.decode(providerName, "UTF-8"));
            	APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                        version);
            	
                APIConsumer apiConsumer = getAPIConsumer(thisObj);
                content = apiConsumer.getDocumentationContent(apiId, docName);
            } catch (Exception e) {
                handleException("Error while getting Inline Document Content ", e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (content == null) {
                content = "";
            }

            NativeObject row = new NativeObject();
            row.put("providerName", row, providerName);
            row.put("apiName", row, apiName);
            row.put("apiVersion", row, version);
            row.put("docName", row, docName);
            row.put("content", row, content);
            myn.put(0, myn, row);

        }
        return myn;
    }

    /*
      * here return boolean with checking all objects in array is string
      */
    public static boolean isStringArray(Object[] args) {
        int argsCount = args.length;
        for (int i = 0; i < argsCount; i++) {
            if (!(args[i] instanceof String)) {
                return false;
            }
        }
        return true;

    }

    public static boolean jsFunction_hasSubscribePermission(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        APIConsumer consumer = getAPIConsumer(thisObj);
        if (consumer instanceof UserAwareAPIConsumer) {
            try {
                ((UserAwareAPIConsumer) consumer).checkSubscribePermission();
                return true;
            } catch (APIManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static void jsFunction_addUser(Context cx, Scriptable thisObj, Object[] args, Function funObj)
                                                                                    throws APIManagementException {
        String customErrorMsg = null;

        if (args != null && isStringArray(args)) {
            String username = args[0].toString();
            String password = args[1].toString();
            String fields = args[2].toString();

            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            /*
             * boolean workFlowEnabled =
             * Boolean.parseBoolean(config.getFirstProperty
             * (APIConstants.SELF_SIGN_UP_ENABLED));
             * if (!workFlowEnabled) {
             * handleException("Self sign up has been disabled on this server");
             * }
             */
            String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));

            /* fieldValues will contain values up to last field user entered */
            String fieldValues[] = fields.split("\\|");
            UserFieldDTO[] userFields = getOrderedUserFieldDTO();
            for (int i = 0; i < fieldValues.length; i++) {
                if (fieldValues[i] != null) {
                    userFields[i].setFieldValue(fieldValues[i]);
                }
            }
            /* assign empty string for rest of the user fields */
            for (int i = fieldValues.length; i < userFields.length; i++) {
                userFields[i].setFieldValue("");
            }

            boolean isTenantFlowStarted = false;

            try {

                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                // get the signup configuration
                UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);
                // set tenant specific sign up user storage
                if (signupConfig != null && signupConfig.getSignUpDomain() != "") {
                    if (!signupConfig.isSignUpEnabled()) {
                        handleException("Self sign up has been disabled for this tenant domain");
                    }
                    int index = username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                    /*
                     * if there is a different domain provided by the user other than one given in the configuration, 
                     * add the correct signup domain. Here signup domain refers to the user storage
                     */

                    if (index > 0) {
                        username =
                                   signupConfig.getSignUpDomain().toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR +
                                           username.substring(index + 1);
                    } else {
                        username =
                                   signupConfig.getSignUpDomain().toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR +
                                           username;
                    }
                }

                // check whether admin credentials are correct.
                boolean validCredentials =
                                           checkCredentialsForAuthServer(signupConfig.getAdminUserName(),
                                                                         signupConfig.getAdminPassword(), serverURL);

                if (validCredentials) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserFields(userFields);
                    userDTO.setUserName(username);
                    userDTO.setPassword(password);

                    UserRegistrationAdminServiceStub stub = new UserRegistrationAdminServiceStub(null, serverURL +
                                                                                       "UserRegistrationAdminService");
                    ServiceClient client = stub._getServiceClient();
                    Options option = client.getOptions();
                    option.setManageSession(true);

                    stub.addUser(userDTO);

                    WorkflowExecutor userSignUpWFExecutor = WorkflowExecutorFactory.getInstance()
                                                        .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);

                    WorkflowDTO signUpWFDto = new WorkflowDTO();
                    signUpWFDto.setWorkflowReference(username);
                    signUpWFDto.setStatus(WorkflowStatus.CREATED);
                    signUpWFDto.setCreatedTime(System.currentTimeMillis());
                    signUpWFDto.setTenantDomain(tenantDomain);

                    try {
                        int tenantId =
                                       ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                             .getTenantId(tenantDomain);
                        signUpWFDto.setTenantId(tenantId);
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        log.error("Error while loading Tenant ID for given tenant domain :" + tenantDomain);
                    }

                    signUpWFDto.setExternalWorkflowReference(userSignUpWFExecutor.generateUUID());
                    signUpWFDto.setWorkflowType(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
                    signUpWFDto.setCallbackUrl(userSignUpWFExecutor.getCallbackURL());

                    try {
                        userSignUpWFExecutor.execute(signUpWFDto);
                    } catch (WorkflowException e) {
                        log.error("Unable to execute User SignUp Workflow", e);
                        // removeUser(username, config, serverURL);
                        removeTenantUser(username, signupConfig, serverURL);

                        handleException("Unable to execute User SignUp Workflow", e);
                    }
                } else {
                    customErrorMsg =
                                     "Unable to add a user. Please check credentials in "
                                             + "the signup-config.xml in the registry";
                    handleException(customErrorMsg);
                }

            } catch (RemoteException e) {
                handleException(e.getMessage(), e);
            } catch (UserRegistrationAdminServiceException e) {
                handleException("Error while adding the user: " + username + ". " + e.getMessage(), e);
            } catch (WorkflowException e) {
                handleException("Error while adding the user: " + username + ". " + e.getMessage(), e);
            } catch (UserAdminUserAdminException e) {
                handleException("Error while adding the user: " + username + ". " + e.getMessage(), e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } else {
            handleException("Invalid input parameters.");
        }
    }

	public static boolean jsFunction_changePassword(Context cx, Scriptable thisObj, Object[] args,
	                                                Function funObj) throws APIManagementException {

		String username = (String) args[0];
		String currentPassword = (String) args[1];
		String newPassword = (String) args[2];

		APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
		String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
		String tenantDomain =
				MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));

		//if the current password is wrong return false and ask to retry.
		if (!isAbleToLogin(username, currentPassword, serverURL, tenantDomain)) {
			return false;
		}

		boolean isTenantFlowStarted = false;

		try {
			if (tenantDomain != null &&
			    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext()
				                       .setTenantDomain(tenantDomain, true);
			}
			// get the signup configuration
			UserRegistrationConfigDTO signupConfig =
					SelfSignUpUtil.getSignupConfiguration(tenantDomain);
			// set tenant specific sign up user storage
			if (signupConfig != null && signupConfig.getSignUpDomain() != "") {
				if (!signupConfig.isSignUpEnabled()) {
					handleException("Self sign up has been disabled for this tenant domain");
				}
			}

			changeTenantUserPassword(username, signupConfig, serverURL, newPassword);

			//if unable to login with new password
			if (!isAbleToLogin(username, newPassword, serverURL, tenantDomain)) {
				throw new APIManagementException("Password change failed");
			}

		} catch (Exception e) {
			handleException("Error while changing the password for: " + username, e);

		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return true;
	}

	/***
	 *
	 * @param username username
	 * @param signupConfig signup configuration of user
	 * @param serverURL server URL
	 * @param newPassword new password to be set.
	 *
	 */
	private static void changeTenantUserPassword(String username,
	                                             UserRegistrationConfigDTO signupConfig,
	                                             String serverURL, String newPassword)
			throws RemoteException, UserAdminUserAdminException {

		UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
		String adminUsername = signupConfig.getAdminUserName();
		String adminPassword = signupConfig.getAdminPassword();

		CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword, true,
		                                          userAdminStub._getServiceClient());

		String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
		int index = tenantAwareUserName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
		//remove the 'PRIMARY' part from the user name
		if (index > 0) {
			if (tenantAwareUserName.substring(0, index).equalsIgnoreCase(
					UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
				tenantAwareUserName = tenantAwareUserName.substring(index + 1);
			}
		}
		userAdminStub.changePassword(tenantAwareUserName, newPassword);
	}

	/***
	 *
	 * @param username username to be ckecked
	 * @param password password of the user
	 * @param serverURL server URL
	 * @param tenantDomain denant domain of the user
	 *
	 */
	private static boolean isAbleToLogin(String username, String password, String serverURL,
	                                     String tenantDomain) throws APIManagementException {

		boolean loginStatus = false;
		//String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
		if (serverURL == null) {
			handleException("API key manager URL unspecified");
		}

		try {
			AuthenticationAdminStub authAdminStub =
					new AuthenticationAdminStub(null, serverURL + "AuthenticationAdmin");
			//String tenantDomain = MultitenantUtils.getTenantDomain(username);
			//update permission cache before validate user
			int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
			                                     .getTenantId(tenantDomain);
			PermissionUpdateUtil.updatePermissionTree(tenantId);
			String host = new URL(serverURL).getHost();
			if (authAdminStub.login(username, password, host)) {
				loginStatus = true;
			}
		} catch (AxisFault axisFault) {
			log.error("Error while checking the ability to login", axisFault );
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			log.error("Error while checking the ability to login", e );
		} catch (MalformedURLException e) {
			log.error("Error while checking the ability to login", e);
		} catch (RemoteException e) {
			log.error("Error while checking the ability to login", e);
		} catch (LoginAuthenticationExceptionException e) {
			log.error("Error while checking the ability to login", e );
		}
		return loginStatus;
	}

    private static void removeUser(String username, APIManagerConfiguration config, String serverURL)
			throws RemoteException,
			UserAdminUserAdminException {
		UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
		String adminUsername = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
		String adminPassword = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);

		CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword, true,
		                                          userAdminStub._getServiceClient());
		userAdminStub.deleteUser(username);
	}

	/**
	 * remove user
	 * 
	 * @param username
	 * @param signupConfig
	 *            tenant based configuration
	 * @param serverURL
	 * @throws RemoteException
	 * @throws UserAdminUserAdminException
	 */
	private static void removeTenantUser(String username, UserRegistrationConfigDTO signupConfig,
	                                     String serverURL) throws RemoteException,
	                                     UserAdminUserAdminException {
		UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
		String adminUsername = signupConfig.getAdminUserName();
		String adminPassword = signupConfig.getAdminPassword();

		CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword, true,
		                                          userAdminStub._getServiceClient());
		String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
		int index = tenantAwareUserName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
		//remove the 'PRIMARY' part from the user name		
		if (index > 0) {
			if(tenantAwareUserName.substring(0, index)
					.equalsIgnoreCase(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)){
				tenantAwareUserName = tenantAwareUserName.substring(index + 1);
			}			
		} 

		userAdminStub.deleteUser(tenantAwareUserName);
	}

	/**
	 * check whether UserAdmin service can be accessed using the admin credentials in the 
	 * @param userName
	 * @param password
	 * @param serverURL
	 * @return
	 */
	private static boolean checkCredentialsForAuthServer(String userName, String password, String serverURL) {
		
		boolean status = false;
		try {
			UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
			CarbonUtils.setBasicAccessSecurityHeaders(userName, password, true,
	                userAdminStub._getServiceClient());
			//send a request. if exception occurs, then the credentials are not correct.
			FlaggedName[] roles = userAdminStub.getRolesOfCurrentUser();
			status = true;
		} catch (RemoteException e) {
			log.error(e);
			status = false;
		} catch (UserAdminUserAdminException e) {
			log.error("Error in checking admin credentials. Please check credentials in "
						+ "the signup-config.xml in the registry. ");
			status = false;
		}		
		return status;
	}
	
    public static boolean jsFunction_isUserExists(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
            APIManagementException, org.wso2.carbon.user.api.UserStoreException {
        if (args == null || args.length == 0) {
            handleException("Invalid input parameters to the isUserExists method");
        }

        String username = (String) args[0];
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
        UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);
        //add user storage info 
        username = SelfSignUpUtil.getDomainSpecificUserName(username, signupConfig );
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        boolean exists = false;
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            //UserRealm realm = realmService.getBootstrapRealm();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
    				.getTenantId(tenantDomain);
    		UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            if (manager.isExistingUser(tenantAwareUserName)) {
                exists = true;
            }
        } catch (UserStoreException e) {
            handleException("Error while checking user existence for " + username);
        }
        return exists;
    }

    public static boolean jsFunction_removeSubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws APIManagementException {
        if (args == null || args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        String username = (String) args[0];
        int applicationId = ((Number) args[1]).intValue();
        NativeObject apiData = (NativeObject) args[2];
        String provider = APIUtil.replaceEmailDomain((String) apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        boolean isTenantFlowStarted = false;

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            apiConsumer.removeSubscription(apiId, username, applicationId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while removing the subscription of" + name + "-" + version, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public static NativeArray  jsFunction_getPublishedAPIsByProvider(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj)
            throws APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args != null) {
            String providerName = APIUtil.replaceEmailDomain(args[0].toString());
            String username = args[1].toString();
            String limitArg = args[2].toString();
            int limit = Integer.parseInt(limitArg);
            String apiOwner = args[3].toString();
            String apiBizOwner = null;
            //If api biz-owner is not null
            if(args[4] != null){
                apiBizOwner = args[4].toString();
            }

            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            boolean isTenantFlowStarted = false;
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                apiSet = apiConsumer.getPublishedAPIsByProvider(providerName, username, limit, apiOwner, apiBizOwner);
            } catch (APIManagementException e) {
                handleException("Error while getting published APIs information of the provider - " +
                        providerName, e);
                return null;
            } catch (Exception e) {
                handleException("Error while getting published APIs information of the provider", e);
                return null;
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    int apiId = ApiMgtDAO.getAPIID(apiIdentifier, null);

                    // API is partially created/deleted. We shouldn't be showing this API.
                    if (apiId == -1) {
                        continue;
                    }
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                            APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                            apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    //Rating should retrieve from db
                    currentApi.put("rates", currentApi, ApiMgtDAO.getAverageRating(apiId));
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
            }
            return apiArray;

        } else {
            handleException("Invalid types of input parameters.");
            return null;
        }
    }

    public static NativeObject jsFunction_refreshToken(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws AxisFault, APIManagementException {

        NativeObject row = new NativeObject();
        if (args != null && args.length != 0) {
            //String tokenType = (String) args[2];
            //Token type would be default with new scopes implementation introduced in 1.7.0
            String requestedScopes = (String)args[8];
            String oldAccessToken = (String) args[3];
            NativeArray accessAllowDomainsArr = (NativeArray) args[4];
            String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
            String clientId = (String) args[5];
            String clientSecret = (String) args[6];
            String validityTime = (String) args[7];
            String[] requestedScopeArray = new String[]{requestedScopes};

            //TODO:should take JSON input as an argument.
            String jsonInput = null;

            for (Object domain : accessAllowDomainsArr.getIds()) {
                int index = (Integer) domain;
                accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
            }

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            //Check whether old access token is already available
            AccessTokenInfo response = null;
            try {
                response = apiConsumer.renewAccessToken(oldAccessToken, clientId, clientSecret,
                                                        validityTime,
                                                        accessAllowDomainsArray, requestedScopeArray, jsonInput);
            } catch (APIManagementException e) {
                String errorMessage = "Error while renewing Access Token for Consumer Key " + clientId;
                if (args[0] != null) {
                    errorMessage = "Error while renewing Access Token for Consumer Key " + clientId 
                            + " and user " + args[0];
                }
                handleException(errorMessage);
            }

            row.put("accessToken", row, response.getAccessToken());
            row.put("consumerKey", row, response.getConsumerKey());
            row.put("consumerSecret", row, response.getConsumerKey());

            row.put("responseParams", row, response.getJSONString());
            row.put("tokenScope", row, response.getScopes());

            boolean isRegenarateOptionEnabled = true;
            if (getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                isRegenarateOptionEnabled = false;
            }

            if (isApplicationAccessTokenNeverExpire(response.getValidityPeriod())) {
                row.put("validityTime", row, -1);
            } else {
                row.put("validityTime", row, response.getValidityPeriod());
            }
            row.put("enableRegenarate", row, isRegenarateOptionEnabled);
            return row;
         } else {
            handleException("Invalid types of input parameters.");
            return null;
            }
    }


    public static void jsFunction_addAccessAllowDomains(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj) throws APIManagementException {
        String oAuthConsumerKey = args[0].toString();
        NativeArray accessAllowDomainsArr = (NativeArray) args[1];
        String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];

        for (Object domain : accessAllowDomainsArr.getIds()) {
            int index = (Integer) domain;
            accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
        }
        try {
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            apiConsumer.addAccessAllowDomains(oAuthConsumerKey, accessAllowDomainsArray);
        } catch (APIManagementException e) {
            handleException("Error while adding allowed domains for oauth consumer: " + oAuthConsumerKey, e);
        }
    }

    public static void jsFunction_updateAccessAllowDomains(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws APIManagementException, AxisFault {
        String accessToken = (String) args[0];
        NativeArray accessAllowDomainsArr = (NativeArray) args[1];
        String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
        for (Object domain : accessAllowDomainsArr.getIds()) {
            int index = (Integer) domain;
            accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
        }
        try {
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            apiConsumer.updateAccessAllowDomains(accessToken, accessAllowDomainsArray);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * Given a name of a user the function checks whether the subscriber role is present
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     * @throws AxisFault
     */
    public static NativeObject jsFunction_checkIfSubscriberRoleAttached(Context cx, Scriptable thisObj,
                                                                        Object[] args,
                                                                        Function funObj) throws APIManagementException, AxisFault {
        String userName = (String) args[0];
        Boolean valid;

        NativeObject row = new NativeObject();
    

        if (userName != null) {
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);

            UserAdminStub userAdminStub = new UserAdminStub(null, serverURL + "UserAdmin");
            String adminUsername = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
            String adminPassword = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);

            CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                    true, userAdminStub._getServiceClient());
            
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(userName));
            UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);
            //add user storage info 
			userName = SelfSignUpUtil.getDomainSpecificUserName(userName, signupConfig );
            try {
                valid = APIUtil.checkPermissionQuietly(userName, APIConstants.Permissions.API_SUBSCRIBE);
                if (valid) {
                    row.put("error", row, false);
                    return row;
                }
            } catch (Exception e) {
                handleException(e.getMessage(), e);
                row.put("error", row, true);
                row.put("message", row, "Error while checking if " + userName + " has subscriber role.");
                return row;
            }
            row.put("error", row, true);
            row.put("message", row, "User does not have subscriber role.");
            return row;
        } else {
            row.put("error", row, true);
            row.put("message", row, "Please provide a valid username");
            return row;
        }
    }

    public static NativeArray jsFunction_getAPIUsageforSubscriber(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws APIManagementException {
        List<APIVersionUserUsageDTO> list = null;
        if (args == null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.isStatPublishingEnabled()) {
            return myn;
        }
        if (!HostObjectUtils.isUsageDataSourceSpecified()) {
            return myn;
        }
        String subscriberName = (String) args[0];
        String period = (String) args[1];

        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getUsageBySubscriber(subscriberName, period);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        } catch (Exception e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;

        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIVersionUserUsageDTO usage = (APIVersionUserUsageDTO) usageObject;
                row.put("api", row, usage.getApiname());
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                row.put("costPerAPI", row, usage.getCostPerAPI());
                row.put("cost", row, usage.getCost());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    /**
     * Check the APIs' adding comment is turned on or off
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static boolean jsFunction_isCommentActivated() throws APIManagementException {

        boolean commentActivated = false;
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        commentActivated = Boolean.valueOf(config.getFirstProperty(APIConstants.API_STORE_DISPLAY_COMMENTS));

        if (commentActivated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check the APIs' adding rating facility is turned on or off
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static boolean jsFunction_isRatingActivated() throws APIManagementException {

        boolean ratingActivated = false;
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        ratingActivated = Boolean.valueOf(config.getFirstProperty(APIConstants.API_STORE_DISPLAY_RATINGS));

        if (ratingActivated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if billing enabled else false
     * @throws APIManagementException
     */
    public static boolean jsFunction_isBillingEnabled()
            throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String billingConfig = config.getFirstProperty(APIConstants.BILLING_AND_USAGE_CONFIGURATION);
        return Boolean.parseBoolean(billingConfig);
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj,
                                                  Object[] args,
                                                  Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Set<Tier> tiers;
        try {
            //If tenant domain is present in url we will use it to get available tiers
            if (args.length > 0 && args[0] != null) {
                tiers = apiConsumer.getTiers((String) args[0]);
            } else {
                tiers = apiConsumer.getTiers();
            }

            List<Tier> tierList = APIUtil.sortTiers(tiers);
            int i = 0;
            for (Tier tier : tierList) {
                NativeObject row = new NativeObject();
                row.put("tierName", row, tier.getName());
                row.put("tierDisplayName", row, tier.getDisplayName());
                row.put("tierDescription", row,
                        tier.getDescription() != null ? tier.getDescription() : "");
                row.put("defaultTier", row, i == 0);
                myn.put(i, myn, row);
                i++;
            }

        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getDeniedTiers(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj) throws APIManagementException {

        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);

        try {
            Set<String> tiers = apiConsumer.getDeniedTiers();
            int i = 0;
            for (String tier : tiers) {
                NativeObject row = new NativeObject();
                row.put("tierName", row, tier);
                myn.put(i, myn, row);
                i++;
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getUserFields(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        UserFieldDTO[] userFields = getOrderedUserFieldDTO();
        NativeArray myn = new NativeArray(0);
        int limit = userFields.length;
        for (int i = 0; i < limit; i++) {
            NativeObject row = new NativeObject();
            row.put("fieldName", row, userFields[i].getFieldName());
            row.put("claimUri", row, userFields[i].getClaimUri());
            row.put("required", row, userFields[i].getRequired());
            myn.put(i, myn, row);
        }
        return myn;
    }

    public static boolean jsFunction_hasUserPermissions(Context cx,
                                                        Scriptable thisObj, Object[] args,
                                                        Function funObj)
            throws ScriptException, APIManagementException {
        if (args != null && isStringArray(args)) {
            String username = args[0].toString();
            return APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_SUBSCRIBE);
        } else {
            handleException("Invalid types of input parameters.");
        }
        return false;
    }

    private static UserFieldDTO[] getOrderedUserFieldDTO() {
        UserRegistrationAdminServiceStub stub;
        UserFieldDTO[] userFields = null;
        try {
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
            if (url == null) {
                handleException("API key manager URL unspecified");
            }
            stub = new UserRegistrationAdminServiceStub(null, url + "UserRegistrationAdminService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            userFields = stub.readUserFieldsForUserRegistration(UserCoreConstants.DEFAULT_CARBON_DIALECT);
            Arrays.sort(userFields, new HostObjectUtils.RequiredUserFieldComparator());
            Arrays.sort(userFields, new HostObjectUtils.UserFieldComparator());
        } catch (Exception e) {
            log.error("Error while retrieving User registration Fields", e);
        }
        return userFields;
    }


    private static void updateRolesOfUser(String serverURL, String adminUsername,
                                          String adminPassword, String userName, String role) throws Exception {
        String url = serverURL + "UserAdmin";

        UserAdminStub userAdminStub = new UserAdminStub(url);
        CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                                                  true, userAdminStub._getServiceClient());
        FlaggedName[] flaggedNames = userAdminStub.getRolesOfUser(userName, "*", -1);
        List<String> roles = new ArrayList<String>();
        if (flaggedNames != null) {
            for (int i = 0; i < flaggedNames.length; i++) {
                if (flaggedNames[i].getSelected()) {
                    roles.add(flaggedNames[i].getItemName());
                }
            }
        }
        roles.add(role);
        userAdminStub.updateRolesOfUser(userName, roles.toArray(new String[roles.size()]));
    }

    private static long getApplicationAccessTokenValidityPeriodInSeconds() {
        return OAuthServerConfiguration.getInstance().getApplicationAccessTokenValidityPeriodInSeconds();
    }

    public static NativeArray jsFunction_getActiveTenantDomains(Context cx, Scriptable thisObj,
                                                                Object[] args, Function funObj)
            throws APIManagementException {

        try {
            Set<String> tenantDomains = APIUtil.getActiveTenantDomains();
            NativeArray domains = null;
            int i = 0;
            if (tenantDomains == null || tenantDomains.size() == 0) {
                return domains;
            } else {
                domains = new NativeArray(tenantDomains.size());
                for (String tenantDomain : tenantDomains) {
                    domains.put(i, domains, tenantDomain);
                    i++;
                }
            }
            return domains;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new APIManagementException("Error while checking the APIStore is running in tenant mode or not.", e);
        }


    }

    private static boolean isApplicationAccessTokenNeverExpire(long validityPeriod) {
        return validityPeriod < 0 || validityPeriod == Long.MAX_VALUE;
    }

    public static boolean jsFunction_isEnableEmailUsername(Context cx,
                                                           Scriptable thisObj, Object[] args,
                                                           Function funObj) {
        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName"));
    }

    public static String jsFunction_getAPIPublisherURL(Context cx,
                                                       Scriptable thisObj, Object[] args,
                                                       Function funObj) {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        if (config != null) {
            return config.getFirstProperty(APIConstants.API_PUBLISHER_URL);
        }
        return null;
    }

    public static boolean jsFunction_hasPublisherAccess(Context cx,
                                                        Scriptable thisObj, Object[] args,
                                                        Function funObj) {
        String usernameWithDomain = (String) args[0];
        String tenantDomain = MultitenantUtils.getTenantDomain(usernameWithDomain);
        boolean isSuperTenant = false;
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            usernameWithDomain = usernameWithDomain + "@" + tenantDomain;
        }
        boolean displayPublishUrlFromStore = false;
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        if (config != null) {
            displayPublishUrlFromStore = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SHOW_API_PUBLISHER_URL_FROM_STORE));
        }
        boolean loginUserHasPublisherAccess = false;
        if (displayPublishUrlFromStore) {
            loginUserHasPublisherAccess = APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_CREATE) ||
                                          APIUtil.checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_PUBLISH);
        }
        return loginUserHasPublisherAccess;
    }

    public static boolean jsFunction_isDataPublishingEnabled(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws APIManagementException {
        if (HostObjectUtils.checkDataPublishingEnabled()) {
            return true;
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public APIConsumer getApiConsumer() {
        return apiConsumer;
    }

    /**
     * This method will return domain mappings of gateways if exists
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return NativeObject that contains list of domain mappings of gateways
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeObject jsFunction_getDomainMappings(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj) throws APIManagementException {
        NativeObject myn = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Map<String, String> domains = new HashMap<String, String>();
        if (args.length > 0 && args[0] != null) {
            domains = apiConsumer.getTenantDomainMappings((String) args[0]);
        }
        if(domains == null || domains.size() == 0 ){
            return null;
        }
        Iterator entries = domains.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            String key = (String) thisEntry.getKey();
            String value = (String) thisEntry.getValue();
            myn.put(key,myn,value);
        }
        return myn;
    }

    /**
     * This method is to Download API-DOCS from APIStore
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return NativeObject that contains Input stream of Downloaded File
     * @throws APIManagementException Wrapped exception by org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static NativeObject jsFunction_getDocument(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws ScriptException,
                   APIManagementException {
        if (args == null || args.length != 1 || !isStringArray(args)) {
            handleException("Invalid input parameters expected resource Url");
        }
        NativeObject data = new NativeObject();

        String username = getUsernameFromObject(thisObj);
        // Set anonymous user if no user is login to the system
        if (username == null) {
            username = APIConstants.END_USER_ANONYMOUS;
        }
        String resource = (String) args[0];
        //String tenantDomain = (String) args[0];
        Map<String, Object> docResourceMap = APIUtil.getDocument(username, resource);
        if (!docResourceMap.isEmpty()) {
            data.put("Data", data,
                     cx.newObject(thisObj, "Stream", new Object[] { docResourceMap.get("Data") }));
            data.put("contentType", data, docResourceMap.get("contentType"));
            data.put("name", data, docResourceMap.get("name"));
        }

        return data;
    }

    /**
     * This method gets the group Id of the current logged in user.
     * @param cx Rhino Context
     * @param thisObj Scriptable object
     * @param args Passing arguments
     * @param funObj Function object
     * @return String group id. 
     * 
     */
    public static String jsFunction_getGroupIds(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        String response = (String) args[0];
        APIConsumer consumer = getAPIConsumer(thisObj);
        String groupId = null;
        try {
            groupId = consumer.getGroupIds(response);
        } catch (APIManagementException e) {
        	//This is actually not an exception, that should abort the user flow. If the groupId is not available then 
        	//the flow for which the group id is not required will be run.
            log.error("Error occurred while getting group id");
        }
        return groupId;

    }

    /**
     * This method create the json object of the environments in the API
     * @param api API object of selected api .
     * @return json object of environments
     */
    private static JSONObject getEnvironmentsOfAPI(API api) {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        JSONObject environmentObject = new JSONObject();
        JSONObject productionEnvironmentObject = new JSONObject();
        JSONObject sandboxEnvironmentObject = new JSONObject();
        JSONObject hybridEnvironmentObject = new JSONObject();
        Set<String> environmentsPublishedByAPI =
                new HashSet<String>(api.getEnvironments());
        environmentsPublishedByAPI.remove("none");
        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            JSONObject jsonObject = new JSONObject();
            List<String> environmenturls = new ArrayList<String>();
            environmenturls.addAll(Arrays.asList((environment.getApiGatewayEndpoint().split(","))));
            List<String> transports = new ArrayList<String>();
            transports.addAll(Arrays.asList((api.getTransports().split(","))));
            jsonObject.put("http", filterUrlsByTransport(environmenturls, transports, "http"));
            jsonObject.put("https", filterUrlsByTransport(environmenturls, transports, "https"));
            if (APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType())) {
                productionEnvironmentObject.put(environment.getName(), jsonObject);
            } else if (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType())) {
                sandboxEnvironmentObject.put(environment.getName(), jsonObject);
            } else {
                hybridEnvironmentObject.put(environment.getName(), jsonObject);
            }
        }
        if (productionEnvironmentObject != null && !productionEnvironmentObject.isEmpty()){
            environmentObject.put(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, productionEnvironmentObject);
        }
        if (sandboxEnvironmentObject != null && !sandboxEnvironmentObject.isEmpty()){
            environmentObject.put(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxEnvironmentObject);
        }
        if (hybridEnvironmentObject != null && !hybridEnvironmentObject.isEmpty()){
            environmentObject.put(APIConstants.GATEWAY_ENV_TYPE_HYBRID, hybridEnvironmentObject);
        }
        return environmentObject;
    }

    /**
     * this method used to iterate environments according to type
     *  @param environments json
     * @param api API object of selected api .
     * @param version version of API
     * @param myn
     * @param envCount count parameter
     * @param type type of environment
     */
    private static int createAPIEndpointsPerType(JSONObject environments, API api, String version, NativeArray myn,
                                                 int envCount, String type) {
        for (Object prodKeys : environments.keySet()) {
            JSONObject environmentObject = (JSONObject) environments.get(prodKeys);
            NativeObject appObj = new NativeObject();
            appObj.put("environmentName", appObj, prodKeys);
            appObj.put("environmentType", appObj, type);
            NativeArray envs = new NativeArray(0);
            int index = 0;
            for (Object envURL : environmentObject.entrySet()) {
                envs.put(index, envs, envURL + api.getContext());
                if (api.isDefaultVersion()) {
                    String apiContext = api.getContext();
                    apiContext = apiContext.replace(version + "/", "");
                    envs.put(++index, envs, envURL + apiContext);
                }
                index++;
                appObj.put("environmentURLs", appObj, envs);
                myn.put(envCount, myn, appObj);
            }
        }
        envCount++;
        return envCount;
    }
}
