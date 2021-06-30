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
import { withRouter } from 'react-router';
import { useTheme } from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import OnboardingMenuCard from 'AppComponents/Shared/Onboarding/OnboardingMenuCard';
import Onboarding from 'AppComponents/Shared/Onboarding/Onboarding';
import { PropTypes } from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import AuthManager from 'AppData/AuthManager';
import Alert from 'AppComponents/Shared/MuiAlert';

/**
 * Show Initial Welcome card if no APIs are available to list
 * Handle deploying a sample API (Create and Publish)
 *
 * @class SampleAPI
 * @extends {Component}
 */
function SampleAPI() {
    const theme = useTheme();
    const { apiproductAddIcon } = theme.custom.landingPage.icons;
    return (
        <Onboarding
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPIProduct.title'
                    defaultMessage='Let’s get started !'
                />
            )}
            subTitle={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPIProduct.description'
                    defaultMessage='Combine multiple API resources in to a single API'
                />
            )}
        >
            {AuthManager.isNotPublisher()
            && (
                <>
                    <Grid item xs={6}>
                        <Alert variant='outlined' severity='warning'>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPIProduct.creator.error'
                                defaultMessage='API is not deployed yet! Please deploy the API before trying out'
                            />
                        </Alert>
                    </Grid>
                    <Grid item xs={12} />
                </>
            )}
            <OnboardingMenuCard
                disabled={AuthManager.isNotPublisher()}
                id='itest-id-create-api-product'
                to='/api-products/create'
                name='API Product'
                iconName={apiproductAddIcon}
            />
        </Onboarding>
    );
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default withRouter(injectIntl(SampleAPI));
