/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState } from 'react';
import API from 'AppData/api';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import Alert from 'AppComponents/Shared/Alert';
import settings from '../../../../../../site/public/conf/settings';

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
        display: 'flex',
        flexDirection: 'column',
        padding: 20,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(3),
    },
    titleLink: {
        color: theme.palette.primary.main,
        marginRight: theme.spacing(1),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    mainTitle: {
        paddingLeft: 0,
    },
    FormControl: {
        padding: `0 0 0 ${theme.spacing(1)}px`,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: `0 0 0 ${theme.spacing(1)}px`,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    FormControlLabel: {
        marginBottom: theme.spacing(1),
        marginTop: theme.spacing(1),
        fontSize: theme.typography.caption.fontSize,
    },
    buttonSection: {
        paddingTop: theme.spacing(3),
    },
    saveButton: {
        marginRight: theme.spacing(1),
    },
    helpText: {
        color: theme.palette.text.hint,
        marginTop: theme.spacing(1),
    },
    extraPadding: {
        paddingLeft: theme.spacing(2),
    },
    addNewOther: {
        paddingTop: 40,
    },
    titleGrid: {
        ' & .MuiGrid-item': {
            padding: 0,
            margin: 0,
        },
    },
    descriptionForm: {
        marginTop: theme.spacing(1),
    },
    progress: {
        marginLeft: theme.spacing(1),
    },
});

let application = {};
let mhistory;

const updateApplicationOwner = (
    applicationId,
    setUpdatingApplicationOwner,
    applicationList,
    intl,
) => {
    const valid = validateOwner(applicationList);
    if (valid.invalid) {
        Alert.error(valid.error);
        return;
    }
    const restApi = new API();
    setUpdatingApplicationOwner(true);
    const promisedUpdate = restApi.updateApplicationOwner(
        applicationId,
        application.owner,
    );
    promisedUpdate
        .then(() => {
            Alert.info(
                intl.formatMessage({
                    id:
                        'applications.change.application.owner.updated.successfully',
                    defaultMessage: 'Application owner updated successfully',
                }),
            );
            mhistory.push(settings.app.context + '/settings/applications');
        })
        .catch((error) => {
            const { response } = error;
            if (response.body) {
                const { description } = response.body;
                Alert.error(description);
            }
        })
        .finally(() => {
            application = {};
            setUpdatingApplicationOwner(false);
        });
};

const handleAplicationOwnerInput = ({ target: { id, value } }) => {
    application[id] = value;
};

const validateOwner = (applicationList) => {
    let valid = { invalid: false, error: '' };
    const { name, owner } = application;

    const applicationsWithSameName = applicationList.filter(
        (app) => app.name === name && app.owner === owner,
    );
    if (applicationsWithSameName.length > 0) {
        valid.error = `${owner} already has an application with name: ${name}`;
        valid.invalid = true;
    }

    // todo: Validate whether the owner is an existing subscriber or not.

    return valid;
};

const EditApplication = (props) => {
    const { classes, history, intl } = props;
    const { name, owner, applicationList } = props.location;
    const applicationId = props.match.params.applicationId;
    const [isUpdatingApplicationOwner, setUpdatingApplicationOwner] = useState(
        false,
    );
    mhistory = history;
    application = { owner: owner, name: name };
    return (
        <Grid container spacing={3}>
            <Grid item sm={12} md={12} />
            <Grid item sm={0} md={0} lg={2} />
            <Grid item sm={12} md={12} lg={8}>
                <Grid container spacing={5} className={classes.titleGrid}>
                    <Grid item md={12}>
                        <div className={classes.titleWrapper}>
                            <Link
                                to={
                                    settings.app.context +
                                    '/settings/applications'
                                }
                                className={classes.titleLink}
                            >
                                <Typography variant='h4'>
                                    <FormattedMessage
                                        id='contents.main.title.applications'
                                        defaultMessage='Applications'
                                    />
                                </Typography>
                            </Link>
                            <Icon>keyboard_arrow_right</Icon>
                            <Typography variant='h4'>
                                <FormattedMessage
                                    id='contents.application.change.application.owner'
                                    defaultMessage='Change Application Owner'
                                />
                            </Typography>
                        </div>
                    </Grid>
                    <Grid item md={12}>
                        <Paper elevation={0} className={classes.root}>
                            <FormControl margin='normal'>
                                <TextField
                                    id='name'
                                    label='Application Name'
                                    placeholder='Name'
                                    helperText={
                                        <FormattedMessage
                                            id='applications.change.application.owner.application.name'
                                            defaultMessage='Editing Application Name'
                                        />
                                    }
                                    fullWidth
                                    margin='normal'
                                    variant='outlined'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    value={name}
                                    disabled={true}
                                />
                            </FormControl>
                            <FormControl
                                margin='normal'
                                classes={{ root: classes.descriptionForm }}
                            >
                                <TextField
                                    id='owner'
                                    label='Owner'
                                    variant='outlined'
                                    placeholder='Owner'
                                    helperText={
                                        <FormattedMessage
                                            id='applications.change.application.owner.owner'
                                            defaultMessage='Owner'
                                        />
                                    }
                                    margin='normal'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    defaultValue={owner}
                                    multiline
                                    onChange={handleAplicationOwnerInput}
                                />
                            </FormControl>
                            <div className={classes.addNewOther}>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() =>
                                        updateApplicationOwner(
                                            applicationId,
                                            setUpdatingApplicationOwner,
                                            applicationList,
                                            intl,
                                        )
                                    }
                                    className={classes.saveButton}
                                    disabled={isUpdatingApplicationOwner}
                                >
                                    {isUpdatingApplicationOwner ? (
                                        <>
                                            <FormattedMessage
                                                id='applications.change.application.owner.saving'
                                                defaultMessage='Saving'
                                            />
                                            <CircularProgress
                                                size={16}
                                                classes={{
                                                    root: classes.progress,
                                                }}
                                            />
                                        </>
                                    ) : (
                                        <FormattedMessage
                                            id='applications.change.application.owner.save'
                                            defaultMessage='Save'
                                        />
                                    )}
                                </Button>
                                <Link
                                    to={
                                        settings.app.context +
                                        '/settings/applications'
                                    }
                                >
                                    <Button>
                                        <FormattedMessage
                                            id='applications.change.application.owner.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            </div>
                        </Paper>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};

export default injectIntl(withStyles(styles)(EditApplication));
