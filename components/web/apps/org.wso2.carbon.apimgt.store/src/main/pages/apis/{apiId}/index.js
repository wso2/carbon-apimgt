//noinspection JSUnusedGlobalSymbols
function onRequest(env) {
    sendToClient("swaggerURL", env.config.swaggerURL);
    return {"apiId":env.pathParams['apiId']};
}

