/**
 * This file contains the Publisher web app related configurations
 * Note: In future,this configuration will be be parameterized and manage from deployment.toml
 */
const AppConfig = {
    app: {
        context: '/publisher', // Note the leading `/` and no trailing `/`
        customUrl: { // Dynamically set the redirect origin according to the forwardedHeader host|proxyPort combination
            enabled: false,
            forwardedHeader: 'X-Forwarded-For',
        },
        origin: {
            host: 'localhost', // Used to construct the loopback origin, It's very unlike you need to change this hostname,
            // It is `localhost` in 99.99% case, If you want to change server host name change it in deployment.toml
        },
        feedback: { // If enabled, Feedback form option(an icon) will be available in the footer LHS bottom
            enable: false,
            serviceURL: '', // Check `/source/src/app/components/Base/Footer/FeedbackForm.jsx` for details
        },
        singleLogout: {
            enabled: true, // If enabled, user will be logged out from the App when logged out from the IDP (eg: SSO logout from a different App).
            timeout: 4000, // Defines the timeout for the above periodical session status check
        },
        propertyDisplaySuffix: '__display',
        loadDefaultLocales: true, // If false, Default en.json file will not be used/loaded in app.
        // loadDefaultLocales = false is good for performance but text overrides using the locale file will not work
    },
    serviceCatalogDefinitionTypes: {
        OAS2: 'Swagger',
        OAS3: 'Open API V3',
        WSDL1: 'WSDL 1',
        WSDL2: 'WSDL 2',
        GRAPHQL_SDL: 'GraphQL SDL',
        ASYNC_API: 'AsyncAPI',
    },
    serviceCatalogSecurityTypes: {
        BASIC: 'Basic',
        DIGEST: 'Digest',
        OAUTH2: 'OAuth2',
        NONE: 'None',
        X509: 'X509',
        API_KEY: 'API Key',
    },
    apis: {
        alwaysShowDeploySampleButton: true,
    },
};

if (typeof module !== 'undefined') {
    module.exports = AppConfig; // For Jest unit tests
}
