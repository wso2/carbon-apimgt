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
import Typography from 'material-ui/Typography';
import ExpansionPanel, { ExpansionPanelDetails, ExpansionPanelSummary } from 'material-ui/ExpansionPanel';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { withStyles } from 'material-ui/styles';

import Grid from 'material-ui/Grid';
import ApiThumb from '../../Listing/ApiThumb';
import API from '../../../../data/api';
import { Progress } from '../../../Shared';
import AuthManager from '../../../../data/AuthManager';
import Utils from '../../../../data/Utils';
import EnvironmentPanelMessage from './EnvironmentPanelMessage';

const styles = theme => ({
    header: {
        borderTop: `1px solid ${theme.palette.divider}`,
    },
    lifeCycleMenu: {
        marginTop: '1.5em',
        marginBottom: '2em',
    },
});

class EnvironmentPanel extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isAuthorize: true,
        };
    }

    componentDidMount() {
        const { rootAPI, environment } = this.props;

        let api;
        if (AuthManager.getUser(environment.label)) {
            api = new API(environment);
        } else {
            this.setState({ isAuthorize: false });
            return;
        }

        const promisedAPIs = api.getAll({ query: `name:${rootAPI.name}` });
        promisedAPIs
            .then((response) => {
                // Filter more since getAll({query: name:apiName}) is not filtering with exact name
                const allApis = response.obj.list.filter(api => api.name === rootAPI.name);
                this.setState({
                    apis: allApis,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }

    render() {
        const { environment, rootAPI, classes } = this.props;
        const { apis, isAuthorize } = this.state;
        const isFeatureEnabled = Utils.isMultiEnvironmentOverviewEnabled(environment.label);

        return (
            <ExpansionPanel defaultExpanded>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant='title' gutterBottom>
                        {`${environment.label} Environment`}
                    </Typography>
                </ExpansionPanelSummary>

                <ExpansionPanelDetails className={classes.header}>
                    <Grid container>
                        <Grid item xs={12}>
                            {!isFeatureEnabled ? (
                                <EnvironmentPanelMessage message='Multi-Environment Overview Feature is not enabled in this environment.' />
                            ) : !isAuthorize ? (
                                <EnvironmentPanelMessage message='You are not login to this environment.' />
                            ) : !apis ? (
                                <Progress />
                            ) : apis.length === 0 ? (
                                <EnvironmentPanelMessage message='No APIs Found...' />
                            ) : (
                                <Grid container>
                                    {this.state.apis.map((api) => {
                                        return (
                                            <ApiThumb
                                                key={api.id}
                                                listType={this.state.listType}
                                                api={api}
                                                environmentName={environment.label}
                                                rootAPI={rootAPI}
                                                environmentOverview
                                            />
                                        );
                                    })}
                                </Grid>
                            )}
                        </Grid>
                    </Grid>
                </ExpansionPanelDetails>
            </ExpansionPanel>
        );
    }
}

export default withStyles(styles)(EnvironmentPanel);
