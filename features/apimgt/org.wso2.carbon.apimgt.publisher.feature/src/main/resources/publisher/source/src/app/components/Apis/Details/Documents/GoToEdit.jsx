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

import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import { FormattedMessage } from 'react-intl';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

function GoToEdit(props) {
    const { doc } = props;
    const [open, setOpen] = React.useState(true);
    const { api, isAPIProduct } = useContext(APIContext);
    const urlPrefix = isAPIProduct ? 'api-products' : 'apis';
    const listingPath = `/${urlPrefix}/${api.id}/documents`;
    let docContentEditPath = null;
    if (doc && doc.body && doc.body.documentId) {
        docContentEditPath = `/${urlPrefix}/${api.id}/documents/${doc.body.documentId}/edit-content`;
    }

    let displayAddContent;
    if (doc.body.sourceType === 'INLINE'  || doc.body.sourceType === 'MARKDOWN') {
        displayAddContent = true;
    } else {
        displayAddContent= false;
    }

    console.info('printing doc', doc);
    function handleClose() {
        setOpen(false);
    }

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            aria-labelledby='alert-dialog-title'
            aria-describedby='alert-dialog-description'
        >
            <DialogTitle id='alert-dialog-title'>
                {' '}
                <FormattedMessage
                    id='Apis.Details.Documents.GoToEdit.title'
                    defaultMessage='Document Created Successfully'
                />
            </DialogTitle>
            <DialogContent>
                <DialogContentText id='alert-dialog-description'>
                    {displayAddContent ? (
                    <FormattedMessage
                        id='Apis.Details.Documents.GoToEdit.description.content'
                        defaultMessage= 'You can add content to the document or go back to the document listing page.'
                    />
                    ) : (
                    <FormattedMessage
                        id='Apis.Details.Documents.GoToEdit.description.file'
                        defaultMessage= {'You can go back to the document listing page and upload' + 
                        ' the file by editing the document.'}
                    />
                    )}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                {displayAddContent && (<Link
                    to={{
                        pathname: docContentEditPath,
                        state: { doc: doc.body },
                    }}
                >
                    <Button color='primary'>
                        <FormattedMessage
                            id='Apis.Details.Documents.GoToEdit.add.content'
                            defaultMessage='Add Content'
                        />
                    </Button>
                </Link>)}
                <Link to={listingPath}>
                    <Button color='primary' autoFocus>
                        <FormattedMessage
                            id='Apis.Details.Documents.GoToEdit.back.to.listing'
                            defaultMessage='Back to Listing'
                        />
                    </Button>
                </Link>
            </DialogActions>
        </Dialog>
    );
}
GoToEdit.propTypes = {
    doc: PropTypes.shape({}).isRequired,
};
export default GoToEdit;
