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
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import ToolBar from '@material-ui/core/Toolbar';
import AppBar from '@material-ui/core/AppBar';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Footer from '../Base/Footer/Footer';
import Utils from "../../data/Utils";
import classNames from 'classnames';
import { Link } from 'react-router-dom';
import AuthManager from "../../data/AuthManager";
import ConfigManager from "../../data/ConfigManager";

const styles = theme => ({
    buttonsWrapper: {
        marginTop: theme.spacing.unit ,
        marginLeft: theme.spacing.unit * 175
    },
    buttonAlignment: {
        marginLeft: theme.spacing.unit * 2
    },
    linkDisplay: {
        textDecoration: 'none'
    },
});

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
        }).catch(() => {
            console.error('Error while receiving environment configurations');
        });
    };

    render(){
        const { classes } = this.props;
        return(
            <div></div>
        );
    }
}

AnonymousView.propTypes = {
    classes: PropTypes.object.isRequired,
};


export default withStyles(styles)(AnonymousView);
