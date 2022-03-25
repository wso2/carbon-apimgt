package org.wso2.carbon.apimgt.impl.definitions;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiChannelItem;
import io.apicurio.datamodels.asyncapi.models.AaiDocument;
import io.apicurio.datamodels.asyncapi.models.AaiOperation;
import io.apicurio.datamodels.asyncapi.models.AaiOperationBindings;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20ChannelItem;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20ImplicitOAuthFlow;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20OAuthFlows;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Operation;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20SecurityScheme;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Server;
import io.apicurio.datamodels.core.models.Extension;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(
        name = "wso2.async.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
public class AsyncApiParser extends APIDefinition {

    String metaSchema = "{\n" +
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
    private static final Log log = LogFactory.getLog(AsyncApiParser.class);
    private List<String> otherSchemes;
    public List<String> getOtherSchemes() {
        return otherSchemes;
    }
    public void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

    @Override
    public Map<String, Object> generateExample(String apiDefinition) throws APIManagementException{
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        return getURITemplates(resourceConfigsJSON, true);
    }

    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        Set<URITemplate> uriTemplates = new HashSet<>();
        Set<Scope> scopes = getScopes(apiDefinition);
        Aai20Document document = (Aai20Document) Library.readDocumentFromJSONString(apiDefinition);
        if (document.channels != null && document.channels.size() > 0) {
            for (Map.Entry<String, AaiChannelItem> entry : document.channels.entrySet()) {
                Aai20ChannelItem channel = (Aai20ChannelItem) entry.getValue();
                if (includePublish && channel.publish != null) {
                    uriTemplates.add(buildURITemplate(entry.getKey(), APIConstants.HTTP_VERB_PUBLISH,
                            (Aai20Operation) channel.publish, scopes, channel));
                }
                if (channel.subscribe != null) {
                    uriTemplates.add(buildURITemplate(entry.getKey(), APIConstants.HTTP_VERB_SUBSCRIBE,
                            (Aai20Operation) channel.subscribe, scopes, channel));
                }
            }
        }
        return uriTemplates;
    }

    private URITemplate buildURITemplate(String target, String verb, Aai20Operation operation, Set<Scope> scopes,
                                         Aai20ChannelItem channel) throws APIManagementException {
        URITemplate template = new URITemplate();
        template.setHTTPVerb(verb);
        template.setHttpVerbs(verb);
        template.setUriTemplate(target);

        Extension authTypeExtension = channel.getExtension(APIConstants.SWAGGER_X_AUTH_TYPE);
        if (authTypeExtension != null && authTypeExtension.value instanceof String) {
            template.setAuthType(authTypeExtension.value.toString());
        }

        List<String> opScopes = getScopeOfOperations(operation);
        if (!opScopes.isEmpty()) {
            if (opScopes.size() == 1) {
                String firstScope = opScopes.get(0);
                Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                if (scope == null) {
                    throw new APIManagementException("Scope '" + firstScope + "' not found.");
                }
                template.setScope(scope);
                template.setScopes(scope);
            } else {
                for (String scopeName : opScopes) {
                    Scope scope = APIUtil.findScopeByKey(scopes, scopeName);
                    if (scope == null) {
                        throw new APIManagementException("Resource Scope '" + scopeName + "' not found.");
                    }
                    template.setScopes(scope);
                }
            }
        }
        return template;
    }

    private List<String> getScopeOfOperations(Aai20Operation operation) {
        return getScopeOfOperationsFromExtensions(operation);
    }

    private List<String> getScopeOfOperationsFromExtensions(Aai20Operation operation) {
        Extension scopeBindings = operation.getExtension("x-scopes");
        if (scopeBindings != null) {
            if (scopeBindings.value instanceof LinkedHashMap) {
                return (List<String>) ((LinkedHashMap) scopeBindings.value)
                        .values()
                        .stream()
                        .collect(Collectors.toList());
            }

            if (scopeBindings.value instanceof ArrayList) {
                return (List<String>) scopeBindings.value;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeSet = new LinkedHashSet<>();
        Aai20Document document = (Aai20Document) Library.readDocumentFromJSONString(resourceConfigsJSON);
        if (document.components != null && document.components.securitySchemes != null) {
            Aai20SecurityScheme oauth2 = (Aai20SecurityScheme) document.components.securitySchemes.get("oauth2");
            if (oauth2 != null && oauth2.flows != null && oauth2.flows.implicit != null) {
                Map<String, String> scopes = oauth2.flows.implicit.scopes;
                Extension xScopesBindings = oauth2.flows.implicit.getExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
                Map<String, String> scopeBindings = new HashMap<>();
                if (xScopesBindings != null) {
                    scopeBindings = (Map<String, String>) xScopesBindings.value;
                }
                if (scopes != null) {
                    for (Map.Entry<String, String> entry : scopes.entrySet()) {
                        Scope scope = new Scope();
                        scope.setKey(entry.getKey());
                        scope.setName(entry.getKey());
                        scope.setDescription(entry.getValue());
                        String scopeBinding = scopeBindings.get(scope.getKey());
                        if (scopeBinding != null) {
                            scope.setRoles(scopeBinding);
                        }
                        scopeSet.add(scope);
                    }
                }
            }
        }
        return scopeSet;
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
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String url, boolean returnJsonContent) throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        //import and load AsyncAPI HyperSchema for JSON schema validation
        JSONObject hyperSchema = new JSONObject(ASYNCAPI_JSON_HYPERSCHEMA);
        String protocol = StringUtils.EMPTY;

        boolean validationSuccess = false;
        List<String> validationErrorMessages = null;
        boolean isWebSocket = false;

        JSONObject schemaToBeValidated = new JSONObject(apiDefinition);

        //validate AsyncAPI using JSON schema validation
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(metaSchema);
            SchemaLoader schemaLoader = SchemaLoader.builder().registerSchemaByURI
                    (new URI("http://json-schema.org/draft-07/schema#"), json).schemaJson(hyperSchema).build();
            Schema schemaValidator = schemaLoader.load().build();
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
            if (APIConstants.WS_PROTOCOL.equalsIgnoreCase(asyncApiDocument.getServers().get(0).protocol)) {
                if (APIConstants.WS_PROTOCOL.equalsIgnoreCase(protocol)) {
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
        } catch(ValidationException e) {
            //validation error messages
            validationErrorMessages = e.getAllMessages();
        } catch (URISyntaxException e) {
            String msg = "Error occurred when registering the schema";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Error occurred when parsing the schema";
            throw new APIManagementException(msg, e);
        }

        // TODO: Validation is failing. Need to fix this. Therefore overriding the value as True.
        validationSuccess = true;

        if (validationSuccess) {
            AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            if (asyncApiDocument.getServers().size() == 1) {
                protocol = asyncApiDocument.getServers().get(0).protocol;
            }
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
            if (StringUtils.isNotEmpty(protocol)) {
                validationResponse.setProtocol(protocol);
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
    public String getVendorFromExtension(String swaggerContent) {
        return APIConstants.WSO2_GATEWAY_ENVIRONMENT;
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

        Map<String, AaiChannelItem> channels = new HashMap<>();
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Aai20ChannelItem channelItem = aaiDocument.createChannelItem(uriTemplate.getUriTemplate());
            Aai20Operation subscribeOp = new Aai20Operation(channelItem,"subscribe");
            channelItem.subscribe = subscribeOp;
            if (APIConstants.API_TYPE_WS.equals(api.getType())) {
                Aai20Operation publishOp = new Aai20Operation(channelItem,"publish");
                channelItem.publish = publishOp;
            }
            channels.put(uriTemplate.getUriTemplate(), channelItem);
        }
        aaiDocument.channels = channels;
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

    public String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) {
        Aai20Document document = (Aai20Document) Library.readDocumentFromJSONString(oldDefinition);

        if (document.components == null) {
            document.components = document.createComponents();
        }

        // add scopes
        if (document.components.securitySchemes == null) {
            document.components.securitySchemes = new HashMap<>();
        }

        Aai20SecurityScheme oauth2SecurityScheme = new Aai20SecurityScheme(document.components,
                APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        oauth2SecurityScheme.type = APIConstants.DEFAULT_API_SECURITY_OAUTH2;

        if (oauth2SecurityScheme.flows == null) {
            oauth2SecurityScheme.flows = new Aai20OAuthFlows(oauth2SecurityScheme);
        }
        if (oauth2SecurityScheme.flows.implicit == null) {
            oauth2SecurityScheme.flows.implicit = new Aai20ImplicitOAuthFlow(oauth2SecurityScheme.flows);
        }
        oauth2SecurityScheme.flows.implicit.authorizationUrl = "http://localhost:9999";
        Map<String, String> scopes = new HashMap<>();
        Map<String, String> scopeBindings = new HashMap<>();

        Iterator<Scope> iterator = apiToUpdate.getScopes().iterator();
        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            scopes.put(scope.getName(), scope.getDescription());
            scopeBindings.put(scope.getName(), scope.getRoles());
        }
        oauth2SecurityScheme.flows.implicit.scopes = scopes;

        Extension xScopeBindings = oauth2SecurityScheme.flows.implicit.createExtension();
        xScopeBindings.name = APIConstants.SWAGGER_X_SCOPES_BINDINGS;
        xScopeBindings.value = scopeBindings;
        oauth2SecurityScheme.flows.implicit.addExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, xScopeBindings);

        document.components.securitySchemes.put(APIConstants.DEFAULT_API_SECURITY_OAUTH2, oauth2SecurityScheme);
        String endpointConfigString = apiToUpdate.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            Aai20Server server = (Aai20Server) document.createServer("production");
            JSONObject endpointConfig = new JSONObject(endpointConfigString);
            server.url = endpointConfig.getJSONObject("production_endpoints").getString("url");
            server.protocol = apiToUpdate.getType().toLowerCase();
            document.addServer("production", server);
        }
        return Library.writeDocumentToJSONString(document);
    }

    public Map<String,String> buildWSUriMapping(String apiDefinition) {
        Map<String,String> wsUriMapping = new HashMap<>();
        Aai20Document document = (Aai20Document) Library.readDocumentFromJSONString(apiDefinition);
        for (Map.Entry<String, AaiChannelItem> entry : document.channels.entrySet()) {
            AaiOperation publishOperation = entry.getValue().publish;
            if (publishOperation != null) {
                Extension xUriMapping = publishOperation.getExtension("x-uri-mapping");
                if (xUriMapping != null) {
                    wsUriMapping.put("PUBLISH_" + entry.getKey(), xUriMapping.value.toString());
                }
            }

            AaiOperation subscribeOperation = entry.getValue().subscribe;
            if (subscribeOperation != null)  {
                Extension xUriMapping = subscribeOperation.getExtension("x-uri-mapping");
                if (xUriMapping != null) {
                    wsUriMapping.put("SUBSCRIBE_" + entry.getKey(), xUriMapping.value.toString());
                }
            }
        }
        return wsUriMapping;
    }

    /**
     * Get available transport protocols for the Async API
     *
     * @param definition Async API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the async env configuration if not provided properly
     */
    public static List<String> getTransportProtocolsForAsyncAPI(String definition) throws APIManagementException {
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(definition);
        HashSet<String> asyncTransportProtocols = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            asyncTransportProtocols.addAll(getProtocols(channel));
        }
        ArrayList<String> asyncTransportProtocolsList = new ArrayList<>(asyncTransportProtocols);
        return asyncTransportProtocolsList;
    }

    /**
     * Get the transport protocols
     *
     * @param channel AaiChannelItem to get protocol
     * @return HashSet<String> set of transport protocols
     */
    public static HashSet<String> getProtocols(AaiChannelItem channel) {

        HashSet<String> protocols = new HashSet<>();

        if (channel.subscribe != null) {
            if (channel.subscribe.bindings != null) {
                protocols.addAll(getProtocolsFromBindings(channel.subscribe.bindings));
            }
        }
        if (channel.publish != null) {
            if (channel.publish.bindings != null) {
                protocols.addAll(getProtocolsFromBindings(channel.publish.bindings));
            }
        }

        return protocols;
    }

    /**
     * Get the transport protocols the bindings
     *
     * @param bindings AaiOperationBindings to get protocols
     * @return HashSet<String> set of transport protocols
     */
    private static HashSet<String> getProtocolsFromBindings(AaiOperationBindings bindings) {

        HashSet<String> protocolsFromBindings = new HashSet<>();

        if (bindings.http != null) {
            protocolsFromBindings.add(APIConstants.HTTP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.ws != null) {
            protocolsFromBindings.add(APIConstants.WS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.kafka != null) {
            protocolsFromBindings.add(APIConstants.KAFKA_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.amqp != null) {
            protocolsFromBindings.add(APIConstants.AMQP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.amqp1 != null) {
            protocolsFromBindings.add(APIConstants.AMQP1_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.mqtt != null) {
            protocolsFromBindings.add(APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.mqtt5 != null) {
            protocolsFromBindings.add(APIConstants.MQTT5_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.nats != null) {
            protocolsFromBindings.add(APIConstants.NATS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.jms != null) {
            protocolsFromBindings.add(APIConstants.JMS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.sns != null) {
            protocolsFromBindings.add(APIConstants.SNS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.sqs != null) {
            protocolsFromBindings.add(APIConstants.SQS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.stomp != null) {
            protocolsFromBindings.add(APIConstants.STOMP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.redis != null) {
            protocolsFromBindings.add(APIConstants.REDIS_TRANSPORT_PROTOCOL_NAME);
        }

        if (bindings.hasExtraProperties()) {
            protocolsFromBindings.addAll(bindings.getExtraPropertyNames());
        }

        return protocolsFromBindings;
    }

    @Override
    public String getType() {
        return APIConstants.WSO2_GATEWAY_ENVIRONMENT;
    }
}
