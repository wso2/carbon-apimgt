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
import React, {Component} from 'react';
import {Grid, RadioGroup, Radio, Button, Tooltip, Card} from 'material-ui'
import {withStyles} from 'material-ui'
import {FormControl, Input, InputLabel, FormHelperText, FormControlLabel} from 'material-ui'
import {Done, ErrorOutline} from 'material-ui-icons'
import FileUpload from './FileUpload'
import Alert from '../../../../Shared/Alert'
import API from '../../../../../data/api'

const styles = theme => ({
    radioGroup: {
        'margin-left': '0px',
    },
});

class ProvideWSDL extends Component {
    constructor(props) {
        super(props);
        const {uploadMethod, file, url} = props;
        this.state = {uploadMethod: uploadMethod, file: file, url: url, isValid: null, errorMessage: ''};
        this.validateWSDL = this.validateWSDL.bind(this);
        this.updateURL = this.updateURL.bind(this);
    }

    toggleType = (event) => {
        this.setState({uploadMethod: event.target.value});
    };

    handleUploadFile = acceptedFiles => {
        this.setState({file: acceptedFiles[0]});
    };

    updateURL(event) {
        const value = event.target.value;
        this.setState({url: value});
    }

    /**
     *
     * @param updateWSDLValidity {Function} Call back function to trigger after pass/fail the validation
     */
    validateWSDL(updateWSDLValidity) {
        // do not invoke callback in case of React SyntheticMouseEvent
        updateWSDLValidity = updateWSDLValidity.constructor.name === 'SyntheticMouseEvent' ? false : updateWSDLValidity;
        const {uploadMethod, url, file} = this.state;
        let new_api = new API();
        let promised_validation;
        let wsdlBean = {};
        if (uploadMethod === 'file') {
            if (!file) {
                this.setState({isValid: false, errorMessage: 'WSDL file not provided!'});
                return;
            }
            wsdlBean.file = file;
            promised_validation = new_api.validateWSDLFile(file);
        } else {
            if (!url) {
                this.setState({isValid: false, errorMessage: 'WSDL url not provided!'});
                return;
            }
            wsdlBean.url = url;
            promised_validation = new_api.validateWSDLUrl(url);
        }
        promised_validation.then(response => {
            const {isValid, wsdlInfo} = response.obj;
            wsdlBean.info = wsdlInfo;
            this.setState({isValid: isValid});
            updateWSDLValidity && updateWSDLValidity(isValid, wsdlBean);
        }).catch(error => {
            updateWSDLValidity && updateWSDLValidity(false, wsdlBean);
            const response = error.response && error.response.obj;
            const message = "Error while validating WSDL!! " + response && "[" + response.message + "] " + response.description;
            this.setState({isValid: false, errorMessage: message});
            console.error(error);
            Alert.error(message);
        });

    }

    componentWillReceiveProps(nextProps) {
        const {validate, updateWSDLValidity} = nextProps;
        if (validate) {
            this.validateWSDL(updateWSDLValidity);
        }
    }

    render() {
        const {isValid, errorMessage, uploadMethod, file, url} = this.state;
        const {classes} = this.props;
        const currentFile = file ? [file] : [];
        const error = (isValid === false); // Because of null case, which means validation haven't done yet
        return (
            <div>
                <Grid item xs={10}>
                    <FormControl component="fieldset">
                        <RadioGroup aria-label="swagger-upload-method"
                                    name="swagger-upload-method"
                                    value={uploadMethod}
                                    onChange={this.toggleType}
                        >
                            <FormControlLabel value="file" control={<Radio />} label="File" checked
                                              className={classes.radioGroup}/>
                            <FormControlLabel value="url" control={<Radio />} label="URL"
                                              className={classes.radioGroup}/>
                        </RadioGroup>
                    </FormControl>
                    <Grid container spacing={0} justify="center">
                        <Grid item style={{minHeight: '250px'}} xs={8}>
                            {uploadMethod === 'file' ? (
                                <FileUpload currentFiles={currentFile}
                                            onDropHandler={this.handleUploadFile}/>
                            ) : (
                                <form>
                                    <FormControl fullWidth aria-describedby="url-text">
                                        <InputLabel htmlFor="url">WSDL URL</InputLabel>
                                        <Input id="url" value={url} onChange={this.updateURL}/>
                                        <FormHelperText id="url-text">The WSDL will be validated upon submission</FormHelperText>
                                    </FormControl>
                                </form>
                            )}
                        </Grid>
                        <Grid item xs={4}>
                            <Grid container>
                                <Grid item xs={12}>
                                    <Tooltip title={"Validates WSDL " + uploadMethod} placement="bottom">
                                        <Button color={error ? "accent" : "primary"} onClick={this.validateWSDL}>
                                            Validate {isValid && <Done/>}
                                        </Button>
                                    </Tooltip>
                                </Grid>
                                <Grid item xs={12}>
                                    {error && (<span style={{color: 'red'}}><ErrorOutline/> {errorMessage}</span>)}
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
    uploadMethod: 'file'
}
export default withStyles(styles)(ProvideWSDL)