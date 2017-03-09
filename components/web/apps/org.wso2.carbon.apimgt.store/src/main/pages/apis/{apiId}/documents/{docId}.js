//noinspection JSUnusedGlobalSymbols
function onGet(env) {
    return {"apiId":env.pathParams['apiId'], "docId":env.pathParams['docId']};
}

