package org.wso2.carbon.apimgt.gateway.utils;

import ballerina.lang.system;

json apis = {"count":2,"list":[{"id":"314214e7-2a0d-4ab5-9edb-d94ab7b0968b","name":"SwaggerPetstore","context":"/petstore","version":"/petstore","lifeCycleStatus":"Published"},{"id":"71986bb1-3a09-46fe-b255-515122e6f73c","name":"MyAPI","context":"/myapi","version":"1.0.0","lifeCycleStatus":"Published"}]};
json endPoints;
json blockConditions;

function main (string[] args) {
    system:println("returnOfflineAPIs");
}

function getOfflineAPIList()(json){
    return apis;
}

function setOfflineAPIList(json APIs){
    //have to feed the array from a pre call

}

function getOfflineEndPoints()(json){
    return endPoints;
}

function setOfflineEndPoints(json APIs){}

function getBlockConditionList()(json){
    return blockConditions;
}

function setBlockConditionList(json APIs){}