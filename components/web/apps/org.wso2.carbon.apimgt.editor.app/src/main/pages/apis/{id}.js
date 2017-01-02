//noinspection JSUnusedGlobalSymbols
function onRequest(env) {
    return {"tags": getTags(env.pathParams['id'])};
}

function getTags(petName) {
    return ['white', 'short-hair'];
}