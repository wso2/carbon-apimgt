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

import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
import { makeStyles } from '@material-ui/core/styles';
import { Card } from '@material-ui/core';
import CardContent from '@material-ui/core/CardContent';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import CategoryIcon from '@material-ui/icons/Category';
import LaunchIcon from '@material-ui/icons/Launch';
import Box from '@material-ui/core/Box';
import Progress from 'AppComponents/Shared/Progress';
import Divider from '@material-ui/core/Divider';

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


/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function APICategoriesCard() {
    const classes = useStyles();
    const [apiCategoriesList, setApiCategoriesList] = useState();
    const restApi = new API();

    useEffect(() => {
        restApi.apiCategoriesListGet()
            .then((result) => {
                setApiCategoriesList(result.body.list);
            })
            .catch(() => {
                setApiCategoriesList([]);
            });
    }, []);

    const noApiCategoriesCard = (
        <Card className={classes.root}>
            <CardContent>

                <Box>
                    <CategoryIcon color='secondary' style={{ fontSize: 60 }} />
                </Box>

                <Typography className={classes.title} gutterBottom>
            API Category based grouping
                </Typography>

                {/* todo make the learn more link */}
                <Typography variant='body2' component='p'>
            API categories allow API providers to categorize APIs
             that have similar attributes. When a categorized API
             gets published to the Developer Portal, its categories
             appear as clickable links to the API consumers.
             The API consumers can use the available API categories
             to quickly jump to a category of interest. Learn more
                </Typography>

                <Box mt={3}>
                    <Button
                        size='small'
                        variant='contained'
                        color='primary'
                        href='settings/api-categories'
                    >
                Add new Category
                        <LaunchIcon fontSize='inherit' />
                    </Button>
                </Box>
            </CardContent>
        </Card>
    );

    const apiCategoriesListingCard = () => {
        return (
            <Card className={classes.root} style={{ textAlign: 'left' }}>
                <CardContent>
                    <Box display='flex'>
                        <Box flexGrow={1}>
                            <Typography className={classes.title} gutterBottom>
                            API Categories
                            </Typography>
                        </Box>
                        <Box>
                            <Typography className={classes.title} gutterBottom>
                                {apiCategoriesList.length}
                            </Typography>
                        </Box>
                    </Box>

                    <Divider light />

                    {/* Listing last 4 categories on the card */}
                    {/* todoL impl to display at most 4 categories only */}
                    {apiCategoriesList.map((category) => {
                        return (
                            <Box display='flex' alignItems='center'>
                                <Box flexGrow={1}>
                                    <Typography variant='subtitle2'>
                                        {category.name}
                                    </Typography>
                                    <Typography variant='body2'>
                                        {category.description}
                                    </Typography>
                                </Box>
                                <Box>
                                    <Typography variant='body2'>
                                        {category.numberOfAPIs}
                                        {' APIs'}
                                    </Typography>
                                </Box>
                            </Box>
                        );
                    })}
                </CardContent>
                <CardActions alignItems='flex-end'>
                    <Box width={1} display='flex' flexDirection='row-reverse'>
                        <Box>
                            <Button
                                size='small'
                                color='primary'
                                href='settings/api-categories'
                            >
                        View All
                                <LaunchIcon fontSize='small' />
                            </Button>
                        </Box>
                    </Box>
                </CardActions>
            </Card>
        );
    };

    if (apiCategoriesList) {
        if (apiCategoriesList.length === 0) {
            return noApiCategoriesCard;
        } else {
            return apiCategoriesListingCard();
        }
    } else {
        return <Progress message='Loading Card ...' />;
    }
}
