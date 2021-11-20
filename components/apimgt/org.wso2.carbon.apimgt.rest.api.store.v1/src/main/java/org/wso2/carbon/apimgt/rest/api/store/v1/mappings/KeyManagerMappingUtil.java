package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerApplicationConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerListDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyManagerMappingUtil {

    public static KeyManagerInfoDTO fromKeyManagerConfigurationDtoToKeyManagerInfoDto(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        KeyManagerInfoDTO keyManagerInfoDTO = new KeyManagerInfoDTO();
        keyManagerInfoDTO.setId(keyManagerConfigurationDTO.getUuid());
        keyManagerInfoDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerInfoDTO.setDescription(keyManagerConfigurationDTO.getDescription());
        keyManagerInfoDTO.setDisplayName(keyManagerConfigurationDTO.getDisplayName());
        keyManagerInfoDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerInfoDTO.setType(keyManagerConfigurationDTO.getType());
        keyManagerInfoDTO.setAlias(keyManagerConfigurationDTO.getAlias());
        keyManagerInfoDTO.setTokenType(KeyManagerInfoDTO.TokenTypeEnum.valueOf(keyManagerConfigurationDTO.getTokenType()));
        JsonObject jsonObject = fromConfigurationMapToJson(keyManagerConfigurationDTO.getAdditionalProperties());
        JsonElement grantTypesElement = jsonObject.get(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE);
        if (grantTypesElement instanceof JsonArray) {
            keyManagerInfoDTO.setAvailableGrantTypes(new Gson().fromJson(grantTypesElement, List.class));
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION)){
            keyManagerInfoDTO.setEnableOAuthAppCreation(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION)){
            keyManagerInfoDTO.setEnableTokenGeneration(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_MAP_OAUTH_CONSUMER_APPS)){
            keyManagerInfoDTO.setEnableMapOAuthConsumerApps(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_MAP_OAUTH_CONSUMER_APPS).getAsBoolean());
        }

        if (jsonObject.has(APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION)){
            keyManagerInfoDTO.setEnableTokenEncryption(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.ENABLE_TOKEN_HASH)){
            keyManagerInfoDTO.setEnableTokenHashing(
                    jsonObject.get(APIConstants.KeyManager.ENABLE_TOKEN_HASH).getAsBoolean());
        }
        if (jsonObject.has(APIConstants.KeyManager.DISPLAY_TOKEN_ENDPOINT) &&
                !jsonObject.get(APIConstants.KeyManager.DISPLAY_TOKEN_ENDPOINT).isJsonNull() &&
                !jsonObject.get(APIConstants.KeyManager.DISPLAY_TOKEN_ENDPOINT).getAsString().trim().isEmpty()) {
            keyManagerInfoDTO.setTokenEndpoint(
                    jsonObject.get(APIConstants.KeyManager.DISPLAY_TOKEN_ENDPOINT).getAsString());
        } else {
            if (jsonObject.has(APIConstants.KeyManager.TOKEN_ENDPOINT)){
                keyManagerInfoDTO.setTokenEndpoint(
                        jsonObject.get(APIConstants.KeyManager.TOKEN_ENDPOINT).getAsString());
            }
        }
        if (jsonObject.has(APIConstants.KeyManager.DISPLAY_REVOKE_ENDPOINT) &&
                !jsonObject.get(APIConstants.KeyManager.DISPLAY_REVOKE_ENDPOINT).isJsonNull() &&
                !jsonObject.get(APIConstants.KeyManager.DISPLAY_REVOKE_ENDPOINT).getAsString().trim().isEmpty()) {
            keyManagerInfoDTO.setRevokeEndpoint(
                    jsonObject.get(APIConstants.KeyManager.DISPLAY_REVOKE_ENDPOINT).getAsString());
        } else {
            if (jsonObject.has(APIConstants.KeyManager.REVOKE_ENDPOINT)) {
                keyManagerInfoDTO.setRevokeEndpoint(
                        jsonObject.get(APIConstants.KeyManager.REVOKE_ENDPOINT).getAsString());
            }
        }
        Map<String,String> additionalProperties = new HashMap<>();
        if (keyManagerConfigurationDTO.getAdditionalProperties()
                .containsKey(APIConstants.KeyManager.PRODUCTION_TOKEN_ENDPOINT)) {
            additionalProperties.put(APIConstants.KeyManager.PRODUCTION_TOKEN_ENDPOINT,
                    (String) keyManagerConfigurationDTO.getAdditionalProperties()
                            .get(APIConstants.KeyManager.PRODUCTION_TOKEN_ENDPOINT));
        }
        if (keyManagerConfigurationDTO.getAdditionalProperties()
                .containsKey(APIConstants.KeyManager.PRODUCTION_REVOKE_ENDPOINT)) {
            additionalProperties.put(APIConstants.KeyManager.PRODUCTION_REVOKE_ENDPOINT,
                    (String) keyManagerConfigurationDTO.getAdditionalProperties()
                            .get(APIConstants.KeyManager.PRODUCTION_REVOKE_ENDPOINT));
        }
        if (keyManagerConfigurationDTO.getAdditionalProperties()
                .containsKey(APIConstants.KeyManager.SANDBOX_TOKEN_ENDPOINT)) {
            additionalProperties.put(APIConstants.KeyManager.SANDBOX_TOKEN_ENDPOINT,
                    (String) keyManagerConfigurationDTO.getAdditionalProperties()
                            .get(APIConstants.KeyManager.SANDBOX_TOKEN_ENDPOINT));
        }
        if (keyManagerConfigurationDTO.getAdditionalProperties()
                .containsKey(APIConstants.KeyManager.SANDBOX_REVOKE_ENDPOINT)) {
            additionalProperties.put(APIConstants.KeyManager.SANDBOX_REVOKE_ENDPOINT,
                    (String) keyManagerConfigurationDTO.getAdditionalProperties()
                            .get(APIConstants.KeyManager.SANDBOX_REVOKE_ENDPOINT));
        }
        keyManagerInfoDTO.setAdditionalProperties(additionalProperties);
        keyManagerInfoDTO
                .setApplicationConfiguration(fromKeyManagerConfigurationDto(keyManagerConfigurationDTO.getType()));
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

    private static List<KeyManagerApplicationConfigurationDTO> fromKeyManagerConfigurationDto(String type) {

        List<KeyManagerApplicationConfigurationDTO> keyManagerApplicationConfigurationDTOS = new ArrayList<>();
        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration =
                APIUtil.getKeyManagerConnectorConfigurationsByConnectorType(type);
        if (keyManagerConnectorConfiguration != null &&
                keyManagerConnectorConfiguration.getApplicationConfigurations() != null) {
            for (ConfigurationDto configurationDto : keyManagerConnectorConfiguration.getApplicationConfigurations()) {
                KeyManagerApplicationConfigurationDTO keyManagerApplicationConfigurationDTO =
                        new KeyManagerApplicationConfigurationDTO();
                keyManagerApplicationConfigurationDTO.setName(configurationDto.getName());
                keyManagerApplicationConfigurationDTO.setLabel(configurationDto.getLabel());
                keyManagerApplicationConfigurationDTO.setType(configurationDto.getType());
                keyManagerApplicationConfigurationDTO.setRequired(configurationDto.isRequired());
                keyManagerApplicationConfigurationDTO.setMask(configurationDto.isMask());
                keyManagerApplicationConfigurationDTO.setMultiple(configurationDto.isMultiple());
                keyManagerApplicationConfigurationDTO.setTooltip(configurationDto.getTooltip());
                keyManagerApplicationConfigurationDTO.setDefault(configurationDto.getDefaultValue());
                keyManagerApplicationConfigurationDTO.setValues(configurationDto.getValues());
                keyManagerApplicationConfigurationDTOS.add(keyManagerApplicationConfigurationDTO);
            }
        }
        return keyManagerApplicationConfigurationDTOS;
    }
}
