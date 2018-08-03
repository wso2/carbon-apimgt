/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import ToolBar from 'material-ui/Toolbar';
import AppBar from 'material-ui/AppBar';
import Button from 'material-ui/Button';
import PropTypes from 'prop-types';
import {withStyles} from 'material-ui/styles';
import DarkTheme from "../Shared/DarkTheme";
import LightTheme from "../Shared/LightTheme";
import { createMuiTheme} from 'material-ui/styles';
import Footer from '../Base/Footer/Footer';
import Utils from "../../data/Utils";
import IconButton from 'material-ui/IconButton';
import Info from 'material-ui-icons/Info';
import classNames from 'classnames';
import { Link } from 'react-router-dom';
import AuthManager from "../../data/AuthManager";
import ConfigManager from "../../data/ConfigManager";

const themes = [];

themes.push(createMuiTheme(LightTheme));
themes.push(createMuiTheme(DarkTheme));

const styles = {
    buttonsWrapper: {
        marginTop: 5,
        marginLeft:1300
    },
    buttonAlignment: {
        marginLeft: 30,
    }
};

class AnonymousView extends React.Component{
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            environments: [],
            environmentId: 0
        };
    }

    componentDidMount(){
        ConfigManager.getConfigs().environments.then(response => {
            const environments = response.data.environments;
            let environmentId = Utils.getEnvironmentID(environments);
            if (environmentId === -1) {
                environmentId = 0;
            }
            this.setState({environments, environmentId});

            const environment = environments[environmentId];
            Utils.setEnvironment(environment);

        }).catch(error => {
            console.error('Error while receiving environment configurations');
        });
    };

    handleAuthentication = (e) => {
        e.preventDefault();
        let authManager = new AuthManager();
        let signup_promise = authManager.registerUser(this.state.environments[0]);
        signup_promise.then( (response) => {
            let redirect_url = "/sign-up";
            this.props.history.push(redirect_url);
        }).catch((error) => {
                console.log(error);
            }
        );
    };

    render(){
        const { classes } = this.props;
        return(
            <Grid>
                <AppBar
                    position="absolute"
                    className={classNames(classes.appBar, classes.appBarShift)}
                >
                    <ToolBar>
                        <IconButton
                            color="inherit"
                            aria-label="open drawer"
                            onClick={this.handleDrawerOpen}
                            className={classNames(classes.menuButton, classes.hide)}
                        >
                        </IconButton>
                        <Typography variant="title"  noWrap className={classes.brand}>
                            <img className={classes.siteLogo} src="/store/public/app/images/logo.png"
                                 alt="wso2-logo"/> <span>API STORE</span>
                        </Typography>


                        <IconButton aria-label="Search Info" color="default">
                            <Info onClick={this.handleClickTips}/>
                        </IconButton>
                        <div className={classes.buttonsWrapper}>
                            <Link to={"/sign-up"}>
                        <Button variant="raised" color="secondary">
                            Sign-up
                        </Button>
                            </Link>
                        <Link to={"/login"}>
                            <Button variant="raised" color="secondary" className={classes.buttonAlignment}>
                                Sign-in
                            </Button>
                        </Link>
                        </div>
                </ToolBar>
                </AppBar>
                <Footer/>
            </Grid>
        );
    }
}

AnonymousView.propTypes = {
    classes: PropTypes.object.isRequired,
};


export default withStyles(styles)(AnonymousView);