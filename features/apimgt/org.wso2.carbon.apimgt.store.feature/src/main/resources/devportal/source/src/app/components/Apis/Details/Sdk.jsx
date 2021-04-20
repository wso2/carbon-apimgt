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
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import AuthManager from 'AppData/AuthManager';
import { app } from 'Settings';
import Api from '../../../data/api';

const styles = (theme) => ({
    genericMessageWrapper: {
        margin: theme.spacing(2),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    gridRoot: {
        marginLeft: theme.spacing(2),
    },
    titleWrappper: {
        display: 'flex',
        alignItems: 'center',
        '& h4': {
            marginRight: theme.spacing(1),
        },
    },
    cardTitle: {
        background: theme.palette.grey[50],
    },
    cardRoot: {
        background: theme.custom.apiDetailPages.sdkBackground,
    },
});

/**
 * @class Sdk
 * @extends {React.Component}
 */
class Sdk extends React.Component {
    /**
     * Create instance of Sdk
     * @param {JSON} props props passed from parent
     */
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
     * @memberof Sdk
     */
    componentDidMount() {
        const api = new Api();
        const user = AuthManager.getUser();
        if (user != null) {
            const promisedLanguages = api.getSdkLanguages();

            promisedLanguages
                .then((response) => {
                    if (response.obj.length === 0) {
                        this.setState({ sdkLanguages: false });
                        return;
                    }
                    this.setState({ sdkLanguages: response.obj });
                    this.setState({ items: response.obj });
                })
                .catch((error) => {
                    console.log(error);
                    Alert.error(error);
                });
        }
    }

    /**
     * Call the REST API to generate the SDK
     *
     * @param {string} apiId api id
     * @param {string} language language selected
     * @memberof Sdk
     */
    getSdkForApi(apiId, language) {
        const api = new Api();
        const promisedSdk = api.getSdk(apiId, language);

        promisedSdk
            .then((response) => {
                const sdkZipName = response.headers['content-disposition'].match(/filename="(.+)"/)[1];
                const sdkZip = response.data;
                // Prompt to download zip file for the SDK
                JSFileDownload(sdkZip, sdkZipName);
            })
            .catch((error) => {
                console.log(error);
                Alert.error(error);
            });
    }

    /**
     * Handle the click event of the download button
     * @param {event} _event click event
     * @param {string} item selected language
     * @memberof Sdk
     */
    handleClick = (_event, item) => {
        const apiId = this.api_uuid;
        const language = item;
        this.getSdkForApi(apiId, language);
    };

    /**
     * Handle the change event of the Search input field
     * @param {event} event click event
     * @memberof Sdk
     */
    handleChange = (event) => {
        const { sdkLanguages } = this.state;
        let updatedList = sdkLanguages;
        updatedList = updatedList.filter((item) => {
            return item.toLowerCase().search(event.target.value.toLowerCase()) !== -1;
        });
        this.setState({ items: updatedList });
    };

    /**
     * Handle sdk image not found issue. Point to a default image
     * @param {event} ev click event
     * @memberof Sdk
     */
    addDefaultSrc = (ev) => {
        const evLocal = ev;
        evLocal.target.src = app.context + '/site/public/images/sdks/default.svg';
    };

    /**
     * @returns  {JSX} rendered sdk ui
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
        const filteredLanguageList = languageList && languageList.length > 0 && onlyShowSdks && onlyShowSdks.length > 0
            ? languageList.filter((lang) => onlyShowSdks.includes(lang.toLowerCase()))
            : languageList;
        if (onlyIcons) {
            return (
                filteredLanguageList && (
                    <>
                        {filteredLanguageList.map((language, index) => index < 3 && (
                            <Grid item xs={4} key={language}>
                                <a
                                    onClick={(event) => this.handleClick(event, language)}
                                    onKeyDown={(event) => this.handleClick(event, language)}
                                    style={{ cursor: 'pointer' }}
                                >
                                    <img
                                        alt={language}
                                        src={
                                            app.context
                                                    + '/site/public/images/sdks/'
                                                    + String(language)
                                                    + '.svg'
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
                    </>
                )
            );
        }
        return (
            <>
                <div className={classes.titleWrappper}>
                    <Typography variant='h4' component='h2' className={classes.titleSub}>
                        <FormattedMessage id='Apis.Details.Sdk.title' defaultMessage='Software Development Kits (SDKs)' />
                    </Typography>
                    {filteredLanguageList && this.state.sdkLanguages.length >= this.filter_threshold && (
                        <TextField
                            variant='outlined'
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
                    )}
                </div>
                {filteredLanguageList ? (
                    <Grid container spacing={0} className={classes.gridRoot}>
                        <Grid item xs={12} sm={6} md={9} lg={9} xl={10}>
                            <Grid container justify='flex-start' spacing={4}>
                                {filteredLanguageList.map((language) => (
                                    <Grid key={language} item>
                                        <div style={{ width: 'auto', textAlign: 'center', margin: '10px' }}>
                                            <Card className={classes.cardRoot}>
                                                <div>{language.toString().toUpperCase()}</div>
                                                <Divider />
                                                <CardMedia
                                                    title={language.toString().toUpperCase()}
                                                    src={
                                                        app.context + '/site/public/images/sdks/'
                                                        + String(language)
                                                        + '.svg'
                                                    }
                                                    classes={{ root: classes.cardTitle }}
                                                >
                                                    <img
                                                        alt={language}
                                                        onError={this.addDefaultSrc}
                                                        src={`${app.context}/site/public/images/sdks/${language}.svg`}
                                                        style={{ width: '100px', height: '100px', margin: '30px' }}
                                                    />
                                                </CardMedia>
                                                <CardActions>
                                                    <Grid container justify='center'>
                                                        <Button
                                                            color='secondary'
                                                            onClick={(event) => this.handleClick(event, language)}
                                                            aria-label={'Download ' + language + ' SDK'}
                                                        >
                                                            <Icon>arrow_downward</Icon>
                                                            Download
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
            </>
        );
    }
}

Sdk.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Sdk));
