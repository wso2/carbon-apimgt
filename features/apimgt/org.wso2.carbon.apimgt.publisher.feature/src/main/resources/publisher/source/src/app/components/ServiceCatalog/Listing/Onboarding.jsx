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
import Box from '@material-ui/core/Box';
import Link from '@material-ui/core/Link';
import Button from '@material-ui/core/Button';
import AddIcon from '@material-ui/icons/Add';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Alert from 'AppComponents/Shared/Alert';
import Configurations from 'Config';
import { getSampleServiceMeta, getSampleOpenAPI } from 'AppData/SamplePizzaShack';

const useStyles = makeStyles((theme) => ({
    root: {
        marginTop: theme.spacing(6),
        marginLeft: theme.spacing(3),
        marginRight: theme.spacing(3),
        width: '100%',
    },
    preview: {
        height: theme.spacing(10),
        marginBottom: theme.spacing(5),
        marginTop: theme.spacing(10),
    },
    spacing: {
        paddingTop: theme.spacing(5),
        paddingBottom: theme.spacing(5),
        paddingLeft: theme.spacing(10),
        paddingRight: theme.spacing(10),
    },
    docLinkStyle: {
        paddingLeft: theme.spacing(1),
        marginBottom: theme.spacing(-0.5),
    },
    addLinkStyle: {
        paddingRight: theme.spacing(1),
        marginBottom: theme.spacing(-0.5),
    },
    links: {
        cursor: 'pointer',
    },
    headingCaptionStyle: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(5),
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

    const handleOnClick = () => {
        const serviceMetadata = getSampleServiceMeta();
        const inlineContent = getSampleOpenAPI();
        const promisedService = ServiceCatalog.addService(serviceMetadata, inlineContent);
        promisedService.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Onboarding.add.sample.success',
                defaultMessage: 'Sample Service added successfully!',
            }));
            // Reload the listing page
            history.push('/service-catalog');
        }).catch(() => {
            Alert.error(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Onboarding.error.creating.sample.service',
                defaultMessage: 'Error while creating Sample Service',
            }));
        });
    };

    return (
        <div className={classes.root}>
            <Grid container>
                <Grid item xs={12} md={12}>
                    <Box textAlign='center' mb={2}>
                        <Typography variant='h6'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.onboarding.create.new'
                                defaultMessage='Service Catalog'
                            />
                        </Typography>
                    </Box>
                </Grid>
                <Grid item xs={12} md={12}>
                    <Box textAlign='center'>
                        <Typography variant='body2'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.onboarding.description'
                                defaultMessage='Enabling API-first Integration'
                            />
                        </Typography>
                    </Box>
                </Grid>
            </Grid>
            <Grid container direction='row'>
                <Grid item md={2} lg={2} />
                <Grid item xs={12} md={4} lg={4}>
                    <Box textAlign='center'>
                        <div align='center'>
                            <img
                                className={classes.preview}
                                src={Configurations.app.context + '/site/public/images/wso2-intg-service-icon.svg'}
                                alt='Get Started'
                            />
                        </div>
                        <Box>
                            <Typography variant='h6' component='div'>
                                <b>
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Onboarding.write.first.intg'
                                        defaultMessage='Write your first'
                                    />
                                </b>
                            </Typography>
                            <Typography align='center' variant='body2' className={classes.headingCaptionStyle}>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.Heading1.subHeading'
                                    defaultMessage='Integration Service'
                                />
                            </Typography>
                            <Typography align='center' variant='body2' className={classes.spacing}>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.Heading1.description'
                                    defaultMessage={'Create and Deploy your first Integration Service '
                                    + 'easily using WSO2 Micro Integrator'}
                                />
                            </Typography>
                            <Typography variant='body1'>
                                <Link
                                    target='_blank'
                                    style={{ textDecoration: 'none' }}
                                    href={'https://ei.docs.wso2.com/en/latest/'
                                    + 'micro-integrator/develop/integration-development-kickstart/'}
                                >
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Onboarding.get.started'
                                        defaultMessage='Get Started'
                                    />
                                    <OpenInNewIcon className={classes.docLinkStyle} />
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </Grid>
                <Grid item xs={12} md={4} lg={4}>
                    <Box textAlign='center'>
                        <div align='center'>
                            <img
                                className={classes.preview}
                                src={Configurations.app.context
                                    + '/site/public/images/wso2-intg-service-sample-icon.svg'}
                                alt='Add Sample Service'
                            />
                        </div>
                        <Box>
                            <Typography variant='h6' component='div'>
                                <b>
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Onboarding.add.sample'
                                        defaultMessage='Add a sample'
                                    />
                                </b>
                            </Typography>
                            <Typography align='center' variant='body2' className={classes.headingCaptionStyle}>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.Heading2.subHeading'
                                    defaultMessage='Integration Service'
                                />
                            </Typography>
                            <Typography align='center' variant='body2' className={classes.spacing}>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Onboarding.Heading1.description2'
                                    defaultMessage={'Create and Deploy the Sample Integration Service already '
                                    + 'available with WSO2 API Manager and get started in one click'}
                                />
                            </Typography>
                            <Typography variant='body1'>
                                <Button
                                    style={{ textDecoration: 'none' }}
                                    className={classes.links}
                                    onClick={handleOnClick}
                                >
                                    <AddIcon className={classes.addLinkStyle} />
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Onboarding.add.sample.service'
                                        defaultMessage='Add Sample Service'
                                    />
                                </Button>
                            </Typography>
                        </Box>
                    </Box>
                </Grid>
                <Grid item md={2} lg={2} />
            </Grid>
        </div>
    );
}

export default Onboarding;
