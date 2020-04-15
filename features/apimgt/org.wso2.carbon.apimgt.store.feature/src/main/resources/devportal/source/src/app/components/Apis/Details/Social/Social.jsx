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

import React, { useContext } from 'react';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import { app } from 'Settings';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import EmbadCode from 'AppComponents/Apis/Details/Social/EmbadCode';
import MailOutlineIcon from '@material-ui/icons/MailOutline';

const useStyles = makeStyles((theme) => ({
    socialLink: {
        display: 'inline-block',
        '& img': {
            width: 32,
            marginLeft: theme.spacing(1),
        },
    },
    oneFlex: {
        flex: 1,
    },
    socialLinkWrapper: {
        display: 'flex',
        alignItems: 'center',
        paddingRight: theme.spacing(2),
        '& > div': {
            display: 'inline-block',
        },
    },
    divider: {
        display: 'inline-block',
        borderRight: 'solid 1px #ccc',
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        height: 30,
    },
    codeIcon: {
        cursor: 'pointer',
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
    },
}));

/**
 * Render the social icons
 * @param {int} num1 The first number.
 * @param {int} num2 The second number.
 * @returns {int} The sum of the two numbers.
 */
function Social() {
    const classes = useStyles();
    const { api } = useContext(ApiContext);
    const { name: apiName } = api;
    const apiUrl = encodeURI(window.location);
    const theme = useTheme();
    const {
        custom: {
            social: {
                showSharing: {
                    active, showFacebook, showReddit, showTwitter, showEmbad, showEmail,
                },
            },
        },
    } = theme;
    if (!active) {
        return <span />;
    }
    return (
        <>
            <div className={classes.oneFlex} />
            <div className={classes.socialLinkWrapper}>
                {/* Facebook */}
                {showFacebook && (
                    <a
                        className={classes.socialLink}
                        id='facebook'
                        href={`http://www.facebook.com/sharer.php?u=${apiUrl}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Social.Social.facebook'
                                defaultMessage='Facebook'
                            />
                        )}
                    >
                        <img
                            src={`${app.context}/site/public/images/social/facebook.png`}
                            alt={(
                                <FormattedMessage
                                    id='Apis.Details.Social.Social.facebook'
                                    defaultMessage='Facebook'
                                />
                            )}
                        />
                    </a>
                )}
                {/* Twitter */}
                {showTwitter && (
                    <a
                        className={classes.socialLink}
                        id='facebook'
                        href={`http://twitter.com/share?url=${apiUrl}&text=${apiName}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Social.Social.twitter'
                                defaultMessage='Twitter'
                            />
                        )}
                    >
                        <img
                            src={`${app.context}/site/public/images/social/twitter.png`}
                            alt={(
                                <FormattedMessage
                                    id='Apis.Details.Social.Social.twitter'
                                    defaultMessage='Twitter'
                                />
                            )}
                        />
                    </a>
                )}
                {/* Reddit */}
                {showReddit && (
                    <a
                        className={classes.socialLink}
                        id='facebook'
                        href={`http://www.reddit.com/submit?url=${apiUrl}&title=${apiName}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Social.Social.reddit'
                                defaultMessage='Reddit'
                            />
                        )}
                    >
                        <img
                            src={`${app.context}/site/public/images/social/reddit.png`}
                            alt={(
                                <FormattedMessage
                                    id='Apis.Details.Social.Social.reddit'
                                    defaultMessage='Reddit'
                                />
                            )}
                        />
                    </a>
                )}
                {showEmbad && (
                    <>
                        <div className={classes.divider} />
                        <EmbadCode />
                    </>
                )}
                {showEmail && (
                    <>
                        <div className={classes.divider} />
                        <a href={`mailto:?Subject=${apiName}&body=Link+:+${apiUrl}"`} className={classes.codeIcon}>
                            <MailOutlineIcon />
                        </a>
                    </>
                )}
            </div>
        </>
    );
}

export default injectIntl(Social);
