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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import { Link, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import { FormattedMessage, injectIntl } from 'react-intl';
import FormControl from '@material-ui/core/FormControl';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Radio from '@material-ui/core/Radio';
import FormLabel from '@material-ui/core/FormLabel';
import Alert from 'AppComponents/Shared/Alert';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const styles = (theme) => ({
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 20,
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    FormLabel: {
        transform: 'translate(0, 1.5px) scale(0.75)',
        transformOrigin: 'top left',
    },
    buttonWrapper: {
        paddingTop: 20,
    },
    root: {
        padding: 20,
        marginTop: 20,
    },
    group: {
        flexDirection: 'row',
    },
    helpButton: {
        padding: 0,
        minWidth: 20,
    },
    helpIcon: {
        fontSize: 16,
    },
    htmlTooltip: {
        backgroundColor: '#f5f5f9',
        color: 'rgba(0, 0, 0, 0.87)',
        maxWidth: 220,
        fontSize: theme.typography.pxToRem(14),
        border: '1px solid #dadde9',
        '& b': {
            fontWeight: theme.typography.fontWeightMedium,
        },
    },
});

/**
 * React component to create a new version of an API
 * @class CreateNewVersion
 * @extends {Component}
 */
class CreateNewVersion extends React.Component {
    /**
     * Creates an instance of CreateNewVersion.
     * @param {any} props @inheritDoc
     * @memberof CreateNewVersion
     */
    constructor(props) {
        super(props);
        this.state = {
            isDefaultVersion: 'no',
            serviceVersion: null,
            versionList: [],
            valid: {
                version: {
                    empty: false,
                    alreadyExists: false,
                    hasSpecialChars: false,
                    MaxLengthExceeds: false,
                },
            },
        };
    }

    componentDidMount() {
        const { api } = this.props;
        if (api.serviceInfo !== null) {
            const promisedServices = ServiceCatalog.getServiceByName(api.serviceInfo);
            promisedServices.then((data) => {
                const array = data.list.map((item) => item.version);
                this.setState({ versionList: array });
            }).catch((error) => {
                console.error(error);
                Alert.error('Error while loading services version');
            });
        }
    }

    handleDefaultVersionChange = () => (event) => {
        const { value } = event.target;
        this.setState({
            isDefaultVersion: value,
        });
    };

    handleServiceVersionChange = () => (event) => {
        const { value } = event.target;
        this.setState({
            serviceVersion: value,
        });
    };

    handleVersionChange = () => (event) => {
        const { value } = event.target;
        this.setState({
            newVersion: value,
            valid: {
                version: {
                    empty: !value,
                    alreadyExists: false,
                    hasSpecialChars: this.hasSpecialChars(value),
                    MaxLengthExceeds: this.isMaxLengthExceeds(value),
                },
            },
        });
    };

    /**
     * Handles the submit action for new version creation
     *
     * @param {API} api current API
     * @param {string} newVersion new version to create
     * @param {string} isDefaultVersion specifies whether the new API should be marked as default version ('yes' | 'no')
     */
    handleSubmit(api, newVersion, isDefaultVersion, serviceVersion) {
        if (!newVersion) {
            this.setState({ valid: { version: { empty: true } } });
            return;
        }
        const isDefaultVersionBool = isDefaultVersion === 'yes';
        const { intl } = this.props;
        api.createNewAPIVersion(newVersion, isDefaultVersionBool, serviceVersion)
            .then((response) => {
                this.setState({
                    redirectToReferrer: true,
                    apiId: response.obj.id,
                });
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.NewVersion.NewVersion.success',
                    defaultMessage: 'Successfully created new version ',
                }) + newVersion);
            })
            .catch((error) => {
                if (error.status === 409) {
                    this.setState({ valid: { version: { alreadyExists: true } } });
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.NewVersion.NewVersion.error',
                        defaultMessage: 'Something went wrong while creating a new version!. Error: ',
                    }) + error.status);
                }
            });
    }

    /**
     *
     * @param {String} value String to be checked for special characters
     * @returns {Boolean} Has special character or not
     */
    hasSpecialChars(value) {
        if (/^[^~!@#;:%^*()+={}|\\<>"',&/$]+$/.test(value)) {
            return false;
        } else {
            return true;
        }
    }

    isMaxLengthExceeds(value) {
        if (value.length > 30) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Renders the CreateNewVersion component.
     * Once new version is created, redirects to the overview page of the new API.
     * @returns {*} CreateNewVersion component
     */
    render() {
        const { classes, api } = this.props;
        const {
            isDefaultVersion, newVersion, redirectToReferrer, apiId, valid, serviceVersion, versionList,
        } = this.state;
        if (redirectToReferrer) {
            return <Redirect to={'/apis/' + apiId + '/overview'} />;
        }

        let helperText = '';
        if (valid.version.empty) {
            helperText = 'This field cannot be empty';
        } else if (valid.version.alreadyExists) {
            helperText = 'An API with version "' + newVersion + '" already exists.';
        } else if (valid.version.hasSpecialChars) {
            helperText = 'API Version should not contain special characters';
        } else if (valid.version.MaxLengthExceeds) {
            helperText = 'API version exceeds maximum length of 30 characters';
        }

        return (
            <>
                <Container maxWidth='md'>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.NewVersion.NewVersion.create.new.version'
                                defaultMessage='Create New Version'
                            />
                        </Typography>
                    </div>
                    <Grid container spacing={7}>
                        <Grid item xs={12}>
                            <Paper className={classes.root} elevation={0}>
                                <FormControl margin='normal' className={classes.FormControlOdd}>
                                    <TextField
                                        fullWidth
                                        id='newVersion'
                                        error={
                                            valid.version.empty
                                            || valid.version.alreadyExists
                                            || valid.version.hasSpecialChars
                                            || valid.version.MaxLengthExceeds
                                        }
                                        label={(
                                            <FormattedMessage
                                                id='Apis.Details.NewVersion.NewVersion.new.version'
                                                defaultMessage='New Version'
                                            />
                                        )}
                                        helperText={
                                            helperText
                                        }
                                        type='text'
                                        name='newVersion'
                                        placeholder='Eg: 2.0.0'
                                        value={newVersion}
                                        variant='outlined'
                                        onChange={this.handleVersionChange()}
                                        margin='normal'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        autoFocus
                                    />
                                </FormControl>
                                {api.serviceInfo && (
                                    <FormControl margin='normal' className={classes.FormControlOdd}>
                                        <TextField
                                            id='version-selector'
                                            select
                                            label={(
                                                <FormattedMessage
                                                    id='Apis.Details.NewVersion.NewVersion.service.version'
                                                    defaultMessage='Service Version'
                                                />
                                            )}
                                            name='selectVersion'
                                            onChange={this.handleServiceVersionChange()}
                                            margin='dense'
                                            variant='outlined'
                                        >
                                            {versionList && versionList.map((item) => (
                                                <MenuItem value={item}>
                                                    {item}
                                                </MenuItem>
                                            ))}
                                        </TextField>
                                    </FormControl>
                                )}
                                <FormControl margin='normal' className={classes.FormControl}>
                                    <FormLabel className={classes.FormLabel} component='legend'>
                                        <FormattedMessage
                                            id='Apis.Details.NewVersion.NewVersion.default'
                                            defaultMessage='Make this the default version'
                                        />
                                        <Tooltip
                                            placement='top'
                                            classes={{
                                                tooltip: classes.htmlTooltip,
                                            }}
                                            title={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Details.NewVersion.NewVersion.tooltip'
                                                        defaultMessage={
                                                            'Indicates if this is the default version of the API. '
                                                            + 'If an API is invoked without specifying a version, '
                                                            + 'the API Gateway will route the request to the default '
                                                            + 'version of the API.'
                                                        }
                                                    />
                                                </>
                                            )}
                                            interactive
                                            aria-label='Default Version Selector'
                                            tabIndex='-1'
                                        >
                                            <Button className={classes.helpButton}>
                                                <HelpOutline className={classes.helpIcon} />
                                            </Button>
                                        </Tooltip>
                                    </FormLabel>
                                    <RadioGroup
                                        name='isDefaultVersion'
                                        id='isDefaultVersion'
                                        className={classes.group}
                                        value={isDefaultVersion}
                                        onChange={this.handleDefaultVersionChange()}
                                    >
                                        <FormControlLabel value='yes' control={<Radio color='primary' />} label='Yes' />
                                        <FormControlLabel value='no' control={<Radio color='primary' />} label='No' />
                                    </RadioGroup>
                                </FormControl>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={1}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    id='createBtn'
                                                    onClick={() => this.handleSubmit(api, newVersion, isDefaultVersion,
                                                        serviceVersion)}
                                                    disabled={
                                                        valid.version.empty
                                                        || valid.version.alreadyExists
                                                        || valid.version.hasSpecialChars
                                                        || valid.version.MaxLengthExceeds
                                                        || api.isRevision
                                                    }
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.NewVersion.NewVersion.create'
                                                        defaultMessage='Create'
                                                    />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button id='cancelBtn'>
                                                    <FormattedMessage
                                                        id='Apis.Details.NewVersion.NewVersion.cancel'
                                                        defaultMessage='Cancel'
                                                    />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Paper>
                        </Grid>
                    </Grid>
                </Container>
            </>
        );
    }
}

CreateNewVersion.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withAPI(withStyles(styles)(CreateNewVersion)));
