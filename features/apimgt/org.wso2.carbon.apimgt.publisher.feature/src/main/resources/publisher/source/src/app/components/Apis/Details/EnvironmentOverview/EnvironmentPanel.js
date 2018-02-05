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

import React, {Component} from 'react'
import Typography from 'material-ui/Typography';
import ExpansionPanel, {ExpansionPanelDetails, ExpansionPanelSummary} from 'material-ui/ExpansionPanel';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Grid from 'material-ui/Grid';
import LifecycleMenu from "./LifecycleMenu";
import ApiThumb from "../../Listing/ApiThumb";
import API from "../../../../data/api";
import Loading from "../../../Base/Loading/Loading";
import {LifeCycleStatus} from "../../../../data/LifeCycle";
import AuthManager from "../../../../data/AuthManager";

class EnvironmentPanel extends Component {
    constructor(props) {
        super(props);
        this.state = {
            lifecycleStatus: "All",
            lifecycleStatuses: LifeCycleStatus,
            isAuthorize: true,
        };

        this.handleLifecycleStateChange = this.handleLifecycleStateChange.bind(this);
    }

    componentDidMount() {
        const {apiName, environment} = this.props;

        let api;
        if (AuthManager.getUser(environment.label)) {
            api = new API(environment);
        } else {
            this.setState({isAuthorize: false});
            return;
        }

        let promised_apis = api.getAll({query: `name:${apiName}`});
        promised_apis.then((response) => {
            // Filter more since getAll({query: name:apiName}) is not filtering with exact name
            const allApis = response.obj.list.filter(api => api.name === apiName);
            this.setState({
                allApis,
                apis: allApis
            });
        }).catch(error => {
            if (process.env.NODE_ENV !== "production")
                console.log(error);
            let status = error.status;
            if (status === 404) {
                this.setState({notFound: true});
            } else if (status === 401) {
                this.setState({isAuthorize: false});
            }
        });
    }

    handleLifecycleStateChange(event) {
        const lifecycleStatus = event.target.value;
        let apis;

        if (lifecycleStatus === "All") {
            apis = this.state.allApis;
        } else {
            apis = this.state.allApis.filter(api => api.lifeCycleStatus === lifecycleStatus);
        }

        this.setState({
            lifecycleStatus,
            apis
        });
    }

    render() {
        const {environment} = this.props;
        const {apis, isAuthorize, notFound} = this.state;

        if (isAuthorize) {
            return (
                <ExpansionPanel defaultExpanded>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                        <Typography> {`${environment.label} Environment`} </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                        <Grid container>
                            <Grid item xs={12}>
                                <Grid container direction={"row-reverse"}>
                                    <Grid item lg={2} md={3} sm={4} xs={12}>
                                        <LifecycleMenu
                                            lifecycleStatus={this.state.lifecycleStatus}
                                            lifecycleStatuses={this.state.lifecycleStatuses}
                                            handleLifecycleStateChange={this.handleLifecycleStateChange}
                                        />
                                    </Grid>
                                    {
                                        (apis && apis.length === 0) ?
                                            <Grid item lg={10} md={9} sm={8} xs={12}>
                                                <div>No APIs found...</div>
                                            </Grid>
                                            :
                                            null
                                    }
                                </Grid>

                            </Grid>
                            <Grid item xs={12}>
                                {
                                    (!apis) ? <Loading/> :
                                        <Grid container>
                                            {this.state.apis.map((api, i) => {
                                                return <ApiThumb key={api.id} listType={this.state.listType}
                                                                 api={api}
                                                                 environmentName={environment.label}
                                                                 environmentOverview/>
                                            })}
                                        </Grid>
                                }
                            </Grid>
                        </Grid>
                    </ExpansionPanelDetails>
                </ExpansionPanel>
            );
        } else {
            return null;
        }
    }
}

export default EnvironmentPanel;
