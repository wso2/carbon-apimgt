package org.wso2.carbon.apimgt.gateway.utils;

import ballerina.lang.system;

json apis = {"count":2,"list":[{"id":"f986efa1-6e97-4be9-8bdf-9a588cc07864","name":"SwaggerPetstore","context":"/petstore","version":"1.0.0","lifeCycleStatus":"Published"},{"id":"6170fba0-7c28-4235-a616-1a81d06ad84d","name":"MyAPI","context":"/myapi","version":"1.0.0","lifeCycleStatus":"Published"}]};
json endPoints;
json blockConditions;
json subscriptions;
string apiConfig;

function main (string[] args) {
    system:println("returnOfflineAPIs");
}

function getOfflineAPIList()(json){
    return apis;
}

function setOfflineAPIList(json APIs){
    //have to feed the array from a pre call
}

function getOfflineEndPointList()(json){
    return endPoints;
}

function setOfflineEndPointList(json APIs){}

function getBlockConditionList()(json){
    return blockConditions;
}

function setBlockConditionList(json APIs){}

function getSubscriptionList()(json){
    return blockConditions;
}

function setSubscriptionList(json APIs){}