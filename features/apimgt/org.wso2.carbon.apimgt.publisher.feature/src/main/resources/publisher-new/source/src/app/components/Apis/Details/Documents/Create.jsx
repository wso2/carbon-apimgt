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

import React, { useRef, useContext } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import CreateEditForm from './CreateEditForm';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

const styles = theme => ({
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 3,
    },
    addNewHeader: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
});

function Create(props) {
    const { api } = useContext(APIContext);
    const { classes, toggleAddDocs, intl, apiType } = props;
    const restAPI = apiType === Api.CONSTS.APIProduct ? new APIProduct() : new Api();
    let createEditForm = useRef(null);

    const addDocument = apiId => {
        const promiseWrapper = createEditForm.addDocument(apiId);
        promiseWrapper.docPromise
            .then(doc => {
                const { documentId, name } = doc.body;
                if (promiseWrapper.file && documentId) {
                    const filePromise = restAPI.addFileToDocument(apiId, documentId, promiseWrapper.file[0]);
                    filePromise
                        .then(doc => {
                            Alert.info(
                                `${name} ${intl.formatMessage({
                                    id: 'Apis.Details.Documents.Create.successful.file.upload.message',
                                    defaultMessage: 'File uploaded successfully.',
                                })}`,
                            );
                            props.getDocumentsList();
                            toggleAddDocs();
                        })
                        .catch(error => {
                            if (process.env.NODE_ENV !== 'production') {
                                console.log(error);
                                Alert.error(
                                    intl.formatMessage({
                                        id: 'Apis.Details.Documents.Create.markdown.editor.upload.error',
                                        defaultMessage: 'Error uploading the file',
                                    }),
                                );
                            }
                        });
                } else {
                    Alert.info(
                        `${doc.body.name} ${intl.formatMessage({
                            id: 'Apis.Details.Documents.Create.markdown.editor.success',
                            defaultMessage: ' added successfully.',
                        })}`,
                    );
                    props.getDocumentsList();
                    toggleAddDocs();
                }
            })
            .catch(error => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(
                        intl.formatMessage({
                            id: 'Apis.Details.Documents.Create.markdown.editor.add.error',
                            defaultMessage: 'Error adding the document',
                        }),
                    );
                }
            });
    };

    return (
        <div className={classes.addNewWrapper}>
            <Typography className={classes.addNewHeader}>
                <FormattedMessage
                    id="Apis.Details.Documents.Create.markdown.editor.create.title"
                    defaultMessage="Add New Document"
                />
            </Typography>
            <Divider />
            <CreateEditForm
                innerRef={node => {
                    createEditForm = node;
                }}
                apiType={apiType}
            />
            <Divider />

            <div className={classes.addNewOther}>
                <Button variant="contained" color="primary" onClick={() => addDocument(api.id)}>
                    <FormattedMessage
                        id="Apis.Details.Documents.Create.markdown.editor.add.document.button"
                        defaultMessage="Add Document"
                    />
                </Button>
                <Button className={classes.button} onClick={toggleAddDocs}>
                    <FormattedMessage
                        id="Apis.Details.Documents.Create.markdown.editor.add.document.cancel.button"
                        defaultMessage="Cancel"
                    />
                </Button>
            </div>
        </div>
    );
}

Create.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.func.isRequired,
    apiType: PropTypes.oneOf([Api.CONSTS.API, Api.CONSTS.APIProduct]).isRequired,
};

export default injectIntl(withStyles(styles)(Create));
