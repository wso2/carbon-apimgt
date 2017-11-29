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

@doc:Description { value:"Return JSON files in a given folder"}
@doc:Param { value:"string: Path of the folder" }
@doc:Return { value:"string[]: array containing json file names" }
native function listJSONFiles (string folderPath) (any );

@doc:Description { value:"Returns an array of keys contained in the specified JSON."}
@doc:Param { value:"json: A JSON object" }
@doc:Return { value:"string[]: A string array of keys contained in the specified JSON" }
native function getKeys(json j) (string[]);