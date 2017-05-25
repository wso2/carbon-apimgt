function onGet(env) {
    return {editMode : true,createMode : false,globalLevel : true,id:env.pathParams['id']};
}
