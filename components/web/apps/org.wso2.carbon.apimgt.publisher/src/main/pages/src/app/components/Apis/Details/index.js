import React, {Component} from 'react'
import {Route, Switch, Redirect} from 'react-router-dom'

import Overview from './Overview'
import NavBar from './NavBar'
import LifeCycle from './LifeCycle'
import {PageNotFound, APINotFound} from '../../Base/Errors/index'
import Loading from '../../Base/Loading/Loading'

import Api from '../../../data/api'

export default class Details extends Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            api: null,
            isAuthorize: false,
            notFound: false
        }
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.bindApi(response);
            }
        ).catch(
            error => {
                this.bindApi(error)
            }
        );
    }

    bindApi(api_response) {
        let status = api_response.status;
        let api = api_response.obj;
        if (status === 200) {
            this.setState({api: api});
        } else if (status === 404) {
            this.setState({notFound: true});
        }
    }

    render() {
        let redirect_url = this.state.api ? "/apis/" + this.props.match.params.api_uuid + "/overview" : "/apis/notFound";
        if (this.state.api) {
            return (
                <div>
                    <NavBar/>
                    <div className="tab-content">
                        <Switch>
                            <Redirect exact from="/apis/:api_uuid"
                                      to={redirect_url}/>
                            <Route path="/apis/:api_uuid/overview"
                                   render={ props => <Overview api={this.state.api} {...props} /> }/>
                            <Route path="/apis/:api_uuid/life-cycle" component={LifeCycle}/>
                            <Route component={PageNotFound}/>
                        </Switch>
                    </div>
                </div>
            );
        } else if (this.state.notFound) {
            return (
                <APINotFound {...this.props}/>
            );
        } else {
            return (
                <Loading/>
            );
        }
    }
}