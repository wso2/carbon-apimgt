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

import React, { useState } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import { isRestricted } from 'AppData/AuthManager';
import API from 'AppData/api';

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

/**
*
* @param {any} props Props for delete function.
* @returns {any} Returns the rendered UI for scope delete.
*/
function Delete(props) {
    const { intl, fetchScopeData, usageCount } = props;
    const [open, setOpen] = useState(false);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const deleteScope = () => {
        const restApi = new API();
        const { scopeId } = props;
        const setOpenLocal = setOpen; // Need to copy this to access inside the promise.then
        const promisedScopeDelete = restApi.deleteSharedScope(scopeId);
        promisedScopeDelete
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Scopes.Delete.Delete.scope.deleted.successfully',
                    defaultMessage: 'API Scope deleted successfully!',
                }));
                setOpenLocal(!open);
                fetchScopeData();
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error(intl.formatMessage({
                    id: 'Scopes.Delete.Delete.something.went.wrong.while.updating.the.api',
                    defaultMessage: 'Error occurred while deleting scope',
                }));
                setOpenLocal(!open);
            });
    };

    const runAction = (confirm) => {
        if (confirm) {
            deleteScope();
        } else {
            setOpen(!open);
        }
    };
    const { scopeName } = props;

    return (
        <div>
            <Button onClick={toggleOpen} disabled={isRestricted(['apim:shared_scope_manage']) || usageCount > 0}>
                <Icon>delete_forever</Icon>
                <FormattedMessage
                    id='Scopes.Delete.Delete.scope.delete'
                    defaultMessage='Delete'
                />
            </Button>
            <ConfirmDialog
                key='key-dialog'
                labelCancel={(
                    <FormattedMessage
                        id='Scopes.Delete.Delete.scope.listing.label.cancel'
                        defaultMessage='Cancel'
                    />
                )}
                title={(
                    <FormattedMessage
                        id='Scopes.Delete.Delete.scope.listing.delete.confirm'
                        defaultMessage='Confirm Delete'
                    />
                )}
                message={(
                    <FormattedMessage
                        id='Scopes.Delete.Delete.document.scope.label.ok.confirm'
                        defaultMessage='Are you sure you want to delete scope {scope} ?'
                        values={{ scope: scopeName }}
                    />
                )}
                labelOk={(
                    <FormattedMessage
                        id='Scopes.Delete.Delete.scope.listing.label.ok.yes'
                        defaultMessage='Yes'
                    />
                )}
                callback={runAction}
                open={open}
            />
        </div>
    );
}
Delete.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    scopeName: PropTypes.string.isRequired,
    scopeId: PropTypes.string.isRequired,
    intl: PropTypes.shape({}).isRequired,
    fetchScopeData: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Delete));
