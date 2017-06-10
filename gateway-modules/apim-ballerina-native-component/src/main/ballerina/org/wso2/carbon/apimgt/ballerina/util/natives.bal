package org.wso2.carbon.apimgt.ballerina.util;

import ballerina.doc;

@doc:Description { value:"Wait utility"}
@doc:Param { value:"value: wait time in miliseconds" }
@doc:Return { value:"int: status" }
native function wait (int value) (int);