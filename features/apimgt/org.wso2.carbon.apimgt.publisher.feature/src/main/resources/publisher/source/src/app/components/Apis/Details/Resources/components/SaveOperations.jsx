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
import { isRestricted } from 'AppData/AuthManager';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import { useHistory } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import CustomSplitButton from 'AppComponents/Shared/CustomSplitButton';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function SaveOperations(props) {
    const {
        updateOpenAPI, updateAsyncAPI, operationsDispatcher, api,
    } = props;
    const [isUpdating, setIsSaving] = useState(false);
    const history = useHistory();
    const [isOpen, setIsOpen] = useState(false);
    function handleSaveAndDeploy() {
        setIsSaving(true);
        if (updateAsyncAPI) {
            updateAsyncAPI('save')
                .finally(() => history.push({
                    pathname: api.isAPIProduct() ? `/api-products/${api.id}/deployments`
                        : `/apis/${api.id}/deployments`,
                    state: 'deploy',
                }));
        } else {
            updateOpenAPI('save')
                .finally(() => history.push({
                    pathname: api.isAPIProduct() ? `/api-products/${api.id}/deployments`
                        : `/apis/${api.id}/deployments`,
                    state: 'deploy',
                }));
        }
    }

    /**
     * Handle the Save button event,
     *
     */
    function handleSave() {
        setIsSaving(true);
        if (updateAsyncAPI) {
            updateAsyncAPI('save').finally(() => setIsSaving(false));
        } else {
            updateOpenAPI('save').finally(() => setIsSaving(false));
        }
    }

    return (
        <>
            <Grid container direction='row' spacing={1} style={{ marginTop: 20 }}>
                <Grid item>
                    {api.isRevision
                        || isRestricted(['apim:api_create'], api) ? (
                            <Button
                                disabled
                                type='submit'
                                variant='contained'
                                color='primary'
                            >
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Resources.save'
                                    defaultMessage='Save'
                                />
                            </Button>
                        ) : (
                            <CustomSplitButton
                                handleSave={handleSave}
                                handleSaveAndDeploy={handleSaveAndDeploy}
                                isUpdating={isUpdating}
                            />
                        )}
                </Grid>
                <Grid item>
                    <Button onClick={() => setIsOpen(true)}>Reset</Button>
                </Grid>
            </Grid>
            <Dialog
                open={isOpen}
                aria-labelledby='bulk-delete-dialog-title'
                aria-describedby='bulk-delete-dialog-description'
                onBackdropClick={() => setIsOpen(false)}
            >
                <DialogTitle id='bulk-delete-dialog-title'>Discard changes</DialogTitle>
                <DialogContent>
                    <DialogContentText id='bulk-delete-dialog-description'>
                        Please confirm the discard all changes action
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setIsOpen(false)}>CLOSE</Button>
                    <Box ml={1}>
                        <Button
                            onClick={() => {
                                operationsDispatcher({ action: 'init' });
                                setIsOpen(false);
                            }}
                            color='error'
                        >
                            RESET
                        </Button>
                    </Box>
                </DialogActions>
            </Dialog>
        </>
    );
}
SaveOperations.propTypes = {
    updateOpenAPI: PropTypes.func,
    updateAsyncAPI: PropTypes.func,
    operationsDispatcher: PropTypes.func.isRequired,
};

SaveOperations.defaultProps = {
    updateOpenAPI: undefined,
    updateAsyncAPI: undefined,
};
