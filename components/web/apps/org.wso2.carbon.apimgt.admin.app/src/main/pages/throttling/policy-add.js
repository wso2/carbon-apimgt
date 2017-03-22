function onGet(env) {
    print(JSON.parse(env.request).query.split("=")[0]);
    sendToClient("policyQuery", JSON.parse(env.request).query.split("=")[0]);
}