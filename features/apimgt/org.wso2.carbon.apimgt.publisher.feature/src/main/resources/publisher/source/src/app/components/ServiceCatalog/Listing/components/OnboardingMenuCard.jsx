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
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Paper from '@material-ui/core/Paper';

const useStyles = makeStyles((theme) => ({
    cardIcons: {
        width: 190,
    },
    cardContainer: {
        height: theme.spacing(63),
    },
}));

/**
 * Service Catalog On boarding menu card
 *
 * @returns {void} Onboarding page for Services
 */
function OnboardingMenuCard(props) {
    const {
        iconSrc, heading, subHeading, description, children,
    } = props;
    const classes = useStyles();

    return (
        <Grid item xs={12} md={4} lg={3}>
            <Paper>
                <Grid
                    container
                    direction='row'
                    justify='center'
                    alignItems='flex-end'
                    className={classes.cardContainer}
                >
                    <Grid item xs={12}>
                        <Box textAlign='center' pb={2}>
                            <img
                                className={classes.cardIcons}
                                src={iconSrc}
                                alt={heading}
                                aria-hidden='true'
                            />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box
                            textAlign='center'
                            fontSize='h4.fontSize'
                            fontFamily='fontFamily'
                        >
                            {heading}
                            <Box
                                color='text.secondary'
                                fontSize='h5.fontSize'
                                fontFamily='fontFamily'
                            >
                                {subHeading}
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box
                            mx={1}
                            textAlign='center'
                            color='text.secondary'
                            fontSize='body1.fontSize'
                            fontFamily='fontFamily'
                            height={60}
                        >
                            {description}
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box justifyContent='center' mb={2} display='flex'>
                            {children}
                        </Box>
                    </Grid>
                </Grid>
            </Paper>
        </Grid>
    );
}

export default OnboardingMenuCard;
