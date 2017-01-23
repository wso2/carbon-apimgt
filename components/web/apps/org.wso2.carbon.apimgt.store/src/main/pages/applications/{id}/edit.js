//noinspection JSUnusedGlobalSymbols
function onGet(env) {
    sendToClient("swaggerURL", env.config.swaggerURL);
    return {"applicationId":env.pathParams['id']};
}

