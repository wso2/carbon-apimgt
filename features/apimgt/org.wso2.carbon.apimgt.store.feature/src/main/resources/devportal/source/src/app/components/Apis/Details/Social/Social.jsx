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
    const { github_repo: github, slack_url: slack } = api.additionalProperties;
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
                {slack && (
                    <>
                        <a
                            className={classes.socialLink}
                            id='Slack'
                            href={slack}
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
                            href={github}
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
                {(slack || github) && (
                    <div className={classes.divider} />
                )}
                {showFacebook && (
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
                {showTwitter && (
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
                {showReddit && (
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
                {showEmbad && (
                    <>
                        <div className={classes.divider} />
                        {/* TODO: Fix spelling mistake ~tmkb */}
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
