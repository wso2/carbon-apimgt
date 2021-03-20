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

import React, { useState, useEffect, useContext } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import API from 'AppData/api.js';
import APIProduct from 'AppData/APIProduct';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';

function DeleteMultiple(props) {
    const {
        intl, docsToDelete, docs, getDocumentsList,
    } = props;
    const { api, isAPIProduct } = useContext(APIContext);
    const restApi = isAPIProduct ? new APIProduct() : new API();

    const [open, setOpen] = useState(true);

    const runAction = (action) => {
        if (action === 'yes') {
            deleteDocs();
        } else {
            setOpen(!open);
        }
    };
    const toggleOpen = () => {
        setOpen(!open);
    };
    const deleteDocs = () => {
        const docPromises = [];

        docsToDelete.data.map((docIndexObj) => {
            const doc = docs[docIndexObj.index];
            docPromises.push(restApi.deleteDocument(api.id, doc.documentId));
        });
        Promise.all(docPromises)
            .then((values) => {
                console.log(values);
                Alert.info(`${intl.formatMessage({
                    id: 'Apis.Details.Documents.Delete.document.delete.successfully',
                    defaultMessage: 'deleted successfully.',
                })}`);
                setOpen(!open);
                getDocumentsList();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                    Alert.error(`${intl.formatMessage({
                        id: 'Apis.Details.Documents.Delete.document.delete.error',
                        defaultMessage: 'Error while deleting documents!',
                    })}`);
                }
            });
    };
    useEffect(() => {
        setOpen(true);
    }, [docsToDelete]);

    return (
        <Dialog
            open={open}
            onClose={toggleOpen}
            aria-labelledby='alert-dialog-title'
            aria-describedby='alert-dialog-description'
        >
            <DialogTitle id='alert-dialog-title'>
                <FormattedMessage
                    id='Apis.Details.Documents.Delete.selected.document.listing.delete.confirm.title'
                    defaultMessage='Delete Selected Documents'
                />
            </DialogTitle>
            <DialogContent>
                <DialogContentText id='alert-dialog-description'>
                    <strong>{api.name}</strong>
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.selected.document.listing.delete.confirm.body'
                        defaultMessage={
                            'Selected documents will be deleted from the API.' +
                            ' You will not be able to undo this action.'
                        }
                    />
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => runAction('no')} color='default'>
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.delete.cancel'
                        defaultMessage='Cancel'
                    />
                </Button>
                <Button onClick={() => runAction('yes')} color='primary' autoFocus>
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.delete.yes'
                        defaultMessage='Delete'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
}
DeleteMultiple.propTypes = {
    docs: PropTypes.instanceOf(Array).isRequired,
    getDocumentsList: PropTypes.func.isRequired,
    docsToDelete: PropTypes.instanceOf(Array).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(DeleteMultiple);
