package org.wso2.carbon.apimgt.ballerina.deployment;

import ballerina.doc;

@doc:Description { value:"deployment service"}
@doc:Param { value:"fileName: path to the service file" }
@doc:Param { value:"config: ballerina source" }
@doc:Return { value:"string: status" }
native function deployService (string fileName, string config) (string);