function onGet(env) {
    sendToClient("swaggerURL", env.config.swaggerURL);
    sendToClient("contextPath", env.contextPath);
    return {env:env};
}