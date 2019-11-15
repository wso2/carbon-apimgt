/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
    FormControl,
    Grid,
    Paper,
    RadioGroup,
    FormControlLabel,
    Radio,
    withStyles, Typography,
} from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import InlineEndpoints from 'AppComponents/Apis/Details/Endpoints/Prototype/InlineEndpoints';
import GenericEndpoint from 'AppComponents/Apis/Details/Endpoints/GenericEndpoint';
import cloneDeep from 'lodash.clonedeep';

const endpointImplementationTypes = [{ key: 'INLINE', value: 'Inline' }, { key: 'ENDPOINT', value: 'Endpoint' }];

const styles = theme => ({
    prototypeEndpointSelectorWrapper: {
        padding: theme.spacing(2),
    },
    prototypeEndpointsWrapper: {
        marginTop: theme.spacing(1),
        width: '100%',
    },
    genericEndpointWrapper: {
        padding: theme.spacing(1),
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
    },
    implementationMethodHeader: {
        fontWeight: 900,
    },
});

/**
 * The prototype endpoints component.
 * @param {any} props The input props.
 * @return {any} The HTML representation of the prototype endpoints.
 * */
function PrototypeEndpoints(props) {
    const {
        api,
        modifyAPI,
        classes,
        swaggerDef,
        updateSwagger,
    } = props;
    const [endpointImplementationType, setImplementationType] = useState('endpoint');
    const [endpointUrl, setEndpointUrl] = useState('http://localhost');

    useEffect(() => {
        setImplementationType(api.endpointImplementationType);
        setEndpointUrl(api.endpointConfig.production_endpoints.url);
    }, [props]);

    /**
     * Method to handle the implementation type change event. (inline, endpoint)
     * @param {any} event The change event.
     * */
    const handleImplementationTypeChange = (event) => {
        const tmpApi = cloneDeep(api);
        modifyAPI({ ...tmpApi, endpointImplementationType: event.target.value });
    };

    /**
     * Method to edit the prototype endpoint.
     * @param {number} index The endpoint index. (Not used)
     * @param {string} category The endpoint category. (Not used)
     * @param {string} serviceUrl The endpoint string.
     * */
    const editPrototypeEndpoint = (index, category, serviceUrl) => {
        const tmpApi = JSON.parse(JSON.stringify(api));
        const { endpointConfig } = tmpApi;
        endpointConfig.production_endpoints = { url: serviceUrl };
        endpointConfig.sandbox_endpoints = { url: serviceUrl };
        modifyAPI({ ...tmpApi, endpointConfig });
    };

    /**
     * Method to update the resource paths object in the swagger.
     * @param {any} paths The updated paths object.
     * */
    const updatePaths = (paths) => {
        updateSwagger({ ...swaggerDef, paths });
    };
    return (
        <React.Fragment>
            <Grid container direction='column'>
                <Paper>
                    <Grid item className={classes.prototypeEndpointSelectorWrapper}>
                        <Typography className={classes.implementationMethodHeader}>
                            <FormattedMessage
                                id={'Apis.Details.Endpoints.Prototype.PrototypeEndpoints' +
                                '.endpoint.implementation.method'}
                                defaultMessage='Endpoint Implementation Method'
                            />
                        </Typography>
                        <FormControl component='fieldset'>
                            <RadioGroup
                                aria-label='gender'
                                name='gender1'
                                className={classes.radioGroup}
                                value={endpointImplementationType}
                                onChange={handleImplementationTypeChange}
                            >
                                <FormControlLabel
                                    value={endpointImplementationTypes[0].key}
                                    control={<Radio color='primary' />}
                                    label={<FormattedMessage
                                        id='Apis.Details.Endpoints.Prototype.PrototypeEndpoints.mock'
                                        defaultMessage='Mock'
                                    />}
                                />
                                <FormControlLabel
                                    value={endpointImplementationTypes[1].key}
                                    control={<Radio color='primary' />}
                                    label={<FormattedMessage
                                        id='Apis.Details.Endpoints.Prototype.PrototypeEndpoints.endpoint'
                                        defaultMessage='Endpoint'
                                    />}
                                />
                            </RadioGroup>
                        </FormControl>
                    </Grid>
                </Paper>
                <Grid item className={classes.prototypeEndpointsWrapper}>
                    {endpointImplementationType === endpointImplementationTypes[0].key ?
                        <InlineEndpoints paths={swaggerDef.paths} updatePaths={updatePaths} /> :
                        <Paper className={classes.genericEndpointWrapper}>
                            <GenericEndpoint
                                endpointURL={endpointUrl}
                                type='prototyped'
                                index={0}
                                category='prototyped'
                                editEndpoint={editPrototypeEndpoint}
                                setAdvancedConfigOpen={null}
                                apiId={api.id}
                            />
                        </Paper>
                    }
                </Grid>
            </Grid>
        </React.Fragment>);
}

PrototypeEndpoints.propTypes = {
    api: PropTypes.shape({}).isRequired,
    modifyAPI: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    swaggerDef: PropTypes.shape({}).isRequired,
    updateSwagger: PropTypes.func.isRequired,
};

export default withStyles(styles)(PrototypeEndpoints);
