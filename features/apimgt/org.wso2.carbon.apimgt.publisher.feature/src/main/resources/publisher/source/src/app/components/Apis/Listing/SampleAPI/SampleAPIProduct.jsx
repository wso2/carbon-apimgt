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
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { PropTypes } from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import Create from '@material-ui/icons/Create';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import AuthManager from 'AppData/AuthManager';
import InlineMessage from 'AppComponents/Shared/InlineMessage';


const useStyles = makeStyles((theme) => ({
    head: {
        paddingBottom: theme.spacing(2),
        fontWeight: 200,
    },
    content: {
        paddingBottom: theme.spacing(2),
    },
}));

/**
 * Show Initial Welcome card if no APIs are available to list
 * Handle deploying a sample API (Create and Publish)
 *
 * @class SampleAPI
 * @extends {Component}
 */
function SampleAPI() {
    const classes = useStyles();

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
                            'The API resources in an API product can come from'
                            + ' one or more APIs, so you can mix and match resources from multiple'
                            + ' API resources to create specialized feature sets.'
                        }
                    />
                </Typography>
                {!AuthManager.isNotPublisher() && (
                    <div className={classes.actions}>
                        <Link id='itest-id-createdefault' to='/api-products/create' className={classes.links}>
                            <Button
                                size='small'
                                color='primary'
                                variant='contained'
                                className='rightAlign'
                            >

                                <Create />
                                <FormattedMessage
                                    id='Apis.Listing.SampleAPI.SampleAPIProduct.create.new.api.product'
                                    defaultMessage='Create New API Product'
                                />
                            </Button>
                        </Link>
                    </div>
                )}
            </div>
        </InlineMessage>
    );
}

SampleAPI.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default withRouter(injectIntl(SampleAPI));
