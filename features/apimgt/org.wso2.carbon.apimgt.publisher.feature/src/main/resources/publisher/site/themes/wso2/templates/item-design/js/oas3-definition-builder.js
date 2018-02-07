function OpenAPI3() {
}

OpenAPI3.prototype.get_parameter_definition = function (paramName, paramIn, paramRequired, paramType) {
    return {
        "name": paramName,
        "in": paramIn,
        "required": paramRequired,
        "schema": {"type": paramType}
    }
}

OpenAPI3.prototype.add_default_request_body = function (resource) {
    var default_request_body =
        {
            "content": {
                "application/json": {
                    "schema": {
                        "type": "object",
                        "properties": {
                            "payload": {"type": "string"}
                        }
                    }
                }
            },
            "required": true,
            "description": "Request Body"
        };
    resource.requestBody = default_request_body;
}

OpenAPI3.prototype.get_param_types = function (isBodyRequired) {
    return [
        {value: "query", text: "query"},
        {value: "header", text: "header"}];
}

OpenAPI3.prototype.update_element = function (element, obj, newValue) {
    var i = $(element).attr('data-attr');
    if (i != "body" && i != "formData" && i != "consumes" && i != "produces") {
        if (i == "type") {
            if (obj.schema == undefined) obj.schema = {};
            obj.schema[i] = newValue;
        } else if (i == "content-type") {
            var key = $(element).attr('data-index');
            obj[newValue] = obj[key];
            delete obj[key];
        }
        else {
            obj[i] = newValue;
        }
    }
}