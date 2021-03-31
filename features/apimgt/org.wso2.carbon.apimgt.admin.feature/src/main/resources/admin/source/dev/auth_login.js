// Experiment 1 https://github.com/wso2/carbon-apimgt/pull/8190/files
const https = require('https');
const fetch = require('node-fetch');
const querystring = require('querystring');
const fs = require('fs');
const path = require('path');

// From https://github.com/node-fetch/node-fetch/issues/19#issuecomment-289709519
const agent = new https.Agent({
    rejectUnauthorized: false,
});
const callbackUrl = 'https://localhost:8081/admin/services/auth/callback/login';

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
    const res = await fetch('https://localhost:9443/api/am/admin/v2/settings', { agent });
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
        clientName: 'admin_webpack_dev',
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
    if (req.path.startsWith('/admin/site/public/images/')) {
        return req.path.split('/admin')[1];
    } else if (req.headers.accept.indexOf('html') !== -1) {
        console.log('Skipping proxy for browser request.');
        return '/admin/index.html';
    }
    return null;
};

/**
 *
 * @param {*} xapp
 * @param {*} server
 * @param {*} compiler
 */
function devServerBefore(app, server, compiler) {
    app.get('/admin/services/auth/login', async (req, res, next) => {
        const dcrData = await doDCR();
        const { clientId } = dcrData;
        const settingsData = await getSettings();
        const { scopes } = settingsData;
        res.write = () => { };
        const authRequestParams = '?response_type=code&client_id='
            + clientId + '&scope=' + scopes.join(' ') + ' service_catalog:service_view service_catalog:service_write'
            + '&redirect_uri=' + callbackUrl;

        const location = 'https://localhost:9443/oauth2/authorize' + authRequestParams;

        res.redirect(location);
    });
    app.get('/admin/services/auth/callback/login', async (req, res, next) => {
        const { code, session_state } = req.query;
        const keys = oauthAppCache();
        const tokens = await generateToken(code, keys);

        const accessToken = tokens.access_token;

        const idTokenLength = tokens.id_token.length;
        const idToken = tokens.id_token;

        const idTokenPart1 = idToken.substring(0, idTokenLength / 2);
        const idTokenPart2 = idToken.substring(idTokenLength / 2, idTokenLength);
        const tokenLength = tokens.refresh_token.length;

        const accessTokenPart1 = accessToken.substring(0, tokenLength / 2);
        const accessTokenPart2 = accessToken.substring(tokenLength / 2, tokenLength);

        const refreshToken = tokens.refresh_token;
        const refreshTokenPart1 = refreshToken.substring(0, tokenLength / 2);
        const refreshTokenPart2 = refreshToken.substring(tokenLength / 2, tokenLength);
        const maxAge = tokens.expires_in * 1000;
        res.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
            path: '/admin',
            httpOnly: true,
            secure: true,
            maxAge,
        });

        res.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
            path: '/api/am/admin/',
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
            path: '/admin',
            httpOnly: true,
            secure: true,
            maxAge: 86400,
        });

        res.cookie('WSO2_AM_TOKEN_1_Default', accessTokenPart1, {
            path: '/admin',
            secure: true,
            maxAge,
        });

        res.cookie('WSO2_AM_REFRESH_TOKEN_1_Default', refreshTokenPart1, {
            path: '/admin',
            secure: true,
            maxAge: 86400,
        });

        res.cookie('AM_ID_TOKEN_DEFAULT_P2', idTokenPart2, {
            path: '/admin/services/logout',
            secure: true,
            maxAge,
        });

        res.cookie('AM_ID_TOKEN_DEFAULT_P1', idTokenPart1, {
            path: '/admin/services/logout',
            secure: true,
            maxAge,
        });

        res.cookie('admin_session_state', session_state, {
            path: '/admin',
            secure: true,
            maxAge: 14400000,
        });

        res.redirect('/admin/');
    });
}
module.exports.clientRoutingBypass = clientRoutingBypass;
module.exports.devServerBefore = devServerBefore;
