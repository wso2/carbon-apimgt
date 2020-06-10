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
import { Card } from '@material-ui/core';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import DescriptionIcon from '@material-ui/icons/Description';
import AirplayIcon from '@material-ui/icons/Airplay';
import RssFeedIcon from '@material-ui/icons/RssFeed';
import CodeIcon from '@material-ui/icons/Code';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@material-ui/core/Link';

const useStyles = makeStyles(() => ({
    root: {
        minWidth: 275,
        minHeight: 270,
        textAlign: 'center',

    },
    title: {
        fontSize: 20,
        fontWeight: 'fontWeightBold',
    },
    pos: {
        marginBottom: 12,
    },
}));

const selectedRateLimitingPolicies = [
    {
        name: 'Advanced Policies',
        description: 'Control access per API or API resource using advanced rules',
        icon: <DescriptionIcon color='inherit' fontSize='small' />,
        path: '/throttling/advanced',
    },
    {
        name: 'Application Policies',
        description: 'Applicable per access token generated for an application',
        icon: <AirplayIcon color='inherit' fontSize='small' />,
    },
    {
        name: 'Subscription Policies',
        description: 'Control access per Subscription',
        icon: <RssFeedIcon color='inherit' fontSize='small' />,
    },
    {
        name: 'Custom Policies',
        description: 'Allows system administrators to define dynamic '
         + 'rules for specific use cases, which are applied globally across all tenants.',
        icon: <CodeIcon color='inherit' fontSize='small' />,
    },
];

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function RateLimitingCard() {
    const classes = useStyles();

    return (
        <Card className={classes.root} style={{ textAlign: 'left' }}>
            <CardContent>
                <Typography className={classes.title} gutterBottom>
                            Rate Limiting
                </Typography>

                <Divider light />

                {selectedRateLimitingPolicies.map((policy) => {
                    return (
                        <Box display='flex'>
                            <Box mx={1} mt={0.5}>
                                {policy.icon}
                            </Box>

                            <Box flexGrow={1}>
                                <Link component={RouterLink} to={policy.path} color='inherit'>
                                    <Typography>
                                        {policy.name}
                                    </Typography>
                                </Link>

                                <Typography variant='body2' gutterBottom>
                                    {policy.description}
                                </Typography>
                            </Box>
                        </Box>
                    );
                })}
            </CardContent>
        </Card>
    );
}
