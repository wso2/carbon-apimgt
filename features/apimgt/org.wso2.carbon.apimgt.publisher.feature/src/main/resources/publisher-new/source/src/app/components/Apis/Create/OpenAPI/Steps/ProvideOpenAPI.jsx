/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React, { Component } from 'react';
import {
    withStyles,
    RadioGroup,
    Radio,
    Button,
    FormControl,
    FormControlLabel,
} from '@material-ui/core';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import Progress from 'AppComponents/Shared/Progress';
import { FormattedMessage } from 'react-intl';
import Dropzone from 'react-dropzone';
import classNames from 'classnames';
import Backup from '@material-ui/icons/Backup';
import API from 'AppData/api';
import { resourceMethod, resourcePath, ScopeValidation } from 'AppData/ScopeValidation';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    radioWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    dropZoneInside: {},
    dropZone: {
        width: '100%',
        color: theme.palette.grey[500],
        border: 'dashed 1px ' + theme.palette.grey[500],
        background: theme.palette.grey[100],
        padding: theme.spacing.unit * 4,
        textAlign: 'center',
        cursor: 'pointer',
    },
    dropZoneIcon: {
        color: theme.palette.grey[500],
        width: 100,
        height: 100,
    },
    dropZoneError: {
        color: theme.palette.error.main,
    },
    dropZoneErrorBox: {
        border: 'dashed 1px ' + theme.palette.error.main,
    },
    errorMessage: {
        color: theme.palette.error.main,
    },
    errorIcon: {
        color: theme.palette.error.main,
        marginRight: theme.spacing.unit * 2,
    },
    fileNameWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        '& div': {
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
        },
    },
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
    },
    errorMessageWrapper: {
        display: 'flex',
        alignItems: 'center',
    },
    urlWrapper: {
        display: 'flex',
        alignItems: 'center',
    },
    button: {
        whiteSpace: 'nowrap',
    },
});

/**
 *
 * @class ProvideOpenAPI
 * @extends {Component}
 */
class ProvideOpenAPI extends Component {
    /**
     * Creates an instance of ProvideWSDL.
     * @param {any} props @inheritDoc
     * @memberof ProvideWSDL
     */
    constructor(props) {
        super(props);
        this.state = {
            uploadMethod: 'file',
            files: [],
            openAPIUrl: '',
            loading: false,
            valid: {
                openAPIUrl: { empty: false, invalidUrl: false },
                openAPIFile: { empty: false, invalidFile: false },
            },
        };
        this.onDrop = this.onDrop.bind(this);
        this.openAPIUrlChange = this.openAPIUrlChange.bind(this);
    }

    /**
     * Handle OpenAPI file ondrop action when user drag and drop file to dopzone, This is passed through props
     * to child component
     * @param {Object} newFiles File object passed from DropZone library
     * @memberof ApiCreateOpenAPI
     */
    onDrop(newFiles) {
        const isEmpty = newFiles.length === 0;
        const { setOpenAPIFile } = this.props;
        this.setState((oldState) => {
            const { valid } = oldState;
            const validUpdated = valid;
            validUpdated.openAPIFile.empty = isEmpty;
            return { valid: validUpdated, files: newFiles };
        });

        if (!isEmpty) {
            setOpenAPIFile(newFiles);
        }
    }

    /**
     * Update openAPIUrl when input get changed
     * @param {React.SyntheticEvent} e Event triggered when URL input field changed
     * @memberof ApiCreateOpenAPI
     */
    openAPIUrlChange(event) {
        this.setState(({ valid, openAPIUrl }) => {
            const validUpdated = { ...valid };
            validUpdated.openAPIUrl.empty = !openAPIUrl;
            return { valid: validUpdated, openAPIUrl: event.target.value };
        });
    }

    /**
     * Do create API from either OpenAPI URL or OpenAPI file upload.In case of URL pre fetch the OpenAPI file
     * and make a blob
     * and the send it over REST API.
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    handleSubmit = (e) => {
        e.preventDefault();
        const { uploadMethod, files, openAPIUrl } = this.state;
        const { intl } = this.props;
        if ((uploadMethod === 'file' && files.length === 0) || (uploadMethod === 'url' && !openAPIUrl)) {
            this.setState(({ valid, files: currentFiles, openAPIUrl: currentOpenAPIUrl }) => {
                const validUpdated = { ...valid };
                validUpdated.openAPIFile.empty = currentFiles.length === 0;
                validUpdated.openAPIUrl.empty = !currentOpenAPIUrl;
                return { valid: validUpdated };
            });
            return;
        }
        this.setState({ loading: true });
        const inputType = uploadMethod;
        if (inputType === 'url') {
            const newApi = new API();
            newApi
                .validateOpenAPIByUrl(openAPIUrl)
                .then(this.validateOpenAPICallback)
                .catch((errorResponse) => {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Create.OpenAPI.ApiCreateOpenAPI.url.upload.error',
                        defaultMessage: 'Something went wrong while creating the API!',
                    }));
                    this.setState({ loading: false });
                    const { response } = errorResponse;
                    if (response.body) {
                        const { code, description, message } = response.body;
                        const messageTxt = 'Error[' + code + ']: ' + description + ' | ' + message + '.';
                        Alert.error(messageTxt);
                    }
                    console.log(errorResponse);
                });
        } else if (inputType === 'file') {
            if (files.length === 0) {
                const err = 'Select a OpenAPI file to upload.';
                Alert.error(err);
                console.log(err);
                return;
            }
            const openAPI = files[0];
            const newApi = new API();
            newApi
                .create(openAPI)
                .then(this.createAPICallback)
                .catch((errorResponse) => {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Create.OpenAPIApiCreateOpenAPI.file.upload.error',
                        defaultMessage: 'Something went wrong while creating the API!',
                    }));
                    this.setState({ loading: false });
                    const { response } = errorResponse;
                    if (response.body) {
                        const { code, description, message } = response.body;
                        const messageTxt = 'Error[' + code + ']: ' + description + ' | ' + message + '.';
                        Alert.error(messageTxt);
                    }
                    console.log(errorResponse);
                });
        }
    };

    handleUploadMethodChange = (e, value) => {
        this.setState({ uploadMethod: value });
    };

    validateOpenAPICallback = (response) => {
        console.log(response.data);
    };

    createAPICallback = (response) => {
        const uuid = JSON.parse(response.data).id;
        const redirectURL = '/apis/' + uuid + '/overview';
        this.props.history.push(redirectURL);
    };

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof ProvideWSDL
     */
    render() {
        const {
            isValid, uploadMethod, files, url, loading, valid,
        } = this.state;
        const { classes } = this.props;
        const error = isValid === false; // Because of null case, which means validation haven't done yet
        if (loading) {
            return <Progress error={error} />;
        }
        return (
            <React.Fragment>
                <form onSubmit={this.handleSubmit}>
                    <FormControl margin='normal' className={classes.FormControl}>
                        <RadioGroup
                            aria-label='inputType'
                            name='inputType'
                            value={uploadMethod}
                            onChange={this.handleUploadMethodChange}
                            className={classes.radioWrapper}
                        >
                            <FormControlLabel
                                value='file'
                                control={<Radio />}
                                label={<FormattedMessage id='file' defaultMessage='File' />}
                            />
                            <FormControlLabel
                                value='url'
                                control={<Radio />}
                                label={<FormattedMessage id='url' defaultMessage='URL' />}
                            />
                        </RadioGroup>
                    </FormControl>
                    {uploadMethod === 'file' && (
                        <FormControl className={classes.FormControlOdd}>
                            {files && files.length > 0 && (
                                <div className={classes.fileNameWrapper}>
                                    <Typography variant='subtitle2' gutterBottom>
                                        <FormattedMessage
                                            id='uploaded.file'
                                            defaultMessage='Uploaded file'
                                        /> :
                                    </Typography>
                                    {files.map(f => (
                                        <div key={f.name} className={classes.fileName}>
                                            <Typography variant='body2' gutterBottom>
                                                {f.name} - {f.size} bytes
                                            </Typography>
                                        </div>
                                    ))}
                                </div>
                            )}
                            <Dropzone
                                onDrop={this.onDrop}
                                multiple={false}
                                className={classNames(classes.dropZone, {
                                    [classes.dropZoneErrorBox]: valid.openAPIFile.empty,
                                })}
                            >
                                <Backup className={classes.dropZoneIcon} />
                                <div>
                                    <FormattedMessage
                                        id='try.dropping.some.files.here.or.click.to.select.files.to.upload'
                                        defaultMessage={
                                            'Try dropping some files ' +
                                            'here, or click to select files to upload.'
                                        }
                                    />
                                </div>
                            </Dropzone>
                            {valid.openAPIFile.empty && (
                                <Typography variant='caption' gutterBottom className={classes.dropZoneError}>
                                    <FormattedMessage
                                        id='error.empty'
                                        defaultMessage='This field can not be empty.'
                                    />
                                </Typography>
                            )}
                        </FormControl>
                    )}
                    {uploadMethod === 'url' && (
                        <FormControl className={classes.FormControlOdd}>
                            <TextField
                                error={valid.openAPIUrl.empty}
                                fullWidth
                                id='openAPIUrl'
                                label={
                                    <FormattedMessage
                                        id='Apis.Create.OpenAPI.ApiCreateOpenAPI.error.empty'
                                        defaultMessage='OpenAPI URL'
                                    />
                                }
                                placeholder='eg: http://petstore.swagger.io/v2/swagger.json'
                                helperText={
                                    valid.openAPIUrl.empty ? (
                                        <FormattedMessage
                                            id='error.empty'
                                            defaultMessage='This field can not be empty.'
                                        />
                                    ) : (
                                        <FormattedMessage
                                            id='create.new.openAPI.help'
                                            defaultMessage={
                                                'Give an OpenAPI definition such' +
                                                ' as http://petstore.swagger.io/v2/swagger.json'
                                            }
                                        />
                                    )
                                }
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                type='text'
                                name='openAPIUrl'
                                margin='normal'
                                value={url}
                                onChange={this.openAPIUrlChange}
                            />
                        </FormControl>
                    )}
                </form>
            </React.Fragment>
        );
    }
}


ProvideOpenAPI.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ProvideOpenAPI);
