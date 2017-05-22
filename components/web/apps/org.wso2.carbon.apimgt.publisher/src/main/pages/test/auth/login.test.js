import Auth from '../../src/app/data/Auth.js'
import {describe, it} from "mocha";
import {assert} from 'chai';
import https from 'https';

describe('Auth',
    function () {
        describe('#authenticateUser()',
            function () {
                it('Should return HTTP 200 status code if user authenticate',
                    function () {
                        let authenticator = new Auth();
                        const agent = new https.Agent({rejectUnauthorized: false});
                        let promised_auth = authenticator.authenticateUser('admin', 'admin');
                        return promised_auth.then((response) => {
                            assert.equal(response.status, 200);
                        });
                    }
                );
            }
        );
    }
);


