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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.jaggeryjs.scriptengine.util.HostObjectUtil;
import org.jaxen.JaxenException;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.common.GovernanceArtifactFilter;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.core.UserRealm;
//import org.wso2.carbon.user.mgt.stub.AddUserUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

@SuppressWarnings("unused")
public class AssetStoreHostObject extends ScriptableObject {

    private static final long serialVersionUID = -3169012616750937045L;
    private static final Log log = LogFactory.getLog(AssetStoreHostObject.class);
    private static final String hostObjectName = "APIStore";
    private static final String httpPort = "mgt.transport.http.port";
    private static final String httpsPort = "mgt.transport.https.port";
    private static final String hostName = "carbon.local.ip";

    private static final String WEB_CONTEXT =
            ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
    private static final String SCXML_NS = "http://www.w3.org/2005/07/scxml";

    private List<Object> managers = new LinkedList<Object>();
    private Map<String, String> mediaTypeToTypeMap = new HashMap<String, String>();

    private String username;
    private Registry registry;
    private Registry rootRegistry;

    public String getUsername() {
        return username;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    // The zero-argument constructor used for create instances for runtime
    public AssetStoreHostObject() throws APIManagementException {
        //this(null);
    }

    private void createRegistry(String username) throws RegistryException {
        Registry governanceRegistry;
        Registry rootRegistry;
        if (username != null) {
            governanceRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(
                            username);
            rootRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getRegistry(username);
        } else {
            governanceRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
            rootRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getRegistry(
                            CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        }

        String tagsQueryPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                RegistryConstants.QUERIES_COLLECTION_PATH + "/tags";
        if (!rootRegistry.resourceExists(tagsQueryPath)) {
            // set-up query for tag-search.
            Resource resource = rootRegistry.newResource();
            resource.setContent("SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT ORDER BY " +
                    "RT.REG_TAG_ID");
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.TAGS_RESULT_TYPE);
            rootRegistry.put(tagsQueryPath, resource);
        }
        registry = governanceRegistry;
        this.rootRegistry = rootRegistry;
    }

    public AssetStoreHostObject(String loggedUser) throws APIManagementException {
        username = loggedUser;
        try {
            createRegistry(loggedUser);
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String assets = config.getFirstProperty("StoreAssets");
            if (assets == null) {
                managers.add(new GenericArtifactManager(registry, "api"));
                mediaTypeToTypeMap.put(GovernanceUtils.findGovernanceArtifactConfiguration(
                        "api", registry).getMediaType(), "api");
            } else {
                String[] keys = assets.split(",");
                for (String key : keys) {
                    String temp = key.trim();
                    if (temp.equals("service")) {
                        managers.add(new ServiceManager(registry));
                        mediaTypeToTypeMap.put(GovernanceConstants.SERVICE_MEDIA_TYPE, temp);
                    } else {
                        managers.add(new GenericArtifactManager(registry, temp));
                        mediaTypeToTypeMap.put(GovernanceUtils.findGovernanceArtifactConfiguration(
                                temp, registry).getMediaType(), temp);
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Unable to connect to registry", e);
        }
    }

    private static GovernanceArtifact[] getArtifacts(int limit, Scriptable thisObj,
                                                     final GovernanceArtifactFilter filter)
            throws GovernanceException {
        List<GovernanceArtifact> list = new LinkedList<GovernanceArtifact>();
        List<Object> consumers = ((AssetStoreHostObject) thisObj).getApiConsumer();
        for (Object consumer : consumers) {
            if (consumer instanceof ServiceManager) {
                ServiceManager manager = (ServiceManager) consumer;
                Service[] allServices = filter != null ? manager.findServices(new ServiceFilter() {
                    public boolean matches(Service service) throws GovernanceException {
                        return filter.matches(service);
                    }
                }) : manager.getAllServices();
                Service[] services = (limit > 0 && limit < allServices.length) ?
                        Arrays.copyOf(allServices, limit) : allServices;
                if (services != null && services.length != 0) {
                    list.addAll(Arrays.<GovernanceArtifact>asList(services));
                }
            } else {
                GenericArtifactManager manager = (GenericArtifactManager) consumer;
                GenericArtifact[] allArtifacts = filter != null ? manager.findGenericArtifacts(
                        new GenericArtifactFilter() {
                            public boolean matches(GenericArtifact artifact)
                                    throws GovernanceException {
                                return filter.matches(artifact);
                            }
                        }) : manager.getAllGenericArtifacts();
                GenericArtifact[] artifacts = (limit > 0 && limit < allArtifacts.length) ?
                        Arrays.copyOf(allArtifacts, limit) : allArtifacts;
                if (artifacts != null && artifacts.length != 0) {
                    list.addAll(Arrays.<GovernanceArtifact>asList(artifacts));
                }
            }
        }
        Collections.sort(list, new Comparator<GovernanceArtifact>() {
            public int compare(GovernanceArtifact o1, GovernanceArtifact o2) {
                return o1.getQName().getLocalPart().compareToIgnoreCase(
                        o2.getQName().getLocalPart());
            }
        });
        return list.toArray(new GovernanceArtifact[list.size()]);
    }

    private static String getMediaType(GovernanceArtifact artifact) {
        if (artifact instanceof GenericArtifact) {
            return ((GenericArtifact)artifact).getMediaType();
        } else if (artifact instanceof Service) {
            return GovernanceConstants.SERVICE_MEDIA_TYPE;
        }
        return null;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, APIManagementException {

        int length = args.length;
        if (length == 1) {
            String username = (String) args[0];
            return new AssetStoreHostObject(username);
        }
        return new AssetStoreHostObject(null);
    }

    private static String getUsernameFromObject(Scriptable obj) {
        return ((AssetStoreHostObject) obj).getUsername();
    }

    public List<Object> getApiConsumer() {
        return managers;
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public Registry getRegistry() {
        return registry;
    }

    private static Registry getRegistry(Scriptable thisObj) {
        return ((AssetStoreHostObject) thisObj).getRegistry();
    }

    public Registry getRootRegistry() {
        return rootRegistry;
    }

    private static Registry getRootRegistry(Scriptable thisObj) {
        return ((AssetStoreHostObject) thisObj).getRootRegistry();
    }

    public Map<String, String> getMediaTypeToTypeMap() {
        return mediaTypeToTypeMap;
    }

    private static Map<String, String> getMediaTypeToTypeMap(Scriptable thisObj) {
        return ((AssetStoreHostObject) thisObj).getMediaTypeToTypeMap();
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
        return "https://" + System.getProperty(hostName) + ":" + System.getProperty(httpsPort);
    }

    public static String jsFunction_getHTTPURL(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws APIManagementException {
        return "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort);
    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {
        if (args.length != 2) {
            throw new ScriptException("Invalid input parameters to the login method");
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
            boolean isLocalTransport = url.startsWith("local");
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(
                    isLocalTransport ? HostObjectUtils.getConfigContext() : null,
                    url + "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String host = isLocalTransport ? NetworkUtils.getLocalHostname() :
                    new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Authentication failed. Invalid username or password.");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            boolean authorized;
            String[] roles;
            if (isLocalTransport) {
                UserRealm realm = RegistryCoreServiceComponent.getRealmService().getBootstrapRealm();
                authorized = realm.getAuthorizationManager().isUserAuthorized(username,
                        APIConstants.Permissions.API_SUBSCRIBE,
                        CarbonConstants.UI_PERMISSION_ACTION);
                roles = realm.getUserStoreManager().getRoleListOfUser(username);
            } else {
                authorized = APIUtil.checkPermissionQuietly(username,
                        APIConstants.Permissions.API_SUBSCRIBE);
                roles = APIUtil.getListOfRolesQuietly(username);
            }

            if (authorized) {
                row.put("user", row, username);
                row.put("roles", row, new NativeArray(roles));
                row.put("sessionId", row, sessionCookie != null ? sessionCookie :
                        UUIDGenerator.generateUUID());
                row.put("error", row, false);
            } else {
                handleException("Insufficient privileges");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        }

        return row;
    }

    private static GovernanceArtifact[] getArtifacts(int limit, Scriptable thisObj)
            throws GovernanceException {
        return getArtifacts(limit, thisObj, null);
    }

    public static NativeArray jsFunction_getTopRatedAPIs(Context cx,
                                                         final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            try {
                GovernanceArtifact[] artifacts = getArtifacts(limit, thisObj);
                Arrays.sort(artifacts, new Comparator<GovernanceArtifact>() {
                    public int compare(GovernanceArtifact o1, GovernanceArtifact o2) {
                        try {
                            Registry registry = getRegistry(thisObj);
                            return Float.compare(
                                    registry.getAverageRating(o1.getPath()),
                                    registry.getAverageRating(o2.getPath()));
                        } catch (RegistryException ignored) {
                            return -1;
                        }
                    }
                });
                buildDataRows(thisObj, myn, artifacts);
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting Top Rated APIs Information, " +
                        "No APIs in Registry", e);
                return myn;
            } catch (Exception e) {
                log.error("Error while getting Top Rated APIs Information", e);
                return myn;
            }

        }// end of the if
        return myn;
    }

    private static String getProviderFromArtifact(GovernanceArtifact artifact)
            throws GovernanceException {
        String provider = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        return provider != null ? provider : "WSO2";
    }

    private static void buildDataRows(Scriptable thisObj, NativeArray myn,
                                      GovernanceArtifact[] artifacts)
            throws RegistryException {
        int i = 0;
        for (GovernanceArtifact artifact : artifacts) {
            NativeObject row = new NativeObject();
            row.put("id", row, artifact.getId());
            row.put("name", row, artifact.getQName().getLocalPart());
            row.put("provider", row, getProviderFromArtifact(artifact));
            row.put("type", row, getArtifactType(artifact, thisObj));
            setFromAttribute(artifact, row, "version", APIConstants.API_OVERVIEW_VERSION);
            setFromAttribute(artifact, row, "description", APIConstants.API_OVERVIEW_DESCRIPTION);
            row.put("rates", row, getRegistry(thisObj).getAverageRating(artifact.getPath()));
            String username = getUsernameFromObject(thisObj);
            if (username != null) {
                row.put("userRate", row, getRegistry(thisObj).getRating(artifact.getPath(),
                        username));
            } else {
                row.put("userRate", row, getRegistry(thisObj).getAverageRating(artifact.getPath()));
            }
            row.put("updatedDate", row,
                    getRegistry(thisObj).get(artifact.getPath()).getLastModified().toString());
            StringBuilder builder = new StringBuilder();
            String[] endpoints_entries = artifact.getAttributes("endpoints_entry");
            if (endpoints_entries != null) {
                for (String entry : endpoints_entries) {
                    builder.append(",").append(entry.substring(entry.indexOf(":") + 1));
                }
                row.put("serverURL", row, builder.substring(1));
            } else {
                row.put("serverURL", row, "");
            }
            row.put("status", row, getLifecycleState(artifact));
            Association[] icons = getRegistry(thisObj).getAssociations(artifact.getPath(), "icon");
            if (icons == null || icons.length == 0) {
                row.put("thumbnailurl", row, "images/api-default.png");
            } else {
                row.put("thumbnailurl", row, WEB_CONTEXT + (WEB_CONTEXT.endsWith("/") ? "" : "/") +
                        "registry/resource" + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        icons[0].getDestinationPath());
            }
            for (String key : artifact.getAttributeKeys()) {
                String[] attributes = artifact.getAttributes(key);
                if (attributes != null) {
                    if (attributes.length > 1) {
                        for (String attribute : attributes) {
                            builder.append(",").append(attribute.substring(attribute.indexOf(":") + 1));
                        }
                        row.put(key, row, builder.substring(1));
                    } else if (attributes.length == 1) {
                        row.put(key, row, attributes[0]);
                    }
                }
            }

            //TODO: Some values are still hard-coded.
            row.put("wsdl", row, "http://appserver/services/echo?wsdl");
            row.put("wadl", row, "http://appserver/services/echo?wadl");
            row.put("endpoint", row, "http://appserver/services/echo");
            row.put("subscribed", row, "true");

            //TODO: Some values are N/A.
            row.put("context",row, "/root");
            row.put("tier", row, "Gold");
            row.put("tierName", row, "");
            row.put("tierDescription", row, "");
            row.put("bizOwner", row, "");
            row.put("bizOwnerMail", row, "");
            row.put("techOwner", row, "");
            row.put("techOwnerMail", row, "");

            myn.put(i++, myn, row);
        }
    }

    private static String getLifecycleState(GovernanceArtifact artifact) throws GovernanceException {
        String lifecycleState = artifact.getLifecycleState();
        return lifecycleState != null ? lifecycleState : "None";
    }

    private static void setFromAttribute(GovernanceArtifact artifact, NativeObject row,
                                         String rowName, String key)
            throws GovernanceException {
        String attribute = artifact.getAttribute(key);
        if (attribute != null) {
            row.put(rowName, row, attribute);
        }
    }

    public static NativeArray jsFunction_getRecentlyAddedAPIs(Context cx,
                                                              final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            try {
                GovernanceArtifact[] artifacts = getArtifacts(limit, thisObj);
                Arrays.sort(artifacts, new Comparator<GovernanceArtifact>() {
                    public int compare(GovernanceArtifact o1, GovernanceArtifact o2) {
                        try {
                            Registry registry = getRegistry(thisObj);
                            return Float.compare(
                                    registry.get(o1.getPath()).getCreatedTime().getTime(),
                                    registry.get(o2.getPath()).getCreatedTime().getTime());
                        } catch (RegistryException ignored) {
                            return -1;
                        }
                    }
                });
                buildDataRows(thisObj, apiArray, artifacts);
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting Recently Added APIs Information," +
                        " No APIs in Registry", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting Recently Added APIs Information", e);
                return apiArray;
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_searchAPI(Context cx,
                                                   final Scriptable thisObj,
                                                   Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            final String searchTerm = args[0].toString();
            try {
                buildDataRows(thisObj, apiArray, getArtifacts(-1, thisObj, new GovernanceArtifactFilter() {
                    public boolean matches(GovernanceArtifact artifact) throws GovernanceException {
                        return doSearch(artifact, searchTerm, thisObj);
                    }
                }));
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting SearchAPI Information, No " +
                        "APIs in Registry", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting SearchAPI APIs Information", e);
                return apiArray;
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_searchAPIbyType(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            final String searchType = args[0].toString();
            final String searchTerm = args[1].toString().toLowerCase();
            try {
                buildDataRows(thisObj, apiArray, getArtifacts(-1, thisObj,
                        new GovernanceArtifactFilter() {
                            public boolean matches(GovernanceArtifact artifact) throws GovernanceException {
                                String type;
                                if (searchType.contains("id")) {
                                    type = artifact.getId();
                                } else if (searchType.contains("provider")) {
                                    type = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
                                } else if (searchType.contains("version")) {
                                    type = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
                                } else {
                                    type = artifact.getQName().getLocalPart();
                                }
                                return doSearch(type, searchTerm);
                            }
                        }));
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting SearchAPI by type Information" +
                        ", No APIs in Registry", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting SearchAPI APIs by type Information", e);
                return apiArray;
            }

        }// end of the if
        return apiArray;
    }

    private static boolean doSearch(String name, String searchTerm) {
        return name.toLowerCase().contains(searchTerm.toLowerCase()) || name.matches(searchTerm);
    }

    private static boolean doSearch(GovernanceArtifact artifact, String searchTerm,
                                    Scriptable thisObj) throws GovernanceException {
        String[] terms = searchTerm.split(" ");
        // we can have multiple search terms which are separated by spaces.
        for (int i = 0; i < terms.length;) {
            StringBuilder temp = new StringBuilder();
            do {
                temp.append(" ").append(terms[i++]);
                // values of some parts can have spaces in them too. (ex:- names with spaces)
            } while (temp.indexOf(":") < 0 && i < terms.length);
            String[] parts = temp.substring(1).split(":");
            if (parts.length == 1) {
                // There were no parts. In that case, this is a name.
                parts = new String[]{null, parts[0]};
            } else if (parts[0].indexOf(" ") > 0) {
                // The name part can be the first, which will not need a qualifier in that case.
                i--;
                parts = new String[]{null, parts[0].substring(0, parts[0].lastIndexOf(" "))};
            }
            String actual;
            if (parts[0] == null || parts[0].equals("name")) {
                actual = artifact.getQName().getLocalPart();
            } else if (parts[0].equals("id")) {
                actual = artifact.getId();
            } else if (parts[0].equals("provider")) {
                actual = getProviderFromArtifact(artifact);
            } else if (parts[0].equals("version")) {
                actual = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            } else if (parts[0].equals("state")) {
                actual = getLifecycleState(artifact);
            } else if (parts[0].equals("type")) {
                actual = getArtifactType(artifact, thisObj);
            } else {
                actual = artifact.getAttribute(parts[0]);
            }
            // If anything fails to match, then we return that, or we continue finding for a
            // failure.
            if (actual == null) {
                return false;
            } else {
                String[] subTerms = parts[1].split(",");
                boolean result = false;
                for (String subTerm : subTerms) {
                    if (result = doSearch(actual, subTerm)) {
                        break;
                    }
                }
                if (!result) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String getArtifactType(GovernanceArtifact artifact, Scriptable thisObj) {
        String actual;
        if (artifact instanceof GenericArtifact) {
            actual = getMediaTypeToTypeMap(thisObj).get(
                    ((GenericArtifact) artifact).getMediaType());
        } else if (artifact instanceof Service) {
            actual = "service";
        } else {
            actual = null;
        }
        return actual;
    }

    public static NativeArray jsFunction_getAPIsWithTag(Context cx,
                                                        final Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            final String tagName = args[0].toString();
            try{
                buildDataRows(thisObj, apiArray, getArtifacts(-1, thisObj,
                        new GovernanceArtifactFilter() {
                            public boolean matches(GovernanceArtifact artifact) throws GovernanceException {
                                try {
                                    Tag[] tags = getRegistry(thisObj).getTags(artifact.getPath());
                                    for (Tag tag : tags) {
                                        if (tag.getTagName().equalsIgnoreCase(tagName)) {
                                            return true;
                                        }
                                    }
                                } catch (RegistryException ignored) {
                                }
                                return false;
                            }
                        }));
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting APIs With Tag Information, " +
                        "No APIs in Registry", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return apiArray;
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            try {
                // TODO: Subscribed list is not supported
                buildDataRows(thisObj, apiArray, getArtifacts(limit, thisObj));
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting Subscribed APIs, No APIs in " +
                        "Registry.", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting Subscribed APIs", e);
                return apiArray;
            }
        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getAllTags(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray tagArray = new NativeArray(0);
        try {
            Collection collection = getRegistry(thisObj).executeQuery(
                    RegistryConstants.QUERIES_COLLECTION_PATH + "/tags",
                    Collections.<String, String>emptyMap());
            int i = 0;
            TreeMap<String, Integer> map = new TreeMap<String, Integer>();
            for (String fullTag : collection.getChildren()) {
                String tag = fullTag.split(";")[1].split(":")[1];
                map.put(tag, 1 + (map.containsKey(tag) ? map.get(tag) : 0));
            }
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                NativeObject currentTag = new NativeObject();
                currentTag.put("name", currentTag, e.getKey());
                currentTag.put("count", currentTag, e.getValue());
                tagArray.put(i++, tagArray, currentTag);
            }
        } catch (Exception e) {
            log.error("Error while getting All Tags", e);
        }
        return tagArray;
    }

    public static NativeArray jsFunction_getAllPublishedAPIs(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray myn = new NativeArray(0);
        try {
            // TODO: We don't test whether this API is published or not.
            buildDataRows(thisObj, myn, getArtifacts(-1, thisObj));
        } catch (Exception e) {
            log.error("Error while getting API Information", e);
            return myn;
        }
        return myn;
    }

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String id;
        String username = null;
        boolean isSubscribed = false;
        String methodName = "getAPI";
        int argsCount = args.length;
        if(argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0],
                    false);
        }
        id = (String) args[0];
        NativeArray myn = new NativeArray(0);
        try {
            buildDataRows(thisObj, myn, getArtifacts(1, thisObj,
                    getFilterForAPIId(id)));
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting API information on " + id, e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting API Information", e);
            return myn;
        }
        return myn;
    }


    public static NativeArray jsFunction_getComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        Comment[] commentlist;
        String id = "";
        if (isStringArray(args)) {
            id = args[0].toString();
        }
        NativeArray myn = new NativeArray(0);
        try {
            String path = getArtifacts(1, thisObj,
                    getFilterForAPIId(id))[0].getPath();
            commentlist = getRegistry(thisObj).getComments(path);
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting Comments for "+ id, e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting Comments for " + id, e);
            return myn;
        }

        int i=0;
        for (Comment n: commentlist) {
            NativeObject row = new NativeObject();
            row.put("userName", row, n.getUser());
            row.put("comment", row, n.getText());
            row.put("createdTime", row, n.getCreatedTime().getTime());
            myn.put(i, myn, row);
            i++;
        }
        return myn;
    }

    public static NativeArray jsFunction_invokeAspect(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String methodName = "invokeAspect";
        String[] items = new String[0];
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0],
                    false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1],
                    false);
        }
        if (args[2] != null) {
            if (!(args[2] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2],
                        false);
            }
            items = ((String)args[2]).split(";");
        }
        String id = (String)args[0];
        String action = (String)args[1];
        NativeArray myn = new NativeArray(0);

        try {
            GovernanceArtifact artifact = getArtifacts(1, thisObj,
                    getFilterForAPIId(id))[0];
            String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
            if (action.equals("itemClick") || action.equals("voteClick")) {
                Map<String, String> parameters = new HashMap<String, String>();
                for (int i = 0; i < items.length; i++) {
                    parameters.put(i + ".item", items[i]);
                }
                getRootRegistry(thisObj).invokeAspect(
                        path, artifact.getLifecycleName(), action, parameters);
            } else {
                getRootRegistry(thisObj).invokeAspect(path, artifact.getLifecycleName(), action);
            }
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting Comments for "+ id, e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting Comments for " + id, e);
            return myn;
        }

        NativeObject row = new NativeObject();
        row.put("id", row, id);
        myn.put(0, myn, row);
        return myn;
    }

    @SuppressWarnings("unchecked")
    public static NativeObject jsFunction_getLifecycleDetails(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeObject myn = new NativeObject();
        String id;
        String state;
        Set<String> roles = new HashSet<String>();
        if (args[0] instanceof String && args[1] instanceof String &&
                args[2] instanceof NativeArray) {
            id = args[0].toString();
            state = args[1].toString();
            NativeArray roleArray = (NativeArray) args[2];
            for (int i = 0; i < roleArray.getLength(); i++) {
                roles.add((String) roleArray.get(i, roleArray));
            }
        } else {
            return myn;
        }

        try {
            GovernanceArtifact artifact = getArtifacts(1, thisObj,
                    getFilterForAPIId(id))[0];
            OMElement configuration =
                    buildLifecycleConfiguration(thisObj, artifact.getLifecycleName());
            if (configuration != null) {
                myn.put("state", myn, getStates(thisObj, artifact, configuration, state, roles));
            }
            myn.put("dependency", myn, getDependencies(thisObj, artifact));
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting Lifecycle Details for "+ id, e);
            return new NativeObject();
        } catch (Exception e) {
            log.error("Error while getting Lifecycle Details for " + id, e);
            return new NativeObject();
        }
        return myn;
    }

    private static NativeArray getStates(
            Scriptable thisObj, GovernanceArtifact artifact,
            OMElement configuration, String state, Set<String> userRoles)
            throws RegistryException, JaxenException {
        NativeArray myn = new NativeArray(0);
        String path = artifact.getPath();
        String currentState = artifact.getLifecycleState();
        Resource resource = getRegistry(thisObj).get(path);
        String historyPath = resource.getProperty("registry.lifecycle_history.originalPath");
        if (historyPath == null) {
            historyPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path;
        }
        OMElement history = buildLifecycleHistory(thisObj,
                "/repository/components/org.wso2.carbon.governance/lifecycles/" +
                "history/" + historyPath.replace(RegistryConstants.PATH_SEPARATOR, "_"));

        List<String> relatedPaths = Arrays.asList(computeRelatedPaths(
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path, history));

        String initialState = configuration.getAttributeValue(new QName("initialstate"));
        Map<String, Integer> stateOrder = computeStateOrder(configuration);
        Iterator stateElements;
        stateElements = configuration.getChildrenWithName(new QName(SCXML_NS, "state"));
        int i = 0;
        while (stateElements.hasNext()) {
            OMElement stateElement = (OMElement) stateElements.next();
            String id = stateElement.getAttributeValue(new QName("id"));
            NativeObject row = new NativeObject();
            row.put("id", row, id);

            boolean isInitialState = id.equals(initialState);
            String exitTime = "";
            String entryTime = "";
            String transitionUser = "";
            Map<String, String> timestampMap = new HashMap<String, String>();
            Map<String, String> ApprovalTimestampMap = new HashMap<String, String>();
            Map<String, String> checkedState = new HashMap<String, String>();
            Map<String, String> approvedState = new HashMap<String, String>();
            if (history != null) {
                AXIOMXPath xPath = new AXIOMXPath("//item[@targetState = '" + id + "' and " +
                        "not(@state = '" + currentState + "')]");
                ArrayList historyItems = ((ArrayList) xPath.evaluate(history));
                for (Object obj : historyItems) {
                    OMElement item = (OMElement) obj;
                    if (!relatedPaths.contains(item.getAttributeValue(new QName("originalPath")))) {
                        continue;
                    }
                    OMElement action = item.getFirstChildWithName(new QName("action"));
                    String type = action.getAttributeValue(new QName("type"));
                    if (type.equals("transition")) {
                        String temp = item.getAttributeValue(new QName("timestamp"));
                        if (entryTime.isEmpty() || entryTime.compareTo(temp) < 0) {
                            entryTime = temp;
                        }
                    }
                }
                xPath = new AXIOMXPath("//item[@state = '" + id + "' and " +
                        "not(@state = '" + currentState + "' and action/@type = 'transition')]");
                historyItems = ((ArrayList) xPath.evaluate(history));
                for (Object obj : historyItems) {
                    OMElement item = (OMElement) obj;
                    if (!relatedPaths.contains(item.getAttributeValue(new QName("originalPath")))) {
                        continue;
                    }
                    OMElement action = item.getFirstChildWithName(new QName("action"));
                    String type = action.getAttributeValue(new QName("type"));
                    if (type.equals("transition")) {
                        String temp = item.getAttributeValue(new QName("timestamp"));
                        if (exitTime.isEmpty() || exitTime.compareTo(temp) < 0) {
                            if (entryTime.isEmpty() || entryTime.compareTo(temp) < 0) {
                                exitTime = temp;
                                transitionUser = item.getAttributeValue(new QName("user"));
                            }
                        }
                    } else if (type.equals("itemClick")) {
                        String name = action.getAttributeValue(new QName("name"));
                        String temp1 = timestampMap.get(name);
                        String temp2 = item.getAttributeValue(new QName("timestamp"));
                        if (temp1 == null || temp1.compareTo(temp2) < 0) {
                            if (entryTime.isEmpty() || entryTime.compareTo(temp2) < 0) {
                                timestampMap.put(name, temp2);
                                checkedState.put(name, item.getAttributeValue(new QName("user")) +
                                        ":" + action.getFirstChildWithName(
                                        new QName("value")).getText().split(":")[1]);
                            }
                        }
                    } else if (type.equals(".vote")) {
                        String name = action.getAttributeValue(new QName("name")) + ":" +
                                item.getAttributeValue(new QName("user"));
                        String temp1 = ApprovalTimestampMap.get(name);
                        String temp2 = item.getAttributeValue(new QName("timestamp"));
                        if (temp1 == null || temp1.compareTo(temp2) < 0) {
                            if (entryTime.isEmpty() || entryTime.compareTo(temp2) < 0) {
                                ApprovalTimestampMap.put(name, temp2);
                                approvedState.put(name,
                                        action.getFirstChildWithName(new QName("value")).getText());
                            }
                        }
                    }
                }
            }
            if (!exitTime.isEmpty()) {
                row.put("mode", row, "completed");
            } else if (entryTime.isEmpty() && !isInitialState) {
                row.put("mode", row, "new");
            } else {
                row.put("mode", row, "active");
            }
            row.put("time", row, timestampToString(exitTime.isEmpty() ? null : exitTime));
            row.put("user", row, transitionUser);

            NativeArray checkItems = new NativeArray(0);
            NativeArray approvals = new NativeArray(0);
            Map<String, Set<String>> transitionPermissions = new HashMap<String, Set<String>>();
            OMElement dataModel =
                    stateElement.getFirstChildWithName(new QName(SCXML_NS, "datamodel"));
            if (dataModel != null) {
                Iterator dataElements =
                        dataModel.getChildrenWithName(new QName(SCXML_NS, "data"));
                while (dataElements.hasNext()) {
                    OMElement dataElement = (OMElement) dataElements.next();
                    String dataName = dataElement.getAttributeValue(new QName("name"));
                    if (dataName.equals("checkItems")) {
                        checkItems = getChecklist(timestampMap, checkedState, userRoles,
                                dataElement);
                    } else if (dataName.equals("transitionApproval")) {
                        approvals = getApprovals(ApprovalTimestampMap, approvedState, userRoles,
                                dataElement);
                    } else if (dataName.equals("transitionPermission")) {
                        Iterator permissions = dataElement.getChildrenWithName(
                                new QName(SCXML_NS, "permission"));
                        while (permissions.hasNext()) {
                            OMElement permissionElement = (OMElement) permissions.next();
                            String event =
                                    permissionElement.getAttributeValue(new QName("forEvent"));
                            Set<String> roleSet = transitionPermissions.get(event);
                            if (roleSet == null) {
                                roleSet = new HashSet<String>();
                            }
                            roleSet.addAll(Arrays.asList(permissionElement
                                    .getAttributeValue(new QName("roles")).split(",")));
                            transitionPermissions.put(event, roleSet);
                        }
                    }
                }
            }
            row.put("checklist", row, checkItems);
            row.put("approvals", row, approvals);

            NativeArray transitions = getTransitions(stateOrder, i, stateElement,
                    transitionPermissions, userRoles);

            if (isInitialState) {
                row.put("type", row, "initial");
            } else if (transitions.getLength() == 0) {
                row.put("type", row, "end");
            } else {
                row.put("type", row, "mid");
            }

            if (id.equals(state)) {
                row.put("current", row, "true");
            }
            row.put("transition", row, transitions);
            row.put("order", row, i);
            myn.put(i++, myn, row);
        }
        return myn;
    }

    private static String[] computeRelatedPaths(String path, OMElement history)
            throws JaxenException {
        Set<String> temp = new HashSet<String>();
        temp.add(path);
        if (history != null) {
            AXIOMXPath xPath = new AXIOMXPath("//item[starts-with(action/executors/executor/" +
                    "operations/operation/info,'" + path + "')]");
            ArrayList historyItems = (ArrayList) xPath.evaluate(history);
            for (Object obj : historyItems) {
                temp.addAll(Arrays.asList(computeRelatedPaths(
                        ((OMElement) obj).getAttributeValue(new QName("originalPath")), history)));
            }
        }
        return temp.toArray(new String[temp.size()]);
    }

    private static NativeArray getDependencies(Scriptable thisObj, GovernanceArtifact artifact)
            throws GovernanceException {
        NativeArray dependencies = new NativeArray(0);
        int j = 0;
        GovernanceArtifact[] depList = artifact.getDependencies();
        if (depList != null && depList.length > 0) {
            Set<String> types = getMediaTypeToTypeMap(thisObj).keySet();
            for (GovernanceArtifact dependency : depList) {
                if (types.contains(getMediaType(dependency))) {
                    NativeObject dependencyRow = new NativeObject();
                    dependencyRow.put("id", dependencyRow, dependency.getId());
                    dependencyRow.put("name", dependencyRow,
                            dependency.getQName().getLocalPart());
                    setFromAttribute(dependency, dependencyRow, "version",
                            APIConstants.API_OVERVIEW_VERSION);
                    String lifecycleState = dependency.getLifecycleState();
                    if (lifecycleState != null) {
                        dependencyRow.put("state", dependencyRow, lifecycleState);
                    }
                    dependencies.put(j++, dependencies, dependencyRow);
                }
            }

        }
        return dependencies;
    }

    private static NativeArray getChecklist(Map<String, String> timestampMap,
                                            Map<String, String> checkedState,
                                            Set<String> userRoles,
                                            OMElement dataElement) {
        NativeArray itemArray = new NativeArray(0);
        Iterator items =
                dataElement.getChildrenWithName(new QName(SCXML_NS, "item"));
        int j = 0;
        while (items.hasNext()) {
            OMElement itemElement = (OMElement) items.next();
            NativeObject item = new NativeObject();
            String name = itemElement.getAttributeValue(new QName("name"));
            item.put("name", item, name);
            String temp = timestampMap.get(name);
            item.put("time", item, timestampToString(temp));
            temp = checkedState.get(name);
            item.put("user", item, temp == null ? "" : temp.split(":")[0]);
            item.put("checked", item, temp != null &&
                    temp.split(":")[1].equalsIgnoreCase(Boolean.toString(true)));

            Set<String> roles = new HashSet<String>();
            OMElement permissions = itemElement.getFirstChildWithName(
                    new QName(SCXML_NS, "permissions"));
            if (permissions != null) {
                Iterator permissionElements = permissions.getChildrenWithName(
                        new QName(SCXML_NS, "permission"));
                while (permissionElements.hasNext()) {
                    roles.addAll(Arrays.asList(((OMElement) items.next())
                            .getAttributeValue(new QName("roles")).split(",")));
                }
            }
            boolean inRole = roles.size() == 0;
            if (!inRole) {
                for (String role : roles) {
                    if (userRoles.contains(role)) {
                        inRole = true;
                        break;
                    }
                }
            }
            item.put("enabled", item, inRole);
            itemArray.put(j++, itemArray, item);
        }
        return itemArray;
    }

    private static NativeArray getApprovals(Map<String, String> timestampMap,
                                            Map<String, String> approvedState,
                                            Set<String> userRoles,
                                            OMElement dataElement) {
        NativeArray itemArray = new NativeArray(0);
        Iterator items =
                dataElement.getChildrenWithName(new QName(SCXML_NS, "approval"));
        int j = 0;
        while (items.hasNext()) {
            OMElement itemElement = (OMElement) items.next();
            NativeObject item = new NativeObject();
            String name = itemElement.getAttributeValue(new QName("forEvent"));
            item.put("name", item, name);
            item.put("votes", item, itemElement.getAttributeValue(new QName("votes")));
            NativeArray approvalArray = new NativeArray(0);
            int i = 0;
            for (Map.Entry<String, String> e : approvedState.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (key.startsWith(name + ":") && value.equalsIgnoreCase(Boolean.toString(true))) {
                    NativeObject obj = new NativeObject();
                    obj.put("user", obj, key.split(":")[1]);
                    obj.put("time", obj, timestampToString(timestampMap.get(key)));
                    approvalArray.put(i++, approvalArray, obj);
                }
            }
            item.put("voters", item, approvalArray);
            String roleStr = itemElement.getAttributeValue(new QName("roles"));
            List<String> roles;
            if (roleStr == null || roleStr.isEmpty()) {
                roles = Collections.emptyList();
            } else {
                roles = Arrays.asList(roleStr.split(","));
            }
            boolean inRole = roles.size() == 0;
            if (!inRole) {
                for (String role : roles) {
                    if (userRoles.contains(role)) {
                        inRole = true;
                        break;
                    }
                }
            }
            item.put("enabled", item, inRole);
            itemArray.put(j++, itemArray, item);
        }
        return itemArray;
    }

    private static String timestampToString(String temp) {
        return temp == null ? "" : temp.substring(0, "yyyy-mm-dd".length());
    }

    private static NativeArray getTransitions(
            Map<String, Integer> stateOrder, int order, OMElement stateElement, Map<String,
            Set<String>> transitionPermissions, Set<String> userRoles) {
        NativeArray transitions = new NativeArray(0);
        Iterator transitionElements = stateElement.getChildrenWithName(
                new QName(SCXML_NS, "transition"));
        int count = 0;
        while (transitionElements.hasNext()) {
            OMElement transitionElement = (OMElement) transitionElements.next();
            NativeObject transitionRow = new NativeObject();
            NativeObject targetObject = new NativeObject();
            String target = transitionElement.getAttributeValue(new QName("target"));
            targetObject.put("name", targetObject, target);
            if (order == stateOrder.get(target) - 1) {
                targetObject.put("type", targetObject, "next");
            } else if (order == stateOrder.get(target) + 1) {
                targetObject.put("type", targetObject, "previous");
            } else {
                targetObject.put("type", targetObject, "");
            }
            transitionRow.put("target", transitionRow, targetObject);

            String event = transitionElement.getAttributeValue(new QName("event"));
            transitionRow.put("event", transitionRow, event);
            Set<String> roles = transitionPermissions.get(event);
            boolean inRole = roles == null || roles.size() == 0;
            if (!inRole) {
                for (String role : roles) {
                    if (userRoles.contains(role)) {
                        inRole = true;
                        break;
                    }
                }
            }
            transitionRow.put("enabled", transitionRow, inRole);
            transitions.put(count++, transitions, transitionRow);
        }
        return transitions;
    }

    private static Map<String, Integer> computeStateOrder(OMElement configuration) {
        Iterator stateElements =
                configuration.getChildrenWithName(new QName(SCXML_NS, "state"));
        Map<String, Integer> stateOrder = new HashMap<String, Integer>();
        int i = 0;
        while (stateElements.hasNext()) {
            stateOrder.put(
                    ((OMElement) stateElements.next()).getAttributeValue(new QName("id")), i++);
        }
        return stateOrder;
    }

    private static OMElement buildLifecycleConfiguration(Scriptable thisObj, String lifecycleName)
            throws RegistryException {
        OMElement configuration = null;
        if (lifecycleName == null) {
            return null;
        }
        Object lifecycleContent =
                getRootRegistry(thisObj).get(RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        "/repository/components/org.wso2.carbon.governance/lifecycles/" +
                        lifecycleName).getContent();
        String temp;
        if (lifecycleContent instanceof String) {
            temp = (String) lifecycleContent;
        } else {
            temp = RegistryUtils.decodeBytes((byte[]) lifecycleContent);
        }
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().
                    createXMLStreamReader(new StringReader(temp));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            AXIOMXPath xPath = new AXIOMXPath("//ns:scxml");
            xPath.addNamespace("ns", "http://www.w3.org/2005/07/scxml");
            configuration = (OMElement) ((ArrayList) xPath.evaluate(
                    builder.getDocumentElement())).get(0);
        } catch (Exception e) {
            log.error("Error in parsing the lifecycle configuration for " + lifecycleName + ".", e);
        }
        return configuration;
    }

    private static OMElement buildLifecycleHistory(Scriptable thisObj, String path)
            throws RegistryException {
        if (!getRegistry(thisObj).resourceExists(path)) {
            return null;
        }
        Object lifecycleContent = getRegistry(thisObj).get(path).getContent();
        String temp;
        if (lifecycleContent instanceof String) {
            temp = (String) lifecycleContent;
        } else {
            temp = RegistryUtils.decodeBytes((byte[]) lifecycleContent);
        }
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().
                    createXMLStreamReader(new StringReader(temp));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (Exception e) {
            log.error("Error in parsing the lifecycle history at " + path + ".", e);
        }
        return null;
    }

    public static NativeArray jsFunction_addComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String id = "";
        String commentStr = "";
        String username = "";
        if (isStringArray(args)) {
            id = args[0].toString();
            commentStr = args[1].toString();
        }
        NativeArray myn = new NativeArray(0);
        try {
            String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + getArtifacts(1, thisObj,
                    getFilterForAPIId(id))[0].getPath();
            getRootRegistry(thisObj).addComment(path, new Comment(commentStr));
        } catch (NullPointerException e) {
            log.error("Error from Registry API while adding Comments for "+ id, e);
            return myn;
        } catch (Exception e) {
            log.error("Error while adding Comments for " + id, e);
            return myn;
        }

        int i=0;
        NativeObject row = new NativeObject();
        row.put("id", row, id);
        row.put("comment", row, commentStr);
        myn.put(i, myn, row);

        return myn;
    }

    private static GovernanceArtifactFilter getFilterForAPIId(final String id) {
        return new GovernanceArtifactFilter() {
            public boolean matches(GovernanceArtifact artifact) throws GovernanceException {
                return  id.equals(artifact.getId());

            }
        };
    }


    public static NativeArray jsFunction_rateAPI(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String id = args[0].toString();
            String rateStr = args[1].toString();
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

            try {
                String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + getArtifacts(1,
                        thisObj, getFilterForAPIId(id))[0].getPath();
                getRootRegistry(thisObj).rateResource(path, rate);
            } catch (IllegalArgumentException e) {
                log.error("Error from Registry API while Rating API " + id, e);
                return myn;
            } catch (NullPointerException e) {
                log.error("Error from Registry API while Rating API " + id, e);
                return myn;
            } catch (Exception e) {
                log.error("Error while Rating API " + id, e);
                return myn;
            }

            NativeObject row = new NativeObject();
            row.put("id", row, id);
            row.put("rates", row, rateStr);
            myn.put(0, myn, row);

        }// end of the if
        return myn;
    }

    public static void jsFunction_addUser(Context cx, Scriptable thisObj,
                                          Object[] args,
                                          Function funObj) throws APIManagementException {

        String username = args[0].toString();
        String password = args[1].toString();

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            handleException("Self sign up has been disabled on this server");
        }
        String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String adminUsername = username;
        String adminPassword = password;
        if (serverURL == null || adminUsername == null || adminPassword == null) {
            handleException("Required parameter missing to connect to the" +
                    " authentication manager");
        }

        String role = config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role == null) {
            handleException("Subscriber role undefined for self registration");
        }
        boolean isLocalTransport = serverURL.startsWith("local");
        String url = serverURL + "UserAdmin";
        try {
            UserAdminStub stub = isLocalTransport ? new UserAdminStub(
                    HostObjectUtils.getConfigContext(), url) : new UserAdminStub(url);
            CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                    true, stub._getServiceClient());
            stub.addUser(username, password, new String[] { role }, null, null);
        } catch (RemoteException e) {
            handleException(e.getMessage(), e);
        }catch (Exception e) {
            handleException("Error while adding the user: " + username, e);
        }

	//	catch (AddUserUserAdminExceptionException e) {
    //        handleException("Error while adding the user: " + username, e);
    //    }
    }

    public static NativeArray jsFunction_getPublishedAPIsByProvider(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj)
            throws APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (isStringArray(args)) {
            final String providerName = args[0].toString();
            try {
                buildDataRows(thisObj, apiArray, getArtifacts(-1, thisObj,
                        new GovernanceArtifactFilter() {
                            public boolean matches(GovernanceArtifact artifact) throws GovernanceException {
                                return providerName.equals(getProviderFromArtifact(artifact));
                            }
                        }));
            } catch (Exception e) {
                handleException("Error while getting Published APIs Information of the provider - " + providerName
                        , e);

            }

        } else {
            handleException("Invalid types of input parameters.");
        }

        return apiArray;

    }

    /*
      * here return boolean with checking all objects in array is string
      */
    public static boolean isStringArray(Object[] args) {
        for (Object arg : args) {
            if (!(arg instanceof String)) {
                return false;
            }
        }
        return true;

    }

    public static boolean jsFunction_isCommentActivated() throws APIManagementException {
        return true;
    }

    public static boolean jsFunction_isRatingActivated() throws APIManagementException {
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Documentation
    /////////////////////////////////////////////////////////////////////////////////

    public static NativeArray jsFunction_getAllDocumentation(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String id = "";
        if (isStringArray(args)) {
            id = args[0].toString();
        }
        NativeArray myn = new NativeArray(0);
        try{
            GovernanceArtifact[] artifacts = getArtifacts(1, thisObj,
                    getFilterForAPIId(id));
            int i = 0;
            for (GovernanceArtifact artifact : artifacts) {
                for (int k = 0; k < 3; k++) {
                    String type = artifact.getAttribute("docLinks_documentType" + (k > 0 ? k : ""));
                    String url = artifact.getAttribute("docLinks_url" + (k > 0 ? k : ""));
                    String comment = artifact.getAttribute("docLinks_documentComment" +
                            (k > 0 ? k : ""));
                    if (type != null && url != null && comment != null) {
                        NativeObject row = new NativeObject();
                        row.put("name", row, DigestUtils.md5Hex(comment + url + type));
                        row.put("sourceType", row, "URL");
                        row.put("summary", row, comment);
                        row.put("sourceUrl", row, url);
                        row.put("type", row, type);
                        myn.put(i, myn, row);
                        i++;
                    }
                }
            }
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting All Documentation on "+ id, e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting All Documentation " + id, e);
            return myn;
        }
        return myn;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        // We have no inline content concept for services.
        return new NativeArray(0);
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Sample Data
    /////////////////////////////////////////////////////////////////////////////////


    /*/////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Subscriptions
    /////////////////////////////////////////////////////////////////////////////////

    public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String username = null;
        String methodName = "isSubscribed";
        int argsCount = args.length;
        if(argsCount != 4) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        if (args[3] != null) {
            if (!(args[3] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, methodName, "4", "string", args[3], false);
            }
            username = (String) args[3];
        }

        String providerName = (String) args[0];
        String apiName = (String) args[1];
        String version = (String) args[2];
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        return username != null && apiConsumer.isSubscribed(apiIdentifier, username);
    }

    *//*
    * getting key for API subscriber args[] list String subscriberID, String
    * api, String apiVersion, String Date
    *//*
    public static String jsFunction_getKey(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj) throws ScriptException {
        int argsCount = args.length;
        String methodName = "getKey";
        if(argsCount != 7) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        if(!(args[3] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "4", "string", args[3], false);
        }
        if(!(args[4] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "5", "string", args[4], false);
        }
        if(!(args[5] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "6", "string", args[5], false);
        }
        if(!(args[5] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "7", "string", args[6], false);
        }
        APIInfoDTO apiInfo = new APIInfoDTO();
        apiInfo.setProviderId((String) args[0]);
        apiInfo.setApiName((String) args[1]);
        apiInfo.setVersion((String) args[2]);
        apiInfo.setContext((String) args[3]);
        try {
            SubscriberKeyMgtClient keyMgtClient = getKeyManagementClient();
            return keyMgtClient.getAccessKey((String) args[5], apiInfo, (String) args[4], (String) args[6]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    *//*
	 * getting key for a subscribed Application - args[] list String subscriberID, String
	 * application name, String keyType
	 *//*
    public static String jsFunction_getApplicationKey(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj) throws ScriptException {
        int argsCount = args.length;
        String methodName = "getApplicationKey";
        if(argsCount != 3) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        try {
            SubscriberKeyMgtClient keyMgtClient = getKeyManagementClient();
            return keyMgtClient.getApplicationAccessKey((String) args[0], (String) args[1], (String) args[2]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    public static NativeArray jsFunction_getAPIKey(Context cx, Scriptable thisObj,
                                                   Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String providerName = "";
        String apiName = "";
        String version = "";
        String apiContext = "";
        if (isStringArray(args)) {
            providerName = args[0].toString();
            apiName = args[1].toString();
            version = args[2].toString();
            apiContext = args[3].toString();
        }

        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName(apiName);
        apiInfoDTO.setContext(apiContext);
        apiInfoDTO.setProviderId(providerName);
        apiInfoDTO.setVersion(version);
        SubscriberKeyMgtClient subscriberKeyMgtClient;
        String key="";
        NativeArray myn = new NativeArray(0);
        NativeObject row = new NativeObject();
        myn.put(0, myn, row);
        return myn;
    }

    public static boolean jsFunction_addSubscription(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj) {
        if(!(args[0] instanceof String) ||
                !(args[1] instanceof String) ||
                !(args[2] instanceof String) ||
                !(args[3] instanceof String) ||
                (!(args[4] instanceof Double) && !(args[4] instanceof Integer) ||
                        !(args[5] instanceof String))) {
            return false;
        }

        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String tier = args[3].toString();
        int applicationId = ((Number) args[4]).intValue();
        String userId = args[5].toString();
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
            return true;
        } catch (APIManagementException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean jsFunction_removeSubscriber(Context cx,
                                                      Scriptable thisObj, Object[] args, Function funObj) {
        String providerName = "";
        String apiName = "";
        String version = "";
        String application = "";
        String userId = "";
        if (isStringArray(args)) {
            providerName = args[0].toString();
            apiName = args[1].toString();
            version = args[2].toString();
            application = (String) args[3];
            userId = args[4].toString();
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setApplicationId(application);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscriber(apiIdentifier, userId);
            return true;
        } catch (APIManagementException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String providerName = args[0].toString();
            String apiName = args[1].toString();
            String version = args[2].toString();
            String user = args[3].toString();

            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            Subscriber subscriber = new Subscriber(user);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> apis = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier);
            int i = 0;
            for(SubscribedAPI api : apis) {
                NativeObject row = new NativeObject();
                row.put("application", row, api.getApplication().getName());
                row.put("applicationId", row, api.getApplication().getId());
                row.put("prodKey", row, getKey(api, APIConstants.API_KEY_TYPE_PRODUCTION));
                row.put("sandboxKey", row, getKey(api, APIConstants.API_KEY_TYPE_SANDBOX));
                myn.put(i++, myn, row);
            }
        }
        return myn;
    }

    private static String getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key.getKey();
            }
        }
        return null;
    }
    private static String getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key.getKey();
            }
        }
        return null;
    }

    public static NativeArray jsFunction_getAllSubscriptions(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if(args.length != 1 || !(args[0] instanceof String)) {
            return null;
        }
        String user = (String) args[0];
        Subscriber subscriber = new Subscriber(user);
        Map<Integer, NativeArray> subscriptionsMap = new HashMap<Integer, NativeArray>();
        NativeArray appsObj = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
        int i = 0;
        for(SubscribedAPI subscribedAPI : subscribedAPIs) {
            NativeArray apisArray = subscriptionsMap.get(subscribedAPI.getApplication().getId());
            if(apisArray == null) {
                apisArray = new NativeArray(1);
                NativeObject appObj = new NativeObject();
                appObj.put("id", appObj, subscribedAPI.getApplication().getId());
                appObj.put("name", appObj, subscribedAPI.getApplication().getName());
                appObj.put("prodKey", appObj, getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_PRODUCTION));
                appObj.put("sandboxKey", appObj, getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_SANDBOX));
                addAPIObj(subscribedAPI, apisArray, thisObj);
                appObj.put("subscriptions", appObj, apisArray);
                appsObj.put(i++, appsObj, appObj);
                //keep a subscriptions map in order to efficiently group appObj vice.
                subscriptionsMap.put(subscribedAPI.getApplication().getId(), apisArray);
            } else {
                addAPIObj(subscribedAPI, apisArray, thisObj);
            }
        }
        return appsObj;
    }

    private static void addAPIObj(SubscribedAPI subscribedAPI, NativeArray apisArray,
                                  Scriptable thisObj) throws ScriptException {
        NativeObject apiObj = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            API api = apiConsumer.getAPI(subscribedAPI.getApiId());
            apiObj.put("name", apiObj, subscribedAPI.getApiId().getApiName());
            apiObj.put("provider", apiObj, subscribedAPI.getApiId().getProviderName());
            apiObj.put("version", apiObj, subscribedAPI.getApiId().getVersion());
            apiObj.put("status", apiObj, api.getStatus().toString());
            apiObj.put("tier", apiObj, subscribedAPI.getTier().getName());
            apiObj.put("thumbnailurl", apiObj, api.getThumbnailUrl());
            apiObj.put("context", apiObj, api.getContext());
            apiObj.put("prodKey", apiObj, getKey(subscribedAPI, APIConstants.API_KEY_TYPE_PRODUCTION));
            apiObj.put("sandboxKey", apiObj, getKey(subscribedAPI, APIConstants.API_KEY_TYPE_SANDBOX));
            apiObj.put("hasMultipleEndpoints", apiObj, String.valueOf(api.getSandboxUrl() != null));
            apisArray.put(apisArray.getIds().length, apisArray, apiObj);
        } catch (APIManagementException e) {
            throw new ScriptException(e);
        }
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            NativeObject user = new NativeObject();
            String userName = args[0].toString();
            Subscriber subscriber;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                subscriber = apiConsumer.getSubscriber(userName);
            }catch (APIManagementException e) {
                log.error("Error from Registry API while getting Subscriber"
                        + e);
                return null;
            } catch (IllegalArgumentException e) {
                log.error("Error from Registry API while getting Subscriber "
                        + e);
                return null;
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting Subscriber"
                        + e);
                return null;
            } catch (Exception e) {
                log.error("Error while getting Subscriber " + e);
                return null;
            }

            if (subscriber == null) {
                return null;
            }
            user.put("name", user, subscriber.getName());
            user.put("id", user, subscriber.getId());
            user.put("email", user, subscriber.getEmail());
            user.put("subscribedDate", user, subscriber.getSubscribedDate());
            return user;
        }
        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            Subscriber subscriber = new Subscriber((String) args[0]);
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiConsumer.addSubscriber(subscriber);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while adding Subscriber"
                        + e);
                return false;
            } catch (IllegalArgumentException e) {
                log.error("Error from Registry API while adding Subscriber "
                        + e);
                return false;
            } catch (NullPointerException e) {
                log.error("Error from Registry API while adding Subscriber"
                        + e);
                return false;
            } catch (Exception e) {
                log.error("Error while adding Subscriber " + e);
                return false;
            }
            return true;
        }
        return false;
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

    public static boolean jsFunction_removeSubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws APIManagementException {
        if (args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        String username = args[0].toString();
        int applicationId = ((Number) args[1]).intValue();
        NativeObject apiData = (NativeObject) args[2];
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscription(apiId, username, applicationId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while removing the subscription of" + name + "-" + version, e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Applications
    /////////////////////////////////////////////////////////////////////////////////

    public static NativeArray jsFunction_getApplications(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String username = args[0].toString();
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications = apiConsumer.getApplications(new Subscriber(username));
            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    NativeObject row = new NativeObject();
                    row.put("name", row, application.getName());
                    row.put("id", row, application.getId());
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_addApplication(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Subscriber subscriber = new Subscriber(username);

            Application[] apps = apiConsumer.getApplications(subscriber);
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    handleException("A duplicate application already exists by the name - " + name);
                }
            }

            Application application = new Application(name, subscriber);
            apiConsumer.addApplication(application, username);
            return true;
        }
        return false;
    }

    public static boolean jsFunction_removeApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
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
        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
            int i = 0;
            for (SubscribedAPI api : subscribedAPIs) {
                if (api.getApplication().getName().equals(name)) {
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, api.getApiId().getApiName());
                    row.put("apiVersion", row, api.getApiId().getVersion());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_updateApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            String name = (String) args[0];
            String oldName = (String) args[1];
            String username = (String) args[2];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(oldName)) {
                    Application application = new Application(name, subscriber);
                    application.setId(app.getId());
                    apiConsumer.updateApplication(application);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeObject jsFunction_refreshToken(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws APIManagementException, AxisFault {

        NativeObject row = new NativeObject();
        if (isStringArray(args)) {
            String userId = (String) args[0];
            String applicationName = (String) args[1];
            String tokenType = (String) args[2];
            String oldAccessToken = (String) args[3];

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            //Check whether old access token is already available
            if (apiConsumer.isApplicationTokenExists(oldAccessToken)) {
                SubscriberKeyMgtClient keyMgtClient = getKeyManagementClient();
                ApplicationKeysDTO dto = new ApplicationKeysDTO();
                String accessToken;
                try {
                    //Regenerate the application access key
                    accessToken = keyMgtClient.regenerateApplicationAccessKey(tokenType, oldAccessToken);
                    if (accessToken != null) {
                        //If a new access token generated successfully,remove the old access token from cache.
                        APIAuthenticationServiceClient authKeyMgtClient = getAPIKeyManagementClient();
                        authKeyMgtClient.invalidateKey(oldAccessToken);
                        //Set newly generated application access token
                        dto.setApplicationAccessToken(accessToken);

                    }
                    row.put("accessToken", row, dto.getApplicationAccessToken());
                    row.put("consumerKey", row, dto.getConsumerKey());
                    row.put("consumerSecret", row, dto.getConsumerSecret());
                } catch (APIManagementException e) {
                    handleException("Error while refreshing the access token.", e);
                } catch (Exception e) {
                    handleException(e.getMessage(), e);
                }
            } else {
                handleException("Cannot regenerate a new access token. There's no access token available as : " + oldAccessToken);
            }

        } else {
            handleException("Invalid types of input parameters.");

        }
        return row;

    }*/


    /////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Subscriptions
    /////////////////////////////////////////////////////////////////////////////////

    public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String username = null;
        String methodName = "isSubscribed";
        int argsCount = args.length;
        if(argsCount != 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if (args[1] != null) {
            if (!(args[1] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
            }
            username = (String) args[1];
        }

        return username != null;
    }

    /*
    * getting key for API subscriber args[] list String subscriberID, String
    * api, String apiVersion, String Date
    */
    public static String jsFunction_getKey(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj) throws ScriptException {
        return "";
    }

    /*
	 * getting key for a subscribed Application - args[] list String subscriberID, String
	 * application name, String keyType
	 */
    public static String jsFunction_getApplicationKey(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj) throws ScriptException {
        return "";
    }

    public static NativeArray jsFunction_getAPIKey(Context cx, Scriptable thisObj,
                                                   Object[] args, Function funObj) throws ScriptException,
            APIManagementException {
        NativeArray myn = new NativeArray(0);
        NativeObject row = new NativeObject();
        myn.put(0, myn, row);
        return myn;
    }

    public static boolean jsFunction_addSubscription(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj) {
        return isStringArray(args);

    }

    public static boolean jsFunction_removeSubscriber(Context cx,
                                                      Scriptable thisObj, Object[] args, Function funObj) {
        return true;
    }

    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return new NativeArray(0);
    }

    public static NativeArray jsFunction_getAllSubscriptions(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if(args.length != 1 || !(args[0] instanceof String)) {
            return null;
        }
        return new NativeArray(0);
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return true;
    }

    public static boolean jsFunction_hasSubscribePermission(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        return false;
    }

    public static boolean jsFunction_removeSubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws APIManagementException {
        if (args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////// Functionality related to Applications
    /////////////////////////////////////////////////////////////////////////////////

    public static NativeArray jsFunction_getApplications(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return new NativeArray(0);
    }

    public static boolean jsFunction_addApplication(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return true;
    }

    public static boolean jsFunction_removeApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return true;
    }

    public static NativeArray jsFunction_getSubscriptionsByApplication(Context cx,
                                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return  new NativeArray(0);
    }

    public static boolean jsFunction_updateApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        return true;
    }

    public static NativeObject jsFunction_refreshToken(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws APIManagementException, AxisFault {

        return null;
    }
}
