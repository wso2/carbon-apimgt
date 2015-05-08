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

package org.wso2.carbon.apimgt.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User aware APIProvider implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core API provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
public class UserAwareAPIProvider extends APIProviderImpl {

    private String username;

    UserAwareAPIProvider(String username) throws APIManagementException {
        super(username);
        this.username = username;
    }

    @Override
    public String addAPI(API api) throws APIManagementException {
        checkCreatePermission();
        return super.addAPI(api);
    }

    @Override
    public boolean createNewAPIVersion(JSONObject api, String newVersion) throws DuplicateAPIException,
            APIManagementException {
        checkCreatePermission();
        super.createNewAPIVersion(api, newVersion);
        return true;
    }

    @Override
    public Map<String, List<String>> updateAPI(API api) throws APIManagementException {
        checkCreatePermission();
        return super.updateAPI(api);
    }

    @Override
    public boolean deleteAPI(JSONObject identifier) throws APIManagementException {
        checkCreatePermission();
        return super.deleteAPI(identifier);
    }

    @Override
    public Map<String, List<String>> changeAPIStatus(API api, APIStatus status, String userId,
                                                     boolean updateGatewayConfig) throws APIManagementException {
        checkPublishPermission();
        return super.changeAPIStatus(api, status, userId, updateGatewayConfig);
    }

    @Override
    public void addDocumentation(JSONObject api,
                                 JSONObject documentation) throws APIManagementException {
        checkCreatePermission();
        super.addDocumentation(api, documentation);
    }

    @Override
    public boolean removeDocumentation(JSONObject apiId, String docName,
                                    String docType) throws APIManagementException {
        checkCreatePermission();
        super.removeDocumentation(apiId, docName, docType);
        return true;
    }

    @Override
    public boolean checkIfAPIExists(String providerName,String apiName,String version) throws APIManagementException {
        return super.checkIfAPIExists(providerName,apiName,version);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException {
        checkCreatePermission();
        super.updateDocumentation(apiId, documentation);
    }
   
    @Override
    public void addDocumentationContent(JSONObject api, String documentationName,
                                        String text) throws APIManagementException {
        checkCreatePermission();
        super.addDocumentationContent(api, documentationName, text);
    }

    @Override
    public String manageAPI(JSONObject api) throws APIManagementException {
        checkCreatePermission();
	    return  super.manageAPI(api);
    }

	@Override
	public String designAPI(JSONObject api) throws APIManagementException {
		checkCreatePermission();
		return  super.designAPI(api);
	}

    @Override
    public String updateDesignAPI(JSONObject api) throws APIManagementException {
        checkCreatePermission();
        return  super.updateDesignAPI(api);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {
        checkCreatePermission();
        super.copyAllDocumentation(apiId, toVersion);
    }

	public boolean isAPIAvailable(APIIdentifier api) throws APIManagementException {
		checkCreatePermission();
		return super.isAPIAvailable(api);
	}

	public boolean isApiNameExist(String apiName) throws APIManagementException {
		checkCreatePermission();
		return super.isApiNameExist(apiName);
	}

	public boolean isContextExist(String context) throws APIManagementException {
		checkCreatePermission();
		return super.isContextExist(context);
	}

	public Set<Tier> getTiers() throws APIManagementException {
		checkCreatePermission();
		return super.getTiers();
	}

	public void checkCreatePermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_CREATE);
    }
    
    public void checkManageTiersPermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.MANAGE_TIERS);
    }

    public void checkPublishPermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_PUBLISH);
    }
}
