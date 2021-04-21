/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import MuiDialogTitle from '@material-ui/core/DialogTitle';
import MuiDialogContent from '@material-ui/core/DialogContent';
import MuiDialogActions from '@material-ui/core/DialogActions';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import DialogContentText from '@material-ui/core/DialogContentText';
import LinkIcon from '@material-ui/icons/Link';
import Box from '@material-ui/core/Box';
// import Link from '@material-ui/core/Link';
import Divider from '@material-ui/core/Divider';
import { Link as RouterLink } from 'react-router-dom';

const styles = (theme) => ({
    root: {
        margin: 0,
        padding: theme.spacing(2),
    },
    closeButton: {
        position: 'absolute',
        right: theme.spacing(1),
        top: theme.spacing(1),
        color: theme.palette.grey[500],
    },
});

const DialogTitle = withStyles(styles)((props) => {
    const {
        children, classes, onClose, ...other
    } = props;
    return (
        <MuiDialogTitle disableTypography className={classes.root} {...other}>
            <Typography variant='h6'>{children}</Typography>
            {onClose ? (
                <IconButton aria-label='close' className={classes.closeButton} onClick={onClose}>
                    <CloseIcon />
                </IconButton>
            ) : null}
        </MuiDialogTitle>
    );
});

const DialogContent = withStyles((theme) => ({
    root: {
        padding: theme.spacing(2),
    },
}))(MuiDialogContent);

const DialogActions = withStyles((theme) => ({
    root: {
        margin: 0,
        padding: theme.spacing(1),
    },
}))(MuiDialogActions);

/**
 *
 * @returns
 */
export default function PublishWithoutDeploy(props) {
    const {
        apiID, handleClick, open, handleClose,
    } = props;

    return (
        <Dialog onClose={handleClose} aria-labelledby='publish-api-confirmation' open={open}>
            <DialogTitle id='itest-publish-confirmation' onClose={handleClose}>
                <FormattedMessage
                    id='Apis.Details.LifeCycle.components.confirm.publish.title'
                    defaultMessage='Publish API without deployments'
                />
            </DialogTitle>
            <Divider light />
            <DialogContent>
                <Box my={1}>
                    <DialogContentText id='itest-confirm-publish-text'>
                        <Typography variant='subtitle1' display='block' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.LifeCycle.components.confirm.publish.message'
                                defaultMessage={'The API will not be available for '
                                        + 'consumption unless it is deployed.'}
                            />
                        </Typography>
                    </DialogContentText>
                </Box>

            </DialogContent>
            <DialogActions>
                <Button
                    color='primary'
                    onClick={handleClick}
                >
                    Publish
                </Button>
                <Button
                    variant='contained'
                    color='primary'
                    component={RouterLink}
                    to={'/apis/' + apiID + '/deployments'}
                >
                    <Box fontSize='button.fontSize' alignItems='center' display='flex' fontFamily='fontFamily'>
                        <FormattedMessage
                            id='Apis.Details.LifeCycle.publish.content.info.deployments'
                            defaultMessage='Deployments'
                        />
                        <Box ml={0.5} mt={0.25} display='flex'>
                            <LinkIcon fontSize='small' />
                        </Box>
                    </Box>
                </Button>
            </DialogActions>
        </Dialog>
    );
}
