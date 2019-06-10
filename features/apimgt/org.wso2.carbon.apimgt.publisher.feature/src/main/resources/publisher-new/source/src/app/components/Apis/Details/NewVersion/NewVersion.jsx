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
/* eslint no-param-reassign: ["error", { "props": false }] */
import React from 'react';
import { Link, Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Radio from '@material-ui/core/Radio';
import { FormHelperText, FormLabel } from '@material-ui/core';
import Alert from 'AppComponents/Shared/Alert';

import ApiContext from '../components/ApiContext';

const styles = theme => ({
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


class CreateNewVersion extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isDefaultVersion: 'no',
            valid: { version: { empty: false, alreadyExists: false } },
        };
    }

    handleDefaultVersionChange = () => (event) => {
        const { value } = event.target;
        this.setState({
            isDefaultVersion: value,
        });
    };

    handleVersionChange = () => (event) => {
        const { value } = event.target;
        this.setState({
            newVersion: value,
            valid: { version: { empty: !value, alreadyExists: false } },
        });
    };

    handleSubmit(api, newVersion, isDefaultVersion) {
        if (!newVersion) {
            this.setState({ valid: { version: { empty: true } } });
            return;
        }
        const isDefaultVersionBool = isDefaultVersion === 'yes';
        api.createNewAPIVersion(newVersion, isDefaultVersionBool)
            .then((response) => {
                this.setState({
                    redirectToReferrer: true,
                    apiId: response.obj.id,
                });
                Alert.info('Successfully created new version "' + newVersion + '"');
            })
            .catch((error) => {
                if (error.status === 409) {
                    this.setState({ valid: { version: { alreadyExists: true } } });
                } else {
                    Alert.error('Something went wrong while creating a new version!. Error: ' + error.status);
                }
            });
    }

    render() {
        const { classes } = this.props;
        const {
            isDefaultVersion, newVersion, redirectToReferrer, apiId, valid,
        } = this.state;
        if (redirectToReferrer) {
            return (
                <Redirect to={'/apis/' + apiId + '/overview'} />
            );
        }

        let helperText = 'Provide new version';
        if (valid.version.empty) {
            helperText = 'This field cannot be empty';
        } else if (valid.version.alreadyExists) {
            helperText = 'An API with version "' + newVersion + '" already exists.';
        }

        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        Create New Version
                    </Typography>
                </div>
                <ApiContext.Consumer>
                    {({ api }) => (
                        <Grid container spacing={24}>
                            <Grid item xs={12}>
                                <Paper className={classes.root} elevation={1}>
                                    <FormControl margin='normal' className={classes.FormControlOdd}>
                                        <TextField
                                            fullWidth
                                            id='name'
                                            error={valid.version.empty || valid.version.alreadyExists}
                                            label='New Version'
                                            helperText={helperText}
                                            type='text'
                                            name='name'
                                            placeholder='Eg: 2.0.0'
                                            value={newVersion}
                                            onChange={this.handleVersionChange()}
                                            margin='normal'
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            autoFocus
                                        />
                                    </FormControl>
                                    <FormControl margin='normal' className={classes.FormControl}>
                                        <FormLabel className={classes.FormLabel} component='legend'>
                                            Make this the default version
                                            <Tooltip
                                                placement='top'
                                                classes={{
                                                    tooltip: classes.htmlTooltip,
                                                }}
                                                disableHoverListener
                                                title={
                                                    <React.Fragment>
                                                        Marks one API version in a group as the default so that it
                                                        can be invoked without specifying the version number in the
                                                        URL. For example, if you mark http://host:port/youtube/2.0
                                                        as the default API, requests made to
                                                        http://host:port/youtube/ are automatically routed to
                                                        version 2.0. If you mark an unpublished API as the default,
                                                        the previous default published API will still be used as the
                                                        default until the new default API is published.
                                                    </React.Fragment>
                                                }
                                            >
                                                <Button className={classes.helpButton}>
                                                    <HelpOutline className={classes.helpIcon} />
                                                </Button>
                                            </Tooltip>
                                        </FormLabel>
                                        <RadioGroup
                                            name='isDefaultVersion'
                                            className={classes.group}
                                            value={isDefaultVersion}
                                            onChange={this.handleDefaultVersionChange()}
                                        >
                                            <FormControlLabel value='yes' control={<Radio />} label='Yes' />
                                            <FormControlLabel value='no' control={<Radio />} label='No' />
                                        </RadioGroup>
                                        <FormHelperText>Indicate whether API should be the default version among the
                                            group of APIs with the same name
                                        </FormHelperText>
                                    </FormControl>
                                </Paper>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={16}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    onClick={() => this.handleSubmit(api, newVersion, isDefaultVersion)}
                                                >
                                                    <FormattedMessage id='create' defaultMessage='Create' />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button>
                                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Grid>
                        </Grid>
                    )}
                </ApiContext.Consumer>
            </div>
        );
    }
}

CreateNewVersion.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(CreateNewVersion);
