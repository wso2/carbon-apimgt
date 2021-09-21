package org.wso2.carbon.apimgt.solace.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Map;

public class SolaceNotifierUtils {


    /**
     * Get and patch client id for Solace application
     *
     * @return SolaceAdminApis  object to invoke Solace
     * @throws APIManagementException If the Solace env configuration if not provided properly
     */
    public static SolaceAdminApis getSolaceAdminApis()
            throws APIManagementException {
        Map<String, Environment> thirdPartyEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        Environment solaceEnvironment = null;

        for (Map.Entry<String, Environment> entry : thirdPartyEnvironments.entrySet()) {
            if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }

        if (solaceEnvironment != null) {
            return new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.
                    getUserName(), solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().
                    get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME));
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

}
