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
import {Grid, Paper, Button, Divider} from 'material-ui'
import {withStyles} from 'material-ui'
import {FormControl, Input, InputLabel, FormHelperText} from 'material-ui'
import Done from 'material-ui-icons/Done'
import FileUpload from './FileUpload'
import Alert from '../../../../Shared/Alert'
import API from '../../../../../data/api'

export default class WSDLValidation extends Component {
    constructor(props) {
        super(props);
        this.state = {uploadMethod: 'file', file: {}, url: '', valid: null};
        this.validateWSDL = this.validateWSDL.bind(this);
        this.updateURL = this.updateURL.bind(this);
    }

    toggleType = (event) => {
        let target = event.target instanceof HTMLSpanElement ? event.target.parentElement : event.target;
        const type = target.getAttribute('data-type');
        this.setState({uploadMethod: type});
    };

    handleUploadFile = acceptedFiles => {
        this.setState({file: acceptedFiles[0]});
    };

    updateURL(event) {
        const value = event.target.value;
        this.setState({url: value});
    }

    validateWSDL(updateWSDLValidity) {
        const {uploadMethod, url, file} = this.state;
        let new_api = new API('');
        let promised_validation;
        let wsdlBean = {};
        if (uploadMethod === 'file') {
            wsdlBean.file = file;
            promised_validation = new_api.validateWSDLFile(file);
        } else {
            wsdlBean.url = url;
            promised_validation = new_api.validateWSDLUrl(url);
        }
        promised_validation.then(response => {
            const {isValid, wsdlInfo} = response.obj;
            wsdlBean.info = wsdlInfo;
            this.setState({valid: isValid});
            updateWSDLValidity(isValid, wsdlBean);
        }).catch(error => {
            this.setState({valid: false});
            updateWSDLValidity(false, wsdlBean);
            console.error(error);
            Alert.error("Error while validating WSDL!!")
        });

    }

    componentWillReceiveProps(nextProps) {
        const {validate, updateWSDLValidity} = nextProps;
        if (validate) {
            this.validateWSDL(updateWSDLValidity);
        }
    }

    render() {
        const {uploadMethod, file, valid, url} = this.state;
        const currentFile = Object.keys(file).length === 0 ? [] : [file];
        const urlError = (valid === false) && (uploadMethod === 'url') ? true : (valid === true) && false; // Because of null case, which means validation haven't done yet
        return (
            <div>
                <Grid item xs={10}>
                    <Button data-type='file' raised disabled={uploadMethod === 'file'}
                            onClick={this.toggleType}>
                        WSDL File
                    </Button>
                    <Button data-type='url' raised disabled={uploadMethod === 'url'} onClick={this.toggleType}>
                        WSDL URL
                    </Button>
                    <Grid container spacing={0} justify="center">
                        <Grid item style={{minHeight: '250px'}}>
                            {uploadMethod === 'file' ? (
                                <FileUpload currentFiles={currentFile} onDropHandler={this.handleUploadFile}/>
                            ) : (
                                <form>
                                    <FormControl error={urlError} aria-describedby="url-text">
                                        <InputLabel htmlFor="url">WSDL URL</InputLabel>
                                        <Input id="url" value={url} onChange={this.updateURL}/>
                                        {valid !== null && !urlError && <Done/>}
                                        <FormHelperText id="url-text">WSDL will be validate upon submit</FormHelperText>
                                    </FormControl>
                                </form>
                            )}
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        );
    }
}