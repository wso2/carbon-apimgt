package org.wso2.carbon.apimgt.gateway.service;

import org.wso2.carbon.apimgt.gateway.jwt.InternalRevokedJWTDataHolder;
import org.wso2.carbon.apimgt.impl.token.RevokedUserEventService;

public class RevokedUserEventServiceImpl implements RevokedUserEventService {
    @Override
    public void addUserEventIntoMap(String userUUID, long revocationTime) {
        InternalRevokedJWTDataHolder.getInstance().addInternalRevokedJWTUserIDToMap(userUUID, revocationTime);
    }
}
