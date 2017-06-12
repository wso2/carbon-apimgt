package org.wso2.carbon.apimgt.ballerina.deployment;

import ballerina.doc;

@doc:Description { value:"deployment service"}
@doc:Param { value:"fileName: path to the service file" }
@doc:Param { value:"serviceName: name of service" }
@doc:Param { value:"config: ballerina source" }
@doc:Param { value:"path: ballerina package" }
@doc:Return { value:"string: status" }

native function deployService (string fileName, string serviceName, string config, string path) (string);

@doc:Description { value:"file create"}
@doc:Param { value:"fileName: path to the service file" }
@doc:Param { value:"config: ballerina source" }
@doc:Param { value:"path: ballerina package" }
@doc:Return { value:"string: status" }
native function deploy (string fileName, string config,string path) (string);