/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import List from '@material-ui/core/List';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

import APIRateLimiting from '../Resources/components/APIRateLimiting';
import Operation from './Operation';


const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        width: 400,
    },
    mainTitle: {
        paddingLeft: 0,
    },
    scopes: {
        width: 400,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonMain: {
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addNewHeader: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        width: 300,
    },
    addResource: {
        width: 600,
        marginTop: 0,
    },
    buttonIcon: {
        marginRight: 10,
    },
    expansionPanel: {
        marginBottom: theme.spacing.unit,
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
});

/**
 * This class defined for operation List
 */
class Operations extends React.Component {
    /**
     *
     * @param {*} props the props parameters
     */
    constructor(props) {
        super(props);
        const { api } = props;
        this.state = {
            notFound: false,
            apiPolicies: [],
            operations: api.operations,
            apiThrottlingPolicy: api.apiThrottlingPolicy,
            isSaving: false,
        };

        this.newApi = new Api();
        this.handleUpdateList = this.handleUpdateList.bind(this);
        this.handleApiThrottlePolicy = this.handleApiThrottlePolicy.bind(this);
        this.updateOperations = this.updateOperations.bind(this);
    }

    /**
     *
     */
    componentDidMount() {
        const promisedResPolicies = Api.policies('api');
        promisedResPolicies
            .then((policies) => {
                this.setState({ apiPolicies: policies.obj.list });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }

    /**
     *
     * @param {*} newOperation
     */
    handleUpdateList(newOperation) {
        const { operations } = this.state;
        const updatedList = operations.map(operation =>
            (operation.target === newOperation.target ? newOperation : operation));
        this.setState({ operations: updatedList });
    }

    /**
     *
     *
     * @param {*} throttlePolicy
     * @memberof Operations
     */
    handleApiThrottlePolicy(apiThrottlingPolicy) {
        this.setState({ apiThrottlingPolicy });
    }
    /**
     *
     */
    updateOperations() {
        const { operations, apiThrottlingPolicy } = this.state;
        const { updateAPI } = this.props;
        this.setState({ isSaving: true });
        updateAPI({ operations, apiThrottlingPolicy }).finally(() => this.setState({ isSaving: false }));
    }

    /**
     * @inheritdoc
     */
    render() {
        const { api } = this.props;
        const {
            operations, apiPolicies, apiThrottlingPolicy, isSaving,
        } = this.state;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFoundMessage} />;
        }
        if (!operations && apiPolicies.length === 0) {
            return <Progress />;
        }
        const { classes } = this.props;
        return (
            <Grid container direction='row' spacing={3} justify='flex-start' alignItems='flex-start'>
                <Grid item md={12}>
                    <APIRateLimiting
                        operationRateLimits={apiPolicies}
                        api={api}
                        value={apiThrottlingPolicy}
                        onChange={this.handleApiThrottlePolicy}
                    />
                </Grid>
                <div className={classes.root}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.Operations.Operations.operation'
                                defaultMessage='Operations'
                            />
                        </Typography>
                    </div>
                    <div className={classes.contentWrapper}>
                        <List>
                            <Grid>
                                <Table>
                                    <TableRow>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.Operation'
                                                    defaultMessage='Operation'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.OperationType'
                                                    defaultMessage='Operation Type'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.throttling.policy'
                                                    defaultMessage='Throttling Policy'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.scopes'
                                                    defaultMessage='Scopes'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.authType'
                                                    defaultMessage='Security Enabled'
                                                />
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                    {operations.map((item) => {
                                        return (
                                            <Operation
                                                operation={item}
                                                handleUpdateList={this.handleUpdateList}
                                                scopes={this.props.api.scopes}
                                                isOperationRateLimiting={!apiThrottlingPolicy}
                                                apiPolicies={apiPolicies}
                                            />
                                        );
                                    })}
                                </Table>
                            </Grid>
                        </List>
                        <div>
                            <Button
                                variant='contained'
                                color='primary'
                                disabled={isSaving}
                                className={classes.buttonMain}
                                onClick={this.updateOperations}
                            >
                                {isSaving && <CircularProgress size={20} />}
                                <FormattedMessage id='Apis.Details.Resources.Resources.save' defaultMessage='Save' />
                            </Button>
                        </div>
                    </div>
                </div>
            </Grid>
        );
    }
}

Operations.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        operations: PropTypes.array,
        scopes: PropTypes.array,
        updateOperations: PropTypes.func,
        policies: PropTypes.func,
        id: PropTypes.string,
    }).isRequired,
    resourceNotFoundMessage: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(Operations));
