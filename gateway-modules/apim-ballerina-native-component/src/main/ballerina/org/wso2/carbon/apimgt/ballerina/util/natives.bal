package org.wso2.carbon.apimgt.ballerina.util;

import ballerina.doc;

@doc:Description { value:"Wait utility"}
@doc:Param { value:"value: wait time in miliseconds" }
@doc:Return { value:"int: status" }
native function wait (int value) (int);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"msg: The current message object" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (message msg, string propertyName, any propertyValue);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"msg: The current message object" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (message msg, string propertyName) (any);

@doc:Description { value:"Convert String to Json"}
@doc:Param { value:"string: Current String object" }
@doc:Return { value:"json: json value" }
native function parse (string value) (json);

@doc:Description { value:"Convert ip to long"}
@doc:Param { value:"string: Current String value of ip" }
@doc:Return { value:"int: integer value" }
native function convertIpToLong (string value) (int );