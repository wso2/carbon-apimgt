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
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Button from '@material-ui/core/Button';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import Tooltip from '@material-ui/core/Tooltip';
import PropTypes from 'prop-types';
/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function SpecErrors(props) {
    const { specErrors } = props;
    const [open, setOpen] = useState(false);
    if (specErrors.length === 0) {
        return null;
    }
    return (
        <>
            <sup>
                <Tooltip title='Show errors'>
                    <IconButton onClick={() => setOpen(true)} color='secondary' aria-label='Errors in spec'>
                        <ErrorOutlineIcon color='error' />
                    </IconButton>
                </Tooltip>
            </sup>
            <Dialog maxWidth='md' aria-labelledby='confirmation-dialog-title' open={open}>
                <DialogTitle id='confirmation-dialog-title'>
                    <Typography display='inline' color='textPrimary' variant='h6'>
                        Errors
                        <Typography display='inline' variant='subtitle2'>
                            {' '}
                            in OpenAPI definition
                        </Typography>
                    </Typography>
                </DialogTitle>
                <DialogContent dividers>
                    <List>
                        {specErrors.map((error, index) => (
                            <>
                                {index % 2 !== 0 && <Divider light variant='inset' />}
                                <ListItem>
                                    <ListItemText
                                        primary={error.message}
                                        primaryTypographyProps={{
                                            color: 'error',
                                        }}
                                        inset
                                    />
                                </ListItem>
                            </>
                        ))}
                    </List>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpen(false)} color='primary'>
                        Ok
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

SpecErrors.propTypes = {
    specErrors: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
