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

import React, { Suspense } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import EditRounded from '@material-ui/icons/EditRounded';
import CloudUploadRounded from '@material-ui/icons/CloudUploadRounded';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import MonacoEditor from 'react-monaco-editor';
import yaml from 'js-yaml';
import Alert from 'AppComponents/Shared/Alert';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';

import Dropzone from 'react-dropzone';
import json2yaml from 'json2yaml';
import SwaggerParser from 'swagger-parser';

import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';

const EditorDialog = React.lazy(() => import('./SwaggerEditorDrawer'));

const styles = theme => ({
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
    dropzone: {
        border: 'none',
        cursor: 'pointer',
        padding: `${theme.spacing.unit * 2}px 0px`,
        position: 'relative',
        textAlign: 'center',
    },
    topBar: {
        display: 'flex',
        flexDirection: 'row',
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
});
/**
 * This component holds the functionality of viewing the api definition content of an api. The initial view is a
 * read-only representation of the api definition file.
 * Users can either edit the content by clicking the 'Edit' button or upload a new api definition file by clicking
 * 'Import API Definition'.
 * */
class APIDefinition extends React.Component {
    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.state = {
            openEditor: false,
            swagger: null,
            format: null,
            convertTo: null,
        };
        this.onDrop = this.onDrop.bind(this);
        this.handleNo = this.handleNo.bind(this);
        this.handleOk = this.handleOk.bind(this);
        this.openEditor = this.openEditor.bind(this);
        this.transition = this.transition.bind(this);
        this.closeEditor = this.closeEditor.bind(this);
        this.hasJsonStructure = this.hasJsonStructure.bind(this);
        this.getConvertToFormat = this.getConvertToFormat.bind(this);
        this.onChangeFormatClick = this.onChangeFormatClick.bind(this);
        this.openUpdateConfirmation = this.openUpdateConfirmation.bind(this);
        this.updateSwaggerDefinition = this.updateSwaggerDefinition.bind(this);
        this.validateAndUpdateApiDefinition = this.validateAndUpdateApiDefinition.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { api } = this.props;
        const promisedApi = api.getSwagger(api.id);
        promisedApi
            .then((response) => {
                this.setState({
                    swagger: json2yaml.stringify(response.obj),
                    format: 'yaml',
                    convertTo: this.getConvertToFormat('yaml'),
                });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }

    /**
     * Handles the file upload.
     * @param {object[]} files The uploaded file object array
     * */
    onDrop(files) {
        const file = files[0];
        const { intl } = this.props;
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const { result } = e.target;
                this.validateAndUpdateApiDefinition(result);
            };
            reader.readAsText(file);
        } else {
            Alert.error(intl.formatMessage({
                id: 'Apis.Details.APIDefinition.APIDefinition.unsupported.file.type',
                defaultMessage: 'Unsupported File Type.',
            }));
        }
    }

    /**
     * Toggle the format of the api definition.
     * JSON -> YAML, YAML -> JSON
     */
    onChangeFormatClick() {
        const { format, swagger, convertTo } = this.state;
        let formattedString = '';
        if (convertTo === 'json') {
            formattedString = JSON.stringify(yaml.load(swagger), null, 1);
        } else {
            formattedString = json2yaml.stringify(JSON.parse(swagger));
        }
        this.setState({ swagger: formattedString, format: convertTo, convertTo: format });
    }

    /**
     * Util function to get the format which the definition can be converted to.
     * @param {*} format : The current format of definition.
     * @returns {string} The possible conversion format.
     */
    getConvertToFormat(format) {
        return format === 'json' ? 'yaml' : 'json';
    }

    /**
     * Validates the given api definition.
     * @param {*} apiDefinition JSON/ YAML api definition.
     */
    validateAndUpdateApiDefinition(apiDefinition) {
        const { intl } = this.props;
        let swaggerObj = {};
        let specFormat = null;
        if (this.hasJsonStructure(apiDefinition)) {
            swaggerObj = JSON.parse(apiDefinition);
            specFormat = 'json';
        } else {
            swaggerObj = yaml.safeLoad(apiDefinition);
            specFormat = 'yaml';
        }
        SwaggerParser.validate(swaggerObj, (err, api) => {
            if (api) {
                this.updateSwaggerDefinition(apiDefinition, specFormat, this.getConvertToFormat(specFormat));
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.file.validation.failed',
                    defaultMessage: 'API Definition file validation failed.',
                }));
            }
        });
    }

    /**
     * Checks whether the swagger content is json type.
     * @param {string} definition The swagger string.
     * @return {boolean} Whether the content is a json or not.
     * */
    hasJsonStructure(definition) {
        if (typeof definition !== 'string') return false;
        try {
            const result = JSON.parse(definition);
            return result && typeof result === 'object';
        } catch (err) {
            return false;
        }
    }

    /**
     * Handles the yes button action of the save api definition confirmation dialog box.
     */
    handleOk() {
        const updatedContent = window.localStorage.getItem('swagger-editor-content');
        this.setState({ openDialog: false }, () => this.updateSwaggerDefinition(updatedContent, '', ''));
    }

    /**
     * Handles the No button action of the save api definition confirmation dialog box.
     */
    handleNo() {
        this.setState({ openDialog: false });
    }

    /**
     * Method to set the state for opening the swagger editor drawer.
     * Swagger editor loads the definition content from the local storage. Hence we set the swagger content to the
     * local storage.
     * */
    openEditor() {
        window.localStorage.setItem('swagger-editor-content', this.state.swagger);
        this.setState({ openEditor: true });
    }

    /**
     * Sets the state to close the swagger-editor drawer.
     * */
    closeEditor() {
        window.localStorage.setItem('swagger-editor-content', '');
        this.setState({ openEditor: false });
    }

    /**
     * Handles the transition of the drawer.
     * @param {object} props list of props
     * @return {object} The Slide transition component
     * */
    transition(props) {
        return <Slide direction='up' {...props} />;
    }

    /**
     * Updates swagger content in the local storage.
     * */
    openUpdateConfirmation() {
        this.setState({ openDialog: true });
    }

    /**
     * Updates swagger definition of the api.
     * @param {string} swaggerContent The swagger file that needs to be updated.
     * @param {string} specFormat The current format of the definition
     * @param {string} toFormat The format it can be converted to.
     * */
    updateSwaggerDefinition(swaggerContent, specFormat, toFormat) {
        const { api, intl } = this.props;
        let parsedContent = {};
        if (this.hasJsonStructure(swaggerContent)) {
            parsedContent = JSON.parse(swaggerContent);
        } else {
            try {
                parsedContent = yaml.load(swaggerContent);
            } catch (err) {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.api.definition',
                    defaultMessage: 'Error while updating the API Definition',
                }));
                return;
            }
        }
        const promise = api.updateSwagger(parsedContent);
        promise
            .then((response) => {
                if (response) {
                    Alert.success(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.api.definition.updated.successfully',
                        defaultMessage: 'API Definition Updated Successfully',
                    }));
                    if (specFormat && toFormat) {
                        this.setState({ swagger: swaggerContent, format: specFormat, convertTo: toFormat });
                    } else {
                        this.setState({ swagger: swaggerContent });
                    }
                }
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.api.definition',
                    defaultMessage: 'Error while updating the API Definition',
                }));
            });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            swagger, openEditor, openDialog, format, convertTo, notFound,
        } = this.state;
        const { classes, resourceNotFountMessage } = this.props;
        const downloadLink = 'data:text/' + format + ';charset=utf-8,' + encodeURIComponent(swagger);
        const fileName = 'swagger.' + format;
        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
        };

        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }
        if (!swagger) {
            return <Progress />;
        }

        return (
            <div className={classes.root}>
                <div className={classes.topBar}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.api.definition'
                                defaultMessage='API Definition'
                            />
                        </Typography>
                        <Button size='small' className={classes.button} onClick={this.openEditor}>
                            <EditRounded className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.edit'
                                defaultMessage='Edit'
                            />
                        </Button>
                        <Dropzone
                            multiple={false}
                            className={classes.dropzone}
                            accept={['application/json', 'application/x-yaml']}
                            onDrop={(files) => {
                                this.onDrop(files);
                            }}
                        >
                            <Button size='small' className={classes.button}>
                                <CloudUploadRounded className={classes.buttonIcon} />
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.import.definition'
                                    defaultMessage='Import Definition'
                                />
                            </Button>
                        </Dropzone>
                        <a className={classes.downloadLink} href={downloadLink} download={fileName}>
                            <Button size='small' className={classes.button}>
                                <CloudDownloadRounded className={classes.buttonIcon} />
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.download.definition'
                                    defaultMessage='Download Definition'
                                />
                            </Button>
                        </a>
                    </div>
                    <div className={classes.converterWrapper}>
                        <Button size='small' className={classes.button} onClick={this.onChangeFormatClick}>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.convert.to'
                                defaultMessage='Convert to:'
                            />
                            {convertTo}
                        </Button>
                    </div>
                </div>
                <div>
                    <MonacoEditor
                        width='100%'
                        height='calc(100vh - 51px)'
                        theme='vs-dark'
                        value={swagger}
                        options={editorOptions}
                    />
                </div>
                <Dialog fullScreen open={openEditor} onClose={this.closeEditor} TransitionComponent={this.transition}>
                    <Paper square className={classes.popupHeader}>
                        <IconButton
                            className={classes.button}
                            color='inherit'
                            onClick={this.closeEditor}
                            aria-label={<FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.btn.close'
                                defaultMessage='Close'
                            />}
                        >
                            <Icon>close</Icon>
                        </IconButton>

                        <Button
                            className={classes.button}
                            variant='contained'
                            color='primary'
                            onClick={this.openUpdateConfirmation}
                        >
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.documents.swagger.editor.update.content'
                                defaultMessage='Update Content'
                            />
                        </Button>
                    </Paper>
                    <Suspense fallback={
                        <div>(
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.loading'
                                defaultMessage='Loading...'
                            />
                        )
                        </div>}
                    >
                        <EditorDialog />
                    </Suspense>
                </Dialog>
                <Dialog
                    open={openDialog}
                    onClose={this.handleNo}
                    aria-labelledby='alert-dialog-title'
                    aria-describedby='alert-dialog-description'
                >
                    <DialogTitle id='alert-dialog-title'>
                        <Typography align='left'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.save.api.definition'
                                defaultMessage='Save API Definition'
                            />
                        </Typography>
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id='alert-dialog-description'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.api.definition.save.confirmation'
                                defaultMessage={'Do you want to save the API Definition? This will affect the' +
                                ' existing resources.'}
                            />
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.handleNo} color='secondary'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.btn.no'
                                defaultMessage='No'
                            />
                        </Button>
                        <Button onClick={this.handleOk} color='primary' autoFocus>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.btn.yes'
                                defaultMessage='Yes'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }
}

APIDefinition.propTypes = {
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
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.object,
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.object,
    }).isRequired,
    resourceNotFountMessage: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default injectIntl(withStyles(styles, { withTheme: true })(APIDefinition));
