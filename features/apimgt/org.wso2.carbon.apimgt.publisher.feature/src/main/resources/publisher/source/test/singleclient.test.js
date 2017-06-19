import {before, it, describe} from 'mocha'
import {assert} from 'chai'

import SingleClient from '../src/app/data/SingleClient'


describe("SingleClient", function () {
    describe("#Constructor", function () {
        it("Should return same instance at all time: ", function () {
            let instance_one = new SingleClient();
            let instance_two = new SingleClient();
            assert.equal(instance_one,instance_two, "Two instances are not equal!");
        })
    })
})