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
import React from 'react';
import {
    Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
} from '@material-ui/core';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';

const styles = (theme) => ({
    dialogWrapper: {
        '& span, & h5, & label, & td, & li, & div, & p': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    deleteConformButton: {
        '& span.MuiButton-label': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        },
    },
});

/**
 * React component for handling confirmation dialog box
 * @class ConfirmDialog
 * @extends {React.Component}
 */
class ConfirmDialog extends React.Component {
    /**
     * If user confirms the action invoke the callback with true else false
     * @param {String} action One of ConfirmDialog.Action actions
     * @memberof ConfirmDialog
     */
    handleRequestClose(action) {
        const { callback } = this.props;
        if (action === ConfirmDialog.Action.OK) {
            callback(true);
        } else {
            callback(false);
        }
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Confirmation box
     * @memberof ConfirmDialog
     */
    render() {
        const {
            title, message, labelCancel, labelOk, open, classes,
        } = this.props;

        return (
            <Dialog role='alertdialog' open={open} onClose={this.handleRequestClose} className={classes.dialogWrapper}>
                <DialogTitle>{title || <FormattedMessage id='Shared.ConfirmDialog.please.confirm' defaultMessage='Please Confirm' />}</DialogTitle>
                <DialogContent>
                    <DialogContentText>{message || <FormattedMessage id='Shared.ConfirmDialog.please.confirm.sure' defaultMessage='Are you sure?' />}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => this.handleRequestClose(ConfirmDialog.Action.CANCEL)} color='primary'>
                        {labelCancel || <FormattedMessage id='Shared.ConfirmDialog.cancel' defaultMessage='Cancel' />}
                    </Button>
                    <Button
                        onClick={() => this.handleRequestClose(ConfirmDialog.Action.OK)}
                        color='primary'
                        variant='contained'
                        className={classes.deleteConformButton}
                    >
                        {labelOk || <FormattedMessage id='Shared.ConfirmDialog.ok' defaultMessage='OK' />}
                    </Button>
                </DialogActions>
            </Dialog>
        );
    }
}

ConfirmDialog.propTypes = {
    title: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
    labelCancel: PropTypes.string.isRequired,
    labelOk: PropTypes.string.isRequired,
    callback: PropTypes.func.isRequired,
    open: PropTypes.bool.isRequired,
};
ConfirmDialog.Action = {
    OK: 'ok',
    CANCEL: 'cancel',
};

export default withStyles(styles)(ConfirmDialog);
