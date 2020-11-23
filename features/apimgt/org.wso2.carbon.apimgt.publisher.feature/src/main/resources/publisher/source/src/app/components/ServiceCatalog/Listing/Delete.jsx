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
import { FormattedMessage, injectIntl, useIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Alert from 'AppComponents/Shared/Alert';
import ConfirmDialog from 'AppComponents/Shared/ConfirmDialog';
import ServiceCatalog from 'AppData/ServiceCatalog';

/**
* Service Catalog service delete
* @param {any} props Props for delete function.
* @returns {any} Returns the rendered UI for service delete.
*/
function Delete(props) {
    const intl = useIntl();
    const [open, setOpen] = useState(false);
    const toggleOpen = () => {
        setOpen(!open);
    };
    const { serviceName, serviceId, getData } = props;
    const deleteService = () => {
        const deleteServicePromise = ServiceCatalog.deleteService(serviceId);
        deleteServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Delete.service.deleted.successfully',
                defaultMessage: 'Service deleted successfully!',
            }));
            // Reload the services list
            getData();
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while deleting service',
                id: 'ServiceCatalog.Listing.Delete.error.delete',
            }));
        });
    };

    const runAction = (confirm) => {
        if (confirm) {
            deleteService();
            setOpen(!open);
        } else {
            setOpen(!open);
        }
    };

    return (
        <>
            <Button onClick={toggleOpen}>
                <Icon>delete_forever</Icon>
            </Button>
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
                        values={{ service: serviceName }}
                    />
                )}
                labelOk={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Delete.ok.yes'
                        defaultMessage='Yes'
                    />
                )}
                callback={runAction}
                open={open}
            />
        </>
    );
}
Delete.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    serviceName: PropTypes.string.isRequired,
    serviceId: PropTypes.string.isRequired,
    getData: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(Delete);
