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
import React, { useContext, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import LaunchIcon from '@material-ui/icons/Launch';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import ApiThumb from 'AppComponents/Apis/Listing/ApiThumb';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import StarRatingSummary from 'AppComponents/Apis/Details/StarRatingSummary';
import Social from 'AppComponents/Apis/Details/Social/Social';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import AuthManager from 'AppData/AuthManager';

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
        height: '100vh',
        paddingLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
    },
    linkTitle: {
        color: '#424242',
    },
    description: {
        color: '#707070',
    },
    textLabel: {
        fontWeight: 500,
        color: '#42424299',
    },
    apiThumb: {
        padding: theme.spacing(1),
        border: 'solid 1px',
        borderColor: '#42424299',
    },
}));
/**
 * @returns {JSX} overview section
 */
function Overview() {
    const { intl } = useIntl();
    const { api, subscribedApplications } = useContext(ApiContext);
    const [descriptionHidden, setDescriptionHidden] = useState(true);
    const [notFound, setNotFound] = useState(false);
    const [rating, setRating] = useState({
        avgRating: 0,
        total: 0,
        count: 0,
    });
    const classes = useStyles();
    const theme = useTheme();
    const {
        custom: {
            leftMenu: { position },
            infoBar: { showThumbnail },
            tagWise: { key, active },
            social: { showRating },
        },
    } = theme;

    // Truncating the description
    let descriptionIsBig = false;
    let smallDescription = '';
    if (api.description) {
        const limit = 40;
        if (api.description.split(' ').length > limit) {
            const newContent = api.description.split(' ').slice(0, limit);
            smallDescription = newContent.join(' ') + '...';
            descriptionIsBig = true;
        }
    }

    /**
     * @param {event} e click event
     */
    const collapseAllDescription = (e) => {
        e.preventDefault();
        setDescriptionHidden(!descriptionHidden);
    };

    /**
     * @returns {string} provider
     */
    const getProvider = () => {
        let { provider } = api;
        if (
            api.businessInformation
            && api.businessInformation.businessOwner
            && api.businessInformation.businessOwner.trim() !== ''
        ) {
            provider = api.businessInformation.businessOwner;
        }
        return provider;
    };
    /**
     * @param {number} ratings rating value
     */
    const setRatingUpdate = (ratingLocal) => {
        if (ratingLocal) {
            const { avgRating, total, count } = ratingLocal;
            setRating({ avgRating, total, count });
        }
    };

    const user = AuthManager.getUser();
    if (notFound) {
        return (
            <ResourceNotFound message={intl.formattedMessage({
                id: 'Apis.Details.Overview.not.found.message',
                defaultMessage: 'Resource Not Found',
            })}
            />
        );
    }
    return (
        <Paper className={classes.root}>
            <Grid container>
                <Grid item xs={8}>
                    <Box display='flex' flexDirection='row'>
                        {showThumbnail && (
                            <Box id='overview-thumbnail' width={86} display='flex' alignItems='center'>
                                <Box className={classes.apiThumb}>
                                    <ApiThumb
                                        api={api}
                                        customWidth={70}
                                        customHeight={50}
                                        showInfo={false}
                                    />
                                </Box>
                            </Box>
                        )}
                        <Box ml={1} mr={2}>
                            <Link to={'/apis/' + api.id + '/overview'} className={classes.linkTitle}>
                                <Typography variant='h4' component='div'>{api.name}</Typography>
                            </Link>
                            {api.description && (
                                <Typography variant='body2' gutterBottom align='left' className={classes.description}>
                                    {(descriptionIsBig && descriptionHidden) ? smallDescription : api.description}
                                    {descriptionIsBig && (
                                        <a onClick={collapseAllDescription} href='#'>
                                            {descriptionHidden ? 'more' : 'less'}
                                        </a>
                                    )}
                                </Typography>
                            )}
                            <Box display='flex' flexDirection='row'>
                                <Typography variant='body2' gutterBottom align='left' className={classes.textLabel}>
                                    <FormattedMessage
                                        id='Apis.Details.InfoBar.list.version'
                                        defaultMessage='Version : '
                                    />
                                </Typography>
                                {' '}
                                <Typography variant='body2' gutterBottom align='left'>
                                    {api.version}
                                </Typography>
                                <VerticalDivider height={20} />
                                <Typography variant='body2' gutterBottom align='left' className={classes.textLabel}>
                                    <FormattedMessage
                                        id='Apis.Details.InfoBar.list.provider'
                                        defaultMessage='By : '
                                    />
                                </Typography>
                                {' '}
                                <Typography variant='body2' gutterBottom align='left'>
                                    {getProvider(api)}
                                </Typography>
                            </Box>
                        </Box>
                    </Box>
                </Grid>
                <Grid item xs={4}>
                    {!api.advertiseInfo.advertised && user && showRating && (
                        <>
                            <StarRatingSummary avgRating={rating.avgRating} reviewCount={rating.total} returnCount={rating.count} />
                            <VerticalDivider height={30} />
                            <StarRatingBar
                                apiId={api.id}
                                isEditable
                                showSummary={false}
                                setRatingUpdate={setRatingUpdate}
                            />
                        </>
                    )}
                    {api.advertiseInfo.advertised && (
                        <>
                            <a
                                target='_blank'
                                rel='noopener noreferrer'
                                href={api.advertiseInfo.originalStoreUrl}
                                className={classes.viewInPubStoreLauncher}
                            >
                                <div>
                                    <LaunchIcon />
                                </div>
                                <div className={classes.linkText}>Visit Publisher Dev Portal</div>
                            </a>
                            <VerticalDivider height={70} />
                        </>
                    )}
                    <Social />
                </Grid>
            </Grid>
        </Paper>
    );
}

export default Overview;
