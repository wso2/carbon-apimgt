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

import React, { useState } from 'react';
import {
    Typography,
    Grid,
    withStyles,
    RadioGroup,
    FormControlLabel,
    Radio,
    FormControl,
    Divider,
    Button,
    Card,
    CardContent,
    CardActions,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';

const styles = (theme) => ({
    inlineMessageContainer: {
        marginBottom: theme.spacing(1),
    },
    endpointTypeCard: {
        margin: theme.spacing(1),
        maxWidth: theme.spacing(30),
        transition: 'box-shadow 0.3s ease-in-out',
        height: theme.spacing(40),
        display: 'flex',
        flexDirection: 'column',
    },
    cardContent: {
        height: theme.spacing(40),
    },
    content: {
        marginTop: theme.spacing(1),
    },
    cardActions: {
        justifyContent: 'flex-end',
        borderTop: 'solid #e0e0e0 thin',
    },
});

/**
 * Component to create new endpoint.
 * This component will render if the api object does not have an endpoint configuration, letting users to create a
 * new endpoint configuration based on their requirement.
 * Following endpoint types are supported.
 * 1. HTTP/ SOAP endpoints
 * 2. Prototyped/ Mock endpoints
 * 3. AWS Lambda endpoints
 * 4. Dynamic Endpoints
 *
 * @param {any} props The input props.
 * @return {any} The HTML representation of the component.
 * */
function NewEndpointCreate(props) {
    const {
        classes,
        intl,
        generateEndpointConfig,
        apiType,
    } = props;
    const [endpointImplType, setImplType] = useState('mock');
    const endpointTypes = [
        {
            type: 'http',
            name: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.http.endpoint',
                defaultMessage: 'HTTP/ REST Endpoint',
            }),
            description: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.http.endpoint.description',
                defaultMessage: 'A REST API endpoint based on a URI template.',
            }),
            options: null,
            disabled: ['SOAPTOREST'],
        },
        {
            type: 'address',
            name: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.soap.endpoint',
                defaultMessage: 'HTTP/ SOAP Endpoint',
            }),
            description: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.soap.endpoint.description',
                defaultMessage: 'The direct URI of the web service.',
            }),
            options: null,
            disabled: ['GRAPHQL', 'SSE'],
        },
        {
            type: 'prototyped',
            name: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.prototype.endpoint',
                defaultMessage: 'Prototype Endpoint',
            }),
            description: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.prototype.endpoint.description',
                defaultMessage: 'Use the inbuilt JavaScript engine to prototype the API or provide an endpoint'
                    + ' to a prototype API. The inbuilt JavaScript engine does support prototype SOAP APIs',
            }),
            options: [
                {
                    type: 'mock',
                    name: intl.formatMessage({
                        id: 'Apis.Details.Endpoints.NewEndpointCreate.prototype.implementation',
                        defaultMessage: 'Prototype Implementation',
                    }),
                    disabled: ['SOAP', 'SOAPTOREST', 'SSE'],
                },
                {
                    type: 'prototyped',
                    name: intl.formatMessage({
                        id: 'Apis.Details.Endpoints.NewEndpointCreate.default.prototype.endpoints',
                        defaultMessage: 'Prototype Endpoint',
                    }),
                    disabled: [],
                },
            ],
            disabled: ['GRAPHQL', 'SSE'],
        },
        {
            type: 'dynamic',
            name: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.dynamic.endpoint',
                defaultMessage: 'Dynamic Endpoint',
            }),
            description: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.dynamic.endpoint.description',
                defaultMessage: 'If you need to send the request to the URI specified in the TO header.',
            }),
            options: null,
            disabled: ['SSE'],
        },
        {
            type: 'awslambda',
            name: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.awslambda.endpoint',
                defaultMessage: 'AWS Lambda Endpoint',
            }),
            description: intl.formatMessage({
                id: 'Apis.Details.Endpoints.NewEndpointCreate.create.awslambda.endpoint.description',
                defaultMessage: 'If you need to invoke AWS Lambda functions through API gateway.',
            }),
            options: null,
            disabled: ['SOAPTOREST', 'GRAPHQL', 'SSE'],
        },
    ];

    const eligibleTypes = endpointTypes.filter((type) => !type.disabled.includes(apiType)).map((type) => {
        return type;
    });

    return (
        <>
            <Typography variant='h4' align='left' className={classes.titleWrapper}>
                <FormattedMessage
                    id='Apis.Details.Endpoints.NewEndpointCreate.add.endpoints.header'
                    defaultMessage='Select an Endpoint Type to Add'
                />
            </Typography>
            <Grid container justify='flex-start' spacing={2}>
                {eligibleTypes.map(((type) => {
                    return (
                        <Grid item className={classes.inlineMessageContainer}>
                            <Card className={classes.endpointTypeCard}>
                                <CardContent className={classes.cardContent}>
                                    <Typography variant='h5' component='h3' className={classes.head}>
                                        {type.name}
                                    </Typography>
                                    <Divider />
                                    <Typography component='p' className={classes.content}>
                                        {type.description}
                                    </Typography>
                                    {type.options
                                        ? (
                                            <div>
                                                <FormControl component='fieldset' className={classes.formControl}>
                                                    <RadioGroup
                                                        aria-label='EndpointType'
                                                        name='endpointType'
                                                        className={classes.radioGroup}
                                                        value={endpointImplType}
                                                        onChange={(event) => { setImplType(event.target.value); }}
                                                    >
                                                        {type.options.filter((types) => !types.disabled
                                                            .includes(apiType)).map(((option) => {
                                                            return (
                                                                <FormControlLabel
                                                                    value={option.type}
                                                                    control={<Radio color='primary' />}
                                                                    label={option.name}
                                                                />
                                                            );
                                                        }))}
                                                    </RadioGroup>
                                                </FormControl>
                                            </div>
                                        )
                                        : <div /> }
                                </CardContent>
                                <CardActions className={classes.cardActions}>
                                    <Button
                                        color='primary'
                                        className={classes.button}
                                        onClick={() => generateEndpointConfig(type.type, endpointImplType)}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Endpoints.NewEndpointCreate.create.button'
                                            defaultMessage='Add'
                                        />
                                    </Button>
                                </CardActions>
                            </Card>
                        </Grid>
                    );
                }))}
            </Grid>
        </>
    );
}

NewEndpointCreate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    generateEndpointConfig: PropTypes.func.isRequired,
    apiType: PropTypes.string.isRequired,
};

export default withStyles(styles)(injectIntl(NewEndpointCreate));
