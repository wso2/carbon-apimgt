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
import { injectIntl } from 'react-intl';
import { app } from 'Settings';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import EmbedCode from 'AppComponents/Apis/Details/Social/EmbedCode';
import MailOutlineIcon from '@material-ui/icons/MailOutline';

const useStyles = makeStyles((theme) => ({
    socialLink: {
        display: 'inline-block',
        '& img': {
            width: 32,
            marginRight: theme.spacing(1),
        },
    },
    oneFlex: {
        flex: 1,
    },
    socialLinkWrapper: {
        marginTop: 16,
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
    const [slack, github] = [
        api.additionalProperties.find((prop) => prop.name === 'slack_url'),
        api.additionalProperties.find((prop) => prop.name === 'github_repo'),
    ];
    const {
        custom: {
            social: {
                showSharing: {
                    active, showFacebook, showReddit, showTwitter, showEmbed, showEmail,
                },
            },
        },
    } = theme;
    return (
        <>
            <div className={classes.oneFlex} />
            <div className={classes.socialLinkWrapper}>
                {slack && (
                    <>
                        <a
                            className={classes.socialLink}
                            id='Slack'
                            href={slack.value}
                            target='_blank'
                            rel='noopener noreferrer'
                            title='Slack'
                        >
                            <img
                                src={`${app.context}/site/public/images/social/slack.png`}
                                alt='Slack'
                            />
                        </a>
                    </>
                )}
                {github && (
                    <>
                        <a
                            className={classes.socialLink}
                            id='github'
                            href={github.value}
                            target='_blank'
                            rel='noopener noreferrer'
                            title='GitHub'
                        >
                            <img
                                src={`${app.context}/site/public/images/social/github.jpg`}
                                alt='GitHub'
                            />
                        </a>
                    </>
                )}
                {active && (slack || github) && (
                    <div className={classes.divider} />
                )}
                {active && showFacebook && (
                    <a
                        className={classes.socialLink}
                        id='facebook'
                        href={`http://www.facebook.com/sharer.php?u=${apiUrl}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title='Facebook'
                    >
                        <img
                            src={`${app.context}/site/public/images/social/facebook.png`}
                            alt='Facebook'
                        />
                    </a>
                )}
                {/* Twitter */}
                {active && showTwitter && (
                    <a
                        className={classes.socialLink}
                        id='Twitter'
                        href={`http://twitter.com/share?url=${apiUrl}&text=${apiName}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title='Twitter'
                    >
                        <img
                            src={`${app.context}/site/public/images/social/twitter.png`}
                            alt='Twitter'
                        />
                    </a>
                )}
                {/* Reddit */}
                {active && showReddit && (
                    <a
                        className={classes.socialLink}
                        id='Reddit'
                        href={`http://www.reddit.com/submit?url=${apiUrl}&title=${apiName}`}
                        target='_blank'
                        rel='noopener noreferrer'
                        title='Reddit'
                    >
                        <img
                            src={`${app.context}/site/public/images/social/reddit.png`}
                            alt='Reddit'
                        />
                    </a>
                )}
                {active && showEmbed && (
                    <>
                        <div className={classes.divider} />
                        {/* TODO: Fix spelling mistake ~tmkb */}
                        <EmbedCode />
                    </>
                )}
                {active && showEmail && (
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
