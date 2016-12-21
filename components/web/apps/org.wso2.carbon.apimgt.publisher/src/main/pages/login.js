//noinspection JSUnusedGlobalSymbols
function onRequest(env) {
    //TODO get the whole config file and pass it to client side
    //var JSONObject = new Java.type("org.json.JSONObject");
    sendToClient("loginRedirectUri",  env.config.loginRedirectUri);
    sendToClient("loginPageUri",  env.config.loginPageUri);
    sendToClient("contextPath",  env.contextPath);
    sendToClient("uri",  env.request.uri);
    return {env:env};
}
