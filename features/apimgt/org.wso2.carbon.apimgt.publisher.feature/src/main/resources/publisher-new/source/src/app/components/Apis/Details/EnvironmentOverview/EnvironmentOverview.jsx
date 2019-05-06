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

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

import Api from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import { Progress } from 'AppComponents/Shared';
import ConfigManager from 'AppData/ConfigManager';

import EnvironmentPanel from './EnvironmentPanel';

class EnvironmentOverview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            environments: [],
            api: null,
            notFound: false,
        };
        this.api_uuid = this.props.match.params.api_uuid;
    }

    componentDidMount() {
        const api = new Api();
        const promisedApi = api.get(this.api_uuid);
        promisedApi
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });

        ConfigManager.getConfigs()
            .environments.then((response) => {
                const { environments } = response.data;
                this.setState({ environments });
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });
    }

    render() {
        const { api } = this.state;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }

        return (
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Typography type='display1' gutterBottom>
                            {api.name} {api.version} - Multi Environment Overview
                        </Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12}>
                    {this.state.environments.map(environment => (
                        <EnvironmentPanel rootAPI={api} environment={environment} key={environment.name} />
                    ))}
                </Grid>
            </Grid>
        );
    }
}

EnvironmentOverview.defaultProps = {
    resourceNotFountMessage: '',
};

EnvironmentOverview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            api_uuid: PropTypes.shape({}),
        }),
    }).isRequired,
    resourceNotFountMessage: PropTypes.string,
};
export default EnvironmentOverview;
