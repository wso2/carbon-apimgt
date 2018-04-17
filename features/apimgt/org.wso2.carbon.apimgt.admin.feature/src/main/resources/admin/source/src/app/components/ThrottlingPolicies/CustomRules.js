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
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Table, {TableBody, TableCell, TableHead, TableRow} from 'material-ui/Table';
import Divider from 'material-ui/Divider';
import Button from 'material-ui/Button';
import {withStyles} from 'material-ui/styles';

import API from '../../data/api'
import Alert from '../Shared/Alert'

const messages = {
    success: 'Deleted custom rule successfully',
    failure: 'Error while deleting custom rule',
    retrieveError: 'Error while retrieving custom rules'
};

const styles = theme => ({
    divider: {
        marginBottom: 20,
    },
    createButton: {
        textDecoration: 'none',
        display: 'inline-block',
        marginLeft: 20,
        alignSelf: 'flex-start',
    },
    titleWrapper: {
        display: 'flex',
    }
});

class CustomRules extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
            selectedRowKeys: [],
            open: false,
            message: ''
        };
        this.deleteCustomRulePolicy = this.deleteCustomRulePolicy.bind(this);

    }

    deleteCustomRulePolicy(event) {
        const api = new API();
        const { id } = event.currentTarget;
        const promisedPolicies = api.deleteCustomRulePolicy(id);
        promisedPolicies.then(
            response => {
                Alert.info(messages.success);
                var data = this.state.policies.filter(obj => {
                    return obj.id !== id;
                });
                this.setState({ policies: data });
            }
        ).catch(
            error => {
                Alert.error(messages.failure);
                console.error(error);
            }
        );
    }

    componentDidMount() {
        const api = new API();

        const promised_policies = api.getCustomRulePolicies();
        promised_policies.then(
            response => {
                this.setState({ policies: response.obj.list });
            }
        ).catch(
            error => {
                Alert.error(messages.retrieveError);
                console.error(error);
            }
        );
    }

    render() {
        /*TODO implement search and pagination*/
        const tiers = this.state.policies;
        const { classes } = this.props;
        let data = [];
        if (tiers) {
            data = tiers;
        }

        return (
            <div>
                <Grid container justify="center" alignItems="center">
                    <Grid item xs={12}>

                        <div className={classes.titleWrapper}>
                            <Typography variant="display1" gutterBottom >
                                Custom Rules
                            </Typography>
                            <Link to={"/policies/custom_rules/create"} className={classes.createButton}>
                                <Button variant="raised" color="primary" className={classes.button}>
                                    Add Custom Rule
                                </Button>
                            </Link>
                        </div>
                        <Divider className={classes.divider} />

                    </Grid>
                    <Grid item xs={12} className="page-content">
                        <Paper>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Name</TableCell>
                                        <TableCell>Description</TableCell>
                                        <TableCell>Key Template</TableCell>

                                        <TableCell></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {data.map(n => {
                                        return (
                                            <TableRow key={n.id}>
                                                <TableCell>{n.policyName}</TableCell>
                                                <TableCell>{n.description}</TableCell>
                                                <TableCell>{n.keyTemplate}</TableCell>
                                                <TableCell>
                                                    <span>
                                                        <Link to={"/policies/custom_rules/" + n.id}>
                                                            <Button color="primary">Edit</Button>
                                                        </Link>
                                                        <Button id={n.id} color="default"
                                                            onClick={this.deleteCustomRulePolicy} >Delete</Button>
                                                    </span>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}
export default withStyles(styles)(CustomRules);
