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
import Api from '../../../../data/api'
import ConfigManager from '../../../../data/ConfigManager'
import LifeCycleUpdate from './LifeCycleUpdate'
import Loading from "../../../Base/Loading/Loading"
import LifeCycleHistory from "./LifeCycleHistory"

import Card, {  CardContent } from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';

class LifeCycle extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {};
        this.api_uuid = props.match.params.api_uuid;
        this.updateData = this.updateData.bind(this);
    }

    componentWillMount() {
        this.updateData();
    }

    updateData() {
        let promised_api = this.api.get(this.api_uuid);
        let promised_tiers = this.api.policies('api');
        let promised_lcState = this.api.getLcState(this.api_uuid);
        let privateJetModeEnabled = false;

        ConfigManager.getConfigs().features.then(response => {

            const features = response.data.list;
            const PRIVATE_JET_MODE = "privateJetMode";

            for (let feature of features) {
                if(feature.id == PRIVATE_JET_MODE){
                    privateJetModeEnabled = feature.isEnabled;
                    break;
                }
            }

            if (privateJetModeEnabled) {

                promised_api.then(function(apiResult) {

                    let hasOwnGateway = apiResult.body.hasOwnGateway;
                    if(!hasOwnGateway) {

                        promised_lcState.then(function(result) {

                            let transitions = result.body.availableTransitionBeanList;
                            const PUBLISHED = "Published";

                            for (let transition of transitions) {
                                if(transition.targetState == PUBLISHED && result.body.state != PUBLISHED) {
                                  const publish_in_private_jet_mode = {
                                    event: "Publish In Private Jet Mode",
                                    targetState: "Published In Private Jet Mode"
                                  };
                                  result.body.availableTransitionBeanList.push(publish_in_private_jet_mode);
                                }
                            }
                        }, function(err) {
                          console.log(err);
                        });
                    }
                }, function(err) {
                    console.log(err);
                });
            }
        });

        let promised_lcHistory = this.api.getLcHistory(this.api_uuid);
        let promised_labels = this.api.labels();
        Promise.all([promised_api, promised_tiers, promised_lcState, promised_lcHistory, promised_labels])
            .then(response => {
                let [api, tiers, lcState, lcHistory, labels] = response.map(data => data.obj);
                this.setState({api: api, policies: tiers, lcState: lcState, lcHistory: lcHistory, labels: labels,
                privateJetModeEnabled: privateJetModeEnabled});
            });
    }

    render() {
        if (this.state.api) {
            return (
                    <Grid item>
                        <Grid item xs={12}>
                            <Paper>
                                <Typography type="display2">
                                    {this.state.api.name} - <span>Change Lifecycle</span>
                                </Typography>
                                <Typography type="caption" gutterBottom align="left">
                                    Manage API lifecycle from cradle to grave: create, publish,
                                    block, deprecate, and retire
                                </Typography>

                            </Paper>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography type="headline" className="title-gap">
                                Change Lifecycle
                            </Typography>
                            <Card>
                                <CardContent>
                                    <LifeCycleUpdate handleUpdate={this.updateData} lcState={this.state.lcState}
                                         api={this.state.api} privateJetModeEnabled={this.state.privateJetModeEnabled}/>
                                </CardContent>
                            </Card>
                            {this.state.lcHistory.length > 1 &&
                                <div>
                                    <Typography type="headline" className="title-gap">
                                        History
                                    </Typography>
                                    <Card>
                                        <CardContent>
                                            <LifeCycleHistory
                                                lcHistory={this.state.lcHistory}/>
                                        </CardContent>
                                    </Card>
                                </div> }
                        </Grid>
                    </Grid>
            );
        } else {
            return <Loading/>
        }
    }
}

export default LifeCycle