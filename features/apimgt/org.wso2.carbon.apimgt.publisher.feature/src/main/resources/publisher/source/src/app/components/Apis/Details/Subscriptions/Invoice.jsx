/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';
import Dialog from '@material-ui/core/Dialog';
import Button from '@material-ui/core/Button';
import MUIDataTable from 'mui-datatables';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import { FormattedMessage } from 'react-intl';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogActions from '@material-ui/core/DialogActions';

const columns = ['Name', 'Value'];

const options = {
    filterType: 'checkbox',
    sort: false,
    search: false,
    viewColumns: false,
    filter: false,
    selectableRowsHeader: false,
    selectableRows: 'none',
    pagination: false,
    download: false,
};

// eslint-disable-next-line require-jsdoc
function Invoice(props) {
    const {
        subscriptionId,
        isMonetizedUsagePolicy,
        isNotAuthorized,
        api,
    } = props;
    const [showPopup, setShowPopup] = useState(false);
    const [showErrorPopup, setShowErrorPopup] = useState(false);
    const [invoice, setInvoice] = useState(null);

    /**
   * Handle the popup for invoice
   */
    const handlePopup = () => {
        setShowPopup(true);
        setInvoice(null);
        if (api.apiType === API.CONSTS.APIProduct) {
            const apiProduct = new APIProduct(api.name, api.context, api.policies);
            const promiseInvoice = apiProduct.getMonetizationInvoice(subscriptionId);
            promiseInvoice.then((response) => {
                const invoiceData = [];
                Object.keys(response.properties).map((invoiceItem) => {
                    const insideArray = [];
                    insideArray.push(invoiceItem);
                    insideArray.push(response.properties[invoiceItem]);
                    invoiceData.push(insideArray);
                    return true;
                });
                setInvoice(invoiceData);
            }).catch((error) => {
                console.error(error);
                setShowErrorPopup(true);
            });
        } else {
            const promiseInvoice = api.getMonetizationInvoice(subscriptionId);
            promiseInvoice.then((response) => {
                const invoiceData = [];
                Object.keys(response.properties).map((invoiceItem) => {
                    const insideArray = [];
                    insideArray.push(invoiceItem);
                    insideArray.push(response.properties[invoiceItem]);
                    invoiceData.push(insideArray);
                    return true;
                });
                setInvoice(invoiceData);
            }).catch((error) => {
                console.error(error);
                setShowErrorPopup(true);
            });
        }
    };

    /**
   * Handle closing the popup for invoice
   */
    const handleClose = () => {
        setShowPopup(false);
    };

    const handleAlertClose = () => {
        setShowErrorPopup(false);
    };

    return (
        <>
            <Button
                variant='outlined'
                size='small'
                color='primary'
                disabled={!isMonetizedUsagePolicy || isNotAuthorized}
                onClick={handlePopup}
            >
                <FormattedMessage
                    id='Applications.Details.Invoice.view.btn'
                    defaultMessage='View Invoice'
                />
            </Button>
            <Dialog open={showPopup} onClose={handleClose} fullWidth='true'>
                {invoice && (
                    <MUIDataTable
                        title='Upcoming Invoice'
                        data={invoice}
                        columns={columns}
                        options={options}
                    />
                )}
            </Dialog>
            <Dialog open={showErrorPopup} onClose={handleAlertClose} fullWidth='true'>
                <DialogTitle>No Data Available</DialogTitle>
                <DialogContent>
                    <DialogContentText id='invoice-dialog-description'>
                        Pending invoice data not found for this subscription.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleAlertClose} color='primary'>
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

Invoice.propTypes = {
    subscriptionId: PropTypes.string.isRequired,
    isMonetizedUsagePolicy: PropTypes.string.isRequired,
    isNotAuthorized: PropTypes.string.isRequired,
    api: PropTypes.instanceOf(API).isRequired,
};

export default Invoice;
