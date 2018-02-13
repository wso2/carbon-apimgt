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
import ApiThumb from "../../Listing/ApiThumb";
import API from "../../../../data/api";
import Loading from "../../../Base/Loading/Loading";
import AuthManager from "../../../../data/AuthManager";
import {withStyles} from 'material-ui/styles';

const styles = theme => ({
    header: {
        borderTop: `1px solid ${theme.palette.divider}`
    },
    lifeCycleMenu: {
        marginTop: '1.5em',
        marginBottom: '2em'
    },
    messageLabel: {
        fontSize: '1.5em'
    }
});

class EnvironmentPanel extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isAuthorize: true,
        };
    }

    componentDidMount() {
        const {rootAPI, environment} = this.props;

        let api;
        if (AuthManager.getUser(environment.label)) {
            api = new API(environment);
        } else {
            this.setState({isAuthorize: false});
            return;
        }

        let promised_apis = api.getAll({query: `name:${rootAPI.name}`});
        promised_apis.then((response) => {
            // Filter more since getAll({query: name:apiName}) is not filtering with exact name
            const allApis = response.obj.list.filter(api => api.name === rootAPI.name);
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

    render() {
        const {environment, rootAPI, classes} = this.props;
        const {apis, isAuthorize, notFound} = this.state;

        if (isAuthorize) {
            return (
                <ExpansionPanel defaultExpanded>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                        <Typography variant="title" gutterBottom> {`${environment.label} Environment`} </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails className={classes.header}>
                        <Grid container>
                            <Grid item xs={12}>
                                {
                                    (!apis) ? <Loading/> :
                                        (apis.length === 0) ?
                                            <Grid container justify={'center'} alignItems={'center'}>
                                                <Grid item>
                                                    <Typography variant="display1" gutterBottom
                                                                className={classes.messageLabel}>
                                                        No APIs found...
                                                    </Typography>
                                                </Grid>
                                            </Grid>
                                            :
                                            <Grid container>
                                                {this.state.apis.map((api, i) => {
                                                    return <ApiThumb key={api.id} listType={this.state.listType}
                                                                     api={api}
                                                                     environmentName={environment.label}
                                                                     rootAPI={rootAPI}
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

export default withStyles(styles)(EnvironmentPanel);
