import React, {Component} from 'react'
import ReactDom from 'react-dom'
import {BrowserRouter as Router, Route, Link, Switch} from 'react-router-dom'

import {Apis, Landing, Base} from './app/components/index'
import {PageNotFound} from './app/components/Base/Errors/index'
import Utils from '../src/app/data/utils.js'

import './App.css'

class Publisher extends Component {
    constructor() {
        super();
    }

    componentDidMount() {
        Utils.autoLogin(); // TODO: Remove once login page is implemented
    }

    render() {
        return (
            <Router basename="/publisher">
                <Base>
                    <Switch>
                        <Route exact path={"/"} component={Landing}/>
                        <Route path={"/apis"} component={Apis}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Base>
            </Router>
        );
    }
}

export default Publisher;
