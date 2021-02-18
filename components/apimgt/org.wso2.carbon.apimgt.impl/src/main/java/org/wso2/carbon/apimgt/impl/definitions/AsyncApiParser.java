package org.wso2.carbon.apimgt.impl.definitions;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiChannelItem;
import io.apicurio.datamodels.asyncapi.models.AaiComponents;
import io.apicurio.datamodels.asyncapi.models.AaiDocument;
import io.apicurio.datamodels.asyncapi.models.AaiMessage;
import io.apicurio.datamodels.asyncapi.models.AaiOperation;
import io.apicurio.datamodels.asyncapi.models.AaiOperationBase;
import io.apicurio.datamodels.asyncapi.models.AaiServer;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20ChannelItem;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Components;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Message;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Operation;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Server;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.*;

public class AsyncApiParser extends APIDefinition {

    private static final String ASYNCAPI_JSON_HYPERSCHEMA = "{\n" +
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
            "          \"patternProperties\": {\n" +
            "            \"^[\\\\w\\\\d\\\\.\\\\-_]+$\": {\n" +
            "              \"oneOf\": [\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/Reference\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"$ref\": \"#/definitions/SecurityScheme\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          }\n" +
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
            "          \"enum\": [\n" +
            "            \"oauth2\"\n" +
            "          ]\n" +
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
    private static final Log log = LogFactory.getLog(AsyncApiParser.class);
    private List<String> otherSchemes;
    public List<String> getOtherSchemes() {
        return otherSchemes;
    }
    public void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

    @Override
    public Map<String, Object> generateExample(String apiDefinition) {
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        //import and load AsyncAPI HyperSchema for JSON schema validation
        JSONObject hyperSchema = new JSONObject(ASYNCAPI_JSON_HYPERSCHEMA);
        Schema schemaValidator = SchemaLoader.load(hyperSchema);

        boolean validationSuccess = false;
        List<String> validationErrorMessages = null;
        boolean isWebSocket = false;

        JSONObject schemaToBeValidated = new JSONObject(apiDefinition);

        //validate AsyncAPI using JSON schema validation
        try {
            schemaValidator.validate(schemaToBeValidated);
            /*AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            validationErrorMessages = new ArrayList<>();
            if (asyncApiDocument.getServers().size() == 1) {
                if (!APIConstants.WS_PROTOCOL.equalsIgnoreCase(asyncApiDocument.getServers().get(0).protocol)) {
                    validationErrorMessages.add("#:The protocol of the server should be 'ws' for websockets");
                }
            }
            if (asyncApiDocument.getServers().size() > 1) {
                validationErrorMessages.add("#:The AsyncAPI definition should contain only a single server for websockets");
            }
            if (asyncApiDocument.getChannels().size() > 1) {
                validationErrorMessages.add("#:The AsyncAPI definition should contain only a single channel for websockets");
            }
            if (validationErrorMessages.size() == 0) {
                validationSuccess = true;
                validationErrorMessages = null;
            }*/

            //AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            /*//Checking whether it is a websocket
            validationErrorMessages = new ArrayList<>();
            if (asyncApiDocument.getServers().size() == 1) {
                if (APIConstants.WS_PROTOCOL.equalsIgnoreCase(asyncApiDocument.getServers().get(0).protocol)) {
                    isWebSocket = true;
                }
            }*/

            //validating channel count for websockets
            /*if (isWebSocket) {
                if (asyncApiDocument.getChannels().size() > 1) {
                    validationErrorMessages.add("#:The AsyncAPI definition should contain only a single channel for websockets");
                }
            }*/

            /*if (validationErrorMessages.size() == 0) {
                validationSuccess = true;
                validationErrorMessages = null;
            }*/

            validationSuccess = true;

        } catch (ValidationException e){
            //validation error messages
            validationErrorMessages = e.getAllMessages();
        }

        if (validationSuccess) {
            AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            /*for (AaiServer x : asyncApiDocument.getServers()){
                endpoints.add(x.url);
            }
            AsyncApiParserUtil.updateValidationResponseAsSuccess(
                    validationResponse,
                    apiDefinition,
                    asyncApiDocument.asyncapi,
                    asyncApiDocument.info.title,
                    asyncApiDocument.info.version,
                    null,                           //asyncApiDocument.getChannels().get(0)._name,
                    asyncApiDocument.info.description,
                    endpoints
            );*/

            /*if (isWebSocket) {
                for (AaiServer x : asyncApiDocument.getServers()){
                    endpoints.add(x.url);
                }
                AsyncApiParserUtil.updateValidationResponseAsSuccess(
                        validationResponse,
                        apiDefinition,
                        asyncApiDocument.asyncapi,
                        asyncApiDocument.info.title,
                        asyncApiDocument.info.version,
                        asyncApiDocument.getChannels().get(0)._name,            //make this null
                        asyncApiDocument.info.description,
                        endpoints
                );
            } else {
                AsyncApiParserUtil.updateValidationResponseAsSuccess(
                        validationResponse,
                        apiDefinition,
                        asyncApiDocument.asyncapi,
                        asyncApiDocument.info.title,
                        asyncApiDocument.info.version,
                        null,
                        asyncApiDocument.info.description,
                        null
                );
            }*/

            AsyncApiParserUtil.updateValidationResponseAsSuccess(
                    validationResponse,
                    apiDefinition,
                    asyncApiDocument.asyncapi,
                    asyncApiDocument.info.title,
                    asyncApiDocument.info.version,
                    null,
                    asyncApiDocument.info.description,
                    null
            );

            validationResponse.setParser(this);
            if (returnJsonContent) {
                validationResponse.setJsonContent(apiDefinition);
            }
        } else {
            if (validationErrorMessages != null){
                validationResponse.setValid(false);
                for (String errorMessage: validationErrorMessages){
                    AsyncApiParserUtil.addErrorToValidationResponse(validationResponse, errorMessage);
                }
            }
        }
        return validationResponse;
    }

    @Override
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition, Map<String, String> hostsWithSchemes) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForPublisher(API api, String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASVersion(String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionWithTierContentAwareProperty(String oasDefinition, List<String> contentAwareTiersList, String apiLevelTier) throws APIManagementException {
        return null;
    }

    @Override
    public String processOtherSchemeScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public API setExtensionsToAPI(String swaggerContent, API api) throws APIManagementException {
        return null;
    }

    @Override
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent) throws APIManagementException {
        return null;
    }

    @Override
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException{
        return null;
    }

    @Override
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException{
        return null;
    }

    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        Aai20Document aaiDocument = new Aai20Document();
        aaiDocument.info = aaiDocument.createInfo();
        aaiDocument.info.title = api.getId().getName();
        aaiDocument.info.version = api.getId().getVersion();
        if (!APIConstants.API_TYPE_WEBSUB.equals(api.getType())) {
            Aai20Server server = (Aai20Server) aaiDocument.createServer("production");
            JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
            server.url = endpointConfig.getJSONObject("production_endpoints").getString("url");
            server.protocol = api.getType().toLowerCase();
            aaiDocument.addServer("production", server);
        }

        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Aai20ChannelItem channelItem = aaiDocument.createChannelItem(uriTemplate.getUriTemplate());
            aaiDocument.addChannelItem(channelItem);
        }
        return Library.writeDocumentToJSONString(aaiDocument);
    }

    /**
     * Update AsyncAPI definition for store
     *
     * @param api            API
     * @param asyncAPIDefinition  AsyncAPI definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return AsyncAPI definition
     * @throws APIManagementException throws if an error occurred
     */
    public String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition, Map<String, String> hostsWithSchemes)
            throws APIManagementException {
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(asyncAPIDefinition);
        String channelName = api.getContext();
        String transports = api.getTransports();

        String url = StringUtils.EMPTY;
        String[] apiTransports = transports.split(",");
        if (ArrayUtils.contains(apiTransports, APIConstants.WSS_PROTOCOL)
                && hostsWithSchemes.get(APIConstants.WSS_PROTOCOL) != null) {
            url = hostsWithSchemes.get(APIConstants.WSS_PROTOCOL).trim()
                    .replace(APIConstants.WSS_PROTOCOL_URL_PREFIX, "");
        }
        if (ArrayUtils.contains(apiTransports, APIConstants.WSS_PROTOCOL)
                && hostsWithSchemes.get(APIConstants.WS_PROTOCOL) != null) {
            if (StringUtils.isEmpty(url)) {
                url = hostsWithSchemes.get(APIConstants.WS_PROTOCOL).trim()
                        .replace(APIConstants.WS_PROTOCOL_URL_PREFIX, "");
            }
        }

        Aai20Server server = (Aai20Server) aai20Document.getServers().get(0);
        server.url = url;

        Map<String, AaiChannelItem> channels = aai20Document.channels;
        Aai20ChannelItem channelDetails = null;
        for (String x : channels.keySet()) {
            channelDetails = (Aai20ChannelItem) channels.get(x);
            aai20Document.channels.remove(x);
        }
        assert channelDetails != null;
        channelDetails._name = channelName;
        aai20Document.channels.put(channelName, channelDetails);

        return Library.writeDocumentToJSONString(aai20Document);
    }

}
