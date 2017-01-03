//noinspection JSUnusedGlobalSymbols
function onRequest(env) {
    return {"apiId":env.pathParams['apiId'], "docId":env.pathParams['docId']};
}

