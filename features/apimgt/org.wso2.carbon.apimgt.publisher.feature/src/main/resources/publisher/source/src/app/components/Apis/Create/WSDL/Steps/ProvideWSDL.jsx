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
    Grid,
    RadioGroup,
    Radio,
    Button,
    Tooltip,
    FormControl,
    Input,
    InputLabel,
    FormHelperText,
    FormControlLabel,
} from 'material-ui';
import { Done, ErrorOutline } from '@material-ui/icons/';
import PropTypes from 'prop-types';

import FileUpload from './FileUpload';
import Alert from '../../../../Shared/Alert';
import API from '../../../../../data/api';

const styles = () => ({
    radioGroup: {
        'margin-left': '0px',
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

    toggleType = (event) => {
        this.setState({ uploadMethod: event.target.value });
    };

    handleUploadFile = (acceptedFiles) => {
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
    validateWSDL(updateWSDLValidity) {
        // do not invoke callback in case of React SyntheticMouseEvent
        const updateHandler =
            updateWSDLValidity.constructor.name === 'SyntheticMouseEvent' ? false : updateWSDLValidity;
        const { uploadMethod, url, file } = this.state;
        const newAPI = new API();
        let promisedValidation;
        const wsdlBean = {};
        if (uploadMethod === 'file') {
            if (!file) {
                this.setState({ isValid: false, errorMessage: 'WSDL file not provided!' });
                return;
            }
            wsdlBean.file = file;
            promisedValidation = newAPI.validateWSDLFile(file);
        } else {
            if (!url) {
                this.setState({ isValid: false, errorMessage: 'WSDL url not provided!' });
                return;
            }
            wsdlBean.url = url;
            promisedValidation = newAPI.validateWSDLUrl(url);
        }
        promisedValidation
            .then((response) => {
                const { isValid, wsdlInfo } = response.obj;
                wsdlBean.info = wsdlInfo;
                this.setState({ isValid });
                if (updateHandler) {
                    updateHandler(isValid, wsdlBean);
                }
            })
            .catch((error) => {
                if (updateHandler) {
                    updateHandler(false, wsdlBean);
                }
                const response = error.response && error.response.obj;
                const message =
                    'Error while validating WSDL!! ' + response && '[' + response.message + '] ' + response.description;
                this.setState({ isValid: false, errorMessage: message });
                console.error(error);
                Alert.error(message);
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof ProvideWSDL
     */
    render() {
        const {
            isValid, errorMessage, uploadMethod, file, url,
        } = this.state;
        const { classes } = this.props;
        const currentFile = file ? [file] : [];
        const error = isValid === false; // Because of null case, which means validation haven't done yet
        return (
            <div>
                <Grid item xs={10}>
                    <FormControl component='fieldset'>
                        <RadioGroup
                            aria-label='swagger-upload-method'
                            name='swagger-upload-method'
                            value={uploadMethod}
                            onChange={this.toggleType}
                        >
                            <FormControlLabel
                                value='file'
                                control={<Radio />}
                                label='File'
                                checked
                                className={classes.radioGroup}
                            />
                            <FormControlLabel
                                value='url'
                                control={<Radio />}
                                label='URL'
                                className={classes.radioGroup}
                            />
                        </RadioGroup>
                    </FormControl>
                    <Grid container spacing={0} justify='center'>
                        <Grid item style={{ minHeight: '250px' }} xs={8}>
                            {uploadMethod === 'file' ? (
                                <FileUpload currentFiles={currentFile} onDropHandler={this.handleUploadFile} />
                            ) : (
                                <form>
                                    <FormControl fullWidth aria-describedby='url-text'>
                                        <InputLabel htmlFor='url'>WSDL URL</InputLabel>
                                        <Input id='url' value={url} onChange={this.updateURL} />
                                        <FormHelperText id='url-text'>
                                            The WSDL will be validated upon submission
                                        </FormHelperText>
                                    </FormControl>
                                </form>
                            )}
                        </Grid>
                        <Grid item xs={4}>
                            <Grid container>
                                <Grid item xs={12}>
                                    <Tooltip title={'Validates WSDL ' + uploadMethod} placement='bottom'>
                                        <Button color={error ? 'accent' : 'primary'} onClick={this.validateWSDL}>
                                            Validate {isValid && <Done />}
                                        </Button>
                                    </Tooltip>
                                </Grid>
                                <Grid item xs={12}>
                                    {error && (
                                        <span style={{ color: 'red' }}>
                                            <ErrorOutline /> {errorMessage}
                                        </span>
                                    )}
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

ProvideWSDL.defaultProps = {
    uploadMethod: 'file',
};

ProvideWSDL.propTypes = {
    url: PropTypes.string.isRequired,
    updateWSDLValidity: PropTypes.func.isRequired,
    uploadMethod: PropTypes.string,
    validate: PropTypes.bool.isRequired,
    file: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ProvideWSDL);
