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

import React, { Component } from 'react';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

import Api from '../../../../data/api';
import ConfigManager from '../../../../data/ConfigManager';
import LifeCycleUpdate from './LifeCycleUpdate';
import { Progress } from '../../../Shared';
import LifeCycleHistory from './LifeCycleHistory';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

class LifeCycle extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {
            notFound: false,
        };
        this.api_uuid = props.match.params.api_uuid;
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.updateData();
    }

    updateData() {
        const promised_api = this.api.get(this.api_uuid);
        const promised_tiers = this.api.policies('api');
        const promised_lcState = this.api.getLcState(this.api_uuid);
        let privateJetModeEnabled = false;

        ConfigManager.getConfigs().features.then((response) => {
            privateJetModeEnabled = response.data.privateJetMode.isEnabled;
        });

        const promised_lcHistory = this.api.getLcHistory(this.api_uuid);
        const promised_labels = this.api.labels();
        Promise.all([promised_api, promised_tiers, promised_lcState, promised_lcHistory, promised_labels])
            .then((response) => {
                const [api, tiers, lcState, lcHistory, labels] = response.map(data => data.obj);

                if (privateJetModeEnabled) {
                    if (!api.hasOwnGateway) {
                        const transitions = lcState.availableTransitionBeanList;
                        const PUBLISHED = 'Published';

                        for (const transition of transitions) {
                            if (transition.targetState == PUBLISHED && lcState.state != PUBLISHED) {
                                const publish_in_private_jet_mode = {
                                    event: 'Publish In Private Jet Mode',
                                    targetState: 'Published In Private Jet Mode',
                                };
                                lcState.availableTransitionBeanList.push(publish_in_private_jet_mode);
                            }
                        }
                    }
                }

                this.setState({
                    api,
                    policies: tiers,
                    lcState,
                    lcHistory,
                    labels,
                    privateJetModeEnabled,
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    render() {
        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }
        return (
            <Grid container>
                <Grid item xs={12}>
                    <LifeCycleUpdate
                        handleUpdate={this.updateData}
                        lcState={this.state.lcState}
                        api={api}
                        privateJetModeEnabled={this.state.privateJetModeEnabled}
                    />
                </Grid>
                <Grid item xs={12}>
                    {this.state.lcHistory.length > 1 && (
                        <div>
                            <Typography variant='headline' gutterBottom>
                                History
                            </Typography>
                            <Paper>
                                <LifeCycleHistory lcHistory={this.state.lcHistory} />
                            </Paper>
                        </div>
                    )}
                </Grid>
            </Grid>
        );
    }
}

export default LifeCycle;
