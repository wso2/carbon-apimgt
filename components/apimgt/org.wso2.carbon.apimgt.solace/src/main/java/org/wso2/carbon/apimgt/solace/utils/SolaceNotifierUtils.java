package org.wso2.carbon.apimgt.solace.utils;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiChannelItem;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    /**
     * Get available transport protocols for the Solace API
     *
     * @param definition Solace API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the Solace env configuration if not provided properly
     */
    public static List<String> getTransportProtocolsForSolaceAPI(String definition) throws APIManagementException {
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(definition);
        SolaceAdminApis solaceAdminApis = getSolaceAdminApis();
        HashSet<String> solaceTransportProtocols = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            solaceTransportProtocols.addAll(solaceAdminApis.getProtocols(channel));
        }
        ArrayList<String> solaceTransportProtocolsList = new ArrayList<>(solaceTransportProtocols);
        return solaceTransportProtocolsList;
    }

}
