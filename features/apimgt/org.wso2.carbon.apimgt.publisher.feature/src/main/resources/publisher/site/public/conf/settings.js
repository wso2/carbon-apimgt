/**
 * This file contains the Publisher web app related configurations
 * Note: In future,this configuration will be be parameterized and manage from deployment.toml
 */
const AppConfig = {
    app: {
        context: '/publisher', // Note the leading `/` and no trailing `/`
        /*
        If the proxy context path is configured, it's required to provide it here as well.
        for example, to serve https://company.com/apim/publisher/ URL the context and proxy_context_path will be as follows.
        context: '/apim/publisher',
        proxy_context_path: '/apim',
        */
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
        markdown: {
            skipHtml: true,
        },
        alertMaxAPIGetLimit: 5000,
        alertMaxAPIProductGetLimit: 5000,
    },
};

if (typeof module !== 'undefined') {
    module.exports = AppConfig; // To be used in JS unit tests
}
