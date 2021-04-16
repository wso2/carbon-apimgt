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
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import Box from '@material-ui/core/Box';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import CircularProgress from '@material-ui/core/CircularProgress';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import { isRestricted } from 'AppData/AuthManager';
import CONSTS from 'AppData/Constants';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "MDMonacoEditor" */));
const ReactMarkdown = lazy(() => import('react-markdown' /* webpackChunkName: "MDReactMarkdown" */));

const useStyles = makeStyles(() => ({
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
    editorHeader: {
        alignItems: 'center',
        display: 'flex',
    },
    markdownViewWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    appBar: {
        position: 'relative',
    },
    button: {
        height: 30,
        marginLeft: 30,
    },
}));

function Transition(props) {
    return <Slide direction='up' {...props} />;
}

/**
 * DescriptionEditor for API Description / Overview
 * @param {*} props properties
 * @returns {*} DescriptionEditor component
 */
export default function DescriptionEditor(props) {
    const classes = useStyles();
    const {
        api,
        updateContent,
        descriptionType,
        overview,
        handleChange,
    } = props;
    const [open, setOpen] = useState(false);
    const [content, setContent] = useState(null);
    const [apiFromContext] = useAPI();
    const [isUpdating, setIsUpdating] = useState(false);

    const toggleOpen = () => {
        if (!open) {
            if (descriptionType === CONSTS.DESCRIPTION_TYPES.DESCRIPTION) {
                setContent(api.description);
            } else if (descriptionType === CONSTS.DESCRIPTION_TYPES.OVERVIEW) {
                setContent(overview);
            }
        }
        setOpen(!open);
    };
    const setNewContent = (newContent) => {
        setContent(newContent);
    };
    const handleTextChange = (event) => {
        const { value } = event.currentTarget;
        setContent(value);
    };
    const modifyContent = () => {
        setIsUpdating(true);
        updateContent(content);
        toggleOpen();
        setIsUpdating(false);
    };
    const editorDidMount = (editor) => {
        editor.focus();
    };

    return (
        <>
            <Button
                variant='outlined'
                color='primary'
                disabled={api.isRevision || isRestricted(['apim:api_create'], apiFromContext)}
                onClick={toggleOpen}
            >
                <FormattedMessage
                    id='Apis.Details.Configuration.components.DescriptionEditor.edit.content.button'
                    defaultMessage='Edit description'
                />
            </Button>
            <Dialog fullScreen open={open} onClose={toggleOpen} TransitionComponent={Transition}>
                <AppBar color='inherit' className={classes.appBar}>
                    <Toolbar variant='dense'>
                        <Grid
                            container
                            direction='row'
                            justify='space-between'
                            alignItems='center'
                        >
                            <Grid item xs={6}>
                                <Box display='flex'>
                                    <IconButton edge='start' color='inherit' onClick={toggleOpen} aria-label='close'>
                                        <CloseIcon />
                                    </IconButton>
                                    <Box
                                        display='flex'
                                        alignItems='center'
                                        mx={1}
                                        fontFamily='fontFamily'
                                        fontSize='h4.fontSize'
                                        color='primary.main'
                                    >
                                        {api.name}
                                    </Box>
                                    <Typography variant='h5' className={classes.editorHeader}>
                                        <FormattedMessage
                                            id={'Apis.Details.Configuration.components.DescriptionEditor'
                                                + '.edit.description.of'}
                                            defaultMessage='Description :'
                                        />
                                    </Typography>
                                    <Box ml={2}>
                                        <FormControl component='fieldset'>
                                            <RadioGroup
                                                row
                                                aria-label='description-type'
                                                value={descriptionType}
                                                onChange={handleChange}
                                            >
                                                <FormControlLabel
                                                    value={CONSTS.DESCRIPTION_TYPES.DESCRIPTION}
                                                    control={<Radio />}
                                                    label='Text'
                                                />
                                                <FormControlLabel
                                                    value={CONSTS.DESCRIPTION_TYPES.OVERVIEW}
                                                    control={<Radio />}
                                                    label='Markdown'
                                                />
                                            </RadioGroup>
                                        </FormControl>
                                    </Box>
                                </Box>
                            </Grid>
                            <Grid item>
                                <Button
                                    className={classes.button}
                                    variant='contained'
                                    disabled={isUpdating}
                                    color='primary'
                                    onClick={modifyContent}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.DescriptionEditor.update.cont.button'
                                        defaultMessage='Update Content'
                                    />
                                    {isUpdating && <CircularProgress size={24} />}
                                </Button>
                            </Grid>
                        </Grid>
                    </Toolbar>
                </AppBar>
                <div className={classes.splitWrapper}>
                    <Grid container spacing={7}>
                        { descriptionType === CONSTS.DESCRIPTION_TYPES.DESCRIPTION
                            ? (
                                <Grid item xs={12}>
                                    <Box display='flex' m={2}>
                                        <TextField
                                            id='itest-description-textfield'
                                            multiline
                                            fullWidth
                                            rows={4}
                                            variant='outlined'
                                            onChange={handleTextChange}
                                            value={content}
                                        />
                                    </Box>
                                </Grid>
                            ) : (
                                <>
                                    <Grid item xs={6}>
                                        <Suspense fallback={<CircularProgress />}>
                                            <MonacoEditor
                                                width='100%'
                                                language='markdown'
                                                theme='vs-dark'
                                                value={content}
                                                options={{ selectOnLineNumbers: true }}
                                                onChange={setNewContent}
                                                editorDidMount={editorDidMount}
                                            />
                                        </Suspense>
                                    </Grid>
                                    <Grid item xs={6}>
                                        <div className={classes.markdownViewWrapper}>
                                            <Suspense fallback={<CircularProgress />}>
                                                <ReactMarkdown escapeHtml source={content} />
                                            </Suspense>
                                        </div>
                                    </Grid>
                                </>
                            )}
                    </Grid>
                </div>
            </Dialog>
        </>
    );
}

DescriptionEditor.propTypes = {
    api: PropTypes.shape({}).isRequired,
    updateContent: PropTypes.func.isRequired,
    descriptionType: PropTypes.string.isRequired,
    overview: PropTypes.string.isRequired,
};
