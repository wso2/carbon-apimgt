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
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import Checkbox from '@material-ui/core/Checkbox';
import DeleteSweepIcon from '@material-ui/icons/DeleteSweep';
import isEmpty from 'lodash.isempty';
import IconButton from '@material-ui/core/IconButton';
import CircularProgress from '@material-ui/core/CircularProgress';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';

/**
 *
 *
 * @param {*} props
 * @returns
 */
export default function OperationsSelector(props) {
    const {
        selectedOperations, setSelectedOperation, openAPI, updateOpenAPI,
    } = props;
    const [isDeleting, setIsDeleting] = useState(false);
    const [isConfirmOpen, setIsConfirmOpen] = useState(false);

    // TODO: Following logic introduce a limitation in showing `indeterminate` icon state if user
    // select all -> unchecked one operation -> recheck same operation again ~tmkb
    const isSelectAll = Object.is(selectedOperations, openAPI.paths);
    const isIndeterminate = !isSelectAll && !isEmpty(selectedOperations);

    /**
     *
     *
     * @param {*} event
     */
    function handleMultiDelete() {
        setIsDeleting(true);
        updateOpenAPI('deleteAll', {})
            .then(() => setSelectedOperation({}))
            .finally(() => {
                setIsDeleting(false);
                setIsConfirmOpen(false);
            });
    }
    /**
     *
     *
     * @param {*} event
     */
    function handleSelector(event) {
        const {
            target: { checked },
        } = event;
        setSelectedOperation(!checked || isIndeterminate ? {} : openAPI.paths);
    }
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item>
                <Box ml={6}>
                    <Checkbox
                        checked={isSelectAll}
                        indeterminate={!isSelectAll && !isEmpty(selectedOperations)}
                        onChange={handleSelector}
                        value='all'
                        inputProps={{
                            'aria-label': 'primary checkbox',
                        }}
                    />
                </Box>
            </Grid>
            <Grid item>
                <Box mr={19}>
                    <IconButton
                        onClick={() => setIsConfirmOpen(true)}
                        disabled={!(isIndeterminate || isSelectAll) || isDeleting}
                        aria-label='delete all'
                        size='small'
                    >
                        {isDeleting ? <CircularProgress size={24} thickness={3} /> : <DeleteSweepIcon />}
                    </IconButton>
                </Box>
            </Grid>
            <Dialog
                open={isConfirmOpen}
                aria-labelledby='bulk-delete-dialog-title'
                aria-describedby='bulk-delete-dialog-description'
            >
                <DialogTitle id='bulk-delete-dialog-title'>Confirm Bulk Delete</DialogTitle>
                <DialogContent>
                    <DialogContentText id='bulk-delete-dialog-description'>
                        Please confirm the bulk delete action
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setIsConfirmOpen(false)}>CLOSE</Button>
                    <Button disabled={isDeleting} onClick={handleMultiDelete} color='error'>
                        DELETE {isDeleting && <CircularProgress size={20} />}
                    </Button>
                </DialogActions>
            </Dialog>
        </Grid>
    );
}
