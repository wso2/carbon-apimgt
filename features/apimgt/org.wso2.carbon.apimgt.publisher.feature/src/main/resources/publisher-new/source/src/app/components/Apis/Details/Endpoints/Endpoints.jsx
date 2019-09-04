/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useContext, useEffect, useState } from 'react';
import { Grid, FormControlLabel, Radio, RadioGroup } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import NewEndpointCreate from 'AppComponents/Apis/Details/Endpoints/NewEndpointCreate';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import EndpointOverview from './EndpointOverview';
import PrototypeEndpoints from './Prototype/PrototypeEndpoints';
import { getEndpointConfigByImpl, createEndpointConfig } from './endpointUtils';

const styles = theme => ({
    endpointTypesWrapper: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
        margin: '2px',
    },
    root: {
        flexGrow: 1,
        paddingRight: '10px',
    },
    buttonSection: {
        marginTop: theme.spacing(2),
    },
    titleWrapper: {
        paddingTop: '10px',
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        marginLeft: theme.spacing(2),
    },
});

const endpointImplType = ['managed', 'PROTOTYPED'];
const defaultSwagger = { paths: {} };

/**
 * The base component of the endpoints view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Endpoints(props) {
    const { classes } = props;
    const { api, updateAPI } = useContext(APIContext);
    const [apiObject, setModifiedAPI] = useState(api);
    const [endpointImplementation, setEndpointImplementation] = useState('');
    const [swagger, setSwagger] = useState(defaultSwagger);

    /**
     * Method to update the api.
     *
     * @param {function} updateFunc The api update function.
     */
    const saveAPI = () => {
        if (apiObject !== {}) {
            updateAPI(apiObject);
        }
        if (Object.getOwnPropertyNames(defaultSwagger).length !== Object.getOwnPropertyNames(swagger).length) {
            console.log('Updating swagger...');
            api.updateSwagger(swagger).then((resp) => {
                console.log('success', resp);
            }).catch((err) => {
                console.log(err);
            });
        }
    };

    useEffect(() => {
        const { lifeCycleStatus } = api;
        const apiClone = cloneDeep(api.toJSON());
        setModifiedAPI(apiClone);
        const implType = apiClone.endpointConfig === null ? undefined : apiClone.endpointConfig.implementation_status;
        setEndpointImplementation(() => {
            return lifeCycleStatus === 'PROTOTYPED' || implType === 'prototyped' ?
                endpointImplType[1] : endpointImplType[0];
        });
    }, []);

    /**
     * Get the swagger definition if the endpoint implementation type is 'prototyped'
     * */
    useEffect(() => {
        if (endpointImplementation === 'PROTOTYPED') {
            api.getSwagger(apiObject.id).then((resp) => {
                setSwagger(resp.obj);
            }).catch((err) => {
                console.err(err);
            });
        }
    }, [endpointImplementation]);

    /**
     * Method to update the swagger object.
     *
     * @param {any} swaggerObj The updated swagger object.
     * */
    const changeSwagger = (swaggerObj) => {
        setSwagger(swaggerObj);
    };

    /**
     * Method to handle the Managed/ Prototyped endpoint selection.
     *
     * @param {any} event The option change event.
     * */
    const handleEndpointManagedChange = (event) => {
        const implOption = event.target.value;
        setEndpointImplementation(implOption);
        const tmpEndpointConfig = getEndpointConfigByImpl(implOption);
        setModifiedAPI({ ...apiObject, endpointConfig: tmpEndpointConfig });
    };

    /**
     * Generate endpoint configuration based on the selected endpoint type and set to the api object.
     *
     * @param {string} endpointType The endpoint type.
     * @param {string} implementationType The endpoint implementationType. (Required only for prototype endpoints)
     * */
    const generateEndpointConfig = (endpointType, implementationType) => {
        const config = createEndpointConfig(endpointType, implementationType);
        setModifiedAPI(() => {
            if (endpointType === 'prototype') {
                if (implementationType === 'mock') {
                    return { ...apiObject, endpointConfig: config, endpointImplementationType: 'INLINE' };
                }
                return { ...apiObject, endpointConfig: config, endpointImplementationType: 'ENDPOINT' };
            } else {
                return { ...apiObject, endpointConfig: config };
            }
        });
        setEndpointImplementation(() => {
            return apiObject.lifeCycleStatus === 'PROTOTYPED' || endpointType === 'prototyped' ?
                endpointImplType[1] : endpointImplType[0];
        });
    };

    return (
        <React.Fragment>
            {/* Since the api is set to the state in component did mount, check both the api and the apiObject. */}
            {api.endpointConfig === null && apiObject.endpointConfig === null ?
                <NewEndpointCreate generateEndpointConfig={generateEndpointConfig} /> :
                <div className={classes.root}>
                    <Grid container spacing={16}>
                        <Grid item>
                            <Typography variant='h4' align='left' className={classes.titleWrapper}>
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.Endpoints.endpoints.header'
                                    defaultMessage='Endpoints'
                                />
                            </Typography>
                        </Grid>
                        {apiObject.type === 'HTTP' ?
                            <Grid item>
                                <RadioGroup
                                    aria-label='endpointImpl'
                                    name='endpointImpl'
                                    className={classes.radioGroup}
                                    value={endpointImplementation}
                                    onChange={handleEndpointManagedChange}
                                >
                                    <FormControlLabel
                                        value='managed'
                                        control={<Radio />}
                                        label={<FormattedMessage
                                            id='Apis.Details.Endpoints.Endpoints.managed'
                                            defaultMessage='Managed'
                                        />}
                                    />
                                    <FormControlLabel
                                        value='PROTOTYPED'
                                        control={<Radio />}
                                        label={<FormattedMessage
                                            id='Apis.Details.Endpoints.Endpoints.prototyped'
                                            defaultMessage='Prototyped'
                                        />}
                                    />
                                </RadioGroup>
                            </Grid> : <div />
                        }
                    </Grid>
                    <div>
                        <Grid container>
                            <Grid item xs={12} className={classes.endpointsContainer}>
                                {endpointImplementation === 'PROTOTYPED' ?
                                    <PrototypeEndpoints
                                        implementation_method={apiObject.endpointConfig.implementation_status}
                                        api={apiObject}
                                        modifyAPI={setModifiedAPI}
                                        swaggerDef={swagger}
                                        updateSwagger={changeSwagger}
                                    /> :
                                    <EndpointOverview api={apiObject} onChangeAPI={setModifiedAPI} />
                                }
                            </Grid>
                        </Grid>
                        <Grid
                            container
                            direction='row'
                            alignItems='flex-start'
                            spacing={4}
                            className={classes.buttonSection}
                        >
                            <Grid item>
                                <Button
                                    type='submit'
                                    variant='contained'
                                    color='primary'
                                    onClick={() => saveAPI()}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Endpoints.Endpoints.save'
                                        defaultMessage='Save'
                                    />
                                </Button>
                            </Grid>
                            <Grid item>
                                <Link to={'/apis/' + api.id + '/overview'}>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.Endpoints.Endpoints.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            </Grid>
                        </Grid>
                    </div>
                </div>
            }
        </React.Fragment>

    );
}

Endpoints.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.shape({}),
        buttonSection: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Endpoints));
