function onGet(env) {
    sendToClient("appName", env.config.appName);
}