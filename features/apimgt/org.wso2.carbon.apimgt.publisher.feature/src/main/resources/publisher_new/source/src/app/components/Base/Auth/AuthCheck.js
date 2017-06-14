import React, {Component} from 'react'
import {Switch, Redirect} from 'react-router-dom'
import PropTypes from 'prop-types'
import qs from 'qs'

import AuthManager from '../../../data/Auth'
import ResourceNotFound from "../Errors/ResourceNotFound";

class AuthCheck extends Component {

    constructor(props) {
        super(props);
        this.state = {isLogged: AuthManager.isLogged()};
        if (props.response) {
            let status = props.response.status;
            this.state['isAuthorize'] = status !== 401;
            this.state['resourceNotFound'] = status == 404;
        }
    }

    render() {
        if (!this.state.isLogged || this.state.isAuthorize === false) {
            let params = qs.stringify({referrer: this.props.location.pathname});
            return (
                <Switch>
                    <Redirect to={{pathname: "/login", search: params}}/>
                </Switch>
            );
        } else if (this.state.resourceNotFound) {
            return (
                <ResourceNotFound {...this.props}/>
            );
        } else {
            return (
                <div>
                    {this.props.children}
                </div>
            );
        }
    }
}

AuthCheck.propTypes = {
    response: PropTypes.object
};

export default AuthCheck