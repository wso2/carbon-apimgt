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
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Icon from '@material-ui/core/Icon';
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

function Delete(props) {
    const { intl } = props;
    const [open, setOpen] = useState(false);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const deleteScope = () => {
        const { api, scopeName } = props;
        // eslint-disable-next-line no-underscore-dangle
        const apiData = api._data;
        apiData.operations = apiData.operations.map((op) => {
            // eslint-disable-next-line no-param-reassign
            op.scopes = op.scopes.filter((scope) => {
                return scope !== scopeName;
            });
            return op;
        });
        apiData.scopes = apiData.scopes.filter((scope) => {
            return scope.name !== scopeName;
        });
        const setOpenLocal = setOpen; // Need to copy this to access inside the promise.then
        const promisedUpdate = api.update(apiData);
        promisedUpdate
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Resources.Resources.api.scope.deleted.successfully',
                    defaultMessage: 'API Scope Deleted successfully!',
                }));
                setOpenLocal(!open);
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Resources.Resources.something.went.wrong.while.updating.the.api',
                    defaultMessage: 'Error occurred while updating API',
                }));
            });
    };

    const runAction = (action) => {
        if (action === 'yes') {
            deleteScope();
        } else {
            setOpen(!open);
        }
    };
    const { scopeName } = props;
    return (
        <div>
            <Button onClick={toggleOpen}>
                <Icon>delete_forever</Icon>
                <FormattedMessage
                    id='Apis.Details.Documents.Delete.document.delete'
                    defaultMessage='Delete'
                />
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
                        defaultMessage='Are you sure to delete?'
                    />
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id='alert-dialog-description'>
                        <strong>{scopeName}</strong>
                        <FormattedMessage
                            id='Apis.Details.Documents.Delete.document.listing.delete.confirm.body'
                            defaultMessage=' will be permernently deleted. Are you sure?'
                        />
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => runAction('no')} color='primary'>
                        <FormattedMessage
                            id='Apis.Details.Documents.Delete.document.listing.delete.cancel'
                            defaultMessage='Cancel'
                        />
                    </Button>
                    <Button onClick={() => runAction('yes')} color='primary' autoFocus>
                        <FormattedMessage
                            id='Apis.Details.Documents.Delete.document.listing.delete.yes'
                            defaultMessage='Yes. Delete'
                        />
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
Delete.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    apiId: PropTypes.shape({}).isRequired,
    scopeName: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Delete));
