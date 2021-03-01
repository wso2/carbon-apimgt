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
import { Link } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
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

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
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
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonMain: {
        color: theme.palette.getContrastText(theme.palette.primary.main),
        marginRight: theme.spacing(1),
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
    addNewHeader: {
        padding: theme.spacing(2),
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing(2),
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
        marginBottom: theme.spacing(1),
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
        const { operations } = api;
        const operationCopy = [...operations];
        operationCopy.sort((a, b) => ((a.target + a.verb > b.target + b.verb) ? 1 : -1));
        this.state = {
            notFound: false,
            apiPolicies: [],
            operations: operationCopy,
            apiThrottlingPolicy: api.apiThrottlingPolicy,
            filterKeyWord: '',
            isSaving: false,
            sharedScopes: [],
            apiScopesByName: {},
            sharedScopesByName: {},
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
        const { api } = this.props;
        const apiScopesByNameList = {};
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

        for (const scopeObject of api.scopes) {
            const modifiedScope = {};
            modifiedScope.scope = scopeObject.scope;
            modifiedScope.shared = scopeObject.shared;
            apiScopesByNameList[scopeObject.scope.name] = modifiedScope;
        }
        this.setState({ apiScopesByName: apiScopesByNameList });
        this.getAllSharedScopes();
    }

    /**
     *
     * @param {*} event
     */
    setFilterByKeyWord(event) {
        this.setState({ filterKeyWord: event.target.value.toLowerCase() });
    }

    /**
     * @memberof Operations
     */
    getAllSharedScopes() {
        Api.getAllScopes()
            .then((response) => {
                if (response.body && response.body.list) {
                    const sharedScopesList = [];
                    const sharedScopesByNameList = {};
                    const shared = true;
                    for (const scope of response.body.list) {
                        const modifiedScope = {};
                        modifiedScope.scope = scope;
                        modifiedScope.shared = shared;
                        sharedScopesList.push(modifiedScope);
                        sharedScopesByNameList[scope.name] = modifiedScope;
                    }
                    this.setState({ sharedScopesByName: sharedScopesByNameList });
                    this.setState({ sharedScopes: sharedScopesList });
                }
            });
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
     * @param {*} newOperation
     */
    handleUpdateList(newOperation) {
        const { operations, sharedScopesByName, apiScopesByName } = this.state;
        const updatedList = operations.map(
            (operation) => ((operation.target === newOperation.target && operation.verb === newOperation.verb)
                ? newOperation : operation),
        );

        for (const selectedScope of newOperation.scopes) {
            if (selectedScope
                && !apiScopesByName[selectedScope]
                && apiScopesByName[selectedScope] !== '') {
                if (selectedScope in sharedScopesByName) {
                    apiScopesByName[selectedScope] = sharedScopesByName[selectedScope];
                }
            }
        }
        this.setState({ apiScopesByName });
        this.setState({ operations: updatedList });
    }

    /**
     *
     */
    updateOperations() {
        const { operations, apiThrottlingPolicy, apiScopesByName } = this.state;
        const { updateAPI } = this.props;
        this.setState({ isSaving: true });
        this.updateApiScopes(operations);
        const scopes = Object.keys(apiScopesByName).map((key) => { return apiScopesByName[key]; });
        updateAPI({ operations, apiThrottlingPolicy, scopes }).finally(() => this.setState({ isSaving: false }));
    }

    /**
     *
     * This method modifies the security definition scopes by removing the scopes which are not present
     * in operations and which are shared scopes
     * @param {Array} apiOperations Operations list
     */
    updateApiScopes(apiOperations) {
        const { apiScopesByName, sharedScopesByName } = this.state;
        Object.keys(apiScopesByName).forEach((key) => {
            let isScopeExistsInOperation = false;
            for (const operation of apiOperations) {
                // Checking if the scope resides in the operation
                if (operation.scopes.includes(key)) {
                    isScopeExistsInOperation = true;
                    break;
                }
            }
            // Checking if the scope exists in operation and is a shared scope
            if (!isScopeExistsInOperation && (key in sharedScopesByName)) {
                delete apiScopesByName[key];
            }
        });
        this.setState({ apiScopesByName });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { api, resourceNotFoundMessage } = this.props;
        const {
            operations, apiPolicies, apiThrottlingPolicy, isSaving, filterKeyWord, notFound, sharedScopes,
        } = this.state;
        if (notFound) {
            return <ResourceNotFound message={resourceNotFoundMessage} />;
        }
        if (!operations && apiPolicies.length === 0) {
            return <Progress />;
        }
        const { classes } = this.props;
        return (
            <>
                <Box pb={3}>
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Details.Operations.Operations.title'
                            defaultMessage='Operations'
                        />
                    </Typography>
                </Box>

                <Grid container spacing={2}>
                    <Grid item md={12}>
                        <APIRateLimiting
                            operationRateLimits={apiPolicies}
                            api={api}
                            value={apiThrottlingPolicy}
                            onChange={this.handleApiThrottlePolicy}
                        />
                    </Grid>
                    <Grid item md={2}>
                        <Box mt={4} pb={2}>
                            <div className={classes.searchWrapper}>
                                <TextField
                                    id='outlined-full-width'
                                    label='Operation'
                                    placeholder='Filter Operations'
                                    onChange={(e) => this.setFilterByKeyWord(e, api.operations)}
                                    fullWidth
                                    variant='outlined'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                />
                            </div>
                        </Box>
                    </Grid>
                    <Grid item md={12}>
                        <Table>
                            <TableRow>
                                <TableCell>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Operations.operation.operationName'
                                            defaultMessage='Operation'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Operations.Operation.OperationType'
                                            defaultMessage='Operation Type'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Operations.Operation.throttling.policy'
                                            defaultMessage='Rate Limiting'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Operations.Operation.scopes'
                                            defaultMessage='Scope'
                                        />
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant='subtitle2'>
                                        <FormattedMessage
                                            id='Apis.Details.Operations.Operation.authType'
                                            defaultMessage='Security Enabled'
                                        />
                                    </Typography>
                                </TableCell>
                            </TableRow>
                            {operations.filter(
                                (operation) => operation.target.toLowerCase().includes(filterKeyWord),
                            ).map((item) => {
                                return (
                                    <Operation
                                        operation={item}
                                        handleUpdateList={this.handleUpdateList}
                                        scopes={api.scopes}
                                        sharedScopes={sharedScopes}
                                        isOperationRateLimiting={!apiThrottlingPolicy}
                                        apiPolicies={apiPolicies}
                                    />
                                );
                            })}
                        </Table>
                    </Grid>
                    <Grid item>
                        <Button
                            variant='contained'
                            color='primary'
                            disabled={isSaving || api.isRevision}
                            className={classes.buttonMain}
                            onClick={this.updateOperations}
                        >
                            {isSaving && <CircularProgress size={20} />}
                            <FormattedMessage
                                id='Apis.Details.Operations.Operation.save'
                                defaultMessage='Save'
                            />
                        </Button>
                        <Link to={'/apis/' + api.id + '/overview'}>
                            <Button>
                                <FormattedMessage
                                    id='Apis.Details.Operations.Operation.cancel'
                                    defaultMessage='Cancel'
                                />
                            </Button>
                        </Link>
                    </Grid>
                </Grid>
            </>
        );
    }
}

Operations.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        operations: PropTypes.arrayOf(PropTypes.shape({})),
        scopes: PropTypes.arrayOf(PropTypes.string),
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
