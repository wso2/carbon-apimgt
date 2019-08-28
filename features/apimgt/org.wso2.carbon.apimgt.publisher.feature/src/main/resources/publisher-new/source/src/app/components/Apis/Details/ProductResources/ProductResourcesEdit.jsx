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
import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
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
import Button from '@material-ui/core/Button';

import APIProduct from 'AppData/APIProduct';
import API from 'AppData/api';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SearchParser from 'AppComponents/Base/Header/headersearch/SearchParser';
import MethodView from 'AppComponents/Apis/Details/ProductResources/MethodView';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import CONSTS from 'AppData/Constants';

const useStyles = makeStyles(theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    searchWrapper: {
        padding: 0,
        marginTop: theme.spacing.unit,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: theme.spacing.unit * 2,
    },
    paper: {
        height: '100%',
        borderRadius: 0,
    },
    apiWrapper: {
        overflowY: 'auto',
        height: 400,
    },
    colTitle: {
        background: theme.palette.grey[700],
        color: theme.palette.getContrastText(theme.palette.grey[700]),
        padding: theme.spacing.unit,
        fontWeight: 200,
    },
    treeItemMain: {
        background: theme.palette.grey[100],
        color: theme.palette.getContrastText(theme.palette.grey[100]),
        padding: theme.spacing.unit,
    },
    treeItemMainWrapper: {
        paddingLeft: theme.spacing.unit * 4,
    },
    treeItem: {
        '& .material-icons': {
            fontSize: theme.spacing.unit * 2,
            cursor: 'pointer',
            marginRight: theme.spacing.unit,
        },
        display: 'flex',
        alignItems: 'center',
        padding: '6px 0',
    },
    hr: {
        flex: 1,
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        color: theme.palette.getContrastText(theme.palette.grey[100]),
    },
    methodView: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
    },
    middleText: {
        flex: 1,
    },
    selectedApi: {
        background: theme.palette.grey[100],
    },
    selectedTitle: {
        padding: theme.spacing.unit * 2,
    },
    buttonWrapper: {
        marginTop: theme.spacing.unit * 2,
        textDecorate: 'none',
    },
    selectedApiDescription: {
        padding: theme.spacing.unit * 2,
    },
}));

function ProductResourcesEdit() {
    const classes = useStyles();

    // Get the current api product object from the context
    const { api, updateAPI, } = useContext(APIContext);
    const apiCopy = JSON.parse(JSON.stringify(api));
    const { apis } = apiCopy;

    // Define states
    const [allApis, setAllApis] = useState([]);
    const [notFound, setNotFound] = useState(false);
    const [checked, setChecked] = useState([0]);
    const [searchText, setSearchText] = useState('');
    const [selectedApiPaths, setSelectedApiPaths] = useState([]);
    const [selectedApi, setSelectedApi] = useState(null);
    const [apiResources, setApiResources] = useState(apis);

    // Initialize the rest api libraries
    const apiProductRestClient = new APIProduct();
    const apiRestClient = new API();

    /**
     * This method is filtering apis base on the searchText entered. In no searchText provided it will give all apis.
     *
     * @param {*} [searchText=null]
     * @returns a promise
     */
    const filterAPIs = (searchText = null) => {
        if (searchText) {
            // Build the search query and update
            const inputValue = searchText.trim().toLowerCase();
            let composeQuery = SearchParser.parse(inputValue);
            composeQuery = '?query=' + composeQuery;
            const composeQueryJSON = queryString.parse(composeQuery);
            // TODO we need to make the limit and offset changeable from the UI ( paggination )
            composeQueryJSON.limit = 100;
            composeQueryJSON.offset = 0;
            return API.search(composeQueryJSON);
        } else {
            return API.all({});
        }
    };

    // Get the api swagger after an api is selected
    const getApiSwagger = (api) => {
        const { id } = api;
        const promisedAPI = apiRestClient.getSwagger(id);
        promisedAPI
            .then((response) => {
                if (response.obj.paths !== undefined) {
                    setSelectedApiPaths(response.obj.paths);
                    setSelectedApi(api);
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
                if (list.length > 0) {
                    setSelectedApi(list[0]);
                }
                setAllApis(list);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    setNotFound(true);
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
        setSearchText(event.target.value);
    };
    const updateResourceTree = (resourceToAdd, action) => {
        const newApiResources = JSON.parse(JSON.stringify(apiResources));
        const {
            target, verb, apiId, name,
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
                        operation &&
                        operation.target === target &&
                        operation.verb.toLowerCase() === verb.toLowerCase()
                    ) {
                        // Operation is already there
                        operationFound = true;
                        if (action === 'remove') {
                            if (apiResource.operations.length > 1) {
                                apiResource.operations.splice(indexB, 1);
                                setApiResources(newApiResources);
                            } else {
                                newApiResources.splice(indexA, 1);
                                setApiResources(newApiResources);
                            }
                        }
                    }
                });
                // Operation not added .. so we need to add that
                if (!operationFound && action === 'add') {
                    apiResource.operations.push(newResource);
                    setApiResources(newApiResources);
                }
            }
        });

        if (!apiFound) {
            // Add api object
            newApiResources.push({
                name,
                apiId,
                operations: [newResource],
            });
            setApiResources(newApiResources);
        }
    };
    const save = () => {
        const updatePromise = updateAPI(apiCopy,true);
        updatePromise.then((response) => {
            console.info(response);     
        })
        .catch((error) => {
            if (process.env.NODE_ENV !== 'production') console.log(error);
            const status = error.status;
            if (status === 401) {
                doRedirectToLogin();
            }
        });
    }
    useEffect(() => {
        // Get all apis
        const apiPromise = filterAPIs();
        apiPromise
            .then((response) => {
                const {
                    body: { list },
                } = response;
                setAllApis(list);
                if (list.length > 0) {
                    setSelectedApi(list[0]);
                    getApiSwagger(list[0]);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const status = error.status;
                if (status === 404) {
                    setNotFound(true);
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }, []);
    if (notFound) {
        return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
    }
    return (
        <div className={classes.root}>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.ProductResources.ProductResources.title'
                        defaultMessage='Manage Resources'
                    />
                </Typography>
            </div>
            <div className={classes.contentWrapper}>
                <Grid container>
                    <Grid item xs={3}>
                        <div className={classes.colTitle}>
                            <FormattedMessage
                                id='Apis.Details.ProductResources.ProductResources.select.an.api'
                                defaultMessage='Select an API'
                            />
                        </div>
                        <Paper>
                            <ListItem className={classes.searchWrapper}>
                                <TextField
                                    id='outlined-full-width'
                                    label='API'
                                    style={{ margin: 8 }}
                                    placeholder='Filter APIs'
                                    helperText='Filter the visible APIs'
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
                                    {allApis.map((api) => {
                                        const labelId = `checkbox-list-label-${api.id}`;

                                        return (
                                            <ListItem
                                                key={api.id}
                                                role={undefined}
                                                dense
                                                button
                                                className={
                                                    selectedApi && selectedApi.id === api.id && classes.selectedApi
                                                }
                                            >
                                                <ListItemText
                                                    id={labelId}
                                                    primary={api.name}
                                                    secondary={`${api.version} - ${api.context}`}
                                                    onClick={() => getApiSwagger(api)}
                                                />
                                            </ListItem>
                                        );
                                    })}
                                </List>
                            </div>
                        </Paper>
                    </Grid>
                    <Grid item xs={4}>
                        <Paper className={classes.paper}>
                            <div className={classes.colTitle}>
                                <FormattedMessage
                                    id='Apis.Details.ProductResources.ProductResources.api.resources'
                                    defaultMessage='API Resources'
                                />
                            </div>
                            {selectedApi && (
                                <React.Fragment>
                                    <Typography variant='h5' className={classes.selectedTitle}>
                                        {selectedApi.name}
                                    </Typography>
                                    <Typography
                                        variant='caption'
                                        className={classes.selectedApiDescription}
                                        component='div'
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.ProductResources.ProductResources.api.resources'
                                            defaultMessage={
                                                'All resources available with the API are displayed bellow.' +
                                                'Click the right hand side arrow to add them to the api product.'
                                            }
                                        />
                                    </Typography>
                                </React.Fragment>
                            )}
                            <List dense>
                                {Object.keys(selectedApiPaths).map((key) => {
                                    const path = selectedApiPaths[key];
                                    const labelId = `checkbox-list-label-${key}`;
                                    return Object.keys(path).map((innerKey) => {
                                        const methodObj = path[innerKey];
                                        return (
                                            CONSTS.HTTP_METHODS.includes(innerKey) && (
                                                <ListItem key={`${innerKey} - ${key}`} role={undefined} dense>
                                                    <ListItemIcon>
                                                        <Checkbox
                                                            edge='start'
                                                            checked={checked}
                                                            tabIndex={-1}
                                                            disableRipple
                                                            inputProps={{ 'aria-labelledby': labelId }}
                                                        />
                                                    </ListItemIcon>
                                                    <ListItemText
                                                        id={labelId}
                                                        primary={
                                                            <div>
                                                                <span>{key}</span>
                                                                <MethodView
                                                                    method={innerKey}
                                                                    className={classes.methodView}
                                                                />
                                                            </div>
                                                        }
                                                        secondary={
                                                            methodObj['x-auth-type'] &&
                                                            methodObj['x-throttling-tier'] &&
                                                            `${methodObj['x-auth-type']} - ${
                                                                methodObj['x-throttling-tier']
                                                            }`
                                                        }
                                                        onClick={() =>
                                                            updateResourceTree(
                                                                {
                                                                    target: key,
                                                                    verb: innerKey,
                                                                    apiId: selectedApi.id,
                                                                    name: selectedApi.name,
                                                                },
                                                                'add',
                                                            )
                                                        }
                                                        className={classes.middleText}
                                                    />
                                                    <ListItemSecondaryAction>
                                                        <IconButton
                                                            edge='end'
                                                            aria-label='comments'
                                                            onClick={() =>
                                                                updateResourceTree(
                                                                    {
                                                                        target: key,
                                                                        verb: innerKey,
                                                                        apiId: selectedApi.id,
                                                                        name: selectedApi.name,
                                                                    },
                                                                    'add',
                                                                )
                                                            }
                                                        >
                                                            <Icon>fast_forward</Icon>
                                                        </IconButton>
                                                    </ListItemSecondaryAction>
                                                </ListItem>
                                            )
                                        );
                                    });
                                })}
                            </List>
                        </Paper>
                    </Grid>
                    <Grid item xs={5}>
                        <Paper className={classes.paper}>
                            <div className={classes.colTitle}>
                                <FormattedMessage
                                    id='Apis.Details.ProductResources.ProductResources.current.resources'
                                    defaultMessage='Current Resources'
                                />
                            </div>
                            <div className={classes.treeViewRoot}>
                                {Object.keys(apiResources).map((key, indexA) => {
                                    const apiResource = apiResources[key];
                                    return (
                                        <div key={indexA}>
                                            <div className={classes.treeItemMain}>{apiResource.name}</div>
                                            <div className={classes.treeItemMainWrapper}>
                                                {Object.keys(apiResource.operations).map((innerKey, indexB) => {
                                                    const operation = apiResource.operations[innerKey];
                                                    const { target, verb } = operation;
                                                    return (
                                                        <div key={`${indexA}_${indexB}`} className={classes.treeItem}>
                                                            <Typography variant='body2'>{target}</Typography>
                                                            <MethodView method={verb} className={classes.methodView} />
                                                            <hr className={classes.hr} />
                                                            <Icon
                                                                onClick={() =>
                                                                    updateResourceTree(
                                                                        {
                                                                            target,
                                                                            verb,
                                                                            apiId: apiResource.apiId,
                                                                            name: apiResource.name,
                                                                        },
                                                                        'remove',
                                                                    )
                                                                }
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
                        </Paper>
                    </Grid>
                </Grid>
                <div className={classes.buttonWrapper}>
                    <Grid container direction='row' alignItems='flex-start' spacing={4}>
                        <Grid item>
                            <div>
                                <Button variant='contained' color='primary' onClick={save}>
                                    <FormattedMessage
                                        id='Apis.Details.Properties.Properties.save'
                                        defaultMessage='Save'
                                    />
                                </Button>
                            </div>
                        </Grid>
                        <Grid item>
                            <Link to={'/apis/' + api.id + '/overview'}>
                                <Button>
                                    <FormattedMessage
                                        id='Apis.Details.Properties.Properties.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </div>
            </div>
        </div>
    );
}

export default ProductResourcesEdit;
