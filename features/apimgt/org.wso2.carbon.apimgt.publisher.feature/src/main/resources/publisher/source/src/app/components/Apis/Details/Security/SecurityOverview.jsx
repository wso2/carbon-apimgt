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

import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import PropTypes from 'prop-types';

import Typography from '@material-ui/core/Typography';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableHead from '@material-ui/core/TableHead';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Button from '@material-ui/core/Button';
import { withStyles } from '@material-ui/core/styles';
import AddCircle from '@material-ui/icons/AddCircle';
import Divider from '@material-ui/core/Divider';
import { FormattedMessage, injectIntl } from 'react-intl';

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
        marginLeft: theme.spacing(2),
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
        padding: theme.spacing(2),
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
        marginTop: theme.spacing(2),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addJsonContent: {
        whiteSpace: 'pre',
    },
});

class SecurityOverview extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {
            api: {
                name: '',
            },
            policies: [],
            showAddPolicy: false,
        };
        this.updateData = this.updateData.bind(this);
        this.toggleShowAddPolicy = this.toggleShowAddPolicy.bind(this);
        this.updateData = this.updateData.bind(this);
    }

    componentDidMount() {
        this.updateData();
    }

    updateData() {
        const promisedApi = this.api.get(this.props.match.params.api_uuid);
        promisedApi.then((response) => {
            this.setState({ api: response.obj });
            this.updatePolicyData();
        });
    }

    updatePolicyData() {
        this.setState({ policies: [] });
        const policyIds = this.state.api.threatProtectionPolicies.list;
        for (let i = 0; i < policyIds.length; i++) {
            const id = policyIds[i].policyId;
            const promisedPolicies = this.api.getThreatProtectionPolicy(id);
            promisedPolicies.then((response) => {
                const updatedPolicies = this.state.policies;
                updatedPolicies.push(response.obj);
                this.setState({ policies: updatedPolicies });
            });
        }
    }

    deletePolicy(id) {
        const { intl } = this.props;
        const associatedApi = this.state.api;
        const promisedPolicyDelete = this.api.deleteThreatProtectionPolicyFromApi(associatedApi.id, id);
        promisedPolicyDelete.then((response) => {
            if (response.status === 200) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Security.SecurityOverview.policy.remove.success',
                    defaultMessage: 'Policy removed successfully.',
                }));

                //   remove policy from local api
                const index = associatedApi.threatProtectionPolicies.list.indexOf({ policyId: id });
                associatedApi.threatProtectionPolicies.list.splice(index, 1);
                this.setState({ api: associatedApi });
                this.updatePolicyData();
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Security.SecurityOverview.policy.remove.failure',
                    defaultMessage: 'Failed to remove policy.',
                }));
            }
        });
    }

    toggleShowAddPolicy = () => {
        this.setState({ showAddPolicy: !this.state.showAddPolicy });
    }

    formatPolicy = (policy) => {
        let formattedPolicy = policy;
        formattedPolicy = formattedPolicy.replace(':', ' : ');
        formattedPolicy = formattedPolicy.split(',').join(',\n');
        return formattedPolicy;
    }

    render() {
        let data = [];
        if (this.state.policies) {
            data = this.state.policies;
        }
        const { classes } = this.props;
        const { showAddPolicy } = this.state;

        return (
            <div className={classes.root}>
                <div className={classes.contentWrapper}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.Security.SecurityOverview.threat.protection.policies'
                                defaultMessage='Threat Protection Policies'
                            />
                        </Typography>
                        <Button size='small' className={classes.button} onClick={this.toggleShowAddPolicy}>
                            <AddCircle className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Apis.Details.Security.SecurityOverview.add.threat.protection.policy'
                                defaultMessage='Add New Threat Protection Policy'
                            />
                        </Button>
                    </div>
                </div>
                <div className={classes.contentWrapper}>
                    {showAddPolicy &&
                    <AddPolicy
                        id={this.state.api.id}
                        toggleShowAddPolicy={this.toggleShowAddPolicy}
                        updateData={this.updateData}
                    />
                    }
                </div>
                <br />
                <div className={classes.contentWrapper}>
                    <div className={classes.addNewWrapper}>
                        <Typography className={classes.addNewHeader}>
                            <FormattedMessage
                                id='Apis.Details.Security.SecurityOverview.manage.threat.protection.policies'
                                defaultMessage='Manage Threat Protection Policies'
                            />
                        </Typography>
                        <Divider className={classes.divider} />
                        <Table className={classes.table}>
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Apis.Details.Security.SecurityOverview.policy.name'
                                            defaultMessage='Policy Name'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Apis.Details.Security.SecurityOverview.policy.type'
                                            defaultMessage='Policy Type'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Apis.Details.Security.SecurityOverview.policy'
                                            defaultMessage='Policy'
                                        />
                                    </TableCell>
                                    <TableCell />
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {data.map((n) => {
                                    return (
                                        <TableRow key={n.uuid}>
                                            <TableCell>{n.name + (n.uuid === 'GLOBAL-JSON' ? ' (GLOBAL)' : '')}</TableCell>
                                            <TableCell>{n.type}</TableCell>
                                            <TableCell>
                                                <div className={classes.addJsonContent}>
                                                    {this.formatPolicy(n.policy)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <span>
                                                    <Button color='accent' onClick={() => this.deletePolicy(n.uuid)} >
                                                        <FormattedMessage
                                                            id='Apis.Details.Security.SecurityOverview.delete'
                                                            defaultMessage='Delete'
                                                        />
                                                    </Button>
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

SecurityOverview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            api_uuid: PropTypes.string,
        }),
    }).isRequired,
    intl: PropTypes.shape({}).isRequired,
};


export default injectIntl(withStyles(styles)(SecurityOverview));
