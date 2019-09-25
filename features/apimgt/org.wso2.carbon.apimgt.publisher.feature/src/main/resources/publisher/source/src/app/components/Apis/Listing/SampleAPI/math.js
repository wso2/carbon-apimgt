const mathPayload = {
    name: 'MathAPI',
    description: 'This is a simple API for Pizza Shack online pizza delivery store.',
    context: '/mathapi',
    version: '1.0',
    transport: ['http', 'https'],
    tags: ['math'],
    policies: ['Unlimited'],
    securityScheme: ['oauth2'],
    visibility: 'PUBLIC',
    gatewayEnvironments: ['Production and Sandbox'],
    businessInformation: {
        businessOwner: 'Jane Roe',
        businessOwnerEmail: 'marketing@math.com',
        technicalOwner: 'John Doe',
        technicalOwnerEmail: 'architecture@math.com',
    },
    endpointConfig: {
        endpoint_type: 'http',
        sandbox_endpoints: {
            url: 'http://www.mocky.io/v2/5afe55d53200000f00222e02',
        },
        production_endpoints: {
            url: 'http://www.mocky.io/v2/5afe55d53200000f00222e02',
        },
    },
    operations: [
        {
            target: '/area',
            verb: 'GET',
            throttlingPolicy: 'Unlimited',
            authType: 'Application & Application User',
        },
        {
            target: '/volume',
            verb: 'GET',
            throttlingPolicy: 'Unlimited',
            authType: 'Application & Application User',
        },
        {
            target: '/multiply',
            verb: 'POST',
            throttlingPolicy: 'Unlimited',
            authType: 'Application & Application User',
        },
    ],
};

export default mathPayload;
