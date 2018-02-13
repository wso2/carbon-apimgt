package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class APIInfoMappingUtil {

    /**
     * Converts a APIIdentifier object into APIInfoDTO
     *
     * @param apiId APIIdentifier object
     * @return APIInfoDTO corresponds to APIIdentifier object
     */
    private static APIInfoDTO fromAPIInfoToDTO(APIIdentifier apiId)
            throws UnsupportedEncodingException {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(APIUtil.replaceEmailDomainBack(apiId.getProviderName
                ()).replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER),
                URLEncoder.encode(apiId.getApiName(), RestApiConstants.CHARSET).replace(RestApiConstants
                        .API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER), apiId.getVersion().
                replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER));
        apiInfoDTO.setName(apiIdEmailReplacedBack.getApiName());
        apiInfoDTO.setVersion(apiIdEmailReplacedBack.getVersion());
        apiInfoDTO.setProvider(apiIdEmailReplacedBack.getProviderName());
        return apiInfoDTO;
    }

    /**
     * Converts a List object of APIIdentifiers into a DTO
     *
     * @param apiIds a list of APIIdentifier objects
     * @return APIInfoListDTO object containing APIInfoDTOs
     */
    public static APIInfoListDTO fromAPIInfoListToDTO(List<APIIdentifier> apiIds) throws
            UnsupportedEncodingException {
        APIInfoListDTO apiInfoListDTO = new APIInfoListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiInfoListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiInfoListDTO.setList(apiInfoDTOs);
        }
        for (APIIdentifier apiId : apiIds) {
            apiInfoDTOs.add(fromAPIInfoToDTO(apiId));
        }
        apiInfoListDTO.setCount(apiInfoDTOs.size());
        return apiInfoListDTO;
    }
}


