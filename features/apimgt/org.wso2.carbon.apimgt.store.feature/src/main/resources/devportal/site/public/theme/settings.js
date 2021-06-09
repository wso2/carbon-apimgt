const Settings = {
    app: {
        context: '/devportal', // Note the leading `/` and no trailing `/`
        /*
        If the proxy context path is configured, it's required to provide it here as well.
        for example, to serve https://company.com/apim/devportal/ URL the context and proxy_context_path will be as follows.
        context: '/apim/devportal',
        proxy_context_path: '/apim',
        */
        customUrl: {
            enabled: false,
            forwardedHeader: 'X-Forwarded-For',
        },
        origin: {
            host: 'localhost',
        },
        subscriptionLimit: 1000,
        subscribeApplicationLimit: 5000,
        alertMaxAPIGetLimit: 5000,
        isPassive: true,
        singleLogout: {
            enabled: true, // If enabled, user will be logged out from the App when logged out from the IDP (eg: SSO logout from a different App).
            timeout: 4000, // Defines the timeout for the above periodical session status check
        },
        propertyDisplaySuffix: '__display',
        markdown: {
            skipHtml: true,
        }
    },
    grantTypes: {
        authorization_code: 'Code',
        implicit: 'Implicit',
        refresh_token: 'Refresh Token',
        password: 'Password',
        'iwa:ntlm': 'IWA-NTLM',
        client_credentials: 'Client Credentials',
        'urn:ietf:params:oauth:grant-type:saml2-bearer': 'SAML2',
        'urn:ietf:params:oauth:grant-type:jwt-bearer': 'JWT',
        kerberos: 'Kerberos',
        'urn:ietf:params:oauth:grant-type:device_code': 'Device Code',
    },
    passwordChange: {
        guidelinesEnabled: false,
        policyList: [
            'Policy 1',
            'Policy 2',
            'Policy 3',
        ],
    },
};
