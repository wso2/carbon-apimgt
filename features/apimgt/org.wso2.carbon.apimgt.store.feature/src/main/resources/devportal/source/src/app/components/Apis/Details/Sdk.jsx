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

import Button from '@material-ui/core/Button';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardMedia from '@material-ui/core/CardMedia';
import Icon from '@material-ui/core/Icon';
import Divider from '@material-ui/core/Divider';
import Grid from '@material-ui/core/Grid';
import JSFileDownload from 'js-file-download';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import { withStyles, withTheme } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import AuthManager from 'AppData/AuthManager';
import { app } from 'Settings';
import Api from '../../../data/api';

const styles = theme => ({
    genericMessageWrapper: {
        margin: theme.spacing(2),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
});

/**
 *
 *
 * @class Sdk
 * @extends {React.Component}
 */
class Sdk extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            sdkLanguages: null,
            items: null,
        };
        const { match, apiId } = this.props;
        this.api_uuid = match ? match.params.apiUuid : apiId;
        this.filter_threshold = 5;
        this.getSdkForApi = this.getSdkForApi.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.addDefaultSrc = this.addDefaultSrc.bind(this);
    }

    /**
     *
     *
     * @memberof Sdk
     */
    componentDidMount() {
        const api = new Api();
        const user = AuthManager.getUser();
        if (user != null) {
            const promised_languages = api.getSdkLanguages();

            promised_languages
                .then((response) => {
                    if (response.obj.length === 0) {
                        this.setState({ sdkLanguages: false });
                        return;
                    }
                    this.setState({ sdkLanguages: response.obj });
                    this.setState({ items: response.obj });
                })
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
        }
    }

    /**
     * Call the REST API to generate the SDK
     *
     * @param {*} apiId
     * @param {*} language
     * @memberof Sdk
     */
    getSdkForApi(apiId, language) {
        const api = new Api();
        const promised_sdk = api.getSdk(apiId, language);

        promised_sdk
            .then((response) => {
                const sdkZipName = response.headers['content-disposition'].match(/filename="(.+)"/)[1];
                const sdkZip = response.data;
                // Prompt to download zip file for the SDK
                JSFileDownload(sdkZip, sdkZipName);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 400) {
                    this.setState({ badRequest: true });
                } else if (status === 500) {
                    this.setState({ serverError: true });
                }
            });
    }

    /**
     * Handle the click event of the download button
     *
     * @memberof Sdk
     */
    handleClick = (event, item) => {
        const apiId = this.api_uuid;
        const language = item;
        this.getSdkForApi(apiId, language);
    };

    /**
     * Handle the change event of the Search input field
     *
     * @memberof Sdk
     */
    handleChange = (event) => {
        let updatedList = this.state.sdkLanguages;
        updatedList = updatedList.filter((item) => {
            return item.toLowerCase().search(event.target.value.toLowerCase()) !== -1;
        });
        this.setState({ items: updatedList });
    };

    /**
     * Handle sdk image not found issue. Point to a default image
     */
    addDefaultSrc = (ev) => {
        ev.target.src = app.context + '/site/public/images/sdks/default.svg';
    };

    /**
     *
     *
     * @returns
     * @memberof Sdk
     */
    render() {
        const languageList = this.state.items;
        const {
            onlyIcons, intl, classes, theme,
        } = this.props;
        const {
            custom: {
                apiDetailPages: { onlyShowSdks },
            },
        } = theme;
        const filteredLanguageList =
        languageList && languageList.length > 0 && onlyShowSdks && onlyShowSdks.length > 0
                ? languageList.filter(lang => onlyShowSdks.includes(lang.toLowerCase()))
                : languageList;
        if (onlyIcons) {
            return (
                filteredLanguageList && (
                    <React.Fragment>
                        {filteredLanguageList.map((language, index) =>
                            index < 3 && (
                                <Grid item xs={4} key={index}>
                                    <a
                                        onClick={event => this.handleClick(event, language)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <img
                                            alt={language}
                                            src={
                                                app.context +
                                                    '/site/public/images/sdks/' +
                                                    new String(language) +
                                                    '.svg'
                                            }
                                            style={{
                                                width: 80,
                                                height: 80,
                                                margin: 10,
                                            }}
                                        />
                                    </a>
                                </Grid>
                            ))}
                    </React.Fragment>
                )
            );
        }
        return (
            <React.Fragment>
                <Typography variant='h4' className={classes.titleSub}>
                    <FormattedMessage id='Apis.Details.Sdk.title' defaultMessage='Software Development Kits (SDKs)' />
                </Typography>
                {filteredLanguageList ? (
                    <Grid container className='tab-grid' spacing={0}>
                        <Grid item xs={12} sm={6} md={9} lg={9} xl={10}>
                            {this.state.sdkLanguages.length >= this.filter_threshold && (
                                <Grid item style={{ textAlign: 'left', margin: '14px' }}>
                                    <TextField
                                        id='search'
                                        label={intl.formatMessage({
                                            defaultMessage: 'Search SDK',
                                            id: 'Apis.Details.Sdk.search.sdk',
                                        })}
                                        type='text'
                                        margin='normal'
                                        name='searchSdk'
                                        onChange={this.handleChange}
                                    />
                                </Grid>
                            )}
                            <Grid container justify='flex-start' spacing={4}>
                                {filteredLanguageList.map((language, index) => (
                                    <Grid key={index} item key={index}>
                                        <div style={{ width: 'auto', textAlign: 'center', margin: '10px' }}>
                                            <Card>
                                                <div>{language.toString().toUpperCase()}</div>
                                                <Divider />
                                                <CardMedia
                                                    title={language.toString().toUpperCase()}
                                                    src={
                                                        '/devportal/site/public/images/sdks/' +
                                                        new String(language) +
                                                        '.svg'
                                                    }
                                                >
                                                    <img
                                                        alt={language}
                                                        onError={this.addDefaultSrc}
                                                        src={`/devportal/site/public/images/sdks/${language}.svg`}
                                                        style={{ width: '100px', height: '100px', margin: '30px' }}
                                                    />
                                                </CardMedia>
                                                <CardActions>
                                                    <Grid container justify='center'>
                                                        <Button
                                                            color='secondary'
                                                            onClick={event => this.handleClick(event, language)}
                                                        >
                                                            <Icon>arrow_downward</Icon>
                                                            {'Download'}
                                                        </Button>
                                                    </Grid>
                                                </CardActions>
                                            </Card>
                                        </div>
                                    </Grid>
                                ))}
                            </Grid>
                        </Grid>
                    </Grid>
                ) : (
                    <div className={classes.genericMessageWrapper}>
                        <InlineMessage type='info' className={classes.dialogContainer}>
                            <Typography variant='h5' component='h3'>
                                <FormattedMessage id='Apis.Details.Sdk.no.sdks' defaultMessage='No SDKs' />
                            </Typography>
                            <Typography component='p'>
                                <FormattedMessage
                                    id='Apis.Details.Sdk.no.sdks.content'
                                    defaultMessage='No SDKs available for this API'
                                />
                            </Typography>
                        </InlineMessage>
                    </div>
                )}
            </React.Fragment>
        );
    }
}

Sdk.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Sdk));
