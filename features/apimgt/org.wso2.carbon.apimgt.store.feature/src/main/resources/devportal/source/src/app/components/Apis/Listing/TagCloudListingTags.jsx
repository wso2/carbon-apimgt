/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { useTheme, makeStyles } from '@material-ui/core/styles';
import { Link, useHistory } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import ApiTagThumb from './ApiTagThumb';

const useStyles = makeStyles(theme => ({
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {
        flexGrow: 1,
    },
    listContentWrapper: {
        padding: `0 ${theme.spacing.unit * 3}px`,
    },
    textWrapper: {
        color: theme.palette.getContrastText(theme.custom.tagWise.fixedStyles.background),
    },
    tagWiseThumbWrapper: {
        display: 'flex',
    },
    filterTitle: {
        fontWeight: 400,
        padding: theme.spacing(1, 2),
    },
}));

/**
 * Shared listing page
 *
 * @class TagCloudListing
 * @extends {Component}
 */
function TagCloudListingTags(props) {
    const classes = useStyles();
    const theme = useTheme();
    const history = useHistory();
    const {
        custom: {
            tagWise: {
                key, active, style, showAllApis,
            },
        },
    } = theme;


    const tagWiseURL = '/apis?offset=0&query=tag';
    const { allTags } = props;
    let apisTagCloudGroup = null;
    if (allTags.count !== 0) {
        if (allTags !== null) {
            apisTagCloudGroup = allTags.filter(item => active === true && item.value.split(key).length > 1);
        }
        if (apisTagCloudGroup && apisTagCloudGroup.length > 0) {
            // const tagLink = tagWiseURL + ':' + apisTagCloudGroup[0].value;
            // if (style === 'fixed-left') history.push(tagLink);
        }
    }

    /**
     *
     * @inheritdoctheme
     * @returns {React.Component} @inheritdoc
     * @memberof TagCloudListing
     */

    return apisTagCloudGroup && apisTagCloudGroup.length > 0 ? (
        style === 'fixed-left' ? (
            <React.Fragment>
                <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                    <FormattedMessage defaultMessage='Api Groups' id='Apis.Listing.TagCloudListingTags.title' />
                </Typography>
                <Divider />
                <List component='nav' aria-label='main mailbox folders'>
                    {Object.keys(apisTagCloudGroup).map((key) => {
                        return <ApiTagThumb tag={apisTagCloudGroup[key]} path={tagWiseURL} style={style} />;
                    })}
                    {showAllApis && (
                        <React.Fragment>
                            <Divider />

                            <Link to='apis/' className={classes.textWrapper}>
                                <ListItem button>
                                    <ListItemIcon>
                                        <Icon>label</Icon>
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={
                                            <FormattedMessage
                                                defaultMessage='All Apis'
                                                id='Apis.Listing.TagCloudListingTags.allApis'
                                            />
                                        }
                                    />
                                </ListItem>
                            </Link>
                        </React.Fragment>
                    )}
                </List>
            </React.Fragment>
        ) : (
            <div className={classes.tagWiseThumbWrapper}>
                {Object.keys(apisTagCloudGroup).map((key) => {
                    return <ApiTagThumb tag={apisTagCloudGroup[key]} path={tagWiseURL} style={style} />;
                })}
            </div>
        )
    ) : (
        <div className={classes.mainTitle}>
            <Typography variant='subtitle1' gutterBottom align='center'>
                <FormattedMessage
                    defaultMessage='Tags Connot be Found'
                    id='Apis.Listing.TagCloudListingTags.tagsNotFound'
                />
            </Typography>
        </div>
    );
}

TagCloudListingTags.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    allTags: PropTypes.shape({}).isRequired,
};

export default TagCloudListingTags;
