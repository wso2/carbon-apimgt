package org.wso2.carbon.apimgt.impl.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrganizationGatewayDto {
    private Map<String, GatewayDto> gatewayMap = new LinkedHashMap<>();

    public Map<String, GatewayDto> getGatewayMap() {
        return gatewayMap;
    }

    public void setGatewayMap(Map<String, GatewayDto> gatewayMap) {
        this.gatewayMap = gatewayMap;
    }

    public void putGatewayDto(GatewayDto gatewayDto) {
        gatewayMap.put(gatewayDto.getName(), gatewayDto);
    }

    public GatewayDto getGatewayByName(String name) {
        return gatewayMap.get(name);
    }

    public void removeGatewayDtoByName(String name) {

        GatewayDto gatewayDto = gatewayMap.get(name);
        if (gatewayDto != null) {
            gatewayMap.remove(name);
        }
    }
}
