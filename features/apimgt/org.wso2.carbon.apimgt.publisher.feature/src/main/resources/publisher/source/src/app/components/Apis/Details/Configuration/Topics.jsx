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

import React, { Component, lazy } from 'react';
import PropTypes from 'prop-types';
import green from '@material-ui/core/colors/green';
import { withStyles, withTheme } from '@material-ui/core/styles';
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
import NewTopic from 'AppComponents/Apis/Details/Configuration/components/NewTopic';

import $RefParser from '@apidevtools/json-schema-ref-parser';
import { parse } from '@asyncapi/parser';

import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "APIDefMonacoEditor" */));

function RenderMethodBase(props) {
    // TODO:
    const { methods } = props;
    const chipColors = {
        subscribe: '#61affe',
        publish: '#49cc90',
    };
    return methods.map((method) => {
        let chipColor = chipColors[method];
        let chipTextColor = '#000000';
        if (!chipColor) {
            console.log('Check the theme settings. The resourceChipColors is not populated properly');
            chipColor = '#cccccc';
        } else {
            chipTextColor = '#fff';
        }
        return (
            <Chip
                label={method.toUpperCase()}
                style={{
                    backgroundColor: chipColor, color: chipTextColor, height: 20, marginRight: 5,
                }}
            />
        );
    });
}

RenderMethodBase.propTypes = {
    methods: PropTypes.arrayOf(PropTypes.string).isRequired,
    theme: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

const RenderMethod = withTheme(RenderMethodBase);

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
            tabValue: 0,
            asyncAPI: null,
            // eslint-disable-next-line react/no-unused-state
            definition: this.getAsyncAPIDefinition(),
            topics: this.loadTopics(this.getSortedOperations()),
            showAddTopic: false,
        };

        this.updateOperations = this.updateOperations.bind(this);
        this.handleCancelSave = this.handleCancelSave.bind(this);
        this.handleAddTopic = this.handleAddTopic.bind(this);
        this.handleDeleteTopic = this.handleDeleteTopic.bind(this);

        this.handleAddProperty = this.handleAddProperty.bind(this);
        this.renderEditableProperty = this.renderEditableProperty.bind(this);
        this.loadTopics = this.loadTopics.bind(this);
        this.getSortedOperations = this.getSortedOperations.bind(this);
        this.getAsyncAPIDefinition = this.getAsyncAPIDefinition.bind(this);
        this.loadTopicMetaData = this.loadTopicMetaData.bind(this);
        this.renderSchemaForTopic = this.renderSchemaForTopic.bind(this);
    }

    getAsyncAPIDefinition() {
        const result = this.props.api.getAsyncAPIDefinition();
        result.then(async (response) => {
            $RefParser.dereference(response.body, (err) => {
                if (err) {
                    console.error(err);
                } else {
                    this.setState({
                        // eslint-disable-next-line react/no-unused-state
                        definition: response.body,
                    });
                }
            });
            const doc = await parse(response.body);
            this.setState({ asyncAPI: doc }, this.loadTopicMetaData);
        });
    }

    getSortedOperations() {
        const operations = [...this.props.api.operations];
        operations.sort((a, b) => ((a.target + a.verb > b.target + b.verb) ? 1 : -1));
        return operations;
    }

    handleCancelSave() {
        this.setState({ topics: this.loadTopics(this.getSortedOperations()) });
    }

    handleAddTopic(topic) {
        // eslint-disable-next-line react/no-access-state-in-setstate
        const topicsCopy = [...this.state.topics];
        topicsCopy.push({
            name: topic.name,
            description: '',
            scopes: [],
            payload: {
                type: 'object',
                properties: [],
            },
        });
        this.setState({ topics: topicsCopy, showAddTopic: false });
    }

    handleDeleteTopic(i) {
        // eslint-disable-next-line react/no-access-state-in-setstate
        const topicsCopy = [...this.state.topics];
        topicsCopy.splice(i, 1);
        this.setState({ topics: topicsCopy });
    }

    handleAddProperty(i) {
        // eslint-disable-next-line react/no-access-state-in-setstate
        const topicsCopy = [...this.state.topics];
        topicsCopy[i].payload.properties.push({
            name: '',
            type: '',
            advanced: '',
            description: '',
            editable: true,
            new: true,
        });
        this.setState({ topics: topicsCopy });
    }

    loadTopics() {
        const { operations } = this.props.api;
        return operations.map((op) => {
            return {
                name: op.target,
                description: '',
                scopes: [],
                uriMapping: op.uriMapping,
                payload: {
                    type: 'object',
                    properties: this.extractProperties(op.payloadSchema),
                },
            };
        });
    }

    loadTopicMetaData() {
        const { asyncAPI, topics } = this.state;
        topics.forEach((topic) => {
            asyncAPI.channelNames().forEach((name) => {
                const channel = asyncAPI.channel(name);
                if (topic.name === name) {
                    /* if (channel.hasPublish() && topic.mode === 'PUBLISH') {
                        let pubMessage = null;
                        if (!channel.publish().hasMultipleMessages()) {
                            pubMessage = channel.publish().message();
                            // eslint-disable-next-line no-param-reassign
                            topic.description = pubMessage.uid();
                            // eslint-disable-next-line guard-for-in
                            for (const i in pubMessage.payload().properties()) {
                                topic.payload.properties.push({
                                    name: i,
                                    type: pubMessage.payload().properties()[i].type(),
                                    advanced: '',
                                    description: '',
                                    editable: false,
                                    new: false,
                                });
                                if (pubMessage.payload().properties()[i].type() === 'object') {
                                    // eslint-disable-next-line guard-for-in
                                    for (const j in pubMessage.payload().properties()[i].properties()) {
                                        topic.payload.properties.push({
                                            name: i + ' / ' + j,
                                            type: pubMessage.payload().properties()[i].properties()[j].type(),
                                            advanced: '',
                                            description: '',
                                            editable: false,
                                            new: false,
                                        });
                                    }
                                }
                            }
                        } else {
                            // eslint-disable-next-line prefer-destructuring
                            pubMessage = channel.publish().messages()[0];
                            // eslint-disable-next-line no-param-reassign
                            topic.description = pubMessage.uid();
                            // eslint-disable-next-line guard-for-in
                            for (const i in pubMessage.payload().properties()) {
                                topic.payload.properties.push({
                                    name: i,
                                    type: pubMessage.payload().properties()[i].type(),
                                    advanced: '',
                                    description: '',
                                    editable: false,
                                    new: false,
                                });
                                if (pubMessage.payload().properties()[i].type() === 'object') {
                                    // eslint-disable-next-line guard-for-in
                                    for (const j in pubMessage.payload().properties()[i].properties()) {
                                        topic.payload.properties.push({
                                            name: i + ' / ' + j,
                                            type: pubMessage.payload().properties()[i].properties()[j].type(),
                                            advanced: '',
                                            description: '',
                                            editable: false,
                                            new: false,
                                        });
                                    }
                                }
                            }
                        }
                    } */
                    if (channel.hasSubscribe()) {
                        let subMessage = null;
                        if (!channel.subscribe().hasMultipleMessages()) {
                            subMessage = channel.subscribe().message();
                            // eslint-disable-next-line no-param-reassign
                            topic.description = subMessage.uid();
                            // eslint-disable-next-line guard-for-in
                            for (const i in subMessage.payload().properties()) {
                                topic.payload.properties.push({
                                    name: i,
                                    type: subMessage.payload().properties()[i].type(),
                                    advanced: '',
                                    description: '',
                                    editable: false,
                                    new: false,
                                });
                                if (subMessage.payload().properties()[i].type() === 'object') {
                                    // eslint-disable-next-line guard-for-in
                                    for (const j in subMessage.payload().properties()[i].properties()) {
                                        topic.payload.properties.push({
                                            name: i + ' / ' + j,
                                            type: subMessage.payload().properties()[i].properties()[j].type(),
                                            advanced: '',
                                            description: '',
                                            editable: false,
                                            new: false,
                                        });
                                    }
                                }
                            }
                        } else {
                            // eslint-disable-next-line prefer-destructuring
                            subMessage = channel.subscribe().messages()[0];
                            // eslint-disable-next-line no-param-reassign
                            topic.description = subMessage.uid();
                            // eslint-disable-next-line guard-for-in
                            for (const i in subMessage.payload().properties()) {
                                topic.payload.properties.push({
                                    name: i,
                                    type: subMessage.payload().properties()[i].type(),
                                    advanced: '',
                                    description: '',
                                    editable: false,
                                    new: false,
                                });
                                if (subMessage.payload().properties()[i].type() === 'object') {
                                    // eslint-disable-next-line guard-for-in
                                    for (const j in subMessage.payload().properties()[i].properties()) {
                                        topic.payload.properties.push({
                                            name: i + ' / ' + j,
                                            type: subMessage.payload().properties()[i].properties()[j].type(),
                                            advanced: '',
                                            description: '',
                                            editable: false,
                                            new: false,
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            });
        });
    }

    extractProperties(payloadSchema) {
        const obj = JSON.parse(payloadSchema || JSON.stringify({ properties: [] }));
        return obj.properties || [];
    }

    updateOperations() {
        const operations = this.state.topics.map((topic) => {
            return {
                id: '',
                target: topic.name,
                verb: 'subscribe',
                authType: 'Application & Application User',
                throttlingPolicy: 'Unlimited',
                amznResourceName: null,
                amznResourceTimeout: null,
                scopes: [],
                usedProductIds: [],
                payloadSchema: JSON.stringify({
                    properties: topic.payload.properties,
                }),
                uriMapping: topic.uriMapping,
            };
        });
        this.props.updateAPI({ operations });
    }

    buildCallbackURL(topic) {
        const { api } = this.props;
        return `https://{GATEWAY_HOST}:9021/${api.context.toLowerCase()}/${api.version}/`
            + `webhooks_events_receiver_resource?topic=${topic.name.toLowerCase()}`;
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
                    {/* <Typography>
                        {property.advanced}
                    </Typography> */}
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
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => {
                            // eslint-disable-next-line react/no-access-state-in-setstate
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
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => {
                            // eslint-disable-next-line react/no-access-state-in-setstate
                            const topics = [...this.state.topics];
                            topics[i].payload.properties[pi].type = e.target.value;
                            this.setState({ topics });
                        }}
                    />
                </Grid>
                <Grid item xs={6}>
                    {/* <TextField
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
                    /> */}
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
                        margin='normal'
                        variant='outlined'
                        onChange={(e) => {
                            // eslint-disable-next-line react/no-access-state-in-setstate
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

    renderSchemaForTopic(topic) {
        const { asyncAPI } = this.state;
        let schema = {};
        asyncAPI.channelNames().forEach((name) => {
            const channel = asyncAPI.channel(name);
            if (name === topic.name) {
                /* if (topic.mode === 'SUBSCRIBE') {
                    if (channel.hasSubscribe()) {
                        if (!channel.subscribe().hasMultipleMessages()) {
                            if (channel.subscribe().message() !== null) {
                                schema = channel.subscribe().message().payload();
                            }
                        } else {
                            // eslint-disable-next-line no-lonely-if
                            if (channel.subscribe().messages()[0] !== null) {
                                schema = channel.subscribe().messages()[0].payload();
                            }
                        }
                    }
                }
                if (topic.mode === 'PUBLISH') {
                    if (channel.hasPublish()) {
                        if (!channel.publish().hasMultipleMessages()) {
                            if (channel.publish().message() !== null) {
                                schema = channel.publish().message().payload();
                            }
                        } else {
                            // eslint-disable-next-line no-lonely-if
                            if (channel.publish().messages()[0] !== null) {
                                schema = channel.publish().messages()[0].payload();
                            }
                        }
                    }
                } */
                if (channel.hasSubscribe()) {
                    if (!channel.subscribe().hasMultipleMessages()) {
                        if (channel.subscribe().message() !== null) {
                            schema = channel.subscribe().message().payload();
                        }
                    } else {
                        // eslint-disable-next-line no-lonely-if
                        if (channel.subscribe().messages()[0] !== null) {
                            schema = channel.subscribe().messages()[0].payload();
                        }
                    }
                }
            }
        });
        return JSON.stringify(schema, null, '\t');
    }

    renderTopics() {
        const { classes, api } = this.props;
        const { topics, tabValue } = this.state;
        return (
            <div className={classes.root}>
                {topics.map((topic, i) => {
                    const methods = ['subscribe'];
                    if (api.type === 'WS') {
                        methods.push('publish');
                    }
                    return (
                        <Accordion>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Grid container direction='row'>
                                    <Grid item>
                                        <Grid container align-items='flex-start' spacing={2}>
                                            <Grid item>
                                                {/* {
                                                    api.type === 'WS' && (
                                                        <Chip
                                                            label='PUBLISH'
                                                            style={{ height: 20, marginRight: 5 }}
                                                        />
                                                    )
                                                }
                                                <Chip
                                                    label='SUBSCRIBE'
                                                    style={{ height: 20, marginRight: 5 }}
                                                /> */}
                                                <RenderMethod methods={methods} />
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
                                    {api.type === 'WEBSUB' && (
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
                                    )}
                                    {api.type === 'WS' && (
                                        <Grid item>
                                            <TextField
                                                autoFocus
                                                fullWidth
                                                id='topic-description'
                                                label={(
                                                    <>
                                                        <FormattedMessage
                                                            id='Apis.Create.Components.DefaultAPIForm.urlmapping'
                                                            defaultMessage='URL Mapping'
                                                        />
                                                    </>
                                                )}
                                                value={topic.uriMapping}
                                                helperText='URL mapping for the WebSocket API'
                                                name='url-mapping'
                                                margin='normal'
                                                variant='outlined'
                                                onChange={(e) => {
                                                    // eslint-disable-next-line react/no-access-state-in-setstate
                                                    const topicsCopy = [...this.state.topics];
                                                    topicsCopy[i].uriMapping = e.target.value;
                                                    this.setState({ topics: topicsCopy });
                                                }}
                                            />
                                        </Grid>
                                    )}
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
                                            onChange={(e) => {
                                                // eslint-disable-next-line react/no-access-state-in-setstate
                                                const topicsCopy = [...this.state.topics];
                                                topicsCopy[i].description = e.target.value;
                                                this.setState({ topics: topicsCopy });
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
                                    <Grid item style={{ paddingBottom: '2%' }}>
                                        <Tabs
                                            indicatorColor='primary'
                                            textColor='primary'
                                            value={tabValue}
                                            onChange={(event, value) => this.setState({ tabValue: value })}
                                        >
                                            <Tab label='Properties' />
                                            <Tab label='Schema' />
                                        </Tabs>
                                    </Grid>
                                    {
                                        tabValue === 0 ? (
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
                                                            {/* <Typography style={{ fontWeight: 'bold' }}>
                                                                Advanced
                                                            </Typography> */}
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
                                        ) : (
                                            <Grid item style={{ paddingLeft: 0 }}>
                                                <Grid container direction='column'>
                                                    <MonacoEditor
                                                        // value={JSON.stringify(this.state.definition, null, '\t')}
                                                        value={this.renderSchemaForTopic(topic)}
                                                        language='json'
                                                        width='100%'
                                                        height='500px'
                                                        theme='vs-dark'
                                                        options={{
                                                            selectOnLineNumbers: true,
                                                            readOnly: true,
                                                            smoothScrolling: true,
                                                            wordWrap: 'on',
                                                        }}
                                                    />
                                                </Grid>
                                            </Grid>
                                        )
                                    }
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
                                            API needs to have at least one topic. Channels (topics) that will allow
                                            client applications to publish or subscribe to messages (events).
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
        operations: PropTypes.arrayOf(PropTypes.shape({})),
        scopes: PropTypes.arrayOf(PropTypes.shape({})),
        updateOperations: PropTypes.func,
        policies: PropTypes.func,
        id: PropTypes.string,
    }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default withStyles(styles)(Topics);
