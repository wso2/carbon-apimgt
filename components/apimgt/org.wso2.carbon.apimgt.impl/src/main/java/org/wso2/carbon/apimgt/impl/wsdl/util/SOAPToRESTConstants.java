/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.wsdl.util;

/**
 * Constants used for wsdl processing in soap to rest mapping.
 */
public class SOAPToRESTConstants {

    public static final String SOAP_TO_REST_RESOURCE = "soap_to_rest";

    public static final String COMPLEX_TYPE_NODE_NAME = "complexType";
    public static final String SIMPLE_TYPE_NODE_NAME = "simpleType";
    public static final String RESTRICTION_ATTR = "restriction";
    public static final String BASE_ATTR = "base";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String REF_ATTRIBUTE = "ref";
    public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
    public static final String MAX_OCCURS_ATTRIBUTE = "maxOccurs";
    public static final String UNBOUNDED = "unbounded";
    public static final String METHOD = "method";
    public static final String RESOURCE_ID = "id";
    public static final String RESOURCE_PATH = "path";
    public static final String PARAM_TYPE = "type";
    public static final String CONTENT = "content";
    public static final String EMPTY_STRING = "";
    public static final String SOAP_VERSION_11 = "1.1";
    public static final String SOAP_VERSION_12 = "1.2";
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_NAMSPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String TEXT_XML = "text/xml";
    public static final String ELEMENT_FORM_DEFAULT= "elementFormDefault";
    public static final String QUALIFIED = "qualified";
    public static final String X_NAMESPACE_QUALIFIED = "x-namespace-qualified";

    public final class Swagger {
        public static final String DEFINITIONS = "definitions";
        public static final String DEFINITIONS_ROOT = "#/definitions/";
        public static final String SCHEMA = "schema";
        public static final String REF = "$ref";
        public static final String BODY = "body";
        public static final String PROPERTIES =  "properties";
        public static final String PARAMETERS = "parameters";
        public static final String IN = "in";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String PATHS = "paths";
        public static final String ITEMS = "items";

        public static final String INPUT_POSTFIX = "Input";
        public static final String OUTPUT_POSTFIX = "Output";

        //vendor extension specific
        public static final String SOAP_ACTION = "soap-action";
        public static final String SOAP_OPERATION = "soap-operation";
        public static final String NAMESPACE = "namespace";
        public static final String WSO2_SOAP = "x-wso2-soap";
        public static final String SOAP_VERSION= "x-soap-version";

    }

    public final class ParamTypes {
        public static final String QUERY = "query";
        public static final String OBJECT = "object";
        public static final String ARRAY = "array";
    }

    public final class SequenceGen {
        public static final String XPATH = "x-path";
        public static final String INDENT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";
        public static final String INDENT_VALUE = "2";

        public static final String SOAP_TO_REST_IN_RESOURCE = "soap_to_rest/in";
        public static final String SOAP_TO_REST_OUT_RESOURCE = "soap_to_rest/out";
        public static final String XML_FILE_EXTENSION = ".xml";
        public static final String XML_FILE_RESOURCE_PREFIX = "\\.xml";
        public static final String RESOURCE_METHOD_SEPERATOR = "_";
        public static final String NEW_LINE_CHAR = "\n";
        public static final String NAMESPACE_SEPARATOR = ":";
        public static final String COMMA = ",";
        public static final String PATH_SEPARATOR = "/";

        public static final String NAMESPACE_PREFIX = "web";
        public static final String PROPERTY_NAME = "propertyName";
        public static final String PARAMETER_NAME = "paramName";
        public static final String PROPERTY_ACCESSOR = "$";

        public static final String ARG_ELEMENT = "arg";
        public static final String PROPERTY_ELEMENT = "property";
        public static final String EVALUATOR_ATTR = "evaluator";
        public static final String EXPRESSION_ATTR = "expression";
        public static final String EXPRESSION_FUNC_DEF = "get-property('req.var.";
        public static final String EXPRESSION_FUNC_DEF_CLOSING_TAG = "')";
        public static final String REQ_VARIABLE = "req.var.";
        public static final String URL_OPERATOR = "$url:";
        public static final String ROOT_OPERATOR = "$.";
        public static final String JSON_EVAL = "json-eval(";
        public static final String CLOSING_PARANTHESIS = ")";

        public static final String XML_FILE = "xml";
    }

    public final class Template {
        public static final String HTTP_METHOD = "method";
        public static final String SOAP_ACTION = "soapAction";
        public static final String SOAP_NAMESPACE = "soapNamespace";
        public static final String NAMESPACE = "namespace";
        public static final String RESOURCE_PATH = "resourcePath";
        public static final String MAPPING = "mapping";
        public static final String ARRAY_ELEMENTS = "arrayElements";
        public static final String IS_SOAP_TO_REST_MODE = "isSoapToRestMode";
        public static final String IN_SEQUENCES = "in_sequences";
        public static final String OUT_SEQUENCES = "out_sequences";
        public static final String NOT_DEFINED = "not-defined";
    }
}
