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
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage } from 'react-intl';

import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api.js';

const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    buttonProgress: {
        color: 'green',
        position: 'relative',
        marginTop: -20,
        marginLeft: -50,
    },
});
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
            loading: false,
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
        this.setState({ loading: true });
        const inputType = this.state.uploadMethod;
        if (inputType === 'url') {
            const url = this.state.swaggerUrl;
            const data = { url, type: 'swagger-url' };
            const newApi = new API();
            newApi
                .create(data)
                .then(this.createAPICallback)
                .catch((errorResponse) => {
                    Alert.error('Something went wrong while creating the API!');
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
            if (this.state.files.length === 0) {
                Alert.error('Select a swagger file to upload.');
                console.log('Select a swagger file to upload.');
                return;
            }
            const swagger = this.state.files[0];
            const newApi = new API();
            newApi
                .create(swagger)
                .then(this.createAPICallback)
                .catch((errorResponse) => {
                    Alert.error('Something went wrong while creating the API!');
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

    createAPICallback = (response) => {
        const uuid = JSON.parse(response.data).id;
        const redirectURL = '/apis/' + uuid + '/overview';
        this.props.history.push(redirectURL);
    };

    /**
     *
     * @returns {React.Component} @inheritDoc
     * @memberof ApiCreateSwagger
     */
    render() {
        const {
            uploadMethod, files, swaggerUrl, loading,
        } = this.state;
        const { classes } = this.props;
        return (
            <Grid container className={classes.root} spacing={0} justify='center'>
                <Grid item md={10}>
                    <Paper className={classes.paper}>
                        <Typography className='page-title' type='display2' gutterBottom>
                            <FormattedMessage id='create.new.api.swagger' defaultMessage='Create New API - ' />
                            {this.state.uploadMethod === 'file' ? (
                                <span>
                                    <FormattedMessage id='swagger.file.upload' defaultMessage='Swagger file upload' />
                                </span>
                            ) : (
                                <span><FormattedMessage id='by.swagger.url' defaultMessage='By swagger url' /></span>
                            )}
                        </Typography>
                        <Typography type='caption' gutterBottom align='left' className='page-title-help'>
                            <FormattedMessage
                                id='fill.the.mandatory.fields'
                                defaultMessage={'Fill the mandatory fields (Name, Version, Context) '
                                + 'and create the API. Configure advanced configurations later.'}
                            />
                        </Typography>

                        <form onSubmit={this.handleSubmit} className='login-form'>
                            <Grid item>
                                <RadioGroup
                                    aria-label='inputType'
                                    name='inputType'
                                    value={uploadMethod}
                                    onChange={this.handleUploadMethodChange}
                                    className='horizontal'
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
                            </Grid>
                            <Grid item>
                                {uploadMethod === 'file' && (
                                    <FormControl className='horizontal dropzone-wrapper'>
                                        <div className='dropzone'>
                                            <Dropzone onDrop={this.onDrop} multiple={false}>
                                                <p><FormattedMessage
                                                    id='try.dropping.some.files.here.or.click.to.select.files.to.upload'
                                                    defaultMessage={'Try dropping some files here, or click to select' +
                                                    'files to upload.'}
                                                />
                                                </p>
                                            </Dropzone>
                                        </div>
                                        <aside>
                                            <h2><FormattedMessage id='uploaded.files' defaultMessage='Uploaded files' />
                                            </h2>
                                            <ul>
                                                {files.map(f => (
                                                    <li key={f.name}>
                                                        {f.name} - {f.size} bytes
                                                    </li>
                                                ))}
                                            </ul>
                                        </aside>
                                    </FormControl>
                                )}
                                {uploadMethod === 'url' && (
                                    <FormControl className='horizontal full-width'>
                                        <TextField
                                            id='swaggerUrl'
                                            label='Swagger Url'
                                            type='text'
                                            name='swaggerUrl'
                                            required
                                            margin='normal'
                                            value={swaggerUrl}
                                            onChange={this.swaggerUrlChange}
                                        />
                                    </FormControl>
                                )}
                            </Grid>

                            <Grid item>
                                <FormControl>
                                    <Grid container direction='row' alignItems='flex-start' spacing={16}>
                                        <Grid item>
                                            {/* Allowing to create an API from swagger definition, based on scopes */}
                                            <ScopeValidation
                                                resourceMethod={resourceMethod.POST}
                                                resourcePath={resourcePath.APIS}
                                            >
                                                <Button
                                                    variant='outlined'
                                                    disabled={loading}
                                                    color='primary'
                                                    type='submit'
                                                >
                                                    <FormattedMessage id='create.btn' defaultMessage='Create' />
                                                </Button>
                                                {loading && (
                                                    <CircularProgress size={24} className={classes.buttonProgress} />
                                                )}
                                            </ScopeValidation>
                                        </Grid>

                                        <Grid item>
                                            <Button raised onClick={() => this.props.history.push('/apis')}>
                                                <FormattedMessage id='cancel.btn' defaultMessage='Cancel' />
                                            </Button>
                                        </Grid>
                                    </Grid>
                                </FormControl>
                            </Grid>
                        </form>
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

ApiCreateSwagger.propTypes = {
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiCreateSwagger);
