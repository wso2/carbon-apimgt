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

import React, { useState, Suspense, lazy } from 'react';
import { withRouter } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "MDMonacoEditor" */));
const ReactMarkdown = lazy(() => import('react-markdown' /* webpackChunkName: "MDReactMarkdown" */));

const styles = {
    flex: {
        flex: 1,
    },
    popupHeader: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    splitWrapper: {
        padding: 0,
    },
    description: {
        alignItems: 'center',
        display: 'flex',
    },
    markdownViewWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    button: {
        height: 30,
        marginLeft: 30,
    },
};

function Transition(props) {
    return <Slide direction='up' {...props} />;
}

function MarkdownEditor(props) {
    const { api, configDispatcher } = props;
    const [open, setOpen] = useState(false);
    const [description, setDescription] = useState(null);
    const [apiFromContext] = useAPI();
    const [isUpdating, setIsUpdating] = useState(false);

    const toggleOpen = () => {
        if (!open) setDescription(description || api.description);
        setOpen(!open);
    };
    const changeDescription = (newDescription) => {
        setDescription(newDescription);
    };
    const updateDescription = () => {
        setIsUpdating(true);
        configDispatcher({ action: 'description', value: description });
        toggleOpen();
        setIsUpdating(false);
    };
    const editorDidMount = (editor) => {
        editor.focus();
    };

    const { classes } = props;
    return (
        <div>
            <Link onClick={toggleOpen}>
                <Button
                    color='primary'
                    disabled={isRestricted(['apim:api_create'], apiFromContext)}
                >
                    {api.description ? (
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.MarkdownEditor.edit.description.button'
                            defaultMessage='Edit Description'
                        />
                    ) : (
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.MarkdownEditor.add.description.button'
                            defaultMessage='Add Description'
                        />
                    )}
                </Button>
            </Link>
            <Dialog fullScreen open={open} onClose={toggleOpen} TransitionComponent={Transition}>
                <Paper square className={classes.popupHeader}>
                    <IconButton color='inherit' onClick={toggleOpen} aria-label='Close'>
                        <Icon>close</Icon>
                    </IconButton>
                    <Typography variant='h4' className={classes.description}>
                        <FormattedMessage
                            id='Apis.Details.Documents.MarkdownEditor.edit.content.of'
                            defaultMessage='Edit Description of '
                        />
                        {api.name}
                    </Typography>
                    <Button
                        className={classes.button}
                        variant='contained'
                        disabled={isUpdating}
                        color='primary'
                        onClick={updateDescription}
                    >
                        <FormattedMessage
                            id='Apis.Details.Documents.MarkdownEditor.update.content.button'
                            defaultMessage='Update Description'
                        />
                        {isUpdating && <CircularProgress size={24} />}
                    </Button>
                    <Button className={classes.button} onClick={toggleOpen}>
                        <FormattedMessage
                            id='Apis.Details.Documents.MarkdownEditor.cancel.button'
                            defaultMessage='Cancel'
                        />
                    </Button>
                </Paper>
                <div className={classes.splitWrapper}>
                    <Grid container spacing={7}>
                        <Grid item xs={6}>
                            <Suspense fallback={<CircularProgress />}>
                                <MonacoEditor
                                    width='100%'
                                    height='100vh'
                                    language='markdown'
                                    theme='vs-dark'
                                    value={description}
                                    options={{ selectOnLineNumbers: true }}
                                    onChange={changeDescription}
                                    editorDidMount={editorDidMount}
                                />
                            </Suspense>
                        </Grid>
                        <Grid item xs={6}>
                            <div className={classes.markdownViewWrapper}>
                                <Suspense fallback={<CircularProgress />}>
                                    <ReactMarkdown escapeHtml={false} source={description} />
                                </Suspense>
                            </div>
                        </Grid>
                    </Grid>
                </div>
            </Dialog>
        </div>
    );
}

MarkdownEditor.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withRouter(withStyles(styles)(MarkdownEditor)));
