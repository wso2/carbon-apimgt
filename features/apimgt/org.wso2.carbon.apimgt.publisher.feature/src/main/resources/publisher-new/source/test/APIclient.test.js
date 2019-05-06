import {before, it, describe} from 'mocha'
import {assert} from 'chai'

import APIClient from '../src/app/data/APIClient'


describe("APIClient", function () {
    describe("#Constructor", function () {
        it("Should return same instance at all time: ", function () {
            let instance_one = new APIClient();
            let instance_two = new APIClient();
            assert.notEqual(instance_one,instance_two, "Two instances are equal!");
        })
    })
})