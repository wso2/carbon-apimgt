package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyManagerMappingUtil {

    public static KeyManagerInfoDTO fromKeyManagerConfigurationDtoToKeyManagerInfoDto(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        KeyManagerInfoDTO keyManagerInfoDTO = new KeyManagerInfoDTO();
        keyManagerInfoDTO.setId(keyManagerConfigurationDTO.getUuid());
        keyManagerInfoDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerInfoDTO.setDescription(keyManagerConfigurationDTO.getDescription());
        keyManagerInfoDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerInfoDTO.setType(keyManagerConfigurationDTO.getType());
        JsonObject jsonObject = fromConfigurationMapToJson(keyManagerConfigurationDTO.getAdditionalProperties());
        JsonElement grantTypesElement = jsonObject.get(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE);
        if (grantTypesElement instanceof JsonArray) {
            keyManagerInfoDTO.setAvailableGrantTypes(new Gson().fromJson(grantTypesElement, List.class));
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION)){
            keyManagerInfoDTO.setEnableOAuthAppCreation(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION)){
            keyManagerInfoDTO.setEnableTokenEncryption(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_TOKEN_HASH)){
            keyManagerInfoDTO.setEnableTokenHashing(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_TOKEN_HASH).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.TOKEN_ENDPOINT)){
            keyManagerInfoDTO.setTokenEndpoint(
                    jsonObject.get(APIConstants.KeyManager.TOKEN_ENDPOINT).getAsString());
        }
        if (jsonObject.has(APIConstants.KeyManager.REVOKE_ENDPOINT)){
            keyManagerInfoDTO.setRevokeEndpoint(
                    jsonObject.get(APIConstants.KeyManager.REVOKE_ENDPOINT).getAsString());
        }
        return keyManagerInfoDTO;
    }

    public static JsonObject fromConfigurationMapToJson(Map configuration) {

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(new Gson().toJson(configuration));
        return jsonObject;
    }

    public static KeyManagerListDTO toKeyManagerListDto(List<KeyManagerConfigurationDTO> keyManagerConfigurations) {

        KeyManagerListDTO keyManagerListDTO = new KeyManagerListDTO();
        List<KeyManagerInfoDTO> keyManagerInfoDTOList = new ArrayList<>();
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
            keyManagerInfoDTOList.add(fromKeyManagerConfigurationDtoToKeyManagerInfoDto(keyManagerConfigurationDTO));
        }
        keyManagerListDTO.setList(keyManagerInfoDTOList);
        keyManagerListDTO.setCount(keyManagerInfoDTOList.size());
        return keyManagerListDTO;
    }
}
