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

import React, { useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Redirect from 'react-router-dom/Redirect';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import LaunchIcon from '@material-ui/icons/Launch';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import OnboardingMenuCard from 'AppComponents/ServiceCatalog/Listing/components/OnboardingMenuCard';
import Configurations from 'Config';
import { getSampleServiceMeta, getSampleOpenAPI } from 'AppData/SamplePizzaShack';

const useStyles = makeStyles((theme) => ({
    actionStyle: {
        paddingLeft: theme.spacing(4),
        paddingRight: theme.spacing(4),
    },
}));

/**
 * Service Catalog On boarding
 *
 * @returns {void} Onboarding page for Services
 */
function Onboarding() {
    const classes = useStyles();
    const intl = useIntl();
    const [deployStatus, setDeployStatus] = useState({ inprogress: false, completed: false, error: false });

    const handleOnClick = async () => {
        setDeployStatus({ inprogress: true, completed: false, error: false });
        const serviceMetadata = getSampleServiceMeta();
        const inlineContent = getSampleOpenAPI();
        try {
            await ServiceCatalog.addService(serviceMetadata, inlineContent);
        } catch (error) {
            setDeployStatus({ inprogress: false, completed: false, error });
            console.error(error);
            Alert.error(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Onboarding.error.creating.sample.service',
                defaultMessage: 'Error while creating Sample Service',
            }));
        }
        setDeployStatus({ inprogress: false, completed: true, error: false });
        Alert.info(intl.formatMessage({
            id: 'ServiceCatalog.Listing.Onboarding.add.sample.success',
            defaultMessage: 'Sample Service added successfully!',
        }));
    };
    if (deployStatus.completed && !deployStatus.error) {
        const url = '/service-catalog';
        return <Redirect to={url} />;
    }
    return (
        <Box id='itest-service-catalog-onboarding' pt={10}>
            <Grid
                container
                direction='row'
                justify='center'
                alignItems='center'
                spacing={5}
            >
                {/* Link to docs to write your first integration */}
                <OnboardingMenuCard
                    iconSrc={
                        Configurations.app.context + '/site/public/images/wso2-intg-service-sample-icon.svg'
                    }
                    heading={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.learn.heading'
                            defaultMessage='Learn to write your first'
                        />
                    )}
                    subHeading={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.learn.heading.sub'
                            defaultMessage='Integration Service'
                        />
                    )}
                    description={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.learn.heading.text'
                            defaultMessage='Create and Deploy your first Integration Service'
                        />
                    )}
                >
                    <Button
                        className={classes.actionStyle}
                        size='large'
                        variant='outlined'
                        color='primary'
                        href='https://apim.docs.wso2.com/en/4.0.0/design/create-api/create-an-api-using-a-service/'
                        target='_blank'
                        rel='noopener noreferrer'
                        endIcon={<LaunchIcon style={{ fontSize: 15 }} />}
                    >
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.learn.link'
                            defaultMessage='Get Started'
                        />
                    </Button>
                </OnboardingMenuCard>
                {/* Deploy Sample Service */}
                <OnboardingMenuCard
                    iconSrc={
                        Configurations.app.context + '/site/public/images/wso2-intg-service-icon.svg'
                    }
                    heading={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.sample.heading'
                            defaultMessage='Add a sample'
                        />
                    )}
                    subHeading={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.sample.heading.sub'
                            defaultMessage='Integration Service'
                        />
                    )}
                    description={(
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.sample.heading.text'
                            defaultMessage={'Deploy the Sample Integration Service'
                            + ' already available with WSO2 API Manager and get started in one click'}
                        />
                    )}
                >
                    <Button
                        className={classes.actionStyle}
                        size='large'
                        id='itest-services-landing-deploy-sample'
                        variant='outlined'
                        color='primary'
                        onClick={handleOnClick}
                        disabled={deployStatus.inprogress}
                    >
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Onboarding.sample.add'
                            defaultMessage='Add Sample Service'
                        />
                        {deployStatus.inprogress && <CircularProgress size={15} />}
                    </Button>
                </OnboardingMenuCard>
            </Grid>
        </Box>
    );
}

export default Onboarding;
