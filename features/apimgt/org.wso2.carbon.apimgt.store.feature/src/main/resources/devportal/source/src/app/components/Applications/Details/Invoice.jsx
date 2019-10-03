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

import React, { useState} from 'react';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Dialog from '@material-ui/core/Dialog';
import Button from '@material-ui/core/Button';
import MUIDataTable from "mui-datatables";

import { FormattedMessage } from 'react-intl';
import Subscription from 'AppData/Subscription';

const useStyles = makeStyles(theme => ({
    root: {
        padding: theme.spacing(3, 2),
    },
}));

const columns = ["Name", "Value"];

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

function Invoice(props) {
    const { subscriptionId, isMonetizedAPI, isDynamicUsagePolicy } = props;
    const [ showPopup, setShowPopup] = useState(false);
    const [ invoice, setInvoice ] = useState(null);

    /**
     * Handle the popup for invoice
     */
    const handlePopup = () => {
        setShowPopup(true);
        setInvoice(null);
        const client = new Subscription();
        const promiseInvoice = client.getMonetizationInvoice(subscriptionId); 
        promiseInvoice.then((response) => {
            if (response && response.obj) {
                let invoiceData = [];
                const {obj:{properties}} = response;
                Object.keys(properties).map(invoiveItem => {
                    let insideArray = [];
                    insideArray.push (invoiveItem);
                    insideArray.push(properties[invoiveItem]);
                    invoiceData.push(insideArray);
                });
                setInvoice(invoiceData);
            }
        });
    }

    /**
     * Handle closing the popup for invoice
     */
    const handleClose = () => {
        setShowPopup(false);
    }

    return (
        <React.Fragment>
            <Button
                variant = 'outlined'
                size = 'small'
                color = 'primary'
                disabled={!(isMonetizedAPI && isDynamicUsagePolicy)}
                onClick = {handlePopup}
            >
                <FormattedMessage
                    id = 'Applications.Details.SubscriptionTableData.view.subscription.invoice'
                    defaultMessage = 'View Invoice'
                />
            </Button>
            <Dialog open = {showPopup} onClose = {handleClose} fullWidth = 'true'>
                {invoice && (<MUIDataTable
                    title = {"Upcoming Invoice"}
                    data = {invoice}
                    columns = {columns}
                    options = {options}
                />) }
            </Dialog>
        </React.Fragment>
        );
}

Invoice.propTypes  = {
    subscriptionId: PropTypes.string.isRequired,
}

export default Invoice;
