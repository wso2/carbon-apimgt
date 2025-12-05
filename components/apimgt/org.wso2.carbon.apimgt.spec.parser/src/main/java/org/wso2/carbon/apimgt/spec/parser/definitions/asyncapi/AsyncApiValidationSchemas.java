/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi;

public final class AsyncApiValidationSchemas {

    public static final String METASCHEMA = "{\n" +
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "    \"$id\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "    \"title\": \"Core schema meta-schema\",\n" +
            "    \"definitions\": {\n" +
            "        \"schemaArray\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": { \"$ref\": \"#\" }\n" +
            "        },\n" +
            "        \"nonNegativeInteger\": {\n" +
            "            \"type\": \"integer\",\n" +
            "            \"minimum\": 0\n" +
            "        },\n" +
            "        \"nonNegativeIntegerDefault0\": {\n" +
            "            \"allOf\": [\n" +
            "                { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
            "                { \"default\": 0 }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"simpleTypes\": {\n" +
            "            \"enum\": [\n" +
            "                \"array\",\n" +
            "                \"boolean\",\n" +
            "                \"integer\",\n" +
            "                \"null\",\n" +
            "                \"number\",\n" +
            "                \"object\",\n" +
            "                \"string\"\n" +
            "            ]\n" +
            "        },\n" +
            "        \"stringArray\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": { \"type\": \"string\" },\n" +
            "            \"uniqueItems\": true,\n" +
            "            \"default\": []\n" +
            "        }\n" +
            "    },\n" +
            "    \"type\": [\"object\", \"boolean\"],\n" +
            "    \"properties\": {\n" +
            "        \"$id\": {\n" +
            "            \"type\": \"string\",\n" +
            "            \"format\": \"uri-reference\"\n" +
            "        },\n" +
            "        \"$schema\": {\n" +
            "            \"type\": \"string\",\n" +
            "            \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"$ref\": {\n" +
            "            \"type\": \"string\",\n" +
            "            \"format\": \"uri-reference\"\n" +
            "        },\n" +
            "        \"$comment\": {\n" +
            "            \"type\": \"string\"\n" +
            "        },\n" +
            "        \"title\": {\n" +
            "            \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "            \"type\": \"string\"\n" +
            "        },\n" +
            "        \"default\": true,\n" +
            "        \"readOnly\": {\n" +
            "            \"type\": \"boolean\",\n" +
            "            \"default\": false\n" +
            "        },\n" +
            "        \"writeOnly\": {\n" +
            "            \"type\": \"boolean\",\n" +
            "            \"default\": false\n" +
            "        },\n" +
            "        \"examples\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": true\n" +
            "        },\n" +
            "        \"multipleOf\": {\n" +
            "            \"type\": \"number\",\n" +
            "            \"exclusiveMinimum\": 0\n" +
            "        },\n" +
            "        \"maximum\": {\n" +
            "            \"type\": \"number\"\n" +
            "        },\n" +
            "        \"exclusiveMaximum\": {\n" +
            "            \"type\": \"number\"\n" +
            "        },\n" +
            "        \"minimum\": {\n" +
            "            \"type\": \"number\"\n" +
            "        },\n" +
            "        \"exclusiveMinimum\": {\n" +
            "            \"type\": \"number\"\n" +
            "        },\n" +
            "        \"maxLength\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
            "        \"minLength\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
            "        \"pattern\": {\n" +
            "            \"type\": \"string\",\n" +
            "            \"format\": \"regex\"\n" +
            "        },\n" +
            "        \"additionalItems\": { \"$ref\": \"#\" },\n" +
            "        \"items\": {\n" +
            "            \"anyOf\": [\n" +
            "                { \"$ref\": \"#\" },\n" +
            "                { \"$ref\": \"#/definitions/schemaArray\" }\n" +
            "            ],\n" +
            "            \"default\": true\n" +
            "        },\n" +
            "        \"maxItems\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
            "        \"minItems\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
            "        \"uniqueItems\": {\n" +
            "            \"type\": \"boolean\",\n" +
            "            \"default\": false\n" +
            "        },\n" +
            "        \"contains\": { \"$ref\": \"#\" },\n" +
            "        \"maxProperties\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
            "        \"minProperties\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
            "        \"required\": { \"$ref\": \"#/definitions/stringArray\" },\n" +
            "        \"additionalProperties\": { \"$ref\": \"#\" },\n" +
            "        \"definitions\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
            "            \"default\": {}\n" +
            "        },\n" +
            "        \"properties\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
            "            \"default\": {}\n" +
            "        },\n" +
            "        \"patternProperties\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
            "            \"propertyNames\": { \"format\": \"regex\" },\n" +
            "            \"default\": {}\n" +
            "        },\n" +
            "        \"dependencies\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"additionalProperties\": {\n" +
            "                \"anyOf\": [\n" +
            "                    { \"$ref\": \"#\" },\n" +
            "                    { \"$ref\": \"#/definitions/stringArray\" }\n" +
            "                ]\n" +
            "            }\n" +
            "        },\n" +
            "        \"propertyNames\": { \"$ref\": \"#\" },\n" +
            "        \"const\": true,\n" +
            "        \"enum\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": true,\n" +
            "            \"uniqueItems\": true\n" +
            "        },\n" +
            "        \"type\": {\n" +
            "            \"anyOf\": [\n" +
            "                { \"$ref\": \"#/definitions/simpleTypes\" },\n" +
            "                {\n" +
            "                    \"type\": \"array\",\n" +
            "                    \"items\": { \"$ref\": \"#/definitions/simpleTypes\" },\n" +
            "                    \"uniqueItems\": true\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"format\": { \"type\": \"string\" },\n" +
            "        \"contentMediaType\": { \"type\": \"string\" },\n" +
            "        \"contentEncoding\": { \"type\": \"string\" },\n" +
            "        \"if\": { \"$ref\": \"#\" },\n" +
            "        \"then\": { \"$ref\": \"#\" },\n" +
            "        \"else\": { \"$ref\": \"#\" },\n" +
            "        \"allOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
            "        \"anyOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
            "        \"oneOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
            "        \"not\": { \"$ref\": \"#\" }\n" +
            "    },\n" +
            "    \"default\": true\n" +
            "}";

    public static final String ASYNCAPI_JSON_HYPERSCHEMA = "{\n" +
            "  \"title\": \"AsyncAPI 2.0.0 schema.\",\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"required\": [\n" +
            "    \"asyncapi\",\n" +
            "    \"info\",\n" +
            "    \"channels\"\n" +
            "  ],\n" +
            "  \"additionalProperties\": false,\n" +
            "  \"patternProperties\": {\n" +
            "    \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "      \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"properties\": {\n" +
            "    \"asyncapi\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\n" +
            "        \"2.0.0\"\n" +
            "      ],\n" +
            "      \"description\": \"The AsyncAPI specification version of this document.\"\n" +
            "    },\n" +
            "    \"id\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"A unique id representing the application.\",\n" +
            "      \"format\": \"uri\"\n" +
            "    },\n" +
            "    \"info\": {\n" +
            "      \"$ref\": \"#/definitions/info\"\n" +
            "    },\n" +
            "    \"servers\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/server\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"defaultContentType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"channels\": {\n" +
            "      \"$ref\": \"#/definitions/channels\"\n" +
            "    },\n" +
            "    \"components\": {\n" +
            "      \"$ref\": \"#/definitions/components\"\n" +
            "    },\n" +
            "    \"tags\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\n" +
            "        \"$ref\": \"#/definitions/tag\"\n" +
            "      },\n" +
            "      \"uniqueItems\": true\n" +
            "    },\n" +
            "    \"externalDocs\": {\n" +
            "      \"$ref\": \"#/definitions/externalDocs\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"definitions\": {\n" +
            "    \"Reference\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"$ref\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"$ref\": {\n" +
            "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"ReferenceObject\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"format\": \"uri-reference\"\n" +
            "    },\n" +
            "    \"info\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"description\": \"General information about the API.\",\n" +
            "      \"required\": [\n" +
            "        \"version\",\n" +
            "        \"title\"\n" +
            "      ],\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"title\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A unique and precise title of the API.\"\n" +
            "        },\n" +
            "        \"version\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A semantic version number of the API.\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A longer description of the API. Should be different from the title. CommonMark is allowed.\"\n" +
            "        },\n" +
            "        \"termsOfService\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A URL to the Terms of Service for the API. MUST be in the format of a URL.\",\n" +
            "          \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"contact\": {\n" +
            "          \"$ref\": \"#/definitions/contact\"\n" +
            "        },\n" +
            "        \"license\": {\n" +
            "          \"$ref\": \"#/definitions/license\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"contact\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"description\": \"Contact information for the owners of the API.\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"properties\": {\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The identifying name of the contact person/organization.\"\n" +
            "        },\n" +
            "        \"url\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The URL pointing to the contact information.\",\n" +
            "          \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"email\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The email address of the contact person/organization.\",\n" +
            "          \"format\": \"email\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"license\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"name\"\n" +
            "      ],\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"properties\": {\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The name of the license type. It's encouraged to use an OSI compatible license.\"\n" +
            "        },\n" +
            "        \"url\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The URL pointing to the license.\",\n" +
            "          \"format\": \"uri\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"server\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"description\": \"An object representing a Server.\",\n" +
            "      \"required\": [\n" +
            "        \"url\",\n" +
            "        \"protocol\"\n" +
            "      ],\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"url\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"protocol\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The transfer protocol.\"\n" +
            "        },\n" +
            "        \"protocolVersion\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"variables\": {\n" +
            "          \"$ref\": \"#/definitions/serverVariables\"\n" +
            "        },\n" +
            "        \"security\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"$ref\": \"#/definitions/SecurityRequirement\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"bindings\": {\n" +
            "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"serverVariables\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/serverVariable\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"serverVariable\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"description\": \"An object representing a Server Variable for server URL template substitution.\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"enum\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"type\": \"string\"\n" +
            "          },\n" +
            "          \"uniqueItems\": true\n" +
            "        },\n" +
            "        \"default\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"examples\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"type\": \"string\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"channels\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"propertyNames\": {\n" +
            "        \"type\": \"string\",\n" +
            "        \"format\": \"uri-template\",\n" +
            "        \"minLength\": 1\n" +
            "      },\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/channelItem\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"components\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"description\": \"An object to hold a set of reusable objects for different aspects of the AsyncAPI Specification.\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"properties\": {\n" +
            "        \"schemas\": {\n" +
            "          \"$ref\": \"#/definitions/schemas\"\n" +
            "        },\n" +
            "        \"messages\": {\n" +
            "          \"$ref\": \"#/definitions/messages\"\n" +
            "        },\n" +
            "        \"securitySchemes\": {\n" +
            "          \"type\": \"object\",\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"$ref\": \"#/definitions/parameters\"\n" +
            "        },\n" +
            "        \"correlationIds\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"patternProperties\": {\n" +
            "            \"^[\\\\w\\\\d\\\\.\\\\-_]+$\": {\n" +
            "              \"oneOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/Reference\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/correlationId\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"operationTraits\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/operationTrait\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"messageTraits\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/messageTrait\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"serverBindings\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"channelBindings\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"operationBindings\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"messageBindings\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"schemas\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/schema\"\n" +
            "      },\n" +
            "      \"description\": \"JSON objects describing schemas the API uses.\"\n" +
            "    },\n" +
            "    \"messages\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/message\"\n" +
            "      },\n" +
            "      \"description\": \"JSON objects describing the messages being consumed and produced by the API.\"\n" +
            "    },\n" +
            "    \"parameters\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"$ref\": \"#/definitions/parameter\"\n" +
            "      },\n" +
            "      \"description\": \"JSON objects describing re-usable channel parameters.\"\n" +
            "    },\n" +
            "    \"schema\": {\n" +
            "      \"allOf\": [\n" +
            "        {\n" +
            "          \"$ref\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"type\": \"object\",\n" +
            "          \"patternProperties\": {\n" +
            "            \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "              \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "            }\n" +
            "          },\n" +
            "          \"properties\": {\n" +
            "            \"additionalProperties\": {\n" +
            "              \"anyOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/schema\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"type\": \"boolean\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"default\": {}\n" +
            "            },\n" +
            "            \"items\": {\n" +
            "              \"anyOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/schema\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"type\": \"array\",\n" +
            "                  \"minItems\": 1,\n" +
            "                  \"items\": {\n" +
            "                    \"$ref\": \"#/definitions/schema\"\n" +
            "                  }\n" +
            "                }\n" +
            "              ],\n" +
            "              \"default\": {}\n" +
            "            },\n" +
            "            \"allOf\": {\n" +
            "              \"type\": \"array\",\n" +
            "              \"minItems\": 1,\n" +
            "              \"items\": {\n" +
            "                \"$ref\": \"#/definitions/schema\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"oneOf\": {\n" +
            "              \"type\": \"array\",\n" +
            "              \"minItems\": 2,\n" +
            "              \"items\": {\n" +
            "                \"$ref\": \"#/definitions/schema\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"anyOf\": {\n" +
            "              \"type\": \"array\",\n" +
            "              \"minItems\": 2,\n" +
            "              \"items\": {\n" +
            "                \"$ref\": \"#/definitions/schema\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"not\": {\n" +
            "              \"$ref\": \"#/definitions/schema\"\n" +
            "            },\n" +
            "            \"properties\": {\n" +
            "              \"type\": \"object\",\n" +
            "              \"additionalProperties\": {\n" +
            "                \"$ref\": \"#/definitions/schema\"\n" +
            "              },\n" +
            "              \"default\": {}\n" +
            "            },\n" +
            "            \"patternProperties\": {\n" +
            "              \"type\": \"object\",\n" +
            "              \"additionalProperties\": {\n" +
            "                \"$ref\": \"#/definitions/schema\"\n" +
            "              },\n" +
            "              \"default\": {}\n" +
            "            },\n" +
            "            \"propertyNames\": {\n" +
            "              \"$ref\": \"#/definitions/schema\"\n" +
            "            },\n" +
            "            \"contains\": {\n" +
            "              \"$ref\": \"#/definitions/schema\"\n" +
            "            },\n" +
            "            \"discriminator\": {\n" +
            "              \"type\": \"string\"\n" +
            "            },\n" +
            "            \"externalDocs\": {\n" +
            "              \"$ref\": \"#/definitions/externalDocs\"\n" +
            "            },\n" +
            "            \"deprecated\": {\n" +
            "              \"type\": \"boolean\",\n" +
            "              \"default\": false\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"externalDocs\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"description\": \"information about external documentation\",\n" +
            "      \"required\": [\n" +
            "        \"url\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"url\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"format\": \"uri\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"channelItem\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"$ref\": {\n" +
            "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"additionalProperties\": {\n" +
            "            \"$ref\": \"#/definitions/parameter\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A description of the channel.\"\n" +
            "        },\n" +
            "        \"publish\": {\n" +
            "          \"$ref\": \"#/definitions/operation\"\n" +
            "        },\n" +
            "        \"subscribe\": {\n" +
            "          \"$ref\": \"#/definitions/operation\"\n" +
            "        },\n" +
            "        \"deprecated\": {\n" +
            "          \"type\": \"boolean\",\n" +
            "          \"default\": false\n" +
            "        },\n" +
            "        \"bindings\": {\n" +
            "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"parameter\": {\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A brief description of the parameter. This could contain examples of use. GitHub Flavored Markdown is allowed.\"\n" +
            "        },\n" +
            "        \"schema\": {\n" +
            "          \"$ref\": \"#/definitions/schema\"\n" +
            "        },\n" +
            "        \"location\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A runtime expression that specifies the location of the parameter value\",\n" +
            "          \"pattern\": \"^\\\\$message\\\\.(header|payload)\\\\#(\\\\/(([^\\\\/~])|(~[01]))*)*\"\n" +
            "        },\n" +
            "        \"$ref\": {\n" +
            "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"operation\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"traits\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"oneOf\": [\n" +
            "              {\n" +
            "                \"$ref\": \"#/definitions/Reference\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"$ref\": \"#/definitions/operationTrait\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"type\": \"array\",\n" +
            "                \"items\": [\n" +
            "                  {\n" +
            "                    \"oneOf\": [\n" +
            "                      {\n" +
            "                        \"$ref\": \"#/definitions/Reference\"\n" +
            "                      },\n" +
            "                      {\n" +
            "                        \"$ref\": \"#/definitions/operationTrait\"\n" +
            "                      }\n" +
            "                    ]\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"type\": \"object\",\n" +
            "                    \"additionalItems\": true\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        \"summary\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"tags\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"$ref\": \"#/definitions/tag\"\n" +
            "          },\n" +
            "          \"uniqueItems\": true\n" +
            "        },\n" +
            "        \"externalDocs\": {\n" +
            "          \"$ref\": \"#/definitions/externalDocs\"\n" +
            "        },\n" +
            "        \"operationId\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"bindings\": {\n" +
            "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "        },\n" +
            "        \"message\": {\n" +
            "          \"$ref\": \"#/definitions/message\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"message\": {\n" +
            "      \"oneOf\": [\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/Reference\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"oneOf\": [\n" +
            "            {\n" +
            "              \"type\": \"object\",\n" +
            "              \"required\": [\n" +
            "                \"oneOf\"\n" +
            "              ],\n" +
            "              \"additionalProperties\": false,\n" +
            "              \"properties\": {\n" +
            "                \"oneOf\": {\n" +
            "                  \"type\": \"array\",\n" +
            "                  \"items\": {\n" +
            "                    \"$ref\": \"#/definitions/message\"\n" +
            "                  }\n" +
            "                }\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"type\": \"object\",\n" +
            "              \"additionalProperties\": false,\n" +
            "              \"patternProperties\": {\n" +
            "                \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "                  \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "                }\n" +
            "              },\n" +
            "              \"properties\": {\n" +
            "                \"schemaFormat\": {\n" +
            "                  \"type\": \"string\"\n" +
            "                },\n" +
            "                \"contentType\": {\n" +
            "                  \"type\": \"string\"\n" +
            "                },\n" +
            "                \"headers\": {\n" +
            "                  \"allOf\": [\n" +
            "                    { \"$ref\": \"#/definitions/schema\" },\n" +
            "                    { \"properties\": {\n" +
            "                      \"type\": { \"const\": \"object\" }\n" +
            "                    }\n" +
            "                    }\n" +
            "                  ]\n" +
            "                },\n" +
            "                \"payload\": {},\n" +
            "                \"correlationId\": {\n" +
            "                  \"oneOf\": [\n" +
            "                    {\n" +
            "                      \"$ref\": \"#/definitions/Reference\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                      \"$ref\": \"#/definitions/correlationId\"\n" +
            "                    }\n" +
            "                  ]\n" +
            "                },\n" +
            "                \"tags\": {\n" +
            "                  \"type\": \"array\",\n" +
            "                  \"items\": {\n" +
            "                    \"$ref\": \"#/definitions/tag\"\n" +
            "                  },\n" +
            "                  \"uniqueItems\": true\n" +
            "                },\n" +
            "                \"summary\": {\n" +
            "                  \"type\": \"string\",\n" +
            "                  \"description\": \"A brief summary of the message.\"\n" +
            "                },\n" +
            "                \"name\": {\n" +
            "                  \"type\": \"string\",\n" +
            "                  \"description\": \"Name of the message.\"\n" +
            "                },\n" +
            "                \"title\": {\n" +
            "                  \"type\": \"string\",\n" +
            "                  \"description\": \"A human-friendly title for the message.\"\n" +
            "                },\n" +
            "                \"description\": {\n" +
            "                  \"type\": \"string\",\n" +
            "                  \"description\": \"A longer description of the message. CommonMark is allowed.\"\n" +
            "                },\n" +
            "                \"externalDocs\": {\n" +
            "                  \"$ref\": \"#/definitions/externalDocs\"\n" +
            "                },\n" +
            "                \"deprecated\": {\n" +
            "                  \"type\": \"boolean\",\n" +
            "                  \"default\": false\n" +
            "                },\n" +
            "                \"examples\": {\n" +
            "                  \"type\": \"array\",\n" +
            "                  \"items\": {\n" +
            "                    \"type\": \"object\"\n" +
            "                  }\n" +
            "                },\n" +
            "                \"bindings\": {\n" +
            "                  \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "                },\n" +
            "                \"traits\": {\n" +
            "                  \"type\": \"array\",\n" +
            "                  \"items\": {\n" +
            "                    \"oneOf\": [\n" +
            "                      {\n" +
            "                        \"$ref\": \"#/definitions/Reference\"\n" +
            "                      },\n" +
            "                      {\n" +
            "                        \"$ref\": \"#/definitions/messageTrait\"\n" +
            "                      },\n" +
            "                      {\n" +
            "                        \"type\": \"array\",\n" +
            "                        \"items\": [\n" +
            "                          {\n" +
            "                            \"oneOf\": [\n" +
            "                              {\n" +
            "                                \"$ref\": \"#/definitions/Reference\"\n" +
            "                              },\n" +
            "                              {\n" +
            "                                \"$ref\": \"#/definitions/messageTrait\"\n" +
            "                              }\n" +
            "                            ]\n" +
            "                          },\n" +
            "                          {\n" +
            "                            \"type\": \"object\",\n" +
            "                            \"additionalItems\": true\n" +
            "                          }\n" +
            "                        ]\n" +
            "                      }\n" +
            "                    ]\n" +
            "                  }\n" +
            "                }\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"bindingsObject\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": true,\n" +
            "      \"properties\": {\n" +
            "        \"http\": {},\n" +
            "        \"ws\": {},\n" +
            "        \"amqp\": {},\n" +
            "        \"amqp1\": {},\n" +
            "        \"mqtt\": {},\n" +
            "        \"mqtt5\": {},\n" +
            "        \"kafka\": {},\n" +
            "        \"nats\": {},\n" +
            "        \"jms\": {},\n" +
            "        \"sns\": {},\n" +
            "        \"sqs\": {},\n" +
            "        \"stomp\": {},\n" +
            "        \"redis\": {},\n" +
            "        \"mercure\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"correlationId\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"location\"\n" +
            "      ],\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A optional description of the correlation ID. GitHub Flavored Markdown is allowed.\"\n" +
            "        },\n" +
            "        \"location\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A runtime expression that specifies the location of the correlation ID\",\n" +
            "          \"pattern\": \"^\\\\$message\\\\.(header|payload)\\\\#(\\\\/(([^\\\\/~])|(~[01]))*)*\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"specificationExtension\": {\n" +
            "      \"description\": \"Any property starting with x- is valid.\",\n" +
            "      \"additionalProperties\": true,\n" +
            "      \"additionalItems\": true\n" +
            "    },\n" +
            "    \"tag\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"required\": [\n" +
            "        \"name\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"externalDocs\": {\n" +
            "          \"$ref\": \"#/definitions/externalDocs\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"operationTrait\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"summary\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"tags\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"$ref\": \"#/definitions/tag\"\n" +
            "          },\n" +
            "          \"uniqueItems\": true\n" +
            "        },\n" +
            "        \"externalDocs\": {\n" +
            "          \"$ref\": \"#/definitions/externalDocs\"\n" +
            "        },\n" +
            "        \"operationId\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"bindings\": {\n" +
            "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"messageTrait\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"properties\": {\n" +
            "        \"schemaFormat\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"contentType\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"headers\": {\n" +
            "          \"oneOf\": [\n" +
            "            {\n" +
            "              \"$ref\": \"#/definitions/Reference\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"$ref\": \"#/definitions/schema\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"correlationId\": {\n" +
            "          \"oneOf\": [\n" +
            "            {\n" +
            "              \"$ref\": \"#/definitions/Reference\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"$ref\": \"#/definitions/correlationId\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"tags\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"$ref\": \"#/definitions/tag\"\n" +
            "          },\n" +
            "          \"uniqueItems\": true\n" +
            "        },\n" +
            "        \"summary\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A brief summary of the message.\"\n" +
            "        },\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"Name of the message.\"\n" +
            "        },\n" +
            "        \"title\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A human-friendly title for the message.\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"A longer description of the message. CommonMark is allowed.\"\n" +
            "        },\n" +
            "        \"externalDocs\": {\n" +
            "          \"$ref\": \"#/definitions/externalDocs\"\n" +
            "        },\n" +
            "        \"deprecated\": {\n" +
            "          \"type\": \"boolean\",\n" +
            "          \"default\": false\n" +
            "        },\n" +
            "        \"examples\": {\n" +
            "          \"type\": \"array\",\n" +
            "          \"items\": {\n" +
            "            \"type\": \"object\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"bindings\": {\n" +
            "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"SecurityScheme\": {\n" +
            "      \"oneOf\": [\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/userPassword\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/apiKey\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/X509\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/symmetricEncryption\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/asymmetricEncryption\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/HTTPSecurityScheme\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/oauth2Flows\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/openIdConnect\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"userPassword\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"userPassword\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"apiKey\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\",\n" +
            "        \"in\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"apiKey\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"in\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"user\",\n" +
            "            \"password\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"X509\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"X509\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"symmetricEncryption\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"symmetricEncryption\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"asymmetricEncryption\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"asymmetricEncryption\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"HTTPSecurityScheme\": {\n" +
            "      \"oneOf\": [\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/NonBearerHTTPSecurityScheme\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/BearerHTTPSecurityScheme\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"$ref\": \"#/definitions/APIKeyHTTPSecurityScheme\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"NonBearerHTTPSecurityScheme\": {\n" +
            "      \"not\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "          \"scheme\": {\n" +
            "            \"type\": \"string\",\n" +
            "            \"enum\": [\n" +
            "              \"bearer\"\n" +
            "            ]\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"scheme\",\n" +
            "        \"type\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"scheme\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"http\"\n" +
            "          ]\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"BearerHTTPSecurityScheme\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\",\n" +
            "        \"scheme\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"scheme\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"bearer\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"bearerFormat\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"http\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"APIKeyHTTPSecurityScheme\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\",\n" +
            "        \"name\",\n" +
            "        \"in\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"httpApiKey\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"in\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"header\",\n" +
            "            \"query\",\n" +
            "            \"cookie\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"oauth2Flows\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\",\n" +
            "        \"flows\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"flows\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"properties\": {\n" +
            "            \"implicit\": {\n" +
            "              \"allOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"required\": [\n" +
            "                    \"authorizationUrl\",\n" +
            "                    \"scopes\"\n" +
            "                  ]\n" +
            "                },\n" +
            "                {\n" +
            "                  \"not\": {\n" +
            "                    \"required\": [\n" +
            "                      \"tokenUrl\"\n" +
            "                    ]\n" +
            "                  }\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"password\": {\n" +
            "              \"allOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"required\": [\n" +
            "                    \"tokenUrl\",\n" +
            "                    \"scopes\"\n" +
            "                  ]\n" +
            "                },\n" +
            "                {\n" +
            "                  \"not\": {\n" +
            "                    \"required\": [\n" +
            "                      \"authorizationUrl\"\n" +
            "                    ]\n" +
            "                  }\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"clientCredentials\": {\n" +
            "              \"allOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"required\": [\n" +
            "                    \"tokenUrl\",\n" +
            "                    \"scopes\"\n" +
            "                  ]\n" +
            "                },\n" +
            "                {\n" +
            "                  \"not\": {\n" +
            "                    \"required\": [\n" +
            "                      \"authorizationUrl\"\n" +
            "                    ]\n" +
            "                  }\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"authorizationCode\": {\n" +
            "              \"allOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"required\": [\n" +
            "                    \"authorizationUrl\",\n" +
            "                    \"tokenUrl\",\n" +
            "                    \"scopes\"\n" +
            "                  ]\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          },\n" +
            "          \"additionalProperties\": false\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"oauth2Flow\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"authorizationUrl\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"tokenUrl\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"refreshUrl\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"format\": \"uri\"\n" +
            "        },\n" +
            "        \"scopes\": {\n" +
            "          \"$ref\": \"#/definitions/oauth2Scopes\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"oauth2Scopes\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"openIdConnect\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"required\": [\n" +
            "        \"type\",\n" +
            "        \"openIdConnectUrl\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"type\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"enum\": [\n" +
            "            \"openIdConnect\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"description\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"openIdConnectUrl\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"format\": \"uri\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"patternProperties\": {\n" +
            "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
            "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"additionalProperties\": false\n" +
            "    },\n" +
            "    \"SecurityRequirement\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": {\n" +
            "        \"type\": \"array\",\n" +
            "        \"items\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"uniqueItems\": true\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String JSONSCHEMA = "http://json-schema.org/draft-07/schema#";
}
