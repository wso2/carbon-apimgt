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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React, { Component } from 'react';
import {
    withStyles,
    RadioGroup,
    Radio,
    FormControl,
    FormControlLabel,
} from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Dropzone from 'react-dropzone';
import classNames from 'classnames';
import Backup from '@material-ui/icons/Backup';

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
        this.onDrop = this.onDrop.bind(this);
        this.openAPIUrlChange = this.openAPIUrlChange.bind(this);
        this.handleUploadMethodChange = this.handleUploadMethodChange.bind(this);
    }

    /**
     * Handle OpenAPI file ondrop action when user drag and drop file to dopzone, This is passed through props
     * to child component
     * @param {Object} newFiles File object passed from DropZone library
     * @memberof ApiCreateOpenAPI
     */
    onDrop(newFiles) {
        const { setOpenAPIFiles, valid, setValid } = this.props;
        const validUpdated = { ...valid };
        validUpdated.openAPIFile.empty = newFiles.length === 0;
        setOpenAPIFiles(newFiles);
        setValid(validUpdated);
    }

    /**
     * Update openAPIUrl when input get changed
     * @param {React.SyntheticEvent} event Event triggered when URL input field changed
     * @memberof ApiCreateOpenAPI
     */
    openAPIUrlChange(event) {
        const { setOpenAPIUrl, valid, setValid } = this.props;
        const openAPIUrl = event.target.value;
        const validUpdated = { ...valid };
        validUpdated.openAPIUrl.empty = !openAPIUrl;
        setOpenAPIUrl(openAPIUrl);
        setValid(validUpdated);
    }

    handleUploadMethodChange(e, value) {
        const { setUploadMethod } = this.props;
        setUploadMethod(value);
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof ProvideWSDL
     */
    render() {
        const {
            uploadMethod, openAPIUrl, files, valid,
        } = this.props;
        const { classes } = this.props;
        return (
            <React.Fragment>
                <form>
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
                        <FormControl className={classes.FormControl}>
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
                                            <Typography variant='body1' gutterBottom>
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
                        <FormControl className={classes.FormControl}>
                            <TextField
                                error={valid.openAPIUrl.empty}
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
                                value={openAPIUrl}
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
    classes: PropTypes.shape({
        FormControl: PropTypes.shape({}).isRequired,
        radioWrapper: PropTypes.shape({}).isRequired,
        fileNameWrapper: PropTypes.shape({}).isRequired,
        dropZone: PropTypes.shape({}).isRequired,
        fileName: PropTypes.shape({}).isRequired,
        dropZoneErrorBox: PropTypes.shape({}).isRequired,
        dropZoneIcon: PropTypes.shape({}).isRequired,
        dropZoneError: PropTypes.shape({}).isRequired,
    }).isRequired,
    valid: PropTypes.shape({
        openAPIFile: PropTypes.shape({
            empty: PropTypes.bool.isRequired,
        }).isRequired,
        openAPIUrl: PropTypes.shape({
            empty: PropTypes.bool.isRequired,
        }).isRequired,
    }).isRequired,
    setOpenAPIFiles: PropTypes.func.isRequired,
    setOpenAPIUrl: PropTypes.func.isRequired,
    setValid: PropTypes.func.isRequired,
    setUploadMethod: PropTypes.func.isRequired,
    uploadMethod: PropTypes.string.isRequired,
    openAPIUrl: PropTypes.string.isRequired,
    files: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        size: PropTypes.number.isRequired,
    }).isRequired).isRequired,
};

export default withStyles(styles)(ProvideOpenAPI);
