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

import React, { useContext, useState } from 'react';
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
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';

function Delete(props) {
    const { intl } = props;
    const [open, setOpen] = useState(false);
    const { api } = useContext(APIContext);

    const runAction = (action) => {
        if (action === 'yes') {
            deleteDoc();
        } else {
            setOpen(!open);
        }
    };
    const toggleOpen = () => {
        setOpen(!open);
    };
    const deleteDoc = () => {
        const {
            apiId, docId, getDocumentsList, apiType,
        } = props;
        const restApi = apiType === API.CONSTS.APIProduct ? new APIProduct() : new API();
        const docPromise = restApi.deleteDocument(apiId, docId);
        docPromise
            .then(() => {
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
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    };

    const { apiName } = props;
    return (
        <div>
            <Button onClick={toggleOpen} disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api) || api.isRevision}>
                <Icon>delete_forever</Icon>
                <FormattedMessage id='Apis.Details.Documents.Delete.document.delete' defaultMessage='Delete' />
            </Button>
            <Dialog
                open={open}
                onClose={toggleOpen}
                aria-labelledby='alert-dialog-title'
                aria-describedby='alert-dialog-description'
            >
                <DialogTitle id='alert-dialog-title'>
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.delete.confirm.title'
                        defaultMessage='Delete Document'
                    />
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id='alert-dialog-description'>
                        <FormattedMessage
                            id='Apis.Details.Documents.Delete.document.listing.delete.confirm.body'
                            defaultMessage={
                                'Selected document will be deleted from the API.' +
                                'You will not be able to undo this action.'
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
                            id='Apis.Details.Documents.Delete.document.listing.delete'
                            defaultMessage='Yes. Delete'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
Delete.propTypes = {
    apiId: PropTypes.shape({}).isRequired,
    apiType: PropTypes.string.isRequired,
    docId: PropTypes.shape({}).isRequired,
    getDocumentsList: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
        apiType: PropTypes.oneOf([API.CONSTS.API, API.CONSTS.APIProduct]),
    }).isRequired,
};

export default injectIntl(Delete);
