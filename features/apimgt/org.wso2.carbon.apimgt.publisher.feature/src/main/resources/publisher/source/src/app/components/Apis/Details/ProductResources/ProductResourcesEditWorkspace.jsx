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
/* eslint-disable array-callback-return */
/* eslint no-param-reassign: ["error", { "props": true, "ignorePropertyModificationsFor": ["operationObj"] }] */

import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import TextField from '@material-ui/core/TextField';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import Icon from '@material-ui/core/Icon';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';
import IconButton from '@material-ui/core/IconButton';
import queryString from 'query-string';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import cloneDeep from 'lodash.clonedeep';
import PropTypes from 'prop-types';
import API from 'AppData/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import MethodView from 'AppComponents/Apis/Details/ProductResources/MethodView';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import CONSTS from 'AppData/Constants';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';

const useStyles = makeStyles((theme) => ({
    searchWrapper: {
        padding: 0,
        marginTop: theme.spacing(1),
        '& input': {
            padding: '12px 14px',
        },
    },
    paper: {
        height: '100%',
        borderRadius: 0,
    },
    apiWrapper: {
        overflowY: 'auto',
        height: 349,
    },
    ResourceWrapper: {
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2),
        '& .frame': {
            overflowY: 'auto',
            height: 328,
            border: '1px solid ' + theme.palette.grey[400],
            borderRadius: theme.spacing(1),
        },
        '& .frame::-webkit-scrollbar': {
            '-webkit-appearance': 'none',
        },
        '& .frame::-webkit-scrollbar:vertical': {
            width: 11,
        },
        '& .frame::-webkit-scrollbar:horizontal': {
            height: 11,
        },
        '& .frame::-webkit-scrollbar-thumb': {
            borderRadius: theme.spacing(1),
            border: '2px solid' + theme.palette.common.white,
            backgroundColor: theme.palette.grey[400],
        },
        '& .frame.rightFrame': {
            height: 369,
        },
    },
    SelectedResourceWrapper: {
        overflowY: 'auto',
        overflowX: 'auto',
        height: 321,
    },
    leftMost: {
        background: theme.palette.grey[700],
        color: theme.palette.getContrastText(theme.palette.grey[700]),
        padding: theme.spacing(1),
    },
    rightMost: {
        background: theme.palette.grey[600],
        color: theme.palette.getContrastText(theme.palette.grey[600]),
        padding: theme.spacing(1),
    },
    colTitle: {
        background: theme.palette.grey[400],
        color: theme.palette.getContrastText(theme.palette.grey[400]),
        padding: theme.spacing(1),
        fontWeight: 200,
        minHeight: 25,
    },
    treeItemMain: {
        background: theme.palette.grey[100],
        color: theme.palette.getContrastText(theme.palette.grey[100]),
        padding: theme.spacing(1),
    },
    treeItemMainWrapper: {
        paddingLeft: theme.spacing(2),
    },
    treeItem: {
        '& .material-icons': {
            fontSize: theme.spacing(2),
            cursor: 'pointer',
            marginRight: theme.spacing(1),
        },
        display: 'flex',
        alignItems: 'center',
        padding: '6px 0',
    },
    hr: {
        flex: 1,
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        color: theme.palette.getContrastText(theme.palette.grey[100]),
    },
    methodView: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
    },
    middleText: {
        flex: 1,
    },
    selectedApi: {
        background: theme.palette.grey[100],
    },
    selectedTitle: {
        padding: theme.spacing(2),
    },
    buttonWrapper: {
        marginTop: theme.spacing(2),
        textDecorate: 'none',
    },
    selectedApiDescription: {
        padding: '0px 16px',
    },
    messageWrapper: {
        padding: theme.spacing(3),
    },
    tootBar: {
        display: 'flex',
        justifyContent: 'flex-end',
        alignItems: 'center',
        margin: '0 16px',
        background: theme.palette.grey[100],
        color: theme.palette.getContrastText(theme.palette.grey[100]),
        padding: 5,
        borderRadius: 5,
        '& a': {
            cursor: 'pointer',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
        },
    },
    inactiveIcon: {
        color: theme.palette.grey[300],
    },
}));

/**
 *
 *
 * @param {*} props
 * @returns
 */
function ProductResourcesEdit(props) {
    const classes = useStyles();
    const {
        apiResources, setApiResources, isStateCreate, api, resourceNotFountMessage,
    } = props;

    // Define states
    const [allApis, setAllApis] = useState([]);
    const [notFound, setNotFound] = useState(false);
    const [searchText, setSearchText] = useState('');
    const [selectedApiPaths, setSelectedApiPaths] = useState([]);
    const [selectedApi, setSelectedApi] = useState(null);
    const [fromSearch, setFromSearch] = useState(false);
    // Initialize the rest api libraries
    const apiRestClient = new API();

    /**
     * This method is filtering apis base on the searchText entered. In no searchText provided it will give all apis.
     *
     * @param {*} [text=null]
     * @returns a promise
     */
    const filterAPIs = (text = null) => {
        if (text) {
            // Build the search query and update
            const inputValue = text.trim().toLowerCase();
            const composeQuery = '?query=name:' + inputValue + ' type:HTTP';
            const composeQueryJSON = queryString.parse(composeQuery);
            // TODO we need to make the limit and offset changeable from the UI ( paggination )
            composeQueryJSON.limit = 100;
            composeQueryJSON.offset = 0;
            return API.search(composeQueryJSON);
        } else {
            return API.all({
                query: {
                    type: 'HTTP',
                },
            });
        }
    };
    const addPropsToSelectedApiPaths = (paths, apiId, latestApiResources = apiResources) => {
        /* Add checked field to each resource object */
        Object.keys(paths).map((key) => {
            const methodObj = paths[key];
            Object.keys(methodObj).map((innerKey) => {
                // We are setting the check property at this level because we need to
                // add resources for each verb ( post, get, put etc.. )
                methodObj[innerKey].checked = false;

                // We need to check the latestApiResources for the same
                // API/Resource Name / Verb and  indicate it differently
                // Loop latestApiResources object
                const target = key;
                const verb = innerKey;
                let resourceFound = false;
                if (latestApiResources) {
                    Object.keys(latestApiResources).map((resourcekey) => {
                        const apiResource = latestApiResources[resourcekey];

                        // Check if the the api slected from UI is same as the operation api id checking
                        if (apiResource && apiId === apiResource.apiId) {
                            // API is the same
                            Object.keys(apiResource.operations).map((operationKey) => {
                                const operation = apiResource.operations[operationKey];
                                if (
                                    operation
                                    && operation.target === target
                                    && operation.verb.toLowerCase() === verb.toLowerCase()
                                ) {
                                    // Operation is already there
                                    resourceFound = true;
                                }
                            });
                        }
                    });
                }
                if (resourceFound) {
                    methodObj[innerKey].allreadyAdded = true;
                } else {
                    methodObj[innerKey].allreadyAdded = false;
                }
            });
        });
        setSelectedApiPaths(paths);
    };

    // Get the api swagger after an api is selected
    const getApiSwagger = (apiSelected) => {
        const { id } = apiSelected;
        const promisedAPI = apiRestClient.getSwagger(id);
        promisedAPI
            .then((response) => {
                if (response.obj.paths !== undefined) {
                    addPropsToSelectedApiPaths(response.obj.paths, id);
                    setSelectedApi(apiSelected);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    };
    const handleSearchTextChange = (event) => {
        const apiPromise = filterAPIs(event.target.value);
        apiPromise
            .then((response) => {
                const {
                    body: { list },
                } = response;
                const filteredList = list.filter((theApi) => theApi.status !== 'PROTOTYPED');
                if (filteredList.length > 0) {
                    setSelectedApi(list[0]);
                }
                setAllApis(filteredList);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
        setSearchText(event.target.value);
        setFromSearch(event.target.value);
    };
    const updateResourceTree = (resourceToAdd, action, inputApiResources = null) => {
        let updateStateHere = false;
        let newApiResources = null;
        if (!inputApiResources) {
            // If a copy of the state variable is not passed from the calling method we
            // have to make a copy inside here before doing modifications to that
            newApiResources = cloneDeep(apiResources);
            updateStateHere = true;
        } else {
            newApiResources = inputApiResources;
        }
        const {
            target, verb, apiId, name, version,
        } = resourceToAdd;
        const newResource = {
            id: null,
            target,
            verb: verb.toUpperCase(),
            authType: null,
            throttlingPolicy: null,
            scopes: [],
        };
        let apiFound = false;

        // Loop copy of apiResources object
        Object.keys(newApiResources).map((key, indexA) => {
            const apiResource = newApiResources[key];

            // Check if the the api slected from UI is same as the operation api id trying to remove.
            if (apiResource && apiId === apiResource.apiId) {
                apiFound = true;
                let operationFound = false;
                Object.keys(apiResource.operations).map((innerKey, indexB) => {
                    const operation = apiResource.operations[innerKey];
                    if (
                        operation
                        && operation.target === target
                        && operation.verb.toLowerCase() === verb.toLowerCase()
                    ) {
                        // Operation is already there
                        operationFound = true;
                        if (action === 'remove') {
                            if (apiResource.operations.length > 1) {
                                apiResource.operations.splice(indexB, 1);
                            } else {
                                newApiResources.splice(indexA, 1);
                            }
                        }
                    }
                });
                // Operation not added .. so we need to add that
                if (!operationFound && action === 'add') {
                    apiResource.operations.push(newResource);
                }
            }
        });

        if (!apiFound) {
            // Add api object
            newApiResources.push({
                name,
                apiId,
                operations: [newResource],
                version,
            });
        }
        // When we are adding the resources in a loop we do not care about the return but we simply set the state here.
        if (updateStateHere) {
            setApiResources(newApiResources);
            // We need to call this in order to set other properties
            if (apiId === selectedApi.id) {
                addPropsToSelectedApiPaths(cloneDeep(selectedApiPaths), apiId, newApiResources);
            }
        }
        return newApiResources;
    };

    const updateCheckBox = (key, innerKey) => {
        // we need to copy the object from the state and modify it before set it to the state
        const prevSelectedApiPaths = { ...selectedApiPaths };

        // Now we inverse the checked value
        prevSelectedApiPaths[key][innerKey].checked = !prevSelectedApiPaths[key][innerKey].checked;

        // Then we set state
        setSelectedApiPaths(prevSelectedApiPaths);
    };
    const addSelectedResourcesToTree = (addAll = false) => {
        /* Add checked field to each resource object */
        const newApiResources = cloneDeep(apiResources);
        Object.keys(selectedApiPaths).map((key) => {
            const methodObj = selectedApiPaths[key];
            Object.keys(methodObj).map((innerKey) => {
                // We are setting the check property at this level because we need to
                // add resources for each verb ( post, get, put etc.. )
                if (methodObj[innerKey].checked || addAll) {
                    // We need to add this to apiResources array
                    updateResourceTree(
                        {
                            target: key,
                            verb: innerKey,
                            apiId: selectedApi.id,
                            name: selectedApi.name,
                            version: selectedApi.version,
                        },
                        'add',
                        newApiResources,
                    );
                }
            });
        });
        setApiResources(newApiResources);
        addPropsToSelectedApiPaths(cloneDeep(selectedApiPaths), selectedApi.id, newApiResources);
    };
    useEffect(() => {
        // Get all apis
        const apiPromise = filterAPIs();
        apiPromise
            .then((response) => {
                const {
                    body: { list },
                } = response;
                const filteredList = list.filter((theApi) => theApi.lifeCycleStatus !== 'PROTOTYPED');

                setAllApis(filteredList);
                if (filteredList.length > 0) {
                    setSelectedApi(filteredList[0]);
                    getApiSwagger(filteredList[0]);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }, []);
    if (notFound) {
        return <ResourceNotFound message={resourceNotFountMessage} />;
    }
    return (
        <>
            {allApis.length === 0 && !fromSearch ? (
                <Grid container>
                    <Typography className={classes.messageWrapper}>
                        <FormattedMessage
                            id='Apis.Details.ProductResources.ProductResourcesWorkspace.ApisnotFound'
                            defaultMessage='No REST APIs are created yet'
                        />
                    </Typography>
                </Grid>
            ) : (
                <>
                    {!isStateCreate && (
                        <Grid container>
                            <>
                                <Grid item xs={8} className={classes.leftMost}>
                                    <Typography>
                                        <FormattedMessage
                                            id='Apis.Details.ProductResources.ProductResourcesWorkspace.find.and.select'
                                            defaultMessage='Find and select resources for the API Product'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item xs={4} className={classes.rightMost}>
                                    <Typography>
                                        <FormattedMessage
                                            id='Apis.Details.ProductResources.ProductResourcesWorkspace.selected'
                                            defaultMessage='Selected resources of API Product'
                                        />
                                    </Typography>
                                </Grid>
                            </>
                        </Grid>
                    )}
                    <Grid container>
                        {/* ************************************************ */}
                        {/* 1st column API search and select column          */}
                        {/* ************************************************ */}
                        <Grid item xs={3}>
                            <div className={classes.colTitle}>
                                <Typography>
                                    <FormattedMessage
                                        id='Apis.Details.ProductResources.ProductResourcesWorkspace.select.an.api'
                                        defaultMessage='Select an API'
                                    />
                                </Typography>
                            </div>
                            <Paper>
                                <ListItem className={classes.searchWrapper}>
                                    <TextField
                                        id='outlined-full-width'
                                        label='API'
                                        style={{ margin: 8 }}
                                        placeholder='Filter APIs'
                                        helperText='Filter by name'
                                        onChange={handleSearchTextChange}
                                        value={searchText}
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                    />
                                </ListItem>
                                <div className={classes.apiWrapper}>
                                    <List dense>
                                        {allApis.map((apiObj) => {
                                            const labelId = `checkbox-list-label-${apiObj.id}`;

                                            return (
                                                <ListItem
                                                    key={apiObj.id}
                                                    role={undefined}
                                                    dense
                                                    button
                                                    className={
                                                        selectedApi
                                                        && apiObj.id === selectedApi.id
                                                        && classes.selectedApi
                                                    }
                                                >
                                                    <ListItemText
                                                        id={labelId}
                                                        primary={apiObj.name}
                                                        secondary={`${apiObj.version} - ${apiObj.context}`}
                                                        onClick={() => getApiSwagger(apiObj)}
                                                    />
                                                </ListItem>
                                            );
                                        })}
                                    </List>
                                </div>
                            </Paper>
                        </Grid>
                        {/* ************************************************ */}
                        {/* 2nd column Resource Selection                    */}
                        {/* ************************************************ */}
                        <Grid item xs={5}>
                            <Paper className={classes.paper}>
                                <div className={classes.colTitle}>
                                    <Typography>
                                        <FormattedMessage
                                            id='Apis.Details.ProductResources.ProductResourcesEdit.api.resources'
                                            defaultMessage='Select API Resources'
                                        />
                                    </Typography>
                                </div>
                                {selectedApi && (
                                    <Typography variant='h5' className={classes.selectedTitle}>
                                        {selectedApi.name}
                                    </Typography>
                                )}
                                <div className={classes.tootBar}>
                                    <a
                                        onClick={() => addSelectedResourcesToTree()}
                                        onKeyDown={() => addSelectedResourcesToTree()}
                                    >
                                        <Typography variant='body2'>
                                            <FormattedMessage
                                                id='Apis.Details.ProductResources.ProductResourcesWorkspace.
                                        toolbar.add.selected'
                                                defaultMessage='Add Selected'
                                            />
                                        </Typography>
                                        <Icon>fast_forward</Icon>
                                    </a>
                                    <VerticalDivider />
                                    <a
                                        onClick={() => addSelectedResourcesToTree(true)}
                                        onKeyDown={() => addSelectedResourcesToTree(true)}
                                    >
                                        <Typography variant='body2'>
                                            <FormattedMessage
                                                id='Apis.Details.ProductResources.ProductResourcesWorkspace.toolbar.
                                                add.all'
                                                defaultMessage='Add All'
                                            />
                                        </Typography>
                                        <Icon>fast_forward</Icon>
                                    </a>
                                </div>
                                <div className={classes.ResourceWrapper}>
                                    <div className='frame'>
                                        <List dense>
                                            {Object.keys(selectedApiPaths).map((key) => {
                                                const path = selectedApiPaths[key];
                                                const labelId = `checkbox-list-label-${key}`;
                                                return Object.keys(path).map((innerKey) => {
                                                    const methodObj = path[innerKey];
                                                    return (
                                                        CONSTS.HTTP_METHODS.includes(innerKey) && (
                                                            <ListItem
                                                                key={`${innerKey} - ${key}`}
                                                                role={undefined}
                                                                dense
                                                            >
                                                                <ListItemIcon style={{ minWidth: 35 }}>
                                                                    <Checkbox
                                                                        edge='start'
                                                                        checked={methodObj.checked}
                                                                        tabIndex={-1}
                                                                        disableRipple
                                                                        onChange={() => updateCheckBox(key, innerKey)}
                                                                        color='primary'
                                                                        disabled={methodObj.allreadyAdded}
                                                                    />
                                                                </ListItemIcon>
                                                                <ListItemText
                                                                    id={labelId}
                                                                    primary={(
                                                                        <div>
                                                                            <MethodView
                                                                                method={innerKey}
                                                                                className={classes.methodView}
                                                                            />
                                                                            <span>{key}</span>
                                                                        </div>
                                                                    )}
                                                                    secondary={
                                                                        methodObj['x-auth-type']
                                                                    && methodObj['x-throttling-tier']
                                                                    && `${methodObj['x-auth-type']} - ${
                                                                        methodObj['x-throttling-tier']
                                                                    }`
                                                                    }
                                                                    onClick={() => updateResourceTree(
                                                                        {
                                                                            target: key,
                                                                            verb: innerKey,
                                                                            apiId: selectedApi.id,
                                                                            name: selectedApi.name,
                                                                            version: selectedApi.version,
                                                                        },
                                                                        'add',
                                                                    )}
                                                                    className={classes.middleText}
                                                                />
                                                                <ListItemSecondaryAction>
                                                                    {methodObj.allreadyAdded && (
                                                                        <Icon className={classes.inactiveIcon}>
                                                                            chevron_right
                                                                        </Icon>
                                                                    )}
                                                                    {!methodObj.allreadyAdded && (
                                                                        <IconButton
                                                                            edge='end'
                                                                            aria-label='comments'
                                                                            onClick={() => updateResourceTree(
                                                                                {
                                                                                    target: key,
                                                                                    verb: innerKey,
                                                                                    apiId: selectedApi.id,
                                                                                    name: selectedApi.name,
                                                                                    version: selectedApi.version,
                                                                                },
                                                                                'add',
                                                                            )}
                                                                        >
                                                                            <Icon>chevron_right</Icon>
                                                                        </IconButton>
                                                                    )}
                                                                </ListItemSecondaryAction>
                                                            </ListItem>
                                                        )
                                                    );
                                                });
                                            })}
                                        </List>
                                    </div>
                                </div>
                            </Paper>
                        </Grid>
                        {/* ************************************************ */}
                        {/* Third column with  selected resources            */}
                        {/* ************************************************ */}
                        <Grid item xs={4}>
                            <Paper className={classes.paper}>
                                <div className={classes.colTitle} />
                                {api.name && (
                                    <>
                                        <Typography variant='h5' className={classes.selectedTitle}>
                                            {api.name}
                                        </Typography>
                                    </>
                                )}
                                <div className={classes.ResourceWrapper}>
                                    <div className='frame rightFrame'>
                                        {allApis.length > 0 && apiResources && apiResources.length === 0 && (
                                            <div className={classes.messageWrapper}>
                                                <Typography component='p'>
                                                    <FormattedMessage
                                                        id='Apis.Details.ProductResources.ProductResourcesWorkspace.
                                                    empty.title'
                                                        defaultMessage='Use the left side panel to add resources'
                                                    />
                                                </Typography>
                                            </div>
                                        )}
                                        {apiResources
                                        && apiResources.length > 0
                                        && Object.keys(apiResources).map((key) => {
                                            const apiResource = apiResources[key];
                                            return (
                                                <div key={apiResource.name}>
                                                    <div className={classes.treeItemMain}>
                                                        <Typography component='p'>
                                                            {apiResource.name}
                                                            {' - '}
                                                            {apiResource.version}
                                                        </Typography>
                                                    </div>
                                                    <div className={classes.treeItemMainWrapper}>
                                                        {Object.keys(apiResource.operations).map((innerKey) => {
                                                            const operation = apiResource.operations[innerKey];
                                                            const { target, verb } = operation;
                                                            return (
                                                                <div
                                                                    key={`${apiResource.apiId}_${verb}_${target}`}
                                                                    className={classes.treeItem}
                                                                >
                                                                    <MethodView
                                                                        method={verb}
                                                                        className={classes.methodView}
                                                                    />
                                                                    <Typography variant='body2'>{target}</Typography>
                                                                    <hr className={classes.hr} />
                                                                    <Icon
                                                                        onClick={() => updateResourceTree(
                                                                            {
                                                                                target,
                                                                                verb,
                                                                                apiId: apiResource.apiId,
                                                                                name: apiResource.name,
                                                                                version: apiResource.version,
                                                                            },
                                                                            'remove',
                                                                        )}
                                                                    >
                                                                        delete
                                                                    </Icon>
                                                                </div>
                                                            );
                                                        })}
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>
                            </Paper>
                        </Grid>
                    </Grid>
                </>
            )}
        </>
    );
}
ProductResourcesEdit.propTypes = {
    apiResources: PropTypes.instanceOf(Array).isRequired,
    setApiResources: PropTypes.func.isRequired,
    isStateCreate: PropTypes.isRequired,
    api: PropTypes.isRequired,
    resourceNotFountMessage: PropTypes.string.isRequired,
};
export default ProductResourcesEdit;
