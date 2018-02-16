package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.DedicatedGateway;
import org.wso2.carbon.apimgt.rest.api.store.dto.DedicatedGatewayDTO;

public class DedicatedGatewayMappingUtil {


    /**
     * This method maps the the DedicatedGateway object to DedicatedGatewayDTO
     *
     * @param dedicatedGateway  DedicatedGateway object
     * @return Dedicated Gateway Object
     */
    public static DedicatedGatewayDTO toDedicatedGatewayDTO(DedicatedGateway dedicatedGateway) {

        DedicatedGatewayDTO dedicatedGatewayDTO = new DedicatedGatewayDTO();
        dedicatedGatewayDTO.setIsEnabled(dedicatedGateway.isEnabled());
        return dedicatedGatewayDTO;
    }

    /**
     * This method maps the the DedicatedGatewayDTO object to DedicatedGateway Object
     *
     * @param dedicatedGatewayDTO contains data of DedicatedGateway
     * @param apiId UUID of the API
     * @return Dedicated Gateway Object
     */
    public static DedicatedGateway fromDTOtoDedicatedGateway(DedicatedGatewayDTO dedicatedGatewayDTO, String apiId) {

        DedicatedGateway dedicatedGateway = new DedicatedGateway();
        dedicatedGateway.setApiId(apiId);
        if (dedicatedGatewayDTO.getIsEnabled() != null) {
            dedicatedGateway.setEnabled(dedicatedGatewayDTO.getIsEnabled());
        } else {
            dedicatedGateway.setEnabled(false);
        }
        return dedicatedGateway;

    }
}
