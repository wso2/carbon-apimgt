/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, {Component} from 'react'
import {Route, Switch, Redirect} from 'react-router-dom'

import Overview from './Overview'
import ApiConsole from './ApiConsole'
import Documentation from './Documents/Documentation'
import Forum from './Forum'
import Sdk from './Sdk'
import BasicInfo from './BasicInfo'
import {PageNotFound} from '../../Base/Errors/index'
import AppBar from 'material-ui/AppBar';
import Tabs, { Tab } from 'material-ui/Tabs';
import ComputerIcon from 'material-ui-icons/Computer';
import ChromeReaderModeIcon from 'material-ui-icons/ChromeReaderMode';
import Grid from 'material-ui/Grid';
import LibraryBooksIcon from 'material-ui-icons/LibraryBooks';
import ForumIcon from 'material-ui-icons/Forum';
import GavelIcon from 'material-ui-icons/Gavel';
import Typography from 'material-ui/Typography';
import Paper from 'material-ui/Paper';
import {withStyles} from 'material-ui/styles';
import PropTypes from 'prop-types';

const styles = theme => ({
    imageSideContent: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    imageWrapper: {
        display: 'flex',
        flexAlign: 'top',
    },
    headline: {
        marginTop: 20
    },
    titleCase: {
        textTransform: 'capitalize',
    },
    chip: {
        marginLeft: 0,
        cursor: 'pointer',
    },
    openNewIcon: {
        display: 'inline-block',
        marginLeft: 20,
    },
    endpointsWrapper: {
        display: 'flex',
        justifyContent: 'flex-start',
    },
    paper: {
        marginBottom: 20,
    }
});

class Details extends Component {
    constructor(props){
        super(props);
        this.state = {
            value: 'overview',
            api: null,
        };
        this.setDetailsAPI = this.setDetailsAPI.bind(this);
    }

    setDetailsAPI(api){
        this.setState({api: api});
    }

    handleChange = (event, value) => {
        this.setState({ value });
        this.props.history.push({pathname: "/apis/" + this.props.match.params.api_uuid + "/" + value});
    };

    componentDidMount() {
        let currentTab = this.props.location.pathname.match(/[^\/]+(?=\/$|$)/g);
        if( currentTab && currentTab.length > 0){
            this.setState({ value: currentTab[0] });
        }
    }
    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        const classes = this.props.classes;
        return (
            <Grid container spacing={0} justify="center">
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10} >
                    <BasicInfo api_uuid={this.props.match.params.api_uuid} />
                    <Paper className={classes.paper}>
                        <Tabs
                            value={this.state.value}
                            onChange={this.handleChange}
                            fullWidth
                            indicatorColor="primary"
                            textColor="primary"
                        >
                            <Tab value="overview" icon={<ComputerIcon />} label="Overview" />
                            <Tab value="console" icon={<ChromeReaderModeIcon />} label="API Console" />
                            <Tab value="documentation" icon={<LibraryBooksIcon />} label="Documentation" />
                            <Tab value="forum" icon={<ForumIcon />} label="Forum" />
                            <Tab value="sdk" icon={<GavelIcon />} label="SDKs" />
                        </Tabs>
                    </Paper>
                    <Switch>
                        <Redirect exact from="/apis/:api_uuid" to={redirect_url}/>
                        <Route path="/apis/:api_uuid/overview" render={props => <Overview {...props} setDetailsAPI={this.setDetailsAPI}/>}/>
                        <Route path="/apis/:api_uuid/console" component={ApiConsole}/>
                        <Route path="/apis/:api_uuid/documentation" component={Documentation}/>
                        <Route path="/apis/:api_uuid/forum" component={Forum}/>
                        <Route path="/apis/:api_uuid/sdk" component={Sdk}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Grid>
            </Grid>
        );
    }
}

Details.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Details);
