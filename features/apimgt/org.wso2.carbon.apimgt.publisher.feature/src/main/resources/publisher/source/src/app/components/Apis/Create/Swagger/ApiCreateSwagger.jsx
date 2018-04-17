/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Dropzone from 'react-dropzone';

import Radio, { RadioGroup } from 'material-ui/Radio';
import { FormControl, FormControlLabel } from 'material-ui/Form';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Snackbar from 'material-ui/Snackbar';
import IconButton from 'material-ui/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import PropTypes from 'prop-types';

import API from '../../../../data/api.js';
import './ApiCreateSwagger.css';
import { ScopeValidation, resourceMethod, resourcePath } from '../../../../data/ScopeValidation';

/**
 * @inheritDoc
 * @class ApiCreateSwagger
 * @extends {React.Component}
 */
class ApiCreateSwagger extends React.Component {
    /**
     * Creates an instance of ApiCreateSwagger.
     * @param {any} props @inheritDoc
     * @memberof ApiCreateSwagger
     */
    constructor(props) {
        super(props);
        this.state = {
            uploadMethod: 'file',
            files: [],
            swaggerUrl: '',
            open: false,
            message: '',
        };
        this.onDrop = this.onDrop.bind(this);
        this.swaggerUrlChange = this.swaggerUrlChange.bind(this);
    }

    /**
     * Handle Swagger file ondrop action when user drag and drop file to dopzone, This is passed through props
     * to child component
     * @param {Object} files File object passed from DropZone library
     * @memberof ApiCreateSwagger
     */
    onDrop(files) {
        this.setState({
            files,
        });
    }

    /**
     * Update SwaggerURL when input get changed
     * @param {React.SyntheticEvent} e Event triggered when URL input field changed
     * @memberof ApiCreateSwagger
     */
    swaggerUrlChange(e) {
        this.setState({ swaggerUrl: e.target.value });
    }

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file
     * and make a blob
     * and the send it over REST API.
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    handleSubmit = (e) => {
        e.preventDefault();
        const inputType = this.state.uploadMethod;
        if (inputType === 'url') {
            const url = this.state.swaggerUrl;
            if (url === '') {
                console.debug('Swagger Url is empty.');
                this.setState({ message: 'Swagger Url is empty.' });
                this.setState({ open: true });
                return;
            }
            const data = {};
            data.url = url;
            data.type = 'swagger-url';
            const newApi = new API();
            newApi
                .create(data)
                .then(this.createAPICallback)
                .catch((errorResponse) => {
                    const errorData = JSON.parse(errorResponse.data);
                    const messageTxt =
                        'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                    this.setState({ message: messageTxt });
                    this.setState({ open: true });
                    console.debug(errorResponse);
                });
        } else if (inputType === 'file') {
            if (this.state.files.length === 0) {
                this.setState({ message: 'Select a swagger file to upload.' });
                this.setState({ open: true });
                console.log('Select a swagger file to upload.');
                return;
            }
            const swagger = this.state.files[0];
            const newApi = new API();
            newApi
                .create(swagger)
                .then(this.createAPICallback)
                .catch((errorResponse) => {
                    let errorData;
                    let messageTxt;
                    if (errorResponse.obj) {
                        errorData = errorResponse.obj;
                        messageTxt =
                            'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
                    } else {
                        errorData = errorResponse.data;
                        messageTxt = 'Error: ' + errorData + '.';
                    }
                    this.setState({ message: messageTxt });
                    this.setState({ open: true });
                    console.debug(errorResponse);
                });
        }
    };

    handleUploadMethodChange = (e, value) => {
        this.setState({ uploadMethod: value });
    };

    createAPICallback = (response) => {
        const uuid = JSON.parse(response.data).id;
        const redirectURL = '/apis/' + uuid + '/overview';
        this.props.history.push(redirectURL);
    };

    handleRequestClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }

        this.setState({ open: false });
    };

    /**
     *
     * @returns {React.Component} @inheritDoc
     * @memberof ApiCreateSwagger
     */
    render() {
        return (
            <Grid container>
                <Grid item xs={12}>
                    <Paper>
                        <Typography className='page-title' type='display2' gutterBottom>
                            Create New API -
                            {this.state.uploadMethod === 'file' ? (
                                <span>Swagger file upload</span>
                            ) : (
                                <span>By swagger url</span>
                            )}
                        </Typography>
                        <Typography type='caption' gutterBottom align='left' className='page-title-help'>
                            Fill the mandatory fields (Name, Version, Context) and create the API. Configure advanced
                            configurations later.
                        </Typography>
                    </Paper>
                </Grid>
                <Grid item xs={12} className='page-content'>
                    <form onSubmit={this.handleSubmit} className='login-form'>
                        <AppBar position='static' color='default'>
                            <Toolbar>
                                <RadioGroup
                                    aria-label='inputType'
                                    name='inputType'
                                    value={this.state.uploadMethod}
                                    onChange={this.handleUploadMethodChange}
                                    className='horizontal'
                                >
                                    <FormControlLabel value='file' control={<Radio />} label='File' />
                                    <FormControlLabel value='url' control={<Radio />} label='Url' />
                                </RadioGroup>
                            </Toolbar>
                        </AppBar>

                        {this.state.uploadMethod === 'file' && (
                            <FormControl className='horizontal dropzone-wrapper'>
                                <div className='dropzone'>
                                    <Dropzone onDrop={this.onDrop} multiple={false}>
                                        <p>Try dropping some files here, or click to select files to upload.</p>
                                    </Dropzone>
                                </div>
                                <aside>
                                    <h2>Uploaded files</h2>
                                    <ul>
                                        {this.state.files.map(f => (
                                            <li key={f.name}>
                                                {f.name} - {f.size} bytes
                                            </li>
                                        ))}
                                    </ul>
                                </aside>
                            </FormControl>
                        )}
                        {this.state.uploadMethod === 'url' && (
                            <FormControl className='horizontal full-width'>
                                <TextField
                                    id='swaggerUrl'
                                    label='Swagger Url'
                                    type='text'
                                    name='swaggerUrl'
                                    margin='normal'
                                    style={{ width: '100%' }}
                                    value={this.state.swaggerUrl}
                                    onChange={this.swaggerUrlChange}
                                />
                            </FormControl>
                        )}
                        <FormControl className='horizontal'>
                            {/* Allowing to create an API from swagger definition, based on scopes */}
                            <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.APIS}>
                                <Button raised color='primary' type='submit' className='button-left'>
                                    Create
                                </Button>
                            </ScopeValidation>
                            <Button raised onClick={() => this.props.history.push('/api/create/home')}>
                                Cancel
                            </Button>
                        </FormControl>
                    </form>
                    <Snackbar
                        anchorOrigin={{
                            vertical: 'top',
                            horizontal: 'center',
                        }}
                        open={this.state.open}
                        autoHideDuration={6e3}
                        onClose={this.handleRequestClose}
                        SnackbarContentProps={{
                            'aria-describedby': 'message-id',
                        }}
                        message={<span id='message-id'>{this.state.message}</span>}
                        action={[
                            <IconButton
                                key='close'
                                aria-label='Close'
                                color='inherit'
                                onClick={this.handleRequestClose}
                            >
                                <CloseIcon />
                            </IconButton>,
                        ]}
                    />
                </Grid>
            </Grid>
        );
    }
}

ApiCreateSwagger.propTypes = {
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
};

export default ApiCreateSwagger;
