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

import React, { Suspense, lazy } from 'react';
import AppContext from 'AppComponents/Shared/AppContext';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import API from 'AppData/api.js';
import { withRouter } from 'react-router';
import { isRestricted } from 'AppData/AuthManager';
import Utils from 'AppData/Utils';
import Alert from 'AppComponents/Shared/Alert';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import ImportDefinition from './ImportDefinition';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "APIDefMonacoEditor" */));

const styles = (theme) => ({
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    swaggerEditorWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    buttonIcon: {
        marginRight: 10,
    },
    topBar: {
        display: 'flex',
        flexDirection: 'row',
        marginBottom: theme.spacing(2),
    },
    converterWrapper: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        flex: '1',
        fontSize: '0.6964285714285714rem',
    },
    downloadLink: {
        color: 'black',
    },
    button: {
        marginLeft: theme.spacing(1),
    },
});

/**
 * This component holds the functionality of viewing the WSDL content of an api. The view is a
 * read-only representation of the WSDL file. If the API has a WSDL ZIP archive, the preview will not be
 * available and it will be indicated in the UI.
 * Users can upload a new api definition file by clicking 'Import WSDL'.
 *
 * @class WSDL
 * @extends {Component}
 * */
class WSDL extends React.Component {
    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.state = {
            isArchive: false,
            wsdl: null,
        };
        this.loadWSDLInEditor = this.loadWSDLInEditor.bind(this);
        this.handleDownloadWSDLZip = this.handleDownloadWSDLZip.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { api, intl } = this.props;
        const promisedWSDLInfo = api.getWSDLInfo(api.id);
        promisedWSDLInfo
            .then((response) => {
                const wsdlType = response.obj.type;
                if (wsdlType === 'WSDL') {
                    this.loadWSDLInEditor(api);
                } else {
                    this.setState({ isArchive: true, wsdl: null });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.WSDL.error.loading.wsdl.info',
                        defaultMessage: 'Error loading WSDL',
                    }));
                }
            });
    }

    /**
     * Downloads and loads the API's WSDL in the editor. If the WSDL is ZIP, the operation is skipped
     * and it is indicated in the UI.
     *
     *  @param {boolean} isArchive states whether the WSDL is a ZIP
     */
    setSchemaDefinition = (isArchive) => {
        this.setState({ isArchive, wsdl: null });
        if (!isArchive) {
            const { api } = this.props;
            this.loadWSDLInEditor(api);
        }
    }

    /**
     * Downloads WSDL ZIP file.
     *
     */
    handleDownloadWSDLZip() {
        const { api, intl } = this.props;
        const wsdlZipContent = api.getWSDL(api.id);
        wsdlZipContent.then((response) => {
            Utils.forceDownload(response);
        })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.WSDL.download.error',
                    defaultMessage: 'Error downloading WSDL ZIP file',
                }));
            });
    }

    /**
     * Downloads and loads the API's WSDL in the editor.
     *
     * @param {*} api API
     */
    loadWSDLInEditor(api) {
        const { intl } = this.props;
        const promisedApi = api.getWSDL(api.id);

        promisedApi
            .then((response) => {
                response.data.text().then((text) => {
                    this.setState({
                        isArchive: false,
                        wsdl: text,
                    });
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.WSDL.error.loading.wsdl',
                        defaultMessage: 'Error loading WSDL',
                    }));
                }
            });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            wsdl, notFound, isArchive,
        } = this.state;
        const { classes, resourceNotFountMessage, api } = this.props;

        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
            cursorStyle: 'line-thin',
        };

        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }

        if (!isArchive && !wsdl) {
            return <Progress />;
        }

        let downloadWidget;
        const downloadButtonContent = (
            <>
                <CloudDownloadRounded className={classes.buttonIcon} />
                <FormattedMessage
                    id='Apis.Details.APIDefinition.WSDL.download.definition'
                    defaultMessage='Download WSDL'
                />
            </>
        );
        if (!isArchive) {
            const downloadLink = 'data:text/xml;charset=utf-8,' + encodeURIComponent(wsdl);
            const fileName = api.name + '-' + api.version + '.wsdl';
            downloadWidget = (
                <a className={classes.downloadLink} href={downloadLink} download={fileName}>
                    <Button size='small' className={classes.button}>
                        {downloadButtonContent}
                    </Button>
                </a>
            );
        } else {
            downloadWidget = (
                <Button size='small' className={classes.button} onClick={this.handleDownloadWSDLZip}>
                    {downloadButtonContent}
                </Button>
            );
        }

        return (
            <>
                <div className={classes.topBar}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.WSDL.wsdl.definition'
                                defaultMessage='WSDL Definition'
                            />
                        </Typography>
                        <ImportDefinition setSchemaDefinition={this.setSchemaDefinition} />
                        {downloadWidget}
                        {isRestricted(['apim:api_create'], api) && (
                            <Typography variant='body2' color='primary'>
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.WSDL.update.not.allowed'
                                    defaultMessage='Unauthorized: Insufficient permissions to update WSDL Definition'
                                />
                            </Typography>
                        )}
                    </div>
                </div>
                <div>
                    <Suspense fallback={<Progress />}>
                        {isArchive ? (
                            <InlineMessage type='info' height={80} className={classes.emptyBox}>
                                <div className={classes.contentWrapper}>
                                    <Typography component='p' className={classes.content}>
                                        <FormattedMessage
                                            id='Apis.Details.APIDefinition.WSDL.preview.not.available'
                                            defaultMessage='The API has a WSDL ZIP hence the preview is not available.'
                                        />
                                    </Typography>
                                </div>
                            </InlineMessage>
                        ) : (
                            <MonacoEditor
                                language='xml'
                                width='100%'
                                height='calc(100vh - 51px)'
                                theme='vs-dark'
                                value={wsdl}
                                options={editorOptions}
                            />
                        )}

                    </Suspense>
                </div>
            </>
        );
    }
}

WSDL.contextType = AppContext;
WSDL.propTypes = {
    classes: PropTypes.shape({
        button: PropTypes.shape({}),
        popupHeader: PropTypes.shape({}),
        buttonIcon: PropTypes.shape({}),
        root: PropTypes.shape({}),
        topBar: PropTypes.shape({}),
        titleWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
        converterWrapper: PropTypes.shape({}),
        dropzone: PropTypes.shape({}),
        downloadLink: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({
        updateSwagger: PropTypes.func,
        getSwagger: PropTypes.func,
        id: PropTypes.string,
        apiType: PropTypes.oneOf([API.CONSTS.API, API.CONSTS.APIProduct]),
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    resourceNotFountMessage: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default withRouter(injectIntl(withStyles(styles, { withTheme: true })(WSDL)));
