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
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function SaveOperations(props) {
    const { updateOpenAPI } = props;
    const [isSaving, setIsSaving] = useState(false);
    /**
     * Handle the Save button event,
     *
     */
    function saveChanges() {
        setIsSaving(true);
        updateOpenAPI('save').finally(() => setIsSaving(false));
    }
    return (
        <Box>
            <Button disabled={isSaving} onClick={saveChanges} variant='contained' color='primary'>
                Save
                {isSaving && <CircularProgress size={24} />}
            </Button>
            <Button onClick={() => {}}>Reset</Button>
        </Box>
    );
}
