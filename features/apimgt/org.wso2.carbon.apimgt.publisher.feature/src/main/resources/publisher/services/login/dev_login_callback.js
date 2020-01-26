/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const https = require('https');
const querystring = require('querystring');
/**
 *
 * This is a reference implementation for enable webpack dev server development
 * @export
 * @param {*} app
 * @param {*} server
 * @param {*} compiler
 */
function before(xapp, server, compiler) {
    xapp.get('/publisher/services/auth/callback/login', (inBoundReq, outBoundRes) => {
        const tokenRequestData = querystring.stringify({
            grant_type: 'authorization_code',
            code: inBoundReq.query.code,
            redirect_uri: 'https://localhost:8081/' + 'publisher' + '/services/auth/callback/login',
        });
        const requestOptions = {
            auth: 'hcL0Q3Fzf0P2ISAVubsovBoiaiIa:MoR95DwcmGRWTZoaxg361KuCl3ka',
            hostname: 'localhost',
            port: 9443,
            path: '/oauth2/token',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': tokenRequestData.length,
            },
            rejectUnauthorized: false,
            requestCert: true,
            agent: false,
        };

        const req = https.request(requestOptions, (res) => {
            console.log(`statusCode: ${res.statusCode}`);

            res.on('data', (d) => {
                const tokenResponse = JSON.parse(d.toString());
                let tokenLength = tokenResponse.access_token.length;
                const accessToken = String(tokenResponse.access_token);

                const idTokenLength = tokenResponse.id_token.length;
                const idToken = String(tokenResponse.id_token);

                const idTokenPart1 = idToken.substring(0, idTokenLength / 2);
                const idTokenPart2 = idToken.substring(idTokenLength / 2, idTokenLength);

                const accessTokenPart1 = accessToken.substring(0, tokenLength / 2);
                const accessTokenPart2 = accessToken.substring(tokenLength / 2, tokenLength);

                const refreshToken = String(tokenResponse.refresh_token);
                tokenLength = tokenResponse.refresh_token.length;
                const refreshTokenPart1 = refreshToken.substring(0, tokenLength / 2);
                const refreshTokenPart2 = refreshToken.substring(tokenLength / 2, tokenLength);
                const app = {
                    context: '',
                };
                outBoundRes.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
                    path: app.context + '/',
                    httpOnly: true,
                    secure: true,
                    maxAge: parseInt(tokenResponse.expires_in),
                });

                outBoundRes.cookie('AM_ACC_TOKEN_DEFAULT_P2', accessTokenPart2, {
                    path: '/api/am/publisher/',
                    httpOnly: true,
                    secure: true,
                    maxAge: parseInt(tokenResponse.expires_in),
                });

                outBoundRes.cookie('AM_REF_TOKEN_DEFAULT_P2', refreshTokenPart2, {
                    path: app.context + '/',
                    httpOnly: true,
                    secure: true,
                    maxAge: 86400,
                });

                outBoundRes.cookie('WSO2_AM_TOKEN_1_Default', accessTokenPart1, {
                    path: app.context + '/',
                    secure: true,
                    maxAge: parseInt(tokenResponse.expires_in),
                });

                outBoundRes.cookie('WSO2_AM_REFRESH_TOKEN_1_Default', refreshTokenPart1, {
                    path: app.context + '/',
                    secure: true,
                    maxAge: 86400,
                });

                outBoundRes.cookie('AM_ID_TOKEN_DEFAULT_P2', idTokenPart2, {
                    path: app.context + '/services/logout',
                    secure: true,
                    maxAge: parseInt(tokenResponse.expires_in),
                });

                outBoundRes.cookie('AM_ID_TOKEN_DEFAULT_P1', idTokenPart1, {
                    path: app.context + '/services/logout',
                    secure: true,
                    maxAge: parseInt(tokenResponse.expires_in),
                });

                outBoundRes.redirect('/');
            });
        });

        req.on('error', (error) => {
            console.error(error);
        });

        req.write(tokenRequestData);
        req.end();
    });
}

module.exports = before;
