function onGet(env) {
    sendToClient("policyQuery", JSON.parse(env.request).query.split("=")[0]);
}