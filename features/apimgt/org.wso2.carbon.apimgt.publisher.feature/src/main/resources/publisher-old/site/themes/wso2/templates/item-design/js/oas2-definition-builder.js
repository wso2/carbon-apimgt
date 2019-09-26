function OpenAPI2() {
}

OpenAPI2.prototype.get_parameter_definition = function (param_name, param_in, param_required, param_type) {
    return {
        "name": param_name,
        "in": param_in,
        "required": param_required,
        "type": param_type
    }
}

OpenAPI2.prototype.add_default_request_body = function (resource) {

    var parameterList = resource.parameters || [];
    var default_body_param = {
        "name": "Payload",
        "description": "Request Body",
        "required": false,
        "in": "body",
        "schema": {
            "type": "object",
            "properties": {
                "payload": {
                    "type": "string"
                }
            }
        }
    };
    parameterList.push(default_body_param);
    resource.parameters = parameterList;
}

OpenAPI2.prototype.get_param_types = function (isBodyRequired) {
    if (isBodyRequired) {
        return [
            {value: "body", text: "body"},
            {value: "query", text: "query"},
            {value: "header", text: "header"},
            {value: "formData", text: "formData"}];
    } else {
        return [
            {value: "query", text: "query"},
            {value: "header", text: "header"},
            {value: "formData", text: "formData"}];
    }
}

OpenAPI2.prototype.update_element = function (element, obj, newValue) {
    var i = $(element).attr('data-attr');
    obj[i] = newValue;
    if (i == "in") {
        //Add body parameter to the swagger
        if (newValue == "body") {
            var swaggerSchema = JSON.parse('{"type" :"object"}');
            delete obj.type;
            obj['schema'] = swaggerSchema;
        } else { //other parameters
            delete obj.schema;
            obj['type'] = "string";
        }
    }
}
