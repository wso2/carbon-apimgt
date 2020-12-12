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
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import Help from '@material-ui/icons/Help';
import Tooltip from '@material-ui/core/Tooltip';
import Link from '@material-ui/core/Link';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Alert from 'AppComponents/Shared/Alert';
import Configurations from 'Config';

const useStyles = makeStyles((theme) => ({
    root: {
        marginTop: theme.spacing(4),
        marginLeft: theme.spacing(3),
        marginRight: theme.spacing(3),
        width: '100%',
    },
    helpDiv: {
        marginTop: theme.spacing(0.5),
    },
    helpIcon: {
        fontSize: 20,
    },
    horizontalDivider: {
        marginTop: theme.spacing(4),
        borderTop: '0px',
        width: '100%',
    },
    preview: {
        height: theme.spacing(18),
        marginBottom: theme.spacing(5),
        marginTop: theme.spacing(10),
    },
    spacing: {
        paddingTop: theme.spacing(5),
        paddingBottom: theme.spacing(5),
        paddingLeft: theme.spacing(10),
        paddingRight: theme.spacing(10),
    },
    buttonStyle: {
        color: theme.custom.serviceCatalog.onboarding.buttonText,
        borderColor: theme.custom.serviceCatalog.onboarding.buttonBorder,
    },
    docLinkStyle: {
        paddingLeft: theme.spacing(1),
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
        <div className={classes.root}>
            <Grid container direction='row' spacing={10}>
                <Grid item md={11}>
                    <Typography className={classes.heading} variant='h4'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.heading'
                            defaultMessage='Service Catalog'
                        />
                    </Typography>
                </Grid>
                <Grid item md={1}>
                    <Tooltip
                        placement='right'
                        title={(
                            <FormattedMessage
                                id='ServiceCatalog.Listing.Onboarding.help.tooltip'
                                defaultMessage='The Service Catalog enables API-first Integration'
                            />
                        )}
                    >
                        <div className={classes.helpDiv}>
                            <Help className={classes.helpIcon} />
                        </div>
                    </Tooltip>
                </Grid>
            </Grid>
            <hr className={classes.horizontalDivider} />
            <Grid container direction='row'>
                <Grid item md={2} />
                <Grid item md={4}>
                    <div align='center'>
                        <img
                            className={classes.preview}
                            src={Configurations.app.context + '/site/public/images/wso2-intg-service-icon.svg'}
                            alt='Get Started'
                        />
                    </div>
                    <Typography className={classes.heading} variant='h4' align='center'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.Heading1'
                            defaultMessage='Learn to write your first'
                        />
                    </Typography>
                    <Typography align='center'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.Heading1.subHeading'
                            defaultMessage='Integration Service'
                        />
                    </Typography>
                    <Typography align='center' className={classes.spacing}>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.description1'
                            defaultMessage={'From creating and publishing an API to securing, rate-limiting, addresses'
                            + ' all aspects of API Management.'}
                        />
                    </Typography>
                    <div align='center'>
                        <Link
                            target='_blank'
                            style={{ textDecoration: 'none' }}
                            href={'https://ei.docs.wso2.com/en/latest/'
                            + 'micro-integrator/develop/integration-development-kickstart/'}
                        >
                            <Button className={classes.buttonStyle} variant='outlined'>
                                <Typography className={classes.heading} variant='h6'>
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Onboarding.get.started'
                                        defaultMessage='Get Started'
                                    />
                                </Typography>
                                <OpenInNewIcon className={classes.docLinkStyle} />
                            </Button>
                        </Link>
                    </div>
                </Grid>
                <Grid item md={4}>
                    <div align='center'>
                        <img
                            className={classes.preview}
                            src={Configurations.app.context + '/site/public/images/wso2-intg-service-sample-icon.svg'}
                            alt='Add Sample Service'
                        />
                    </div>
                    <Typography className={classes.heading} variant='h4' align='center'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.Heading2'
                            defaultMessage='Add a sample'
                        />
                    </Typography>
                    <Typography align='center'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.Heading2.subHeading'
                            defaultMessage='Integration Service'
                        />
                    </Typography>
                    <Typography align='center' className={classes.spacing}>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.description2'
                            defaultMessage={'From creating and publishing an API to securing, rate-limiting, addresses'
                            + ' all aspects of API Management.'}
                        />
                    </Typography>
                    <div align='center'>
                        <Button className={classes.buttonStyle} variant='outlined' onClick={handleOnClick}>
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.add.sample.service'
                                    defaultMessage='Add Sample Service'
                                />
                            </Typography>
                        </Button>
                    </div>
                </Grid>
                <Grid item md={2} />
            </Grid>
        </div>
    );
}

export default Onboarding;
