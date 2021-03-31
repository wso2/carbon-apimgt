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
import PropTypes from 'prop-types';
import { useTheme, makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import { FormattedMessage } from 'react-intl';
import APICategoryThumb from './APICategoryThumb';

const useStyles = makeStyles((theme) => ({
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {
        flexGrow: 1,
    },
    listContentWrapper: {
        padding: `0 ${theme.spacing(3)}px`,
    },
    textWrapper: {
        color: theme.custom.tagCloud.leftMenu.color,
        '& .material-icons': {
            color: theme.custom.tagCloud.leftMenu.color,
        },
    },
    tagWiseThumbWrapper: {
        display: 'flex',
    },
    filterTitle: {
        fontWeight: 200,
        paddingLeft: theme.spacing(2),
        background: theme.custom.tagCloud.leftMenu.titleBackground,
        color: theme.palette.getContrastText(theme.custom.tagCloud.leftMenu.titleBackground),
        height: theme.custom.infoBar.height,
        alignItems: 'center',
        display: 'flex',
    },
}));

/**
 * Shared listing page
 *
 * @class CategoryListingCategories
 * @extends {Component}
 */
function CategoryListingCategories(props) {
    const classes = useStyles();
    const theme = useTheme();
    const {
        custom: {
            tagWise: {
                style,
            },
        },
    } = theme;


    const tagWiseURL = '/apis?offset=0&query=api-category';
    const { allCategories } = props;

    /**
     *
     * @inheritdoctheme
     * @returns {React.Component} @inheritdoc
     * @memberof TagCloudListing
     */

    return allCategories && allCategories.length > 0 ? (
        (
            <>
                <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                    <FormattedMessage defaultMessage='API Categories' id='Apis.Listing.CategoryListingCategories.title' />
                </Typography>
                <List component='nav' aria-label='main mailbox folders'>
                    {Object.keys(allCategories).map((key) => {
                        return <APICategoryThumb key={key} category={allCategories[key]} path={tagWiseURL} style={style} />;
                    })}
                </List>
            </>
        )
    ) : (
        <div className={classes.mainTitle}>
            <Typography variant='subtitle1' gutterBottom align='center'>
                <FormattedMessage
                    defaultMessage='Categories cannot be found'
                    id='Apis.Listing.CategoryListingCategories.categoriesNotFound'
                />
            </Typography>
        </div>
    );
}

CategoryListingCategories.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    allTags: PropTypes.shape({}).isRequired,
    allCategories: PropTypes.shape({}).isRequired,
};

export default CategoryListingCategories;
