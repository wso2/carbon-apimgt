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
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage } from 'react-intl';
import Backup from '@material-ui/icons/Backup';
import classNames from 'classnames';

import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api.js';

const styles = theme => ({
    root: {
        width: theme.custom.contentAreaWidth,
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    buttonProgress: {
        position: 'relative',
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 6.25,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    subTitle: {
        color: theme.palette.grey[500],
    },
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
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
    buttonSection: {
        paddingTop: theme.spacing.unit * 2,
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
            valid: {
                swaggerUrl: { empty: false, invalidUrl: false },
                swaggerFile: { empty: false, invalidFile: false },
            },
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
        this.state.files = files;
        this.setState((oldState) => {
            const { valid, files } = oldState;
            const validUpdated = valid;
            validUpdated.swaggerFile.empty = files.length === 0;
            return { valid: validUpdated, files };
        });
    }

    /**
     * Update SwaggerURL when input get changed
     * @param {React.SyntheticEvent} e Event triggered when URL input field changed
     * @memberof ApiCreateSwagger
     */
    swaggerUrlChange(event) {
        this.state.swaggerUrl = event.target.value;
        this.setState(({ valid, swaggerUrl }) => {
            const validUpdated = valid;
            validUpdated.swaggerUrl.empty = !swaggerUrl;
            return { valid: validUpdated, swaggerUrl };
        });
    }

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file
     * and make a blob
     * and the send it over REST API.
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    handleSubmit = (e) => {
        e.preventDefault();
        if ((this.state.uploadMethod === 'file' && this.state.files.length === 0) || (this.state.uploadMethod === 'url' && !this.state.swaggerUrl)) {
            this.setState(({ valid, files, swaggerUrl }) => {
                const validUpdated = valid;
                validUpdated.swaggerFile.empty = files.length === 0;
                validUpdated.swaggerUrl.empty = !swaggerUrl;
                return { valid: validUpdated };
            });
            return;
        }
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
                Alert.error('Select a OpenAPI file to upload.');
                console.log('Select a OpenAPI file to upload.');
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
        const redirectURL = '/apis/'  + uuid + '/overview';
        this.props.history.push(redirectURL);
    };

    /**
     *
     * @returns {React.Component} @inheritDoc
     * @memberof ApiCreateSwagger
     */
    render() {
        const {
            uploadMethod, files, swaggerUrl, loading, valid,
        } = this.state;
        const { classes } = this.props;
        return (
            <Grid container spacing={24} className={classes.root}>
                <Grid item xs={12} md={6}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            {this.state.uploadMethod === 'file' ? (
                                <span>
                                    <FormattedMessage id='swagger.file.upload' defaultMessage='OpenAPI file upload' />
                                </span>
                            ) : (
                                <span>
                                    <FormattedMessage id='by.swagger.url' defaultMessage='By OpenAPI url' />
                                </span>
                            )}
                        </Typography>
                        <Typography type='caption' gutterBottom align='left'>
                            <FormattedMessage id='fill.the.mandatory.fields' defaultMessage={'Fill the mandatory fields (Name, Version, Context) and create the API. Configure advanced configurations later.'} />
                        </Typography>
                    </div>
                    <form onSubmit={this.handleSubmit}>
                        <FormControl margin='normal' className={classes.FormControl}>
                            <RadioGroup aria-label='inputType' name='inputType' value={uploadMethod} onChange={this.handleUploadMethodChange} className={classes.radioWrapper}>
                                <FormControlLabel value='file' control={<Radio />} label={<FormattedMessage id='file' defaultMessage='File' />} />
                                <FormControlLabel value='url' control={<Radio />} label={<FormattedMessage id='url' defaultMessage='URL' />} />
                            </RadioGroup>
                        </FormControl>
                        {uploadMethod === 'file' && (
                            <FormControl className={classes.FormControlOdd}>
                                {files &&
                                    files.length > 0 && (
                                    <div className={classes.fileNameWrapper}>
                                        <Typography variant='subtitle2' gutterBottom>
                                            <FormattedMessage id='uploaded.file' defaultMessage='Uploaded file' /> :
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
                                        [classes.dropZoneErrorBox]: valid.swaggerFile.empty,
                                    })}
                                >
                                    <Backup className={classes.dropZoneIcon} />
                                    <div>
                                        <FormattedMessage id='try.dropping.some.files.here.or.click.to.select.files.to.upload' defaultMessage={'Try dropping some files here, or click to select files to upload.'} />
                                    </div>
                                </Dropzone>
                                {valid.swaggerFile.empty && (
                                    <Typography variant='caption' gutterBottom className={classes.dropZoneError}>
                                        <FormattedMessage id='error.empty' defaultMessage='This field can not be empty.' />
                                    </Typography>
                                )}
                            </FormControl>
                        )}
                        {uploadMethod === 'url' && (
                            <FormControl className={classes.FormControlOdd}>
                                <TextField
                                    error={valid.swaggerUrl.empty}
                                    fullWidth
                                    id='swaggerUrl'
                                    label='Swagger Url'
                                    placeholder='eg: http://petstore.swagger.io/v2/swagger.json'
                                    helperText={valid.swaggerUrl.empty ? <FormattedMessage id='error.empty' defaultMessage='This field can not be empty.' /> : <FormattedMessage id='create.new.swagger.help' defaultMessage='Give a swagger definition such as http://petstore.swagger.io/v2/swagger.json' />}
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    type='text'
                                    name='swaggerUrl'
                                    margin='normal'
                                    value={swaggerUrl}
                                    onChange={this.swaggerUrlChange}
                                />
                            </FormControl>
                        )}

                        <FormControl>
                            <Grid container direction='row' alignItems='flex-start' spacing={16} className={classes.buttonSection}>
                                <Grid item>
                                    {/* Allowing to create an API from swagger definition, based on scopes */}
                                    <ScopeValidation resourceMethod={resourceMethod.POST} resourcePath={resourcePath.APIS}>
                                        <Button variant='contained' disabled={loading} color='primary' type='submit'>
                                            <FormattedMessage id='create.btn' defaultMessage='Create' />
                                        </Button>
                                        {loading && <CircularProgress size={24} className={classes.buttonProgress} />}
                                    </ScopeValidation>
                                </Grid>

                                <Grid item>
                                    <Button raised onClick={() => this.props.history.push('/apis')}>
                                        <FormattedMessage id='cancel.btn' defaultMessage='Cancel' />
                                    </Button>
                                </Grid>
                            </Grid>
                        </FormControl>
                    </form>
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
    valid: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiCreateSwagger);
