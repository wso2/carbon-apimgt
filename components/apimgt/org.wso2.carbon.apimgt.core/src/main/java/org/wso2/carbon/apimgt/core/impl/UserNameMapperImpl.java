/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.UserNameMapper;
import org.wso2.carbon.apimgt.core.dao.UserMappingDAO;
import org.wso2.carbon.apimgt.core.dao.impl.UserMappingDAOImpl;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

/**
 * This class is having implementation of UserNameMapping interface.
 * Users can use instance of this class to get logged in user-id(whatever unique is to identify user) using provided
 * pseudo name. Also to get logged in user's pseudo name(pseudo name used within APIM domain) using provided
 * user id.
 */


public class UserNameMapperImpl implements UserNameMapper {
    private static final Logger log = LoggerFactory.getLogger(UserNameMapperImpl.class);
    private UserMappingDAO userMappingDAO;

    /**
     * Default constructor if user do not need to pass DAO implementation specifically.
     */
    public UserNameMapperImpl() {
    }
    /**
     * Initiate user name mapper with provided Data Access Object
     *
     * @param userMappingDAO Data access object to be used to retrieve user mapping.
     */
    public UserNameMapperImpl(UserMappingDAO userMappingDAO) {
        this.userMappingDAO = userMappingDAO;
    }

    /**
     * This method will return logged in user-id(whatever unique is to identify user) using provided
     * pseudo name.
     *
     * @param pseudoName pseudo name of the user.
     * @return user id of matching user in the system.
     */
    @Override
    public String getLoggedInUserIDFromPseudoName(String pseudoName) throws APIManagementException {
        //TODO implement caching layer
        //If pseudo name in map then get from that
        //Else check mapping in database and load it to local map.
        //If mapping is not in database then add it to db and cache both.
        //then return name.
        if (pseudoName != null && pseudoName.equalsIgnoreCase(APIMgtConstants.ADMIN_STRING)) {
            //APIPublisher Implementation checking roles of logged in user as follows.
            //               roles = new HashSet<>(getIdentityProvider().getRoleIdsOfUser(userId));
            //Default auth implementation does not support role retrieval and it gives error due to that.
            //To avoid that there is special admin check which we need to remove. Once we are done with that
            //we can remove this admin skip logic
            return pseudoName;
        } else {
            try {
                return getUserMappingDAO().getUserIDByPseudoName(pseudoName);
            } catch (APIMgtDAOException e) {
                throw new APIManagementException("Error while user getting user details for user : " + pseudoName, e);
            }
        }
    }

    /**
     * This method will return logged in user's pseudo name(pseudo name used within APIM domain) using provided
     * user id.
     *
     * @param userID user identifier of the user who need pseudo name.
     * @return pseudo name of the matching user.
     */
    @Override
    public String getLoggedInPseudoNameFromUserID(String userID) throws APIManagementException {
        //TODO implement caching layer
        //If userName in map then get from that
        //Else check mapping in database and load it to local map.
        //then return name.
        if (userID != null && userID.equalsIgnoreCase(APIMgtConstants.ADMIN_STRING)) {
            //APIPublisher Implementation checking roles of logged in user as follows.
            // Line 1326 : roles = new HashSet<>(getIdentityProvider().getRoleIdsOfUser(userId));
            //Default auth implementation does not support role retrieval and it gives error due to that.
            //To avoid that there is special admin check which we need to remove. Once we are done with that
            //we can remove this admin skip logic
            return userID;
        } else {
            try {
                return getUserMappingDAO().getPseudoNameByUserID(userID);
            } catch (APIMgtDAOException e) {
                //Should not log real user identity due to any reason.
                throw new APIManagementException("Error while user getting user details for user : XXX", e);
            }
        }
    }


    private UserMappingDAO getUserMappingDAO() {
        if (this.userMappingDAO != null) {
            return this.userMappingDAO;
        } else {
            return new UserMappingDAOImpl();
        }
    }
}
