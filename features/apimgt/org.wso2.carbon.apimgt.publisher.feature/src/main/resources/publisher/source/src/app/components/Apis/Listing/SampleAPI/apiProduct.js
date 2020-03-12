const getApiProductPayload = (calculatorApiId, mathApiId) => ({
    name: 'CalculatorAPIProduct',
    context: '/calc_prod',
    description: 'A calculator API Product that supports basic operations',
    visibility: 'PUBLIC',
    visibleRoles: ['testrole', 'admin'],
    visibleTenants: ['string'],
    policies: ['Bronze', 'Silver', 'Unlimited'],
    apiThrottlingPolicy: 'Unlimited',
    state: 'PUBLISHED',
    subscriptionAvailability: 'ALL_TENANTS',
    subscriptionAvailableTenants: [],
    additionalProperties: {
        newprop: 'string',
        additionalProp2: 'string',
        additionalProp3: 'string',
    },
    businessInformation: {
        businessOwner: 'businessowner',
        businessOwnerEmail: 'businessowner@wso2.com',
    },
    gatewayEnvironments: ['Production and Sandbox'],
    transport: ['http', 'https'],
    apis: [
        {
            apiId: calculatorApiId,
            operations: [
                {
                    target: '/add',
                    verb: 'POST',
                },
                {
                    target: '/divide',
                    verb: 'POST',
                },
            ],
        },
        {
            apiId: mathApiId,
            operations: [
                {
                    target: '/area',
                    verb: 'GET',
                },
                {
                    target: '/volume',
                    verb: 'GET',
                },
            ],
        },
    ],
});

export default getApiProductPayload;
