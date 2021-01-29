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
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

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
    const [api, updateAPI] = useAPI();
    const { intl } = props;
    const [open, setOpen] = useState(false);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const deleteScope = () => {
        const { scopeName } = props;
        const ops = api.operations && JSON.parse(JSON.stringify(api.operations));
        const operations = ops && ops.map((op) => {
            // eslint-disable-next-line no-param-reassign
            op.scopes = op.scopes.filter((scope) => {
                return scope !== scopeName;
            });
            return op;
        });
        const scopes = api.scopes.filter((apiScope) => {
            return apiScope.scope.name !== scopeName;
        });
        const updateProperties = { scopes, operations };
        const setOpenLocal = setOpen; // Need to copy this to access inside the promise.then
        const promisedUpdate = updateAPI(updateProperties);
        promisedUpdate
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Resources.Resources.api.scope.deleted.successfully',
                    defaultMessage: 'API Scope deleted successfully!',
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
            <Button onClick={toggleOpen} disabled={isRestricted(['apim:api_create'], api) || api.isRevision}>
                <Icon>delete_forever</Icon>
                <FormattedMessage
                    id='Apis.Details.Documents.Delete.document.delete'
                    defaultMessage='Delete'
                />
            </Button>
            <ConfirmDialog
                key='key-dialog'
                labelCancel={(
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.label.cancel'
                        defaultMessage='Cancel'
                    />
                )}
                title={(
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.delete.confirm'
                        defaultMessage='Confirm Delete'
                    />
                )}
                message={(
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.label.ok.confirm'
                        defaultMessage='Are you sure you want to delete scope {scope} ?'
                        values={{ scope: scopeName }}
                    />
                )}
                labelOk={(
                    <FormattedMessage
                        id='Apis.Details.Documents.Delete.document.listing.label.ok.yes'
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
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Delete));
