package org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.models as models;

struct LabelInfoDto {
    models:Label [] labelList;
    boolean overwriteLabels;
}
