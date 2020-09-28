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

import React, { useEffect, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { Link as RouterLink } from 'react-router-dom';
import { Card } from '@material-ui/core';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import CategoryIcon from '@material-ui/icons/Category';
import LaunchIcon from '@material-ui/icons/Launch';
import Progress from 'AppComponents/Shared/Progress';
import API from 'AppData/api';
import Configurations from 'Config';

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
    cardText: {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
}));

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function APICategoriesCard() {
    const classes = useStyles();
    const [apiCategoriesList, setApiCategoriesList] = useState();
    const [numberOfCategories, setNumberOfCategories] = useState(0);
    const restApi = new API();

    useEffect(() => {
        restApi.apiCategoriesListGet()
            .then((result) => {
                const allCategoriesList = result.body.list;
                // Slice last 4 api categories (if available) to display to maintain card height
                const displayingList = allCategoriesList.slice(Math.max(allCategoriesList.length - 4, 0));
                setApiCategoriesList(displayingList);
                setNumberOfCategories(allCategoriesList.length);
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
                    <FormattedMessage
                        id='Dashboard.apiCategories.noApiCategories.card.title'
                        defaultMessage='API Category based grouping'
                    />
                </Typography>

                <Typography variant='body2' component='p'>
                    <FormattedMessage
                        id='Dashboard.apiCategories.noApiCategories.card.description'
                        values={{
                            learnMoreLink:
    <a
        rel='noopener noreferrer'
        target='_blank'
        href={Configurations.app.docUrl
     + 'develop/customizations/customizing-the-developer-portal/'
     + 'customize-api-listing/categorizing-and-grouping-apis/'
     + 'api-category-based-grouping'}
    >
        Learn More
        <LaunchIcon fontSize='inherit' />
    </a>,
                        }}
                        defaultMessage='API categories allow API providers to categorize APIs
                        that have similar attributes. When a categorized API
                        gets published to the Developer Portal, its categories
                        appear as clickable links to the API consumers.
                        The API consumers can use the available API categories
                        to quickly jump to a category of interest. {learnMoreLink}'
                    />
                </Typography>


                <Box mt={3}>
                    <Button
                        size='small'
                        variant='contained'
                        color='primary'
                        component={RouterLink}
                        to='settings/api-categories'
                    >
                        <Typography variant='inherit'>
                            <FormattedMessage
                                id='Dashboard.apiCategories.noApiCategories.card.add.new.link.text'
                                defaultMessage='Add new Category'
                            />
                        </Typography>
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
                                <FormattedMessage
                                    id='Dashboard.apiCategories.apiCategoriesListing.card.title'
                                    defaultMessage='API Categories'
                                />
                            </Typography>
                        </Box>
                        <Box>
                            <Typography className={classes.title} gutterBottom>
                                {numberOfCategories}
                            </Typography>
                        </Box>
                    </Box>

                    <Divider light />

                    <Box height={170} mt={1} mb={-2}>
                        {apiCategoriesList.map((category) => {
                            return (
                                <Box display='flex' alignItems='center'>
                                    <Box width={50} flexGrow={1} mt={0.5}>
                                        <Typography className={classes.cardText} variant='subtitle2'>
                                            {category.name}
                                        </Typography>
                                        <Typography className={classes.cardText} variant='body2'>
                                            {category.description || (
                                                <FormattedMessage
                                                    id='Dashboard.apiCategories.apiCategoriesListing.no.description'
                                                    defaultMessage='No description available'
                                                />
                                            )}
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
                    </Box>
                </CardContent>

                <Box m={0.5} display='flex' alignSelf='flex-end' flexDirection='row-reverse'>
                    <Box>
                        <Button
                            size='small'
                            color='primary'
                            component={RouterLink}
                            to='settings/api-categories'
                        >
                            <Typography variant='inherit'>
                                <FormattedMessage
                                    id='Dashboard.apiCategories.apiCategoriesListing.card.view.all.link.text'
                                    defaultMessage='View All'
                                />
                                <LaunchIcon fontSize='inherit' />
                            </Typography>
                        </Button>
                    </Box>
                </Box>
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
