package org.wso2.carbon.apimgt.ballerina.publisher;

import ballerina.doc;

connector EventConnector (map options) {
    map sharedMap = {};
    @doc:Description {value:"publishing event to the analyzer by providing the connrector"}
    @doc:Param {value:"c: conector instance"}
    @doc:Param {value:"event: json with payload attibutes"}
    native action publish (EventConnector c, json event);
}
