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

import React, { useState } from 'react';
import intl, { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { EditorState, convertToRaw, ContentState, convertFromHTML } from 'draft-js';
import { Editor } from 'react-draft-wysiwyg';
import draftToHtml from 'draftjs-to-html';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import Alert from 'AppComponents/Shared/Alert';

const styles = {
    appBar: {
        position: 'relative',
    },
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
    docName: {
        alignItems: 'center',
        display: 'flex',
    },
    button: {
        height: 30,
        marginLeft: 30,
    },

};

function Transition(props) {
    return <Slide direction='up' {...props} />;
}

function TextEditor(props) {
    const { intl, apiType } = props;
    const [open, setOpen] = useState(false);

    const [editorState, setEditorState] = useState(EditorState.createEmpty());

    const onEditorStateChange = (newEditorState) => {
        setEditorState(newEditorState);
    };
    const toggleOpen = () => {
        if (!open) updateDoc();
        setOpen(!open);
    };
    const addContentToDoc = () => {
        const restAPI = (apiType === Api.CONSTS.APIProduct) ? new APIProduct() : new Api();
        const contentToSave = draftToHtml(convertToRaw(editorState.getCurrentContent()));
        const docPromise = restAPI.addInlineContentToDocument(props.apiId, props.docId, 'INLINE', contentToSave);
        docPromise
            .then((response) => {
                Alert.info(`${response.obj.name} ${intl.formatMessage({
                    id: 'Apis.Details.Documents.TextEditor.update.success.message',
                    defaultMessage: 'updated successfully.',
                })}`);
                toggleOpen();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.info(`${error} ${intl.formatMessage({
                    id: 'Apis.Details.Documents.TextEditor.update.error.message',
                    defaultMessage: 'update failed.',
                })}`);
            });
    };
    const updateDoc = () => {
        const restAPI = (apiType === Api.CONSTS.APIProduct) ? new APIProduct() : new Api();

        const docPromise = restAPI.getInlineContentOfDocument(props.apiId, props.docId);
        docPromise
            .then((doc) => {
                const blocksFromHTML = convertFromHTML(doc.text);
                const state = ContentState.createFromBlockArray(blocksFromHTML.contentBlocks, blocksFromHTML.entityMap);

                setEditorState(EditorState.createWithContent(state));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    };

    const { classes } = props;
    return (
        <div>
            <Button onClick={toggleOpen}>
                <Icon>description</Icon>
                <FormattedMessage
                    id='Apis.Details.Documents.TextEditor.edit.content'
                    defaultMessage='Edit Content'
                />
            </Button>
            <Dialog fullScreen open={open} onClose={toggleOpen} TransitionComponent={Transition}>
                <Paper square className={classes.popupHeader}>
                    <IconButton color='inherit' onClick={toggleOpen} aria-label='Close'>
                        <Icon>close</Icon>
                    </IconButton>
                    <Typography variant='h4' className={classes.docName}>
                        <FormattedMessage
                            id='Apis.Details.Documents.TextEditor.edit.content.of'
                            defaultMessage='Edit Content of'
                        />{' '}
                        "{props.docName}"
                    </Typography>
                    <Button className={classes.button} variant='contained' color='primary' onClick={addContentToDoc}>
                        <FormattedMessage
                            id='Apis.Details.Documents.TextEditor.update.content.button'
                            defaultMessage='Update Content'
                        />
                    </Button>
                    <Button className={classes.button} onClick={toggleOpen}>
                        <FormattedMessage
                            id='Apis.Details.Documents.TextEditor.cancel.button'
                            defaultMessage='Cancel'
                        />
                    </Button>
                </Paper>
                <div className={classes.splitWrapper}>
                    <Editor
                        editorState={editorState}
                        wrapperClassName='draftjs-wrapper'
                        editorClassName='draftjs-editor'
                        onEditorStateChange={onEditorStateChange}
                    />
                </div>
            </Dialog>
        </div>
    );
}

TextEditor.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    docId: PropTypes.string.isRequired,
    apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(TextEditor));
