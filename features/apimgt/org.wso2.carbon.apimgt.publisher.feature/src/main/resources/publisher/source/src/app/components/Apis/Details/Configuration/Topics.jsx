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

import React, { useReducer, useContext, useState } from 'react';
import PropTypes from 'prop-types';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
// import FormGroup from '@material-ui/core/FormGroup';
// import FormControlLabel from '@material-ui/core/FormControlLabel';
// import Checkbox from '@material-ui/core/Checkbox';
import Chip from '@material-ui/core/Chip';
import IconButton from '@material-ui/core/IconButton';
import InfoIcon from '@material-ui/icons/Info';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import SaveIcon from '@material-ui/icons/Save';
import CancelIcon from '@material-ui/icons/Cancel';
import Divider from '@material-ui/core/Divider';
import MenuItem from '@material-ui/core/MenuItem';
import Box from '@material-ui/core/Box';

import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import NewTopic from 'AppComponents/Apis/Details/Configuration/components/NewTopic'
import API from 'AppData/api';

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
        let newTopics = [...topics];
        switch(action) {
            case 'topic-description':
                newTopics[value.topicIndex].description = value.value;
                break;
            case 'topic-secret':
                newTopics[value.topicIndex].secret = value.value;
                break;
            case 'topic-lease':
                newTopics[value.topicIndex].lease = value.value;
                break;
            case 'property-name':
                newTopics[value.topicIndex].payload.properties[value.propertyIndex].name = value.value;
                break;
            case 'property-type':
                newTopics[value.topicIndex].payload.properties[value.propertyIndex].type = value.value;
                break;
            case 'property-advanced':
                newTopics[value.topicIndex].payload.properties[value.propertyIndex].advanced = value.value;
                break;
            case 'property-description':
                newTopics[value.topicIndex].payload.properties[value.propertyIndex].description = value.value;
                break;
        }
        return newTopics;
    }

    const { classes } = props;
    const { api, updateAPI } = useContext(APIContext);

    function loadTopics() {
        let initialTopics = [];
        if (api && api.operations) {
            api.operations.map(op => {
                initialTopics.push({
                    name: op.target,
                    modes: [op.verb],
                    description: '',
                    operation: {
                        scope: [],
                    },
                    payload: {
                        type: 'object',
                        properties: [],
                    },
                });
            });
        }
        return initialTopics;
    }

    const [topics, setTopics] = useState(loadTopics());
    const [showAddTopic, setShowAddTopic] = useState(false);
    const [currentTopics, inputsDispatcher] = useReducer(configReducer, topics);

    function handleAddTopic(topic) {
        const modes = [];
        if (topic.isPublish) {
            modes.push('publish');
        }
        if (topic.isSubscribe) {
            modes.push('subscribe');
        }
        let newTopics = [...topics];
        newTopics.push({
            name: topic.name,
            modes,
            description: '',
            operation: {
                scope: [],
            },
            payload: {
                type: 'object',
                properties: [],
            },
        });
        setTopics(newTopics);
        setShowAddTopic(false);
    }

    function handleDeleteTopic(topicIndex) {
        let newTopics = [...topics];
        newTopics.splice(topicIndex, 1);
        setTopics(newTopics);
    }

    function handleAddProperty(topicIndex) {
        let newTopics = [...topics];
        newTopics[topicIndex].payload.properties.push({
            name: '',
            type: '',
            advanced: '',
            description: '',
            editable: true,
            new: true
        });
        setTopics(newTopics);
    }

    function handleSaveProperty(topicIndex, propertyIndex) {
        let newTopics = [...topics];
        newTopics[topicIndex].payload.properties[propertyIndex].editable = false;
        newTopics[topicIndex].payload.properties[propertyIndex].new = false;
        setTopics(newTopics);
    }

    function handleCancelSaveProperty(topicIndex, propertyIndex) {
        let newTopics = [...topics];
        if (!!newTopics[topicIndex].payload.properties[propertyIndex].new) {
            newTopics[topicIndex].payload.properties.splice(propertyIndex, 1);
            setTopics(newTopics);
        } else {
            newTopics[topicIndex].payload.properties.splice(propertyIndex, 1);
            // TODO: Restore the previous values
        }
    }

    function handleEditProperty(topicIndex, propertyIndex) {
        let newTopics = [...topics];
        newTopics[topicIndex].payload.properties[propertyIndex].editable = true;
        setTopics(newTopics);
    }

    function handleDeleteProperty(topicIndex, propertyIndex) {
        const newTopics = [...topics];
        newTopics[topicIndex].payload.properties.splice(propertyIndex, 1);
        setTopics(newTopics);
    }

    function handleOnChange(event) {
        const { name: action, value } = event;
        inputsDispatcher({ action, value });
    }

    function handleSaveTopics() {
        let newTopics = [];
        topics.forEach(topic => {
            topic.modes.forEach(mode => {
                newTopics.push({
                    "name": topic.name,
                    "mode": mode,
                    "description": ''
                });
            });
        });
        API.updateTopics(api.id, {
            "list": newTopics,
        });
    }

    function buildCallbackURL(topic) {
        // TODO: Get the host and the port from suitable location
        return 'https://localhost:8243/webhook/cb/' + api.name.toLowerCase() + '/' + api.version + '/'
            + topic.name.toLowerCase();
    }

    // function getScopes() {
    //     let scopes = [];
    //     for (let i = 1; i < 5; i++) {
    //         scopes.push({
    //             id: 'scope' + i,
    //             displayText: 'Scope ' + i,
    //         });
    //     }
    //     return scopes;
    // }

    function renderProperty(property, topicIndex, propertyIndex) {
        return (
            <Grid
                container
                direction='row'
                style={{ marginTop: 15, marginBottom: 15 }}
            >
                <Grid item xs={2}>
                    <Typography>
                        {property.name}
                    </Typography>
                </Grid>
                <Grid item xs={2}>
                    <Typography>
                        {property.type}
                    </Typography>
                </Grid>
                <Grid item xs={6}>
                    <Typography>
                        {property.advanced}
                    </Typography>
                </Grid>
                <Grid item xs={2} align='right'>
                    <IconButton
                        color='primary'
                        variant='contained'
                        onClick={() => handleEditProperty(topicIndex, propertyIndex)}
                    >
                        <EditIcon />
                    </IconButton>
                    <IconButton
                        color='primary'
                        variant='contained'
                        onClick={() => handleDeleteProperty(topicIndex, propertyIndex)}
                    >
                        <DeleteIcon />
                    </IconButton>
                </Grid>
                <Grid item xs={12}>
                    <Typography>
                        {property.description}
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <Divider />
                </Grid>
            </Grid>
        );
    }

    function renderEditableProperty(property, topicIndex, propertyIndex) {
        return (
            <Grid
                container
                direction='row'
                style={{ marginTop: 15, marginBottom: 15 }}
                spacing={2}
            >
                <Grid item xs={2}>
                    <TextField
                        autoFocus
                        fullWidth
                        id='topic-description'
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Topic.Edit.name'
                                    defaultMessage='Name'
                                />
                            </>
                        )}
                        value={property.name}
                        helperText='Provide a name for the property'
                        name='prop-description'
                        InputProps={{
                            id: 'itest-id-apitopic-createtopic-description',
                            onBlur: ({ target: { value } }) => {
                                // TODO: validate
                            },
                        }}
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => handleOnChange({
                            name: 'property-name',
                            value: {
                                topicIndex,
                                propertyIndex,
                                value: e.target.value,
                            }
                        })}
                    />
                </Grid>
                <Grid item xs={2}>
                    <TextField
                        autoFocus
                        fullWidth
                        id='topic-description'
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Topic.Edit.Type'
                                    defaultMessage='Type'
                                />
                            </>
                        )}
                        value={property.type}
                        helperText='Provide a type for the property'
                        name='prop-description'
                        InputProps={{
                            id: 'itest-id-apitopic-createtopic-description',
                            onBlur: ({ target: { value } }) => {
                                // TODO: validate
                            },
                        }}
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => handleOnChange({
                            name: 'property-type',
                            value: {
                                topicIndex,
                                propertyIndex,
                                value: e.target.value,
                            }
                        })}
                    />
                </Grid>
                <Grid item xs={6}>
                    <TextField
                        autoFocus
                        fullWidth
                        id='topic-description'
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Topic.Edit.Advanced'
                                    defaultMessage='Advanced'
                                />
                            </>
                        )}
                        value={property.advanced}
                        helperText='Provide a Advanced function for the property'
                        name='prop-description'
                        InputProps={{
                            id: 'itest-id-apitopic-createtopic-description',
                            onBlur: ({ target: { value } }) => {
                                // TODO: validate
                            },
                        }}
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => handleOnChange({
                            name: 'property-advanced',
                            value: {
                                topicIndex,
                                propertyIndex,
                                value: e.target.value,
                            }
                        })}
                    />
                </Grid>
                <Grid item xs={2} align='right'>
                    <IconButton
                        color='primary'
                        variant='contained'
                    >
                        <SaveIcon onClick={() => handleSaveProperty(topicIndex, propertyIndex)} />
                    </IconButton>
                    <IconButton
                        color='primary'
                        variant='contained'
                    >
                        <CancelIcon onClick={() => handleCancelSaveProperty(topicIndex, propertyIndex)} />
                    </IconButton>
                </Grid>
                <Grid item xs={12}>
                    <TextField
                        autoFocus
                        fullWidth
                        id='topic-description'
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Topic.Edit.Description'
                                    defaultMessage='Description'
                                />
                            </>
                        )}
                        value={property.description}
                        helperText='Provide a description for the property'
                        name='prop-description'
                        InputProps={{
                            id: 'itest-id-apitopic-createtopic-description',
                            onBlur: ({ target: { value } }) => {
                                // TODO: validate
                            },
                        }}
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => handleOnChange({
                            name: 'property-description',
                            value: {
                                topicIndex,
                                propertyIndex,
                                value: e.target.value,
                            }
                        })}
                    />
                </Grid>
                <Grid item xs={12}>
                    <Divider />
                </Grid>
            </Grid>
        );
    }

    /**
     * Render topics
     * @returns {*} HTML content
     */
    function renderTopics() {
        return (
            <div className={classes.root}>
                {topics.map((topic, topicIndex) => {
                    return (
                        <Accordion>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Grid container direction='row'>
                                    <Grid item>
                                        <Grid container align-items='flex-start' spacing={2}>
                                            <Grid item>
                                                {
                                                    topic.modes.map((t) => {
                                                        return (
                                                            <Chip
                                                                label={t.toUpperCase()}
                                                                style={{ height: 20, marginRight: 5 }}
                                                            />
                                                        );
                                                    })
                                                }
                                            </Grid>
                                            <Grid item>
                                                <Typography>
                                                    {topic.name}
                                                </Typography>
                                            </Grid>
                                        </Grid>
                                    </Grid>
                                    <Grid item>
                                        <Button
                                            color='primary'
                                            onClick={() => handleDeleteTopic(topicIndex)}
                                        >
                                            <DeleteIcon />
                                        </Button>
                                    </Grid>
                                </Grid>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Grid container direction='column'>
                                    <Grid item>
                                        <TextField
                                            autoFocus
                                            fullWidth
                                            disabled
                                            id='topic-description'
                                            label={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Create.Components.DefaultAPIForm.callbackUrl'
                                                        defaultMessage='Callback URL'
                                                    />
                                                </>
                                            )}
                                            value={buildCallbackURL(topic)}
                                            helperText='Use the above callback URL when register at the provider'
                                            name='description'
                                            margin='normal'
                                            variant='outlined'
                                        />
                                    </Grid>
                                    <Grid item>
                                        <TextField
                                            autoFocus
                                            fullWidth
                                            id='topic-description'
                                            label={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Create.Components.DefaultAPIForm.description'
                                                        defaultMessage='Description'
                                                    />
                                                </>
                                            )}
                                            value={topic.description}
                                            helperText='Provide a description for the topic'
                                            name='description'
                                            InputProps={{
                                                id: 'itest-id-apitopic-createtopic-description',
                                                onBlur: ({ target: { value } }) => {
                                                    // TODO: validate
                                                },
                                            }}
                                            onChange={(e) => handleOnChange({
                                                name: 'topic-description',
                                                value: {
                                                    topicIndex,
                                                    value: e.target.value,
                                                }
                                            })}
                                            margin='normal'
                                            variant='outlined'
                                        />
                                    </Grid>
                                    {/* <Grid item>
                                        <Typography variant='h6' component='h6'>
                                            Operation Governance
                                        </Typography>
                                    </Grid>
                                    <Grid item>
                                        <TextField
                                            autoFocus
                                            fullWidth
                                            select
                                            id='topic-description'
                                            label={(
                                                <>
                                                    <FormattedMessage
                                                        id='Apis.Create.Components.DefaultAPIForm.opscope'
                                                        defaultMessage='Operation Scope'
                                                    />
                                                </>
                                            )}
                                            value={topic.operation.scope}
                                            helperText='Select an Operation Scope'
                                            name='operation-scope'
                                            InputProps={{
                                                id: 'itest-id-apitopic-createtopic-operationscope',
                                                onBlur: ({ target: { value } }) => {
                                                    // TODO: validate
                                                },
                                            }}
                                            margin='normal'
                                            variant='outlined'
                                            onChange={({ target: { value } }) => {
                                                topics[topicIndex].operation.scope = value;
                                                setTopics(topics);
                                            }}
                                        >
                                            <MenuItem dense>
                                                Select Operation Scope
                                            </MenuItem>
                                            {
                                                getScopes().map((scope) => {
                                                    return (
                                                        <MenuItem
                                                            dense
                                                            id={scope.id}
                                                            key={scope.id}
                                                            value={scope.id}
                                                        >
                                                            {scope.displayText}
                                                        </MenuItem>
                                                    );
                                                })
                                            }
                                        </TextField>
                                    </Grid> */}
                                    <Grid item>
                                        <Typography variant='h6' component='h6' style={{ marginBottom: 10 }}>
                                            Payload
                                        </Typography>
                                    </Grid>
                                    <Grid item style={{ paddingLeft: 0 }}>
                                        <Grid container direction='column'>
                                            {/* <Grid item>
                                                <Typography variant='h6' component='h6'>
                                                    Properties
                                                </Typography>
                                            </Grid> */}
                                            <Grid container direction='row'>
                                                <Grid item xs={2}>
                                                    <Typography style={{ fontWeight: 'bold' }}>
                                                        Name
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={2}>
                                                    <Typography style={{ fontWeight: 'bold' }}>
                                                        Type
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6}>
                                                    <Typography style={{ fontWeight: 'bold' }}>
                                                        Advanced
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={2} align='right'>
                                                    <Button
                                                        color='primary'
                                                        variant='contained'
                                                        onClick={() => {
                                                            handleAddProperty(topicIndex);
                                                        }}
                                                    >
                                                        Add New Property
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                            {
                                                topic.payload.properties.map((property, propertyIndex) => {
                                                    return (property && !!property.editable)
                                                        ? renderEditableProperty(property, topicIndex, propertyIndex)
                                                        : renderProperty(property, topicIndex, propertyIndex);
                                                })
                                            }
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </AccordionDetails>
                        </Accordion>
                    );
                })}
                <Grid container direction='row' justify='flex-start' spacing={2} style={{ paddingTop: 15 }}>
                    <Grid item>
                        <Button
                            id='itest-id-apitopics-addtopic'
                            variant='contained'
                            color='primary'
                            onClick={handleSaveTopics}
                        >
                            Save
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button
                            id='itest-id-apitopics-canceladdtopic'
                        >
                            Cancel
                        </Button>
                    </Grid>
                </Grid>
            </div>
        );
    }

    return (
        <>
            <Box pb={3}>
                <Grid container>
                    <Grid item>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.Overview.Overview.topic.header'
                                defaultMessage='Topics'
                            />
                        </Typography>
                    </Grid>
                    {
                        topics.length > 0 && (
                            <Grid item style={{ paddingLeft: 30 }}>
                                <Button
                                    id='itest-id-apitopics-addtopic'
                                    variant='contained'
                                    color='primary'
                                    onClick={() => setShowAddTopic(true)}
                                >
                                    Add Topic
                                </Button>
                            </Grid>
                        )
                    }
                </Grid>

            </Box>
            <div className={classes.contentWrapper}>
                {topics.length === 0 && !showAddTopic && (
                    <Paper className={classes.root}>
                        <Grid container xs={12}>
                            <Grid item xs={1}>
                                <InfoIcon color='primary' fontSize='large' />
                            </Grid>
                            <Grid container xs={11} spacing={2}>
                                <Grid item xs={12}>
                                    <Typography variant='h6' align='left'>
                                        <FormattedMessage
                                            id='Apis.Details.Overview.Overview.topic.createtopics'
                                            defaultMessage='Create Topics'
                                        />
                                    </Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography component='p' align='left'>
                                        API needs to have at least one topic. Channels (topics) that will allow client
                                        applications to publish or subscribe to messages (events).
                                    </Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Button
                                        id='itest-id-apitopics-addtopic'
                                        variant='contained'
                                        color='primary'
                                        onClick={() => setShowAddTopic(true)}
                                    >
                                        Add Topic
                                    </Button>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Paper>
                )}

                {showAddTopic && (
                    <NewTopic
                        handleAddTopic={handleAddTopic}
                        handleCancelAddTopic={() => setShowAddTopic(false)}
                    />
                )}

                {topics.length > 0 && renderTopics()}
            </div>
        </>
    );
}

Topics.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Topics);
