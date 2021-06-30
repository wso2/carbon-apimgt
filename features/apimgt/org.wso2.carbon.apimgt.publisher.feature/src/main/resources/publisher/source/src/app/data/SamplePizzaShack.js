/**
 * ***NOTE: These sample data are share between API Sample and Service catalog sample deployments, Please test both ends
 * if you are changing anything here!
 */

const getSampleOpenAPI = () => ({
    openapi: '3.0.0',
    info: {
        description: 'This is a RESTFul API for Pizza Shack online pizza delivery store.\n',
        version: '1.0.0',
        title: 'PizzaShackAPI',
        contact: {
            name: 'John Doe',
            url: 'http://www.pizzashack.com',
            email: 'architecture@pizzashack.com',
        },
        license: {
            name: 'Apache 2.0',
            url: 'http://www.apache.org/licenses/LICENSE-2.0.html',
        },
    },
    security: [{ default: [] }],
    paths: {
        '/order': {
            post: {
                description: 'Create a new Order',
                requestBody: {
                    $ref: '#/components/requestBodies/Order',
                },
                responses: {
                    201: {
                        description: 'Created. Successful response with the newly created object as entity inthe body.'
                            + 'Location header contains URL of newly created entity.',
                        headers: {
                            Location: {
                                description: 'The URL of the newly created resource.',
                                schema: {
                                    type: 'string',
                                },
                            },
                            'Content-Type': {
                                description: 'The content type of the body.',
                                schema: {
                                    type: 'string',
                                },
                            },
                        },
                        content: {
                            'application/json': {
                                schema: {
                                    $ref: '#/components/schemas/Order',
                                },
                            },
                        },
                    },
                    400: {
                        description: 'Bad Request. Invalid request or validation error.',
                        content: {
                            'application/json': {
                                schema: {
                                    $ref: '#/components/schemas/Error',
                                },
                            },
                        },
                    },
                    415: {
                        description:
                            'Unsupported Media Type. The entity of the request was in a not supported format.',
                        content: {
                            'application/json': {
                                schema: {
                                    $ref: '#/components/schemas/Error',
                                },
                            },
                        },
                    },
                },
                security: [{ default: [] }],
                'x-auth-type': 'Application & Application User',
                'x-throttling-tier': 'Unlimited',
                'x-wso2-application-security': {
                    'security-types': ['oauth2'],
                    optional: false,
                },
            },
        },
        '/menu': {
            get: {
                description: 'Return a list of available menu items',
                responses: {
                    200: {
                        description: 'OK. List of APIs is returned.',
                        headers: {},
                        content: {
                            'application/json': {
                                schema: {
                                    type: 'array',
                                    items: { $ref: '#/components/schemas/MenuItem' },
                                },
                            },
                        },
                    },
                    406: {
                        description:
                            'Not Acceptable. The requested media type is not supported',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                },
                security: [{ default: [] }],
                'x-auth-type': 'Application & Application User',
                'x-throttling-tier': 'Unlimited',
                'x-wso2-application-security': {
                    'security-types': ['oauth2'],
                    optional: false,
                },
            },
        },
        '/order/{orderId}': {
            get: {
                description: 'Get details of an Order',
                parameters: [
                    {
                        name: 'orderId',
                        in: 'path',
                        description: 'Order Id',
                        required: true,
                        schema: { type: 'string', format: 'string' },
                    },
                ],
                responses: {
                    200: {
                        description: 'OK Requested Order will be returned',
                        headers: {},
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Order' },
                            },
                        },
                    },
                    404: {
                        description: 'Not Found. Requested API does not exist.',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                    406: {
                        description:
                            'Not Acceptable. The requested media type is not supported',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                },
                security: [{ default: [] }],
                'x-auth-type': 'Application & Application User',
                'x-throttling-tier': 'Unlimited',
                'x-wso2-application-security': {
                    'security-types': ['oauth2'],
                    optional: false,
                },
            },
            put: {
                description: 'Update an existing Order',
                parameters: [
                    {
                        name: 'orderId',
                        in: 'path',
                        description: 'Order Id',
                        required: true,
                        schema: { type: 'string', format: 'string' },
                    },
                ],
                requestBody: { $ref: '#/components/requestBodies/Order' },
                responses: {
                    200: {
                        description: 'OK. Successful response with updated Order',
                        headers: {
                            Location: {
                                description: 'The URL of the newly created resource.',
                                schema: { type: 'string' },
                            },
                            'Content-Type': {
                                description: 'The content type of the body.',
                                schema: { type: 'string' },
                            },
                        },
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Order' },
                            },
                        },
                    },
                    400: {
                        description: 'Bad Request. Invalid request or validation error',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                    404: {
                        description:
                            'Not Found. The resource to be updated does not exist.',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                },
                security: [{ default: [] }],
                'x-auth-type': 'Application & Application User',
                'x-throttling-tier': 'Unlimited',
                'x-wso2-application-security': {
                    'security-types': ['oauth2'],
                    optional: false,
                },
            },
            delete: {
                description: 'Delete an existing Order',
                parameters: [
                    {
                        name: 'orderId',
                        in: 'path',
                        description: 'Order Id',
                        required: true,
                        schema: { type: 'string', format: 'string' },
                    },
                ],
                responses: {
                    200: { description: 'OK. Resource successfully deleted.' },
                    404: {
                        description: 'Not Found. Resource to be deleted does not exist.',
                        content: {
                            'application/json': {
                                schema: { $ref: '#/components/schemas/Error' },
                            },
                        },
                    },
                },
                security: [{ default: [] }],
                'x-auth-type': 'Application & Application User',
                'x-throttling-tier': 'Unlimited',
                'x-wso2-application-security': {
                    'security-types': ['oauth2'],
                    optional: false,
                },
            },
        },
    },
    'x-wso2-auth-header': 'Authorization',
    'x-wso2-cors': {
        corsConfigurationEnabled: false,
        accessControlAllowOrigins: ['*'],
        accessControlAllowCredentials: false,
        accessControlAllowHeaders: [
            'authorization',
            'Access-Control-Allow-Origin',
            'Content-Type',
            'SOAPAction',
            'apikey',
            'testKey',
        ],
        accessControlAllowMethods: [
            'GET',
            'PUT',
            'POST',
            'DELETE',
            'PATCH',
            'OPTIONS',
        ],
    },
    'x-wso2-production-endpoints': {
        urls: ['https://localhost:9443/am/sample/pizzashack/v1/api/'],
        type: 'http',
    },
    'x-wso2-sandbox-endpoints': {
        urls: ['https://localhost:9443/am/sample/pizzashack/v1/api/'],
        type: 'http',
    },
    'x-wso2-basePath': '/pizzashack/1.0.0',
    'x-wso2-transports': ['http', 'https'],
    'x-wso2-application-security': {
        'security-types': ['oauth2'],
        optional: false,
    },
    'x-wso2-response-cache': { enabled: false, cacheTimeoutInSeconds: 300 },
    components: {
        requestBodies: {
            Order: {
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/Order' },
                    },
                },
                description: 'Order object that needs to be added',
                required: true,
            },
        },
        securitySchemes: {
            default: {
                type: 'oauth2',
                flows: {
                    implicit: { authorizationUrl: 'https://test.com', scopes: {} },
                },
            },
        },
        schemas: {
            ErrorListItem: {
                required: ['code', 'message'],
                properties: {
                    message: {
                        type: 'string',
                        description: 'Description about individual errors occurred',
                    },
                    code: { type: 'integer', format: 'int64' },
                },
                title:
                    'Description of individual errors that may have occurred during a request.',
            },
            MenuItem: {
                required: ['name'],
                properties: {
                    price: { type: 'string' },
                    description: { type: 'string' },
                    name: { type: 'string' },
                    image: { type: 'string' },
                },
                title: 'Pizza menu Item',
            },
            Order: {
                required: ['orderId'],
                properties: {
                    customerName: { type: 'string' },
                    delivered: { type: 'boolean' },
                    address: { type: 'string' },
                    pizzaType: { type: 'string' },
                    creditCardNumber: { type: 'string' },
                    quantity: { type: 'number' },
                    orderId: { type: 'string' },
                },
                title: 'Pizza Order',
            },
            Error: {
                required: ['code', 'message'],
                properties: {
                    message: { type: 'string', description: 'Error message.' },
                    error: {
                        type: 'array',
                        description:
                            'If there are more than one error list them out. Ex. list out validation errors by each'
                            + ' field.',
                        items: { $ref: '#/components/schemas/ErrorListItem' },
                    },
                    description: {
                        type: 'string',
                        description: 'A detail description about the error message.',
                    },
                    code: { type: 'integer', format: 'int64' },
                    moreInfo: {
                        type: 'string',
                        description: 'Preferably an url with more details about the error.',
                    },
                },
                title: 'Error object returned with 4XX HTTP status',
            },
        },
    },
});

const getSampleAPIData = () => {
    return {
        name: 'PizzaShackAPI',
        description: 'This is a simple API for Pizza Shack online pizza delivery store.',
        context: '/pizzashack',
        version: '1.0.0',
        transport: ['http', 'https'],
        tags: ['pizza'],
        policies: ['Unlimited'],
        securityScheme: ['oauth2'],
        visibility: 'PUBLIC',
        businessInformation: {
            businessOwner: 'Jane Roe',
            businessOwnerEmail: 'marketing@pizzashack.com',
            technicalOwner: 'John Doe',
            technicalOwnerEmail: 'architecture@pizzashack.com',
        },
        endpointConfig: {
            endpoint_type: 'http',
            sandbox_endpoints: {
                url: 'https://localhost:9443/am/sample/pizzashack/v1/api/',
            },
            production_endpoints: {
                url: 'https://localhost:9443/am/sample/pizzashack/v1/api/',
            },
        },
        operations: [
            {
                target: '/order/{orderId}',
                verb: 'GET',
                throttlingPolicy: 'Unlimited',
                authType: 'Application & Application User',
            },
            {
                target: '/order/{orderId}',
                verb: 'DELETE',
                throttlingPolicy: 'Unlimited',
                authType: 'Application & Application User',
            },
            {
                target: '/order/{orderId}',
                verb: 'PUT',
                throttlingPolicy: 'Unlimited',
                authType: 'Application & Application User',
            },
            {
                target: '/menu',
                verb: 'GET',
                throttlingPolicy: 'Unlimited',
                authType: 'Application & Application User',
            },
            {
                target: '/order',
                verb: 'POST',
                throttlingPolicy: 'Unlimited',
                authType: 'Application & Application User',
            },
        ],
    };
};

const getSampleServiceMeta = () => ({
    name: 'Pizzashack-Endpoint',
    description: 'A Catalog Entry that exposes a REST endpoint',
    version: 'v1',
    serviceKey: 'Pizzashack-Endpoint-1.0.0',
    serviceUrl: 'https://localhost:9443/am/sample/pizzashack/v1/api/',
    definitionType: 'OAS3',
});

export { getSampleOpenAPI, getSampleAPIData, getSampleServiceMeta };
