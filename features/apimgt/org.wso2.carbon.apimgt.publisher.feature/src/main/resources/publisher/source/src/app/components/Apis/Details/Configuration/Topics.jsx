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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import green from '@material-ui/core/colors/green';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Chip from '@material-ui/core/Chip';
import IconButton from '@material-ui/core/IconButton';
import InfoIcon from '@material-ui/icons/Info';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import SaveIcon from '@material-ui/icons/Save';
import CancelIcon from '@material-ui/icons/Cancel';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';

import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import NewTopic from 'AppComponents/Apis/Details/Configuration/components/NewTopic'

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
  */
class Topics extends Component {

    constructor(props) {
        super(props);
        this.state = {
            topics: this.loadTopics(this.getSortedOperations()),
            showAddTopic: false,
            isSaving: false,
        };
        this.updateOperations = this.updateOperations.bind(this);
        this.handleCancelSave = this.handleCancelSave.bind(this);
        this.handleAddTopic = this.handleAddTopic.bind(this);
        this.handleDeleteTopic = this.handleDeleteTopic.bind(this);
        this.handleAddProperty = this.handleAddProperty.bind(this);
        this.handleSaveProperty = this.handleSaveProperty.bind(this);
        this.handleCancelSaveProperty = this.handleCancelSaveProperty.bind(this);
        this.handleEditProperty = this.handleEditProperty.bind(this);
        this.handleDeleteProperty = this.handleDeleteProperty.bind(this);
        this.renderEditableProperty = this.renderEditableProperty.bind(this);
        this.loadTopics = this.loadTopics.bind(this);
        this.getSortedOperations = this.getSortedOperations.bind(this);
    }

    getSortedOperations() {
        const operations = [...this.props.api.operations];
        operations.sort((a, b) => ((a.target + a.verb > b.target + b.verb) ? 1 : -1));
        return operations;
    }

    loadTopics() {
        const { operations } = this.props.api;
        return operations.map((op) => {
            return {
                name: op.target,
                mode: op.verb,
                description: '',
                scopes: [],
                payload: {
                    type: 'object',
                    properties: [],
                },
            };
        });
    }

    componentDidMount() {

    }

    updateOperations() {
        this.setState({ isSaving: true });
        const operations = this.state.topics.map(topic => {
            return {
                id: '',
                target: topic.name,
                verb: topic.mode,
                authType: 'Application & Application User',
                throttlingPolicy: "Unlimited",
                amznResourceName: null,
                amznResourceTimeout: null,
                scopes: [],
                usedProductIds: [],
            };
        });
        this.props.updateAPI({ operations }).finally(() => this.setState({ isSaving: false }));
    }

    handleCancelSave() {
        this.setState({ topics: this.loadTopics(this.getSortedOperations()) });
    }

    buildCallbackURL(topic) {
        const { api } = this.props;
        // TODO: Get the host and the port from suitable location
        return 'https://localhost:8243/webhook/cb/' + api.name.toLowerCase() + '/' + api.version + '/'
            + topic.name.toLowerCase();
    }

    handleAddTopic(topic) {
        const modes = [];
        if (topic.isPublish) {
            modes.push('publish');
        }
        if (topic.isSubscribe) {
            modes.push('subscribe');
        }

        const topics = [...this.state.topics];
        modes.forEach((mode) => {
            topics.push({
                name: topic.name,
                mode,
                description: '',
                scopes: [],
                payload: {
                    type: 'object',
                    properties: [],
                },
            });
        });
        this.setState({ topics, showAddTopic: false });
    }

    handleDeleteTopic(i) {
        let topics = [...this.state.topics];
        topics.splice(i, 1);
        this.setState({ topics });
    }

    handleAddProperty(i) {
        let topics = [...this.state.topics];
        topics[i].payload.properties.push({
            name: '',
            type: '',
            advanced: '',
            description: '',
            editable: true,
            new: true
        });
        this.setState({ topics });
    }

    handleSaveProperty(i, pi) {
        let topics = [...this.state.topics];
        topics[i].payload.properties[pi].editable = false;
        topics[i].payload.properties[pi].new = false;
        this.setState({ topics });
    }

    handleCancelSaveProperty(i, pi) {
        let topics = [...this.state.topics];
        if (!!topics[i].payload.properties[pi].new) {
            topics[i].payload.properties.splice(pi, 1);
            this.setState({ topics });
        } else {
            topics[i].payload.properties.splice(pi, 1);
            // TODO: Restore the previous values
        }
    }

    handleEditProperty(i, pi) {
        let topics = [...this.state.topics];
        topics[i].payload.properties[pi].editable = true;
        this.setState({ topics });
    }

    handleDeleteProperty(i, pi) {
        let topics = [...this.state.topics];
        topics[i].payload.properties.splice(pi, 1);
        this.setState({ topics });
    }

    renderProperty(property, i, pi) {
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
                        onClick={() => this.handleEditProperty(i, pi)}
                    >
                        <EditIcon />
                    </IconButton>
                    <IconButton
                        color='primary'
                        variant='contained'
                        onClick={() => this.handleDeleteProperty(i, pi)}
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

    renderEditableProperty(property, i, pi) {
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
                        onChange={(e) => {
                            const topics = [...this.state.topics];
                            topics[i].payload.properties[pi].name = e.target.value;
                            this.setState({ topics });
                        }}
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
                        onChange={(e) => {
                            const topics = [...this.state.topics];
                            topics[i].payload.properties[pi].type = e.target.value;
                            this.setState({ topics });
                        }}
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
                        onChange={(e) => {
                            const topics = [...this.state.topics];
                            topics[i].payload.properties[pi].advanced = e.target.value;
                            this.setState({ topics });
                        }}
                    />
                </Grid>
                <Grid item xs={2} align='right'>
                    <IconButton
                        color='primary'
                        variant='contained'
                    >
                        <SaveIcon onClick={() => this.handleSaveProperty(i, pi)} />
                    </IconButton>
                    <IconButton
                        color='primary'
                        variant='contained'
                    >
                        <CancelIcon onClick={() => this.handleCancelSaveProperty(i, pi)} />
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
                        onChange={(e) => {
                            const topics = [...this.state.topics];
                            topics[i].payload.properties[pi].description = e.target.value;
                            this.setState({ topics });
                        }}
                    />
                </Grid>
                <Grid item xs={12}>
                    <Divider />
                </Grid>
            </Grid>
        );
    }

    renderTopics() {
        const { classes } = this.props;
        const { topics } = this.state;
        return (
            <div className={classes.root}>
                {topics.map((topic, i) => {
                    return (
                        <Accordion>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Grid container direction='row'>
                                    <Grid item>
                                        <Grid container align-items='flex-start' spacing={2}>
                                            <Grid item>
                                                <Chip
                                                    label={topic.mode.toUpperCase()}
                                                    style={{ height: 20, marginRight: 5 }}
                                                />
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
                                            onClick={() => this.handleDeleteTopic(i)}
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
                                            value={this.buildCallbackURL(topic)}
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
                                            onChange={(e) => {
                                                const topics = [...this.state.topics];
                                                topics[i].description = e.target.value;
                                                this.setState({ topics });
                                            }}
                                            margin='normal'
                                            variant='outlined'
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography variant='h6' component='h6' style={{ marginBottom: 10 }}>
                                            Payload
                                        </Typography>
                                    </Grid>
                                    <Grid item style={{ paddingLeft: 0 }}>
                                        <Grid container direction='column'>
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
                                                        onClick={() => this.handleAddProperty(i)}
                                                    >
                                                        Add New Property
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                            {
                                                topic.payload.properties.map((property, pi) => {
                                                    return (property && !!property.editable)
                                                        ? this.renderEditableProperty(property, i, pi)
                                                        : this.renderProperty(property, i, pi);
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
                            onClick={this.updateOperations}
                        >
                            Save
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button
                            id='itest-id-apitopics-canceladdtopic'
                            onClick={this.handleCancelSave}
                        >
                            Cancel
                        </Button>
                    </Grid>
                </Grid>
            </div>
        );
    }

    render() {
        const { classes } = this.props;
        const { showAddTopic, topics } = this.state;
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
                                        onClick={() => this.setState({ showAddTopic: true })}
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
                                            onClick={() => this.setState({ showAddTopic: true })}
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
                            handleAddTopic={this.handleAddTopic}
                            handleCancelAddTopic={() => this.setState({ showAddTopic: false })}
                        />
                    )}

                    {topics.length > 0 && this.renderTopics()}
                </div>
            </>
        );
    }

}

Topics.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        operations: PropTypes.array,
        scopes: PropTypes.array,
        updateOperations: PropTypes.func,
        policies: PropTypes.func,
        id: PropTypes.string,
    }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default withStyles(styles)(Topics);
