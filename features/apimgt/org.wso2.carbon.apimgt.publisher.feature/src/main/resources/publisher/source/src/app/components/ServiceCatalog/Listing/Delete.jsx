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
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import DeleteIcon from '@material-ui/icons/Delete';
import IconButton from '@material-ui/core/IconButton';

/**
* Service Catalog service delete
* @param {any} props Props for delete function.
* @returns {any} Returns the rendered UI for service delete.
*/
function Delete(props) {
    const [open, setOpen] = useState(false);
    const toggleOpen = (event) => {
        event.preventDefault();
        event.stopPropagation();
        setOpen(!open);
    };
    const {
        serviceDisplayName, serviceId, onDelete, isIconButton, id,
    } = props;

    const runAction = (confirm) => {
        if (confirm) {
            onDelete(serviceId);
        }
        setOpen(!open);
    };

    return (
        <>
            {isIconButton ? (
                <IconButton
                    id={id}
                    disableRipple
                    disableFocusRipple
                    aria-label={`Delete ${serviceDisplayName}`}
                    onClick={toggleOpen}
                >
                    <DeleteIcon />
                </IconButton>
            ) : (
                <Button id={id} onClick={toggleOpen}>
                    <Icon>delete_forever</Icon>
                </Button>
            )}
            <ConfirmDialog
                key='key-dialog'
                labelCancel={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Delete.cancel'
                        defaultMessage='Cancel'
                    />
                )}
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Delete.confirm'
                        defaultMessage='Confirm Delete'
                    />
                )}
                message={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Delete.ok.confirm'
                        defaultMessage='Are you sure you want to delete the service {service} ?'
                        values={{ service: serviceDisplayName }}
                    />
                )}
                labelOk={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Delete.ok.yes'
                        defaultMessage='Yes'
                    />
                )}
                idOk='itest-service-card-delete-confirm'
                callback={runAction}
                open={open}
            />
        </>
    );
}
Delete.propTypes = {
    serviceDisplayName: PropTypes.string.isRequired,
    serviceId: PropTypes.string.isRequired,
    onDelete: PropTypes.shape({}).isRequired,
};

export default Delete;
