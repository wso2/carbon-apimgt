/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useReducer } from 'react';
import PropTypes from 'prop-types';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';

const styles = (theme) => ({
    root: {
        ...theme.mixins.gutters(),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    contentWrapper: {
        marginTop: theme.spacing(2),
    },
    buttonSuccess: {
        backgroundColor: green[500],
        '&:hover': {
            backgroundColor: green[700],
        },
    },
    checkItem: {
        textAlign: 'center',
    },
    divider: {
        marginTop: 20,
        marginBottom: 20,
    },
    chip: {
        margin: theme.spacing(0.5),
        padding: 0,
        height: 'auto',
        '& span': {
            padding: '0 5px',
        },
    },
    imageContainer: {
        display: 'flex',
    },
    imageWrapper: {
        marginRight: theme.spacing(3),
    },
    subtitle: {
        marginTop: theme.spacing(0),
    },
    specialGap: {
        marginTop: theme.spacing(3),
    },
    resourceTitle: {
        marginBottom: theme.spacing(3),
    },
    ListRoot: {
        padding: 0,
        margin: 0,
    },
    titleWrapper: {
        display: 'flex',
    },
    title: {
        flex: 1,
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
    lifecycleWrapper: {
        display: 'flex',
        alignItems: 'center',
    },
    lifecycleIcon: {
        fontSize: 36,
        color: 'green',
        marginRight: theme.spacing(1),
    },
    leftSideWrapper: {
        paddingRight: theme.spacing(2),
    },
    notConfigured: {
        color: 'rgba(0, 0, 0, 0.40)',
    },
    url: {
        maxWidth: '100%',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
});

/**
 * API Topics page
 *
 * @param {*} props
 * @returns
 */
function Topics(props) {
    function configReducer(currentState, configAction) {
        const { action, value } = configAction;
        switch (action) {
            case 'name':
                // eslint-disable-next-line no-param-reassign
                currentState[action] = value;
                break;
            default:
                break;
        }
        return currentState;
    }

    const { classes, handleAddTopic, handleCancelAddTopic } = props;
    // eslint-disable-next-line no-unused-vars
    // const { api, updateAPI } = useContext(APIContext);
    const [topic, inputsDispatcher] = useReducer(configReducer, {
        name: '',
    });

    function handleOnChange(event) {
        const { name: action, value } = event;
        inputsDispatcher({ action, value });
    }

    return (
        <Paper className={classes.root}>
            <Grid container direction='row' justify='center' alignItems='center'>
                <Grid item xs={12}>
                    <Typography component='h4' align='left'>
                        Add New Topic
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <TextField
                        autoFocus
                        fullWidth
                        id='topic-name'
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Create.Components.NewTopic.topic.name'
                                    defaultMessage='Topic Name'
                                />
                                <sup>*</sup>
                            </>
                        )}
                        helperText='Provide a name for the topic'
                        name='name'
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => {
                            let { value } = e.target;
                            if (value.length > 0 && value.substr(0, 1) !== '/') {
                                value = '/' + value;
                            }
                            handleOnChange({ name: 'name', value });
                        }}
                    />
                </Grid>
                <Grid container direction='row' justify='flex-start' alignItems='center' spacing={2}>
                    <Grid item>
                        <Button
                            id='itest-id-apitopics-addtopic'
                            variant='contained'
                            color='primary'
                            onClick={() => handleAddTopic(topic)}
                        >
                            Add
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button
                            id='itest-id-apitopics-canceladdtopic'
                            variant='contained'
                            color='primary'
                            onClick={handleCancelAddTopic}
                        >
                            Cancel
                        </Button>
                    </Grid>
                </Grid>
            </Grid>
        </Paper>
    );
}

Topics.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Topics);
