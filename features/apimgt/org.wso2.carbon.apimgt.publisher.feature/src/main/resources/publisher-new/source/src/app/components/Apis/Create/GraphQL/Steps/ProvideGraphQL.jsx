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
    FormHelperText,
} from '@material-ui/core';
import ErrorOutline from '@material-ui/icons/ErrorOutline';
import Typography from '@material-ui/core/Typography';
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
 * @class ProvideGraphQL
 * @extends {Component}
 */
class ProvideGraphQL extends Component {
    /**
     * Creates an instance of ProvideGraphQL.
     * @param {any} props @inheritDoc
     * @memberof ProvideGraphQL
     */
    constructor(props) {
        super(props);
        const { file } = props;
        this.state = {
            file,
            isValid: null,
            errorMessage: '',
            loading: false,
        };
        this.validateGraphQL = this.validateGraphQL.bind(this);
    }

    /**
     * @inheritDoc
     * @param {any} nextProps New props received
     * @memberof ProvideGraphQL
     */
    componentWillReceiveProps(nextProps) {
        const { validate, updateGraphQLValidity } = nextProps;
        if (validate) {
            this.validateGraphQL(updateGraphQLValidity);
        }
    }

    handleUploadFile = (acceptedFiles) => {
        this.setState({ file: acceptedFiles[0] }, () => {
            this.validateGraphQL();
        });
    };

    /**
     *
     * @param validity {Function} Call back function to trigger after pass/fail the validation
     */
    validateGraphQL() {
        // do not invoke callback in case of React SyntheticMouseEvent
        const { file } = this.state;
        const { valid, updateFileErrors, updateGraphQLBean } = this.props;
        this.setState({ loading: true });
        const newAPI = new API();
        let promisedValidation = {};
        const graphQLBean = {};
        const validNew = JSON.parse(JSON.stringify(valid));

        if (!file) {
            // Update the parent's state
            validNew.graphQLFile.empty = true;
            updateFileErrors(validNew);
            return;
        }
        // Update the parent's state
        validNew.graphQLFile.empty = false;
        updateFileErrors(validNew);
        promisedValidation = newAPI.validateGraphQLFile(file);

        promisedValidation
            .then((response) => {
                const { isValid, graphQLInfo } = response.obj;
                graphQLBean.info = graphQLInfo;
                graphQLBean.file = file;
                validNew.graphQLFile.invalidFile = false;
                updateFileErrors(validNew);
                this.setState({ isValid, loading: false, file });
                updateGraphQLBean(graphQLBean);
            })
            .catch((error) => {
                // Update the parent's state
                validNew.graphQLFile.invalidFile = true;
                updateFileErrors(validNew);
                updateGraphQLBean(graphQLBean);
                const response = error.response && error.response.obj;
                const message =
                    'Error while validating GraphQL!! ' + response && '[' + response.message + '] '
                    + response.description;
                this.setState({ isValid: false, errorMessage: message, loading: false });
                console.error(error);
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof ProvideGraphQL
     */
    render() {
        const {
            isValid, errorMessage, file, loading,
        } = this.state;
        const { classes, valid } = this.props;
        const error = isValid === false; // Because of null case, which means validation haven't done yet
        if (loading) {
            return <Progress error={error} />;
        }
        return (
            <React.Fragment>
                <React.Fragment>
                    {file && (
                        <div className={classes.fileNameWrapper}>
                            <Typography variant='subtitle2' gutterBottom>
                                <FormattedMessage id='uploaded.file' defaultMessage='Uploaded file' /> :
                            </Typography>
                            <div className={classes.fileName}>
                                <Typography variant='body2' gutterBottom>
                                    {file.name} - {file.size} bytes
                                </Typography>
                            </div>
                        </div>
                    )}
                    {valid.graphQLFile.invalidFile && (
                        <div className={classes.errorMessageWrapper}>
                            <ErrorOutline className={classes.errorIcon} />
                            <Typography variant='body2' gutterBottom className={classes.errorMessage}>
                                {errorMessage}
                            </Typography>
                        </div>
                    )}
                    <Dropzone
                        onDrop={this.handleUploadFile}
                        multiple={false}
                        className={classNames(classes.dropZone, {
                            [classes.dropZoneErrorBox]: valid.graphQLFile.empty,
                        })}
                    >
                        <Backup className={classes.dropZoneIcon} />
                        <div>
                            <FormattedMessage
                                id='try.dropping.schema.file.here.or.click.to.select.schema.to.upload'
                                defaultMessage='Try dropping schema file here, or click to select schema to upload.'
                            />
                        </div>
                    </Dropzone>
                    <FormHelperText className={classes.errorMessage}>
                        {valid.graphQLFile.empty && (
                            <FormattedMessage id='error.empty' defaultMessage='This field can not be empty.' />
                        )}
                        {valid.graphQLFile.invalidFile && (
                            <FormattedMessage id='error.invalid.graphql.file' defaultMessage='Invalid GraphQL File' />
                        )}
                    </FormHelperText>
                </React.Fragment>
            </React.Fragment>
        );
    }
}

ProvideGraphQL.defaultProps = {
    file: {},
};

ProvideGraphQL.propTypes = {
    updateGraphQLBean: PropTypes.func.isRequired,
    validate: PropTypes.bool.isRequired,
    valid: PropTypes.shape({}).isRequired,
    file: PropTypes.shape({}),
    classes: PropTypes.shape({}).isRequired,
    updateFileErrors: PropTypes.func.isRequired,
    updateGraphQLValidity: PropTypes.func.isRequired,
};

export default withStyles(styles)(ProvideGraphQL);
