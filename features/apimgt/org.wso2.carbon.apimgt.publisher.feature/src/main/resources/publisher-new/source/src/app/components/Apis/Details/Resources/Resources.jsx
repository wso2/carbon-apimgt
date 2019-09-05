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

import React from 'react';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import TextField from '@material-ui/core/TextField';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import Checkbox from '@material-ui/core/Checkbox';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Divider from '@material-ui/core/Divider';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import AddCircle from '@material-ui/icons/AddCircle';
import ScopesIcon from '@material-ui/icons/VpnKey';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import cloneDeep from 'lodash.clonedeep';
import isEmpty from 'lodash/isEmpty';
import Api from 'AppData/api';
import CONSTS from 'AppData/Constants';
import { Progress } from 'AppComponents/Shared';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import { Radio, RadioGroup } from '@material-ui/core';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import Resource from './Resource';
import AuthManager from 'AppData/AuthManager';
import Grid from "@material-ui/core/Grid";

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
    imageContainer: {
        display: 'flex',
    },
    formControl: {
        margin: theme.unit * 3,
    },
    group: {
        margin: theme.unit * 3,
    },
    selectWidth: {
        margin: theme.spacing.unit * 1,
        minWidth: 120,
    },
});

/**
 *  Classed used for resoruces
 */
class Resources extends React.Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            tmpMethods: [],
            tmpResourceName: '',
            paths: {},
            swagger: {},
            scopes: [],
            pathDeleteList: [],
            allChecked: false,
            notFound: false,
            showAddResource: false,
            showPolicy: false,
            apiPolicies: [],
            selectedPolicy: props.api.apiThrottlingPolicy,
            policyLevel: props.api.apiThrottlingPolicy ? 'perAPI' : 'perResource',
        };
        this.api = new Api();
        this.api_uuid = props.api.id;
        this.addResources = this.addResources.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onChangeInput = this.onChangeInput.bind(this);
        this.updatePath = this.updatePath.bind(this);
        this.addRemoveToDeleteList = this.addRemoveToDeleteList.bind(this);
        this.updateResources = this.updateResources.bind(this);
        this.handleScopeChange = this.handleScopeChange.bind(this);
        this.handleCheckAll = this.handleCheckAll.bind(this);
        this.deleteSelected = this.deleteSelected.bind(this);
        this.advancedPolicyTypeChange = this.advancedPolicyTypeChange.bind(this);
        this.handlePolicyChange = this.handlePolicyChange.bind(this);
        this.updateAPI = this.updateAPI.bind(this);
        this.childResources = [];
        this.isNotCreator = AuthManager.isNotCreator();
    }
    handleChange = name => (event) => {
        const tmpMethods = this.state.tmpMethods;
        const index = tmpMethods.indexOf(name);

        if (event.target.checked) {
            // add to tmpMethods
            if (index === -1) {
                tmpMethods.push(name);
            }
        } else {
            // remove from tmpMethods if exists
            if (index > -1) {
                tmpMethods.splice(index, 1);
            }
        }
        this.setState({ tmpMethods });
    };
    onChange(checkedValues) {
        this.setState({ tmpMethods: checkedValues });
    }
    handleScopeChange(e) {
        this.setState({ scopes: e.target.value });
        this.handleScopeChangeInSwaggerRoot(e.target.value);
    }
    handleScopeChangeInSwaggerRoot(scopes) {
        const swagger = this.state.swagger;
        if (swagger.security) {
            swagger.security.map((object, i) => {
                if (object.OAuth2Security) {
                    object.OAuth2Security = scopes;
                }
            });
        } else {
            swagger.security = [{ OAuth2Security: scopes }];
        }
        this.setState({ swagger });
    }

    componentDidMount() {
        const api = new Api();
        const promised_api_object = Api.get(this.api_uuid);
        promised_api_object
            .then((api) => {
                this.setState({ api, scopes: api.scopes });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
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
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });

        const promised_api = this.api.getSwagger(this.api_uuid);
        promised_api
            .then((response) => {
                let tempScopes = [];
                if (response.obj.security && response.obj.security.length !== 0) {
                    response.obj.security.map((object, i) => {
                        if (object.OAuth2Security) {
                            tempScopes = object.OAuth2Security;
                        }
                    });
                }
                this.setState({ swagger: response.obj, scopes: tempScopes });

                if (response.obj.paths !== undefined) {
                    this.setState({ paths: response.obj.paths });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }
    onChangeInput = name => (event) => {
        let value = event.target.value;
        if (value.indexOf('/') === -1) {
            value = '/' + value;
        }
        this.setState({ [name]: value });
    };
    addResources() {
        const defaultGet = {
            description: '',
            parameters: [],
            'x-auth-type': 'Application & Application User',
            'x-throttling-tier': 'Unlimited', // todo: handle when Unlimited tier is disabled
            responses: {
                200: {
                    description: '',
                },
            },
        };

        const defaultPost = {
            description: '',
            'x-auth-type': 'Application & Application User',
            'x-throttling-tier': 'Unlimited', // todo: handle when Unlimited tier is disabled
            responses: {
                200: {
                    description: '',
                },
            },
            parameters: [
                {
                    name: 'Payload',
                    description: 'Request Body',
                    required: false,
                    in: 'body',
                    schema: {
                        type: 'object',
                        properties: {
                            payload: {
                                type: 'string',
                            },
                        },
                    },
                },
            ],
        };

        const defaultDelete = {
            description: '',
            'x-auth-type': 'Application & Application User',
            'x-throttling-tier': 'Unlimited', // todo: handle when Unlimited tier is disabled
            responses: {
                200: {
                    description: '',
                },
            },
            parameters: [],
        };
        const defaultHead = {
            'x-auth-type': 'Application & Application User',
            'x-throttling-tier': 'Unlimited', // todo: handle when Unlimited tier is disabled
            responses: {
                200: {
                    description: '',
                },
            },
            parameters: [],
        };
        const pathValue = {};
        let existingPathVale = {};
        const tmpPaths = this.state.paths;
        if (Object.keys(tmpPaths).length > 0) {
            if (this.state.tmpResourceName in tmpPaths) {
                existingPathVale = tmpPaths[this.state.tmpResourceName];
            }
        }
        CONSTS.HTTP_METHODS.map((method) => {
            switch (method) {
                case 'get':
                    if ('get' in existingPathVale) {
                        pathValue.get = existingPathVale.get;
                    } else if (this.state.tmpMethods.indexOf('get') !== -1) {
                        pathValue.get = defaultGet;
                    }
                    break;
                case 'post':
                    if ('post' in existingPathVale) {
                        pathValue.post = existingPathVale.post;
                    }
                    if (this.state.tmpMethods.indexOf('post') !== -1) {
                        pathValue.post = defaultPost;
                    }
                    break;
                case 'put':
                    if ('put' in existingPathVale) {
                        pathValue.put = existingPathVale.put;
                    }
                    if (this.state.tmpMethods.indexOf('put') !== -1) {
                        pathValue.put = defaultPost;
                    }
                    break;
                case 'patch':
                    if ('patch' in existingPathVale) {
                        pathValue.patch = existingPathVale.patch;
                    }
                    if (this.state.tmpMethods.indexOf('patch') !== -1) {
                        pathValue.patch = defaultPost;
                    }
                    break;
                case 'delete':
                    if ('delete' in existingPathVale) {
                        pathValue.delete = existingPathVale.delete;
                    }
                    if (this.state.tmpMethods.indexOf('delete') !== -1) {
                        pathValue.delete = defaultDelete;
                    }
                    break;
                case 'head':
                    if ('head' in existingPathVale) {
                        pathValue.head = existingPathVale.head;
                    }
                    if (this.state.tmpMethods.indexOf('head') !== -1) {
                        pathValue.head = defaultHead;
                    }
                    break;
            }
        });

        tmpPaths[this.state.tmpResourceName] = pathValue;
        this.setState({ paths: tmpPaths });
    }

    /**
     * Update the resource paths in the state
     * @param {*} path resource path
     * @param {*} method resource method
     * @param {*} value value
     */
    updatePath(path, method, value) {
        const { paths } = this.state;
        const tmpPaths = cloneDeep(paths);
        if (value === null) {
            delete tmpPaths[path][method];
        } else {
            tmpPaths[path][method] = value;
        }
        this.setState({ paths: tmpPaths });
    }

    /**
     *  update the resources in swagger
     */
    updateResources() {
        const { swagger, paths, api } = this.state;
        const { intl } = this.props;
        const tmpSwagger = { ...swagger, paths: cloneDeep(paths) };
        const newPaths = Object.keys(paths).filter(path => !isEmpty(paths[path]))
            .reduce((acc, path) => {
                acc[path] = paths[path];
                return acc;
            }, {});
        tmpSwagger.paths = newPaths;
        const promisedApi = api.updateSwagger(tmpSwagger);
        promisedApi
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Resources.Resources.api.path.updated.successfully',
                    defaultMessage: 'API Resources updated successfully!',
                }));
                this.updateAPI();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }

    /**
     * Updates the api
     */
    updateAPI() {
        const { intl, api, api: { id } } = this.props;
        const { policyLevel, selectedPolicy } = this.state;
        const promisedApi = api.get(id);
        promisedApi
            .then((getResponse) => {
                let apiThrottlingPolicy;
                if (policyLevel === 'perAPI') {
                    apiThrottlingPolicy = selectedPolicy;
                } else {
                    apiThrottlingPolicy = null;
                }
                let apiData = getResponse.body;
                apiData.apiThrottlingPolicy = apiThrottlingPolicy;
                const promisedUpdate = api.update(apiData);
                promisedUpdate
                    .then(() => {
                        Alert.info(intl.formatMessage({
                            id: 'Apis.Details.Resources.Resources.api.updated.successfully',
                            defaultMessage: 'API updated successfully!',
                        }));
                        return this.api.getSwagger(id);
                    })
                    .then((response) => {
                        if (response.obj.paths !== undefined) {
                            this.setState({ paths: response.obj.paths });
                        }
                    })
                    .catch((errorResponse) => {
                        console.error(errorResponse);
                        Alert.error(intl.formatMessage({
                            id: 'Apis.Details.Resources.Resources.something.went.wrong.while.updating.the.api',
                            defaultMessage: 'Error occurred while updating API',
                        }));
                    });
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Resources.Resources.something.went.wrong.while.getting.the.api',
                    defaultMessage: 'Error occurred while retrieving API',
                }));
            });
    }

    addRemoveToDeleteList(path, method) {
        const pathDeleteList = this.state.pathDeleteList;

        const deleteRef = { path, method };
        let itemAlreadyExisted = false;
        for (let i = 0; i < pathDeleteList.length; i++) {
            if (pathDeleteList[i].path === path && pathDeleteList[i].method === method) {
                pathDeleteList.splice(i, 1);
                itemAlreadyExisted = true;
            }
        }

        if (!itemAlreadyExisted) {
            pathDeleteList.push(deleteRef);
        }
        this.setState({ pathDeleteList });
    }
    handleCheckAll = (event) => {
        const paths = this.state.paths;
        const pathDeleteList = [];
        if (event.target.checked) {
            for (let i = 0; i < this.childResources.length; i++) {
                if (this.childResources[i]) {
                    this.childResources[i].toggleDeleteCheck(true);
                }
            }
            // We iterate all the paths and add each method and path to the pathDeleteList Object
            for (const path in paths) {
                if (paths.hasOwnProperty(path)) {
                    if (Object.keys(path) && Object.keys(path).length > 0) {
                        const pathValue = paths[path];
                        for (const method in pathValue) {
                            if (pathValue.hasOwnProperty(method)) {
                                pathDeleteList.push({ path, method });
                            }
                        }
                    } else {
                        console.debug('Error with path object');
                    }
                }
            }
            this.setState({ allChecked: true });
            this.setState({ pathDeleteList });
        } else {
            for (let i = 0; i < this.childResources.length; i++) {
                if (this.childResources[i]) {
                    this.childResources[i].toggleDeleteCheck(false);
                }
            }
            this.setState({ allChecked: false });
            this.setState({ pathDeleteList: [] });
        }
    };
    deleteSelected = () => {
        const tmpPaths = this.state.paths;
        const pathDeleteList = this.state.pathDeleteList;
        for (let i = 0; i < pathDeleteList.length; i++) {
            delete tmpPaths[pathDeleteList[i].path][pathDeleteList[i].method];
            const indexesToDelete = [];
            for (let j = 0; j < this.childResources.length; j++) {
                if (this.childResources[j]) {
                    if (this.childResources[j].props.path === pathDeleteList[i].path && this.childResources[j].props.method === pathDeleteList[i].method) {
                        indexesToDelete.push(j);
                    }
                }
            }
            for (let j = 0; j < indexesToDelete.length; j++) {
                this.childResources.splice(j, 1); // Remove react child from reference array
            }
        }
        for (let i = 0; i < pathDeleteList.length; i++) {
            pathDeleteList.splice(i, 1); // Remove the item from waiting to be deleted list
        }

        this.setState({ pathDeleteList });
        this.setState({ path: tmpPaths });
        for (let i = 0; i < this.childResources.length; i++) {
            if (this.childResources[i]) {
                this.childResources[i].toggleDeleteCheck(false);
            }
        }
    };
    toggleAddResource = () => {
        this.setState({ showAddResource: !this.state.showAddResource });
    }
    toggleAssignPolicy = () => {
        this.setState({ showPolicy: !this.state.showPolicy, showAddResource: false });
    }
    advancedPolicyTypeChange = (event) => {
        const { apiThrottlingPolicy } = this.props.api;
        if (event.target.value === 'perAPI') {
            this.setState({ selectedPolicy: apiThrottlingPolicy });
        }
        this.setState({ policyLevel: event.target.value });
    }
    handlePolicyChange = (event) => {
        this.setState({ selectedPolicy: event.target.value });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            api, showAddResource, showPolicy, policyLevel, apiPolicies, selectedPolicy,
        } = this.state;

        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }
        const plainOptions = ['get', 'post', 'put', 'delete', 'patch', 'head', 'options'];
        const paths = this.state.paths;
        const { classes, intl } = this.props;
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage id='Apis.Details.Resources.Resources.resources' defaultMessage='Resources' />
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.toggleAddResource} disabled={this.isNotCreator}>
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='Apis.Details.Resources.Resources.add.new.resource.button'
                            defaultMessage='Add New Resource'
                        />
                    </Button>
                    <Button size='small' className={classes.button} onClick={this.toggleAssignPolicy} disabled={this.isNotCreator}>
                        <ScopesIcon className={classes.buttonIcon} />
                        <FormattedMessage
                            id='Apis.Details.Resources.Resources.assign.policies'
                            defaultMessage='Assign Advanced Throttling Policies'
                        />
                    </Button>
                </div>
                <div className={classes.contentWrapper}>
                    {showAddResource &&
                        <React.Fragment>
                            <div className={classes.addNewWrapper}>
                                <Typography className={classes.addNewHeader}>
                                    <FormattedMessage
                                        id='Apis.Details.Resources.Resources.add.new.resource.title'
                                        defaultMessage='Add New Resource'
                                    />
                                </Typography>
                                <Divider className={classes.divider} />
                                <div className={classes.addNewOther}>
                                    <TextField
                                        required
                                        id='outlined-required'
                                        label={intl.formatMessage({
                                            id: 'Apis.Details.Resources.Resources.url.pattern',
                                            defaultMessage: 'URL Pattern',
                                        })}
                                        margin='normal'
                                        variant='outlined'
                                        id='tmpResourceName'
                                        className={classes.addResource}
                                        value={this.state.tmpResourceName}
                                        onChange={this.onChangeInput('tmpResourceName')}
                                    />
                                    <div className={classes.radioGroup}>
                                        {plainOptions.map((option, index) => (
                                            <FormGroup key={index} row>
                                                <FormControlLabel
                                                    control={<Checkbox
                                                        checked={this.state.tmpMethods.indexOf(option) > -1}
                                                        onChange={this.handleChange(option)}
                                                        value={option}
                                                    />
                                                    }
                                                    label={option.toUpperCase()}
                                                />
                                            </FormGroup>
                                        ))}
                                    </div>
                                </div>
                                <Divider className={classes.divider} />
                                <div className={classes.addNewOther}>
                                    <Button variant='contained' color='primary' onClick={this.addResources}>
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resources.add.resources.to.path'
                                            defaultMessage='Add Resources to Path'
                                        />
                                    </Button>
                                    <Button className={classes.button} onClick={this.toggleAddResource}>
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resources.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </div>
                            </div>
                        </React.Fragment>}

                    {showPolicy &&
                        <React.Fragment>
                            <div className={classes.addNewWrapper}>
                                <Typography className={classes.addNewHeader}>
                                    <FormattedMessage
                                        id='Apis.Details.Resources.Resources.assign.advanced.throttling.policies'
                                        defaultMessage='Assign advanced throttling policies'
                                    />
                                </Typography>
                                <Divider className={classes.divider} />
                                <div className={classes.addNewOther}>
                                    <FormControl component='fieldset' className={classes.formControl}>
                                        <RadioGroup
                                            aria-label='advancedPolicyType'
                                            name='advancedPolicyType'
                                            className={classes.group}
                                            value={policyLevel}
                                            onChange={this.advancedPolicyTypeChange}
                                        >
                                            <FormControlLabel
                                                value='perAPI'
                                                control={<Radio />}
                                                label={intl.formatMessage({
                                                    id: 'Apis.Details.Resources.Resources.assign.advanced.throttling.perApi',
                                                    defaultMessage: 'Apply per API',
                                                })}
                                            />
                                            <FormControlLabel
                                                value='perResource'
                                                control={<Radio />}
                                                label={intl.formatMessage({
                                                    id: 'Apis.Details.Resources.Resources.assign.advanced.throttling.Resource',
                                                    defaultMessage: 'Apply per Resource',
                                                })}
                                            />
                                        </RadioGroup>
                                    </FormControl>
                                    <div className={classes.rightDataColum}>
                                        {policyLevel === 'perAPI' &&
                                            <Select
                                                className={classes.selectWidth}
                                                value={selectedPolicy}
                                                onChange={this.handlePolicyChange}
                                                fieldName='Throttling Policy'
                                            >
                                                {apiPolicies.map(policy => (
                                                    <MenuItem
                                                        key={policy.name}
                                                        value={policy.name}
                                                    >
                                                        {policy.displayName}
                                                    </MenuItem>
                                                ))}
                                            </Select>}
                                    </div>
                                </div>
                            </div>
                        </React.Fragment>}

                    <List>
                        {this.state.paths && (
                            <ListItem>
                                <FormControlLabel
                                    control={<Checkbox
                                        checked={this.state.allChecked}
                                        onChange={this.handleCheckAll}
                                        value=''
                                        disabled={this.isNotCreator}
                                    />}
                                    label='Check All'
                                />
                                {Object.keys(this.state.pathDeleteList).length !== 0 && (
                                    <ListItemSecondaryAction>
                                        <Button
                                            className={classes.button}
                                            color='secondary'
                                            onClick={this.deleteSelected}
                                            disabled={this.isNotCreator}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.Resources.Resources.delete.selected'
                                                defaultMessage='Delete Selected'
                                            />
                                        </Button>
                                    </ListItemSecondaryAction>
                                )}
                            </ListItem>
                        )}
                        {Object.keys(paths).map((key) => {
                            const path = paths[key];
                            const that = this;
                            return (
                                <div>
                                    <ExpansionPanel defaultExpanded className={classes.expansionPanel}>
                                        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                                            <Typography className={classes.heading} variant='h6'>{key}</Typography>
                                        </ExpansionPanelSummary>
                                        <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                                            {Object.keys(path).map((innerKey) => {
                                                return CONSTS.HTTP_METHODS.includes(innerKey) ?
                                                    <Resource
                                                        path={key}
                                                        method={innerKey}
                                                        methodData={path[innerKey]}
                                                        updatePath={that.updatePath}
                                                        scopes={api.scopes}
                                                        apiPolicies={apiPolicies}
                                                        isAPIProduct={false}
                                                        addRemoveToDeleteList={that.addRemoveToDeleteList}
                                                        onRef={ref => this.childResources.push(ref)}
                                                        policyLevel={policyLevel}
                                                    /> : null;
                                            })}
                                        </ExpansionPanelDetails>
                                    </ExpansionPanel>
                                </div>
                            );
                        })}
                    </List>
                    <Grid  container
                           direction='row'
                           alignItems='center'
                           spacing={4}>
                        <Grid item>
                            <Button
                                variant='contained'
                                color='primary'
                                className={classes.buttonMain}
                                onClick={this.updateResources}
                                disabled={this.isNotCreator}
                            >
                                <FormattedMessage id='Apis.Details.Resources.Resources.save' defaultMessage='Save' />
                            </Button>
                        </Grid>
                        {this.isNotCreator
                            && (
                                <Grid item>
                                    <Typography variant='body2' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resources.update.not.allowed'
                                            defaultMessage='*You are not authorized to update API resources due
                                        to insufficient permissions'
                                        />
                                    </Typography>
                                </Grid>
                            )
                        }
                    </Grid>
                </div>
            </div>
        );
    }
}
Resources.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(Resources));
