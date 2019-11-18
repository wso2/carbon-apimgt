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

import React, { useState } from 'react';
import { withRouter } from 'react-router';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import { makeStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import Icon from '@material-ui/core/Icon';
import { PropTypes } from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import queryString from 'query-string';

import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import apiProduct from './apiProduct';
import mathPayload from './math';
import calculatorPayload from './calculator';
import SampleAPIProductWizard from './SampleAPIProductWizard';

const useStyles = makeStyles(theme => ({
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
    headline: {
        paddingTop: theme.spacing(1.25),
        paddingLeft: theme.spacing(2.5),
    },
    head: {
        paddingBottom: theme.spacing(2),
        fontWeight: 200,
    },
    content: {
        paddingBottom: theme.spacing(2),
    },
    deployButton: {
        '& span.material-icons': {
            fontSize: 24,
            marginRight: theme.spacing(1),
        },
    },
}));

/**
 * Show Initial Welcome card if no APIs are available to list
 * Handle deploying a sample API (Create and Publish)
 *
 * @class SampleAPI
 * @extends {Component}
 */
function SampleAPI(prop) {
    const classes = useStyles();
    const { intl } = prop;

    const [step, setStep] = useState(0);
    const [productPath, setProductPath] = useState(null);
    /**
     * Create API Product
     * @param {*} calculatorApiId
     * @param {*} mathApiId
     */
    function createSampleProduct(calId, mathId) {
        setStep(3);
        const sampleProductPayload = apiProduct(calId, mathId);
        const productRestApi = new APIProduct();

        const productPromise = productRestApi.create(sampleProductPayload);
        productPromise
            .then((prod) => {
                setStep(4);
                setProductPath(`/api-products/${prod.body.id}/overview`);
                Alert.info(intl.formatMessage({
                    id: 'Apis.Listing.SampleAPI.SampleAPIProduct.successful',
                    defaultMessage: 'Sample CalculatorAPIProduct published successfully',
                }));
            })
            .catch((error) => {
                setStep(0);
                this.setState({ deploying: false });
                Alert.error(error);
            });
    }

    /**
     *  Check apis before create them
     * @param {*} api
     */
    function search(api) {
        const composeQuery = '?query=name:' + api.name;
        const composeQueryJSON = queryString.parse(composeQuery);
        composeQueryJSON.limit = 1;
        composeQueryJSON.offset = 0;
        return API.search(composeQueryJSON);
    }


    /**
     *Handle onClick event for `Deploy Sample API` Button
     * @memberof SampleAPI
     */
    const handleDeploySample = () => {
        setStep(1);
        const calculatorSearch = search(calculatorPayload);
        const mathApiSearch = search(mathPayload);
        Promise.all([calculatorSearch, mathApiSearch]).then(([calResponse, mathResponse]) => {
            const calAPI = calResponse.body.list.find(api => api.name === calculatorPayload.name);
            const mathAPI = mathResponse.body.list.find(api => api.name === mathPayload.name);
            let promisedCalAPI;
            let promisedMathAPI;
            if (!calAPI) {
                promisedCalAPI = new API(calculatorPayload).save();
            } else {
                promisedCalAPI = Promise.resolve(calAPI);
            }
            if (!mathAPI) {
                promisedMathAPI = new API(mathPayload).save();
            } else {
                promisedMathAPI = Promise.resolve(mathAPI);
            }
            Promise.all([promisedCalAPI, promisedMathAPI]).then(([calculatorAPI, MathAPI]) =>
                createSampleProduct(calculatorAPI.id, MathAPI.id));
        });
    };

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof SampleAPI
     */

    return (
        <InlineMessage type='info' height={140}>
            <div className={classes.contentWrapper}>
                <Typography variant='h5' component='h3' className={classes.head}>
                    <FormattedMessage
                        id='Apis.Listing.SampleAPIProduct.manager'
                        defaultMessage='Welcome to WSO2 API Manager'
                    />
                </Typography>
                <Typography component='p' className={classes.content}>
                    <FormattedMessage
                        id='Apis.Listing.SampleAPIProduct.description'
                        defaultMessage={
                            'The API resources in an API product can come from' +
                            ' one or more APIs, so you can mix and match resources from multiple' +
                            ' API resources to create specialized feature sets.'
                        }
                    />
                </Typography>
                <div className={classes.actions}>
                    <SampleAPIProductWizard step={step} setStep={setStep} productPath={productPath} />
                    <Button
                        size='small'
                        color='primary'
                        disabled={step !== 0}
                        variant='outlined'
                        onClick={handleDeploySample}
                        className={classes.deployButton}
                    >
                        <Icon>play_circle_outline</Icon>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPIProduct.deploy.button'
                            defaultMessage='Deploy Sample API Product'
                        />
                        {step !== 0 && <CircularProgress size={24} className={classes.buttonProgress} />}
                    </Button>
                </div>
            </div>
        </InlineMessage>
    );
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default withRouter(injectIntl(SampleAPI));
