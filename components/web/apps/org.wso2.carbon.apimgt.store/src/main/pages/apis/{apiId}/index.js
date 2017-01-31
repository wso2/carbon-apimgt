//noinspection JSUnusedGlobalSymbols
function onGet(env) {
    sendToClient("swaggerURL", env.config.swaggerURL);
    return {"apiId":env.pathParams['apiId']};
}

