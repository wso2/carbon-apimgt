
package org.wso2.carbon.apimgt.ballerina.threatprotection;

import ballerina.doc;


@doc:Description { value: "Analyzes payload for threats" }
@doc:Param { value: "payloadType: payload type (json/xml)" }
@doc:Param { value: "payload: json or xml payload to analyze" }
@doc:Param { value: "apiContext: api context" }
@doc:Param { value: "policyId" }
@doc:Return { value: "boolean: true of no threats detected, false otherwise" }
@doc:Return { value: "string: error information" }
native function analyze(string payloadType, string payload, string apiContext, string policyId) (boolean, string);

@doc:Description { value: "Configures the JSON analyzer" }
@doc:Param { value: "jsonInfo: ballerina struct containing JSONAnalyzer configurations" }
@doc:Param {value: "event: Threat Protection Policy event (add/delete/update"}
@doc:Return { value: "boolean: true if success, false otherwise" }
native function configureJsonAnalyzer(any jsonInfo, string event) (boolean);

@doc:Description { value: "Configures the XML analyzer" }
@doc:Param { value: "xmlInfo: ballerina struct containing XMLAnalyzer configurations" }
@doc:Param {value: "event: Threat Protection Policy event (add/delete/update"}
@doc:Return { value: "boolean: true if success, false otherwise" }
native function configureXmlAnalyzer(any xmlInfo, string event) (boolean);