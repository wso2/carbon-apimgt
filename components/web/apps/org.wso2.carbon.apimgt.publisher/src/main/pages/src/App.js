import React, {Component} from 'react'
import {Base, Listing} from "./app/components/index.js"
import Utils from '../src/app/data/utils.js'

class Publisher extends Component {
    constructor() {
        super();
        this.getName = this.getName.bind(this);
        this.state = {
            enteredText: ""
        }
    }

    componentDidMount() {
        Utils.autoLogin(); // TODO: Remove once login page is implemented
    }

    getName(event) {
        this.setState(
            {enteredText: this.nameInput.value}
        );
    }

    render() {
        return (
            <div>
                <Base>
                    <Listing/>
                </Base>
            </div>
        );
    }
}

export default Publisher;