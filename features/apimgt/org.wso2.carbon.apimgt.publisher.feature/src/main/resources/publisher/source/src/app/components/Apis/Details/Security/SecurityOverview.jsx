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
import {Link} from 'react-router-dom'

import Api from 'AppData/api'
import Alert from 'AppComponents/Shared/Alert'

import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableHead from '@material-ui/core/TableHead';
import TableCell from '@material-ui/core/TableCell';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Button from '@material-ui/core/Button';
import { withStyles } from '@material-ui/core/styles';
import AddCircle from '@material-ui/icons/AddCircle';
import Divider from '@material-ui/core/Divider';

import AddPolicy from './AddPolicy';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        marginLeft: theme.spacing.unit*2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonIcon: {
        marginRight: 10,
    },
    table: {
        '& td': {
            fontSize: theme.typography.fontSize,
        },
        '& th': {
            fontSize: theme.typography.fontSize * 1.2,
        },
        tableLayout: 'fixed',
    },
    addNewHeader: {
        padding: theme.spacing.unit*2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit*2,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addJsonContent: {
        whiteSpace: 'pre',
    }
});

class SecurityOverview extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {
            api: {
                name: ''
            },
            policies: [],
            showAddPolicy: false
        };
        this.updateData = this.updateData.bind(this);
        this.toggleShowAddPolicy = this.toggleShowAddPolicy.bind(this);
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.updateData();
    }

    updateData() {
        let promised_api = this.api.get(this.props.id);
        promised_api.then(response => {
            this.setState({api: response.obj});
            this.updatePolicyData();
        });
    }

    updatePolicyData() {
        this.setState({policies: []})
        let policyIds = this.state.api.threatProtectionPolicies.list;
        for (var i=0; i<policyIds.length; i++) {
            let id = policyIds[i].policyId;
            let promisedPolicies = this.api.getThreatProtectionPolicy(id);
            promisedPolicies.then(response => {
                let policies = this.state.policies;
                policies.push(response.obj);
                this.setState({policies: policies});
            });
        }
    }

    deletePolicy(id) {
        let associatedApi = this.state.api;
        let promisedPolicyDelete = this.api.deleteThreatProtectionPolicyFromApi(associatedApi.id, id);
        promisedPolicyDelete.then(response => {
           if (response.status === 200) {
               Alert.info("Policy removed successfully.");

               //remove policy from local api
               let index = associatedApi.threatProtectionPolicies.list.indexOf({policyId: id});
               associatedApi.threatProtectionPolicies.list.splice(index, 1);
               this.setState({api: associatedApi});
               this.updatePolicyData();
           } else {
               Alert.error("Failed to remove policy.");
           }
        });
    }

    toggleShowAddPolicy = () => {
        this.setState({showAddPolicy: !this.state.showAddPolicy});
    }

    formatPolicy = (policy) => {
        policy = policy.replace(":", " : ");
        policy = policy.split(',').join(",\n");
        return policy;
    }

    render() {
        let data = [];
        if (this.state.policies) {
            data = this.state.policies;
        }
        const {classes} = this.props;
        const {showAddPolicy} = this.state;

        return (
            <div className={classes.root}>
                <div className={classes.contentWrapper}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            Threat Protection Policies
                        </Typography>
                        <Button size="small" className={classes.button} onClick={this.toggleShowAddPolicy}>
                            <AddCircle className={classes.buttonIcon}/>
                            Add New Threat Protection Policy
                        </Button>
                    </div>
                </div>
                <div className={classes.contentWrapper}>
                {showAddPolicy &&
                    <AddPolicy
                        id={this.state.api.id}
                        toggleShowAddPolicy={this.toggleShowAddPolicy}
                        updateData={this.updateData}/>
                }
                </div>
                <br/>
                <div className={classes.contentWrapper}>
                    <div className={classes.addNewWrapper}>
                        <Typography className={classes.addNewHeader}>
                            Manage Threat Protection Policies
                        </Typography>
                        <Divider className={classes.divider} />
                        <Table className={classes.table}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Policy Name</TableCell>
                                    <TableCell>Policy Type</TableCell>
                                    <TableCell>Policy</TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map(n => {
                                    return (
                                        <TableRow key={n.uuid}>
                                            <TableCell>{n.name + (n.uuid=="GLOBAL-JSON"? " (GLOBAL)": "")}</TableCell>
                                            <TableCell>{n.type}</TableCell>
                                            <TableCell>
                                                <div className={classes.addJsonContent}>
                                                    {this.formatPolicy(n.policy)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <span>
                                                    <Button color="accent"
                                                            onClick={() => this.deletePolicy(n.uuid)} >Delete</Button>
                                                </span>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </div>
                </div>
            </div>
        );
    }
}

export default withStyles(styles)(SecurityOverview)