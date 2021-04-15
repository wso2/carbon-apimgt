/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { useTheme } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import useMediaQuery from '@material-ui/core/useMediaQuery';

import RestAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/RestAPIMenu';
import SoapAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/SoapAPIMenu';
import GraphqlAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/GraphqlAPIMenu';
import StreamingAPIMenu from 'AppComponents/Apis/Listing/Landing/Menus/StreamingAPIMenu';

const useStyles = makeStyles({
    root: {
        flexGrow: 1,
    },
});

const APILanding = () => {
    const theme = useTheme();
    const isXsOrBelow = useMediaQuery(theme.breakpoints.down('xs'));
    const { root } = useStyles();
    const {
        graphqlIcon,
        restApiIcon,
        soapApiIcon,
        streamingApiIcon,
    } = theme.custom.landingPage.icons;

    return (
        <div className={root}>
            <Grid
                container
                direction='column'
                justify='center'
            >
                <Grid item xs={12}>
                    <Box pt={isXsOrBelow ? 2 : 7} />
                </Grid>
                <Grid item md={12}>
                    <Typography id='itest-apis-welcome-msg' display='block' gutterBottom align='center' variant='h4'>
                        <FormattedMessage
                            id='Apis.Listing.SampleAPI.SampleAPI.create.new'
                            defaultMessage='Let’s get started !'
                        />
                        <Box color='text.secondary' pt={2}>
                            <Typography display='block' gutterBottom align='center' variant='body1'>
                                <FormattedMessage
                                    id='Apis.Listing.SampleAPI.SampleAPI.create.new.description'
                                    defaultMessage='Choose your option to create an API '
                                />
                            </Typography>
                        </Box>
                    </Typography>
                </Grid>

                <Grid item xs={12}>
                    <Box pt={isXsOrBelow ? 2 : 7} pb={5} mx={isXsOrBelow ? 12 : 3}>
                        <Grid
                            container
                            direction='row'
                            justify='center'
                            alignItems='flex-start'
                            spacing={3}
                        >
                            <RestAPIMenu icon={restApiIcon} />
                            <SoapAPIMenu icon={soapApiIcon} />
                            <GraphqlAPIMenu icon={graphqlIcon} />
                            <StreamingAPIMenu icon={streamingApiIcon} />
                        </Grid>
                    </Box>
                </Grid>
            </Grid>
        </div>

    );
};

export default APILanding;
