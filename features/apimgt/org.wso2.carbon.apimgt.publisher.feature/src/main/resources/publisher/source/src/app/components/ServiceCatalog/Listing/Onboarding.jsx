/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { FormattedMessage, useIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import OpenInNewIcon from '@material-ui/icons/OpenInNew';
import AddIcon from '@material-ui/icons/Add';
import Button from '@material-ui/core/Button';
import Link from '@material-ui/core/Link';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';

const useStyles = makeStyles((theme) => ({
    buttonStyle: {
        paddingLeft: theme.spacing(1),
    },
    head: {
        paddingBottom: theme.spacing(2),
        fontWeight: 200,
    },
    content: {
        paddingBottom: theme.spacing(2),
    },
    contentSpacing: {
        padding: theme.spacing(3),
        paddingTop: theme.spacing(2),
        width: '100%',
    },
    buttonLeft: {
        marginRight: theme.spacing(1),
    },
}));

/**
 * Service Catalog On boarding
 *
 * @returns {void} Onboarding page for Services
 */
function Onboarding(props) {
    const classes = useStyles();
    const intl = useIntl();
    const { history } = props;

    /**
     * Function for adding a sample service
     */
    const addSample = () => {
        const promisedService = ServiceCatalog.addSampleService();
        promisedService.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Onboarding.add.sample.success',
                defaultMessage: 'Sample Service added successfully!',
            }));
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Onboarding.error.creating.sample.service',
                defaultMessage: 'Error while creating Sample Service',
            }));
        });
    };

    const handleOnClick = () => {
        addSample();
        // Reload the listing page
        history.push('/service-catalog');
    };

    return (
        <div className={classes.contentSpacing}>
            <InlineMessage type='info' height={140} elevation={0}>
                <div className={classes.contentWrapper}>
                    <Typography variant='h5' component='h3' className={classes.head}>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.welcome.msg'
                            defaultMessage='Welcome to WSO2 API Manager'
                        />
                    </Typography>
                    <Typography component='p' className={classes.content}>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.welcome.description'
                            defaultMessage={
                                'The Service Catalog enables API-First Integration.'
                                + ' Through this, integration services are made discoverable to the'
                                + ' API Management layer so that API proxies can directly be created using them.'
                            }
                        />
                    </Typography>
                    <div className={classes.actions}>
                        <Link
                            target='_blank'
                            style={{ textDecoration: 'none' }}
                            href={'https://ei.docs.wso2.com/en/latest/'
                            + 'micro-integrator/develop/integration-development-kickstart/'}
                        >
                            <Button variant='contained' size='small' color='primary' className={classes.buttonLeft}>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.get.started'
                                    defaultMessage='Get Started'
                                />
                                <OpenInNewIcon size='small' className={classes.buttonStyle} />
                            </Button>
                        </Link>
                        <Button variant='contained' size='small' color='primary' onClick={handleOnClick}>
                            <AddIcon size='small' />
                            <FormattedMessage
                                id='ServiceCatalog.Listing.Onboarding.add.sample.service'
                                defaultMessage='Add Sample Service'
                            />
                        </Button>
                    </div>
                </div>
            </InlineMessage>
        </div>
    );
}

export default Onboarding;
