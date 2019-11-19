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
    FormHelperText,
    FormControlLabel,
} from '@material-ui/core';
import ErrorOutline from '@material-ui/icons/ErrorOutline';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import Progress from 'AppComponents/Shared/Progress';
import { FormattedMessage } from 'react-intl';
import Dropzone from 'react-dropzone';
import classNames from 'classnames';
import Backup from '@material-ui/icons/Backup';
import API from 'AppData/api';

const styles = theme => ({
    radioWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    dropZoneInside: {},
    dropZone: {
        color: theme.palette.grey[500],
        border: 'dashed 1px ' + theme.palette.grey[500],
        background: theme.palette.grey[100],
        padding: theme.spacing(4),
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
        marginRight: theme.spacing(2),
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
 * @class ProvideWSDL
 * @extends {Component}
 */
class ProvideWSDL extends Component {
    /**
     * Creates an instance of ProvideWSDL.
     * @param {any} props @inheritDoc
     * @memberof ProvideWSDL
     */
    constructor(props) {
        super(props);
        const { uploadMethod, file, url } = props;
        this.state = {
            uploadMethod,
            file,
            url,
            isValid: null,
            errorMessage: '',
            loading: false,
        };
        this.validateWSDL = this.validateWSDL.bind(this);
        this.updateURL = this.updateURL.bind(this);
    }

    /**
     * @inheritDoc
     * @param {any} nextProps New props received
     * @memberof ProvideWSDL
     */
    componentWillReceiveProps(nextProps) {
        const { validate, updateWSDLValidity } = nextProps;
        if (validate) {
            this.validateWSDL(updateWSDLValidity);
        }
    }

    getUploadMethod() {
        return this.state.uploadMethod;
    }

    toggleType = (event) => {
        if (event.target.value === 'file') {
            this.setState({ uploadMethod: event.target.value, file: null });
        } else {
            this.setState({ uploadMethod: event.target.value });
        }
        // this.validateWSDL();
    };

    handleUploadFile = (acceptedFiles) => {
        this.validateWSDL((isValid, wsdlBean) => {
            console.info('(isValid, wsdlBean) = ', isValid, wsdlBean);
        });
        this.setState({ file: acceptedFiles[0] });
    };

    /**
     *
     * @param {React.SyntheticEvent} event Event handler for URL input field
     * @memberof ProvideWSDL
     */
    updateURL(event) {
        const { value } = event.target;
        this.setState({ url: value });
    }
    /**
     *
     * @param validity {Function} Call back function to trigger after pass/fail the validation
     */
    validateWSDL() {
        // do not invoke callback in case of React SyntheticMouseEvent
        const { uploadMethod, url, file } = this.state;
        const { valid, updateFileErrors, updateWSDLBean } = this.props;
        this.setState({ loading: true });
        const newAPI = new API();
        let promisedValidation;
        const wsdlBean = {};
        const validNew = JSON.parse(JSON.stringify(valid));
        if (uploadMethod === 'file') {
            if (!file) {
                // Update the parent's state
                validNew.wsdlFile.empty = true;
                updateFileErrors(validNew);
                return;
            }
            // Update the parent's state
            validNew.wsdlFile.empty = false;
            updateFileErrors(validNew);

            wsdlBean.file = file;
            promisedValidation = newAPI.validateWSDLFile(file);
        } else {
            if (!url) {
                // Update the parent's state
                validNew.wsdlUrl.empty = true;
                updateFileErrors(validNew);
                return;
            }

            // Update the parent's state
            validNew.wsdlUrl.empty = false;
            updateFileErrors(validNew);

            wsdlBean.url = url;
            promisedValidation = newAPI.validateWSDLUrl(url);
        }
        promisedValidation
            .then((response) => {
                const { isValid, wsdlInfo } = response.obj;
                wsdlBean.info = wsdlInfo;

                // Update the parent's state
                if (uploadMethod === 'file') {
                    validNew.wsdlFile.invalidFile = false;
                    updateFileErrors(validNew);
                } else {
                    validNew.wsdlUrl.invalidUrl = false;
                    updateFileErrors(validNew);
                }
                this.setState({ isValid, loading: false, file });
                updateWSDLBean(wsdlBean);
            })
            .catch((error) => {
                // Update the parent's state
                if (uploadMethod === 'file') {
                    validNew.wsdlFile.invalidFile = true;
                    updateFileErrors(validNew);
                } else {
                    validNew.wsdlUrl.invalidUrl = true;
                    updateFileErrors(validNew);
                }
                updateWSDLBean(wsdlBean);
                const response = error.response && error.response.obj;
                const message =
                    'Error while validating WSDL!! ' + response && '[' + response.message + '] ' + response.description;
                this.setState({ isValid: false, errorMessage: message, loading: false });
                console.error(error);
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof ProvideWSDL
     */
    render() {
        const {
            isValid, errorMessage, uploadMethod, file, url, loading,
        } = this.state;
        const { classes, valid } = this.props;
        const error = isValid === false; // Because of null case, which means validation haven't done yet
        if (loading) {
            return <Progress error={error} />;
        }
        return (
            <React.Fragment>
                <FormControl className={classes.FormControl}>
                    <RadioGroup
                        aria-label='swagger-upload-method'
                        name='swagger-upload-method'
                        value={uploadMethod}
                        onChange={this.toggleType}
                        className={classes.radioWrapper}
                    >
                        <FormControlLabel
                            value='file'
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.uploaded.file'
                                    defaultMessage='File'
                                />
                            }
                            className={classes.radioGroup}
                        />
                        <FormControlLabel
                            value='url'
                            control={<Radio />}
                            label={
                                <FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.uploaded.url'
                                    defaultMessage='URL'
                                />
                            }
                            className={classes.radioGroup}
                        />
                    </RadioGroup>
                </FormControl>
                {uploadMethod === 'file' ? (
                    <React.Fragment>
                        {file && (
                            <div className={classes.fileNameWrapper}>
                                <Typography variant='subtitle2' gutterBottom>
                                    <FormattedMessage id='uploaded.file' defaultMessage='Uploaded file' /> :
                                </Typography>
                                <div className={classes.fileName}>
                                    <Typography variant='body1' gutterBottom>
                                        {file.name} - {file.size} bytes
                                    </Typography>
                                </div>
                            </div>
                        )}
                        {valid.wsdlFile.invalidFile && (
                            <div className={classes.errorMessageWrapper}>
                                <ErrorOutline className={classes.errorIcon} />
                                <Typography variant='body1' gutterBottom className={classes.errorMessage}>
                                    {errorMessage}
                                </Typography>
                            </div>
                        )}
                        <Dropzone
                            onDrop={this.handleUploadFile}
                            multiple={false}
                            className={classNames(classes.dropZone, {
                                [classes.dropZoneErrorBox]: valid.wsdlFile.empty,
                            })}
                        >
                            <Backup className={classes.dropZoneIcon} />
                            <div>
                                <FormattedMessage
                                    id='try.dropping.some.files.here.or.click.to.select.files.to.upload'
                                    defaultMessage='Try dropping some files here, or click to select files to upload.'
                                />
                            </div>
                        </Dropzone>
                        <FormHelperText className={classes.errorMessage}>
                            {valid.wsdlFile.empty && (
                                <FormattedMessage id='error.empty' defaultMessage='This field cannot be empty.' />
                            )}
                            {valid.wsdlFile.invalidFile && (
                                <FormattedMessage id='error.invalid.wsdl.file' defaultMessage='Invalid WSDL File' />
                            )}
                        </FormHelperText>
                    </React.Fragment>
                ) : (
                    <form>
                        <FormControl fullWidth aria-describedby='url-text'>
                            <div className={classes.urlWrapper}>
                                <TextField
                                    error={valid.wsdlUrl.empty}
                                    fullWidth
                                    id='url'
                                    label='WSDL Url'
                                    placeholder={
                                        'eg: ' +
                                        'https://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php?wsdl'
                                    }
                                    helperText={
                                        <FormattedMessage
                                            id='create.new.wsdl.help'
                                            defaultMessage={
                                                'Give a WSDL URL such as' +
                                                ' https://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php?wsdl'
                                            }
                                        />
                                    }
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    type='text'
                                    name='url'
                                    margin='normal'
                                    value={url}
                                    onChange={this.updateURL}
                                />
                                <Button
                                    variant='outlined'
                                    color='primary'
                                    className={classes.button}
                                    onClick={this.validateWSDL}
                                >
                                    LOAD WSDL
                                </Button>
                            </div>

                            <FormHelperText className={classes.errorMessage}>
                                {valid.wsdlUrl.empty && (
                                    <FormattedMessage id='error.empty' defaultMessage='This field cannot be empty.' />
                                )}
                                {valid.wsdlUrl.invalidUrl && 'Invalid WSDL Url'}
                            </FormHelperText>
                        </FormControl>
                    </form>
                )}
            </React.Fragment>
        );
    }
}

ProvideWSDL.defaultProps = {
    uploadMethod: 'file',
    url: '',
    file: {},
};

ProvideWSDL.propTypes = {
    url: PropTypes.string,
    updateWSDLBean: PropTypes.func.isRequired,
    uploadMethod: PropTypes.string,
    validate: PropTypes.bool.isRequired,
    valid: PropTypes.shape({}).isRequired,
    file: PropTypes.shape({}),
    classes: PropTypes.shape({}).isRequired,
    updateFileErrors: PropTypes.func.isRequired,
    updateWSDLValidity: PropTypes.func.isRequired,
};

export default withStyles(styles)(ProvideWSDL);
