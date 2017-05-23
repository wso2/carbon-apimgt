import Api from '../src/app/data/api.js';
import {describe, it} from "mocha";
import {assert} from 'chai';

describe('Api',
    function () {
        describe('#create()',
            function () {
                it('Should return HTTP 200 status code with newly created API UUID',
                    function () {
                        let api = new Api();
                        let data = {
                            "name": "test api",
                            "context": "/testing",
                            "version": "1.0.0",
                            "endpoint": []
                        };
                        let promised_create = api.create(data);
                        return promised_create.then((response) => {
                            debugger;
                            assert.equal(response.status, 200);
                        });
                    }
                );
            }
        );
    }
);


