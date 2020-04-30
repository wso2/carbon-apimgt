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
import Chip from '@material-ui/core/Chip';
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
    subtitles: {
        marginRight: 20,
    },
});

let mgLabel = {};
let mhistory;
let valid = { name: { invalid: false } };

const validateMgLabelName = (id, value) => {
    mgLabel[id] = value;

    valid[id].invalid = !(value && value.length > 0);
    if (valid[id].invalid) {
        valid[id].error = 'Microgateway Label name cannot be empty';
    }

    if (/\s/.test(value)) {
        valid[id].invalid = true;
        valid[id].error = 'Microgateway Label name cannot have spaces';
    }

    if (!valid[id].invalid && /[!@#$%^&*(),?"{}[\]|<>\t\n]/i.test(value)) {
        valid[id].invalid = true;
        valid[id].error =
            'Microgateway Label name field contains special characters';
    }
    if (!valid[id].invalid) {
        valid[id].error = '';
    }
    return valid;
};

const addMgLabel = (intl, setCreatingMgLabel) => {
    valid = validateMgLabelName('name', mgLabel.name);
    if (valid.name.invalid) {
        console.log(valid.name.error);
        Alert.error(valid.name.error);
        return;
    }
    const restApi = new API();
    setCreatingMgLabel(true);
    const promisedCreateMgLabel = restApi.createMgLabel(
        mgLabel.name,
        mgLabel.description,
    );
    promisedCreateMgLabel
        .then(() => {
            Alert.info(
                intl.formatMessage({
                    id:
                        'microgateway.labels.create.new.category.added.successfully',
                    defaultMessage: 'Microgateway Label added successfully',
                }),
            );
            mhistory.push(settings.app.context + '/microgateway/labels');
        })
        .catch((error) => {
            const { response } = error;
            if (response.body) {
                const { description } = response.body;
                Alert.error(description);
            }
        })
        .finally(() => {
            mgLabel = {};
            setCreatingMgLabel(false);
        });
};

const handleMgLabelNameInput = ({ target: { id, value } }) => {
    validateMgLabelName(id, value);
};

const handleMgLabelDescriptionInput = ({ target: { id, value } }) => {
    mgLabel[id] = value;
};

const CreateMgLabel = (props) => {
    const { classes, history, intl } = props;
    mhistory = history;
    const [isCreatingMgLabel, setCreatingMgLabel] = useState(false);

    const api = { tags: ['abc', 'def'] };
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
                                    '/microgateway/labels'
                                }
                                className={classes.titleLink}
                            >
                                <Typography variant='h4'>
                                    <FormattedMessage
                                        id='contents.main.title.microgateway.labels'
                                        defaultMessage='Microgateway Labels'
                                    />
                                </Typography>
                            </Link>
                            <Icon>keyboard_arrow_right</Icon>
                            <Typography variant='h4'>
                                <FormattedMessage
                                    id='contents.microgateway.labels.create.new.category'
                                    defaultMessage='Create New Microgateway Label'
                                />
                            </Typography>
                        </div>
                    </Grid>
                    <Grid item md={12}>
                        <Paper elevation={0} className={classes.root}>
                            <FormControl margin='normal'>
                                <TextField
                                    id='name'
                                    label='Microgateway Label Name'
                                    placeholder='Name'
                                    helperText={
                                        <FormattedMessage
                                            id='microgateway.labels.create.new.category.short.description.name'
                                            defaultMessage='Enter Microgateway Label Name'
                                        />
                                    }
                                    fullWidth
                                    margin='normal'
                                    variant='outlined'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    onChange={handleMgLabelNameInput}
                                />
                            </FormControl>
                            <FormControl
                                margin='normal'
                                classes={{ root: classes.descriptionForm }}
                            >
                                <TextField
                                    id='description'
                                    label='Description'
                                    variant='outlined'
                                    placeholder='Short description about the label'
                                    helperText={
                                        <FormattedMessage
                                            id='microgateway.labels.create.new.category.short.description.description'
                                            defaultMessage='Short description about the Microgateway labels'
                                        />
                                    }
                                    margin='normal'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    multiline
                                    onChange={handleMgLabelDescriptionInput}
                                />
                            </FormControl>

                            {/* inserted here */}
                            <FormControl>
                                <TextField>
                                    <Grid item xs={12} md={6} lg={4}>
                                        <Typography
                                            component='p'
                                            variant='subtitle2'
                                            className={classes.subtitle}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.MetaData.tags'
                                                defaultMessage='Tags'
                                            />
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={12} md={6} lg={8}>
                                        {api.tags &&
                                            api.tags.map((tag) => (
                                                <Chip
                                                    key={tag}
                                                    label={tag}
                                                    style={{
                                                        'font-size': 13,
                                                        height: 20,
                                                        marginRight: 5,
                                                    }}
                                                />
                                            ))}
                                        {api.tags.length === 0 && (
                                            <>
                                                <Typography
                                                    component='p'
                                                    variant='body1'
                                                    // className={classes.notConfigured}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.NewOverview.MetaData.tags.not.set'
                                                        defaultMessage='-'
                                                    />
                                                </Typography>
                                            </>
                                        )}
                                    </Grid>
                                </TextField>
                            </FormControl>
                            <div className={classes.addNewOther}>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() =>
                                        addMgLabel(intl, setCreatingMgLabel)
                                    }
                                    className={classes.saveButton}
                                    disabled={isCreatingMgLabel}
                                >
                                    {isCreatingMgLabel ? (
                                        <>
                                            <FormattedMessage
                                                id='Apis.Details.Scopes.CreateScope.saving'
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
                                            id='microgateway.labels.create.new.category.save'
                                            defaultMessage='Save'
                                        />
                                    )}
                                </Button>
                                <Link
                                    to={
                                        settings.app.context +
                                        '/microgateway/labels'
                                    }
                                >
                                    <Button>
                                        <FormattedMessage
                                            id='microgateway.labels.create.new.category.cancel'
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

export default injectIntl(withStyles(styles)(CreateMgLabel));
