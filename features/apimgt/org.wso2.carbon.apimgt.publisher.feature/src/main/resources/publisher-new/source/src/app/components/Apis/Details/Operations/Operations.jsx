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
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Divider from '@material-ui/core/Divider';
import List from '@material-ui/core/List';
import ScopesIcon from '@material-ui/icons/VpnKey';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
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
        this.state = {
            swagger: {},
            scopes: [],
            notFound: false,
            showScopes: false,
            apiPolicies: [],
            operationList: null,
        };

        this.newApi = new Api();
        this.handleUpdateList = this.handleUpdateList.bind(this);
        this.toggleAssignScopes = this.toggleAssignScopes.bind(this);
        this.handleScopeChange = this.handleScopeChange.bind(this);
        this.updateOperations = this.updateOperations.bind(this);
        this.handleScopeChangeInSwaggerRoot = this.handleScopeChangeInSwaggerRoot.bind(this);
    }

    /**
     *
     */
    componentDidMount() {
        this.setState({
            operationList: this.props.api.operations,
            scopes: this.props.api.scopes,
        });
        const promisedApi = this.newApi.getSwagger(this.props.api.id);
        promisedApi
            .then((response) => {
                let tempScopes = [];
                if (response.obj.security && response.obj.security.length !== 0) {
                    tempScopes = response.obj.security.filter(object => object.OAuth2Security);
                }
                this.setState({ swagger: response.obj, scopes: tempScopes });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
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
     * @param {*} event triggered for scope change
     */
    handleScopeChange(event) {
        this.setState({ scopes: event.target.value });
        this.handleScopeChangeInSwaggerRoot(event.target.value);
    }

    /**
     *
     * @param {*} scopes
     */
    handleScopeChangeInSwaggerRoot(scopes) {
        const { swagger } = this.state;
        if (swagger.security) {
            swagger.security.map((object) => {
                if (object.OAuth2Security) {
                    object.OAuth2Security = scopes;
                }
                return object.OAuth2Security;
            });
        } else {
            swagger.security = [{ OAuth2Security: scopes }];
        }
        this.setState({ swagger });
    }

    /**
     *
     * @param {*} newOperation
     */
    handleUpdateList(newOperation) {
        const { operationList } = this.state;
        const updatedList = operationList.map(operation => (operation.target === newOperation.target
            ? newOperation : operation));
        this.setState({ operationList: updatedList });
    }

    /**
     *
     */
    updateOperations() {
        const { api } = this.props;
        const { operationList, intl } = this.state;
        api.operations = operationList;
        /* eslint no-underscore-dangle: ["error", { "allow": ["_data"] }] */
        /* eslint no-param-reassign: ["error", { "props": false }] */
        if (api._data) delete api._data;
        if (api.client) delete api.client;
        if (api.apiType) delete api.apiType;
        const promisedApi = this.newApi.update(JSON.parse(JSON.stringify(api)));
        promisedApi
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Operations.Operations.api.updated.successfully',
                    defaultMessage: 'API updated successfully!',
                }));
            })
            .catch((error) => {
                console.error(error);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Operations.Operations.something.went.wrong.while.updating.the.api',
                    defaultMessage: 'Error occurred while updating API',
                }));
            });
    }
    toggleAssignScopes = () => {
        // this.setState({ showScopes: !this.state.showScopes });
        this.setState(prevState => ({ showScopes: !prevState.showScopes }));
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            operationList, scopes, showScopes, apiPolicies,
        } = this.state;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!operationList) {
            return <Progress />;
        }
        const { classes } = this.props;
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.Operations.Operations.operation'
                            defaultMessage='Operations'
                        />
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.toggleAssignScopes}>
                        <ScopesIcon className={classes.buttonIcon} />
                        <FormattedMessage
                            id='Apis.Details.Resources.Resources.assign.global.scope.for.api'
                            defaultMessage='Assign Global Scope for API'
                        />
                    </Button>
                </div>
                <div className={classes.contentWrapper}>
                    {(this.state.scopes && showScopes) &&
                        <React.Fragment>
                            <div className={classes.addNewWrapper}>
                                <Typography className={classes.addNewHeader}>
                                    <FormattedMessage
                                        id='Apis.Details.Resources.Resources.assign.global.scopes.for.api.title'
                                        defaultMessage='Assign Global Scopes for API'
                                    />
                                </Typography>
                                <Divider className={classes.divider} />
                                <div className={classes.addNewOther}>
                                    <FormControl className={classes.formControl}>
                                        <InputLabel htmlFor='select-multiple'><FormattedMessage
                                            id='Apis.Details.Resources.Resources.assign.global.scopes.for.api.input'
                                            defaultMessage='Assign Global Scopes for API'
                                        />
                                        </InputLabel>
                                        <Select
                                            multiple
                                            value={this.state.scopes}
                                            onChange={this.handleScopeChange}
                                            className={classes.scopes}
                                        >
                                            {scopes.list.map(tempScope => (
                                                <MenuItem
                                                    key={tempScope.name}
                                                    value={tempScope.name}
                                                    style={{
                                                        fontWeight: this.state.scopes.indexOf(tempScope.name) !== -1
                                                            ? '500' : '400',
                                                        width: 400,
                                                    }}
                                                >
                                                    {tempScope.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </div>
                                <Divider className={classes.divider} />
                                <div className={classes.addNewOther}>
                                    <Button variant='contained' color='primary' onClick={this.handleScopeChange}>
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resources.assign.global.scopes.for.api.button'
                                            defaultMessage='Assign global scopes for API'
                                        />
                                    </Button>
                                    <Button className={classes.button} onClick={this.toggleAssignScopes}>
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resources.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </div>
                            </div>
                        </React.Fragment>
                    }
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
                                {operationList.map((item) => {
                                    return (
                                        <Operation
                                            operation={item}
                                            handleUpdateList={this.handleUpdateList}
                                            scopes={this.props.api.scopes}
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
                            className={classes.buttonMain}
                            onClick={this.updateOperations}
                        >
                            <FormattedMessage id='Apis.Details.Resources.Resources.save' defaultMessage='Save' />
                        </Button>
                    </div>
                </div>
            </div>
        );
    }
}

Operations.propTypes = {
    classes: PropTypes.shape({
    }).isRequired,
    api: PropTypes.shape({
        operations: PropTypes.object,
        scopes: PropTypes.object,
        updateOperations: PropTypes.func,
        getSwagger: PropTypes.func,
        policies: PropTypes.func,
        id: PropTypes.string,
    }).isRequired,
    resourceNotFountMessage: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(Operations));
