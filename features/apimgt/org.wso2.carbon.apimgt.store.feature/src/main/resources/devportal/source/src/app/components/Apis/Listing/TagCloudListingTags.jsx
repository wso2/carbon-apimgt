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
import React from 'react';
import PropTypes from 'prop-types';
import { useTheme, makeStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import { FormattedMessage } from 'react-intl';
import classNames from 'classnames';
import ApiTagThumb from './ApiTagThumb';

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
    linkTextWrapper: {
        color: theme.palette.primary.main,
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
    mainPageList: {
        display: 'flex',
    },
    mainPageAllApis: {
        width: '100%',
    },
}));

/**
 * Tag cloud listing tags
 * @param {JSON} props properties passed from parent.
 * @returns {void}
 */
function TagCloudListingTags(props) {
    const classes = useStyles();
    const theme = useTheme();
    const {
        custom: {
            tagWise: {
                key, active, style, showAllApis,
            },
        },
    } = theme;


    const tagWiseURL = '/apis?offset=0&query=tag';
    const { allTags, mainPage } = props;
    let apisTagCloudGroup = null;

    if (allTags.count !== 0) {
        if (allTags !== null) {
            apisTagCloudGroup = allTags.filter((item) => active === true && item.value.split(key).length > 1);
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
        <>
            {!mainPage && (
                <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                    <FormattedMessage defaultMessage='API Groups' id='Apis.Listing.TagCloudListingTags.title' />
                </Typography>
            )}
            <List component='nav' aria-label='main mailbox folders' className={classNames({ [classes.mainPageList]: mainPage })}>
                {Object.keys(apisTagCloudGroup).map((keyInner) => {
                    return (
                        <ApiTagThumb
                            key={keyInner}
                            tag={apisTagCloudGroup[keyInner]}
                            path={tagWiseURL}
                            style={style}
                            mainPage={mainPage}
                        />
                    );
                })}
            </List>
            {showAllApis && (
                <div className={classNames({ [classes.mainPageAllApis]: mainPage })}>
                    <Divider />

                    <Link to='apis/' className={classes.textWrapper}>
                        <ListItem button>
                            <ListItemIcon>
                                <Icon>label</Icon>
                            </ListItemIcon>
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        defaultMessage='All Apis'
                                        id='Apis.Listing.TagCloudListingTags.allApis'
                                    />
                                )}
                            />
                        </ListItem>
                    </Link>
                </div>
            )}
        </>

    ) : (
        <>
            {!mainPage && (
                <Typography variant='h6' gutterBottom className={classes.filterTitle}>
                    <FormattedMessage defaultMessage='API Groups' id='Apis.Listing.TagCloudListingTags.title' />
                </Typography>
            )}
            <div className={classes.mainTitle}>
                <Typography variant='subtitle1' gutterBottom align='center'>
                    <FormattedMessage
                        defaultMessage='API groups cannot be found'
                        id='Apis.Listing.TagCloudListingTags.tagsNotFound'
                    />
                </Typography>
                <Link to='apis/' className={classes.linkTextWrapper}>
                    <Typography variant='subtitle1' gutterBottom align='center'>
                        <FormattedMessage
                            defaultMessage='All Apis'
                            id='Apis.Listing.TagCloudListingTags.allApis'
                        />
                    </Typography>
                </Link>
            </div>
        </>
    );
}

TagCloudListingTags.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    allTags: PropTypes.shape({}).isRequired,
};

export default TagCloudListingTags;
