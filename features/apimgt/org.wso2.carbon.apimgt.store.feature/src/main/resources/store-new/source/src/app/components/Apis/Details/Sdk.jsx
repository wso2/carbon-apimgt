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
import Paper from '@material-ui/core/Paper';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl, } from 'react-intl';
import AuthManager from 'AppData/AuthManager';
import Api from '../../../data/api';
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
     *
     *
     * @returns
     * @memberof Sdk
     */
    render() {
        const language_list = this.state.items;
        const { onlyIcons, intl } = this.props;
        if (onlyIcons) {
            return (
                language_list && (
                    <React.Fragment>
                        {language_list.map(
                            (language, index) => index < 3 && (
                                <Grid item xs={4}>
                                    <a
                                        onClick={event => this.handleClick(event, language)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <img
                                            alt={language}
                                            src={
                                                '/store-new/site/public/images/sdks/'
                                                    + new String(language)
                                                    + '.svg'
                                            }
                                            style={{ width: 80, height: 80, margin: 15 }}
                                        />
                                    </a>
                                </Grid>
                            ),
                        )}
                    </React.Fragment>
                )
            );
        }
        return language_list ? (
            <Grid container className='tab-grid' spacing={0}>
                <Grid item xs={12} sm={6} md={9} lg={9} xl={10}>
                    {this.state.sdkLanguages.length >= this.filter_threshold && (
                        <Grid item style={{ textAlign: 'center' }}>
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
                    <Grid container justify='flex-start' spacing={Number(24)}>
                        {language_list.map((language, index) => (
                            <Grid key={index} item>
                                <div style={{ width: 'auto', textAlign: 'center' }}>
                                    <Card>
                                        <div>{language.toString().toUpperCase()}</div>
                                        <Divider />
                                        <CardMedia
                                            title={language.toString().toUpperCase()}
                                            src={'/store-new/site/public/images/sdks/' + new String(language) + '.svg'}
                                        >
                                            <img
                                                alt={language}
                                                src={
                                                    '/store-new/site/public/images/sdks/'
                                                    + new String(language)
                                                    + '.svg'
                                                }
                                                style={{ width: '100px', height: '100px', margin: '15px' }}
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
            <Paper>
                <Grid container style={{ marginLeft: '10%', marginRight: '10%', width: '100%' }} align='center'>
                    <Grid item xs={12} sm={6} md={9} lg={9} xl={10}>
                        <Paper>
                            <Typography>
                                <Icon>info</Icon>
                                <FormattedMessage
                                    id='Apis.Details.Sdk.no.lanuages'
                                    defaultMessage='No languages are configured.'
                                />
                            </Typography>
                        </Paper>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default injectIntl(Sdk);
