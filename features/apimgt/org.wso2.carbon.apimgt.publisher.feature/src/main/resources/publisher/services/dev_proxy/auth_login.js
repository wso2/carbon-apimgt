// Experiment 1 https://github.com/wso2/carbon-apimgt/pull/8190/files
const https = require('https');
const fetch = require('node-fetch');
const querystring = require('querystring');
const fs = require('fs');
const path = require('path');
const appSettings = require('../../site/public/conf/settings');

// From https://github.com/node-fetch/node-fetch/issues/19#issuecomment-289709519
const agent = new https.Agent({
    rejectUnauthorized: false,
});
const callbackUrl = 'https://localhost:8081/publisher/services/auth/callback/login';

/**
 *
 * @param {*} data
 */
function oauthAppCache(data) {
    if (data) {
        return fs.writeFileSync(path.join(__dirname, '.', 'keys.json'), JSON.stringify(data), 'utf8');
    } else {
        const rData = fs.readFileSync(path.join(__dirname, '.', 'keys.json'), 'utf8');
        return JSON.parse(rData);
    }
}

/**
 *
 */
async function getSettings() {
    const res = await fetch('https://localhost:9443/api/am/publisher/v2/settings', { agent });
    const data = await res.json();
    return data;
}

/**
 *
 */
async function generateToken(code, keys) {
    const { clientId, clientSecret } = keys;
    const encodedKeys = Buffer.from(`${clientId}:${clientSecret}`).toString('base64');
    const tokenRequestData = new URLSearchParams({
        grant_type: 'authorization_code',
        code,
        redirect_uri: callbackUrl,
    });
    const tokenEndpoint = 'https://localhost:9443/oauth2/token';
    const response = await fetch(tokenEndpoint, {
        method: 'post',
        body: tokenRequestData,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            Authorization: 'Basic ' + encodedKeys,
        },
        agent,
    });
    const data = await response.json();
    console.log('OAuth token generated !');
    return data;
}

/**
 *
 * @param {*} params
 */
async function doDCR() {
    const dcrRequestData = {
        callbackUrl,
        clientName: 'publisher_webpack_dev',
        owner: 'admin',
        grantType: 'authorization_code refresh_token',
        saasApp: true,
    };
    const dcrURL = 'https://localhost:9443/client-registration/v0.17/register';
    const response = await fetch(dcrURL, {
        method: 'post',
        body: JSON.stringify(dcrRequestData),
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Basic YWRtaW46YWRtaW4=',
        },
        agent,
    });
    const data = await response.json();
    oauthAppCache(data);
    return data;
}

const clientRoutingBypass = (req, res, proxyOptions) => {
    if (req.path.startsWith('/publisher/site/public/images/')) {
        return req.path.split('/publisher')[1];
    } else if (req.headers.accept.indexOf('html') !== -1) {
        console.log('Skipping proxy for browser request.');
        return '/publisher/index.html';
    }
    return null;
};


const setResponseSessionCookies = (res, accessToken, refreshToken, idToken, sessionState, expiresIn) => {
    const idTokenLength = idToken.length;

    const idTokenPart1 = idToken.substring(0, idTokenLength / 2);
    const idTokenPart2 = idToken.substring(idTokenLength / 2, idTokenLength);
    const tokenLength = refreshToken.length;

    const accessTokenPart1 = accessToken.substring(0, tokenLength / 2);
    const accessTokenPart2 = accessToken.substring(tokenLength / 2, tokenLength);

    const refreshTokenPart1 = refreshToken.substring(0, tokenLength / 2);
    const refreshTokenPart2 = refreshToken.substring(tokenLength / 2, tokenLength);
    const maxAge = expiresIn * 1000;
    res.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
        path: '/publisher',
        httpOnly: true,
        secure: true,
        maxAge,
    });

    res.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
        path: '/api/am/publisher/',
        httpOnly: true,
        secure: true,
        maxAge,
    });

    res.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
        path: '/api/am/service-catalog/v0/',
        httpOnly: true,
        secure: true,
        maxAge,
    });

    res.cookie('AM_REF_TOKEN_DEFAULT_P2', refreshTokenPart2, {
        path: '/publisher',
        httpOnly: true,
        secure: true,
        maxAge: 86400,
    });

    res.cookie('WSO2_AM_TOKEN_1_Default', accessTokenPart1, {
        path: '/publisher',
        secure: true,
        maxAge,
    });

    res.cookie('WSO2_AM_REFRESH_TOKEN_1_Default', refreshTokenPart1, {
        path: '/publisher',
        secure: true,
        maxAge: 86400,
    });

    res.cookie('AM_ID_TOKEN_DEFAULT_P2', idTokenPart2, {
        path: '/publisher/services/logout',
        secure: true,
        maxAge,
    });

    res.cookie('AM_ID_TOKEN_DEFAULT_P1', idTokenPart1, {
        path: '/publisher/services/logout',
        secure: true,
        maxAge,
    });

    res.cookie('publisher_session_state', sessionState, {
        path: '/publisher',
        secure: true,
        maxAge: 14400000,
    });
};


/**
 *
 * @param {*} xapp
 * @param {*} server
 * @param {*} compiler
 */
function devServerBefore(app, server, compiler) {
    const isTestRun = process.env && process.env.WSO2_UI_TEST === 'ci';
    if (isTestRun) {
        app.get('/services/settings/settings.js', async (req, res, next) => {
            res.setHeader('Content-Type', 'application/javascript');
            const updatedConfig = { ...appSettings };
            updatedConfig.idp = {
                origin: 'https://localhost:8081',
                checkSessionEndpoint: 'https://localhost:9443/oidc/checksession',
            };
            const content = 'const AppConfig =' + JSON.stringify(updatedConfig) + ';';
            res.send(content);
        });
        app.get('/publisher/services/auth/introspect', async (req, res, next) => {
            const mockIntrospect = {
                aut: 'APPLICATION_USER',
                nbf: 1619542841,
                scope: 'apim:admin apim:api_create apim:api_delete apim:api_generate_key apim:api_import_export'
                + ' apim:api_product_import_export apim:api_publish apim:api_view apim:app_import_export apim:clien'
                + 't_certificates_add apim:client_certificates_update apim:client_certificates_view apim:comment_vie'
                + 'w apim:comment_write apim:document_create apim:document_manage apim:ep_certificates_add apim:ep_ce'
                + 'rtificates_update apim:ep_certificates_view apim:mediation_policy_create apim:mediation_policy_mana'
                + 'ge apim:mediation_policy_view apim:pub_alert_manage apim:publisher_settings apim:shared_scope_manag'
                + 'e apim:subscription_block apim:subscription_view apim:threat_protection_policy_create apim:threat_p'
                + 'rotection_policy_manage openid service_catalog:service_view service_catalog:service_write',
                active: true,
                token_type: 'Bearer',
                exp: 1619546441,
                iat: 1619542841,
                client_id: 'ZyEB9ocLHxI2CaH1VvpUceD2tPIa',
                username: 'itestUser@carbon.super',
            };
            res.setHeader('Content-Type', 'application/json');
            res.json(mockIntrospect);
        });
    }

    app.get('/publisher/services/auth/login', async (req, res, next) => {
        if (isTestRun) {
            const mockIdToken = 'eyJraWQiOiIxZTlnZGs3IiwiYWxnIjoiUlMyNTYifQ.ewogImlzcyI6ICJodHRwOi8vc2VydmVyLmV4YW1'
            + 'wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUz'
            + 'ZfV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5NzAsCiAibmFtZSI6ICJKYW5l'
            + 'IERvZSIsCiAiZ2l2ZW5'
            + 'fbmFtZSI6ICJKYW5lIiwKICJmYW1pbHlfbmFtZSI6ICJEb2UiLAogImdlbmRlciI6ICJmZW1hbGUiLAogImJpcnRoZGF0ZSI6ICIwMD'
            + 'AwLTEwLTMxIiwKICJlbWFpbCI6ICJqYW5lZG9lQGV4YW1wbGUuY29tIiwKICJwaWN0dXJlIjogImh0dHA6Ly9leGFtcGxlLmNvbS9qY'
            + 'W5lZG9lL21lLmpwZyIKfQ.rHQjEmBqn9Jre0OLykYNnspA10Qql2rvx4FsD00jwlB0Sym4NzpgvPKsDjn_wMkHxcp6CilPcoKrWHcipR'
            + '2iAjzLvDNAReF97zoJqq880ZD1bwY82JDauCXELVR9O6_B0w3K-E7yM2macAAgNCUwtik6SjoSUZRcf-O5lygIyLENx882p6MtmwaL1h'
            + 'd6qn5RZOQ0TLrOYu0532g9Exxcm-ChymrB4xLykpDj3lUivJt63eEGGN6DH5K6o33TcxkIjNrCD4XB1CKKumZvCedgHHF3IAK4dVEDSU'
            + 'oGlH9z4pP_eWYNXvqQOjGs-rDaQzUHl6cQQWNiDpWOl_lxXjQEvQ';
            setResponseSessionCookies(res, 'mock_access_token', 'mock_refresh_token',
                mockIdToken, '', 99999999);
            res.redirect('/publisher/');
        } else {
            const dcrData = await doDCR();
            const { clientId } = dcrData;
            const settingsData = await getSettings();
            const { scopes } = settingsData;
            res.write = () => { };
            const authRequestParams = '?response_type=code&client_id='
                + clientId + '&scope=' + scopes.join(' ')
                + ' service_catalog:service_view service_catalog:service_write'
                + '&redirect_uri=' + callbackUrl;

            const location = 'https://localhost:9443/oauth2/authorize' + authRequestParams;

            res.redirect(location);
        }
    });
    app.get('/publisher/services/auth/callback/login', async (req, res, next) => {
        const { code, session_state } = req.query;
        const keys = oauthAppCache();
        const tokens = await generateToken(code, keys);
        setResponseSessionCookies(res, tokens.access_token, tokens.refresh_token,
            tokens.id_token, session_state, tokens.expires_in);
        res.redirect('/publisher/');
    });
}
module.exports.clientRoutingBypass = clientRoutingBypass;
module.exports.devServerBefore = devServerBefore;
