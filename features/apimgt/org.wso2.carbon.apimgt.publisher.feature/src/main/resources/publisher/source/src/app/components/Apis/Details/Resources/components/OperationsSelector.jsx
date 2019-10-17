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
import PropTypes from 'prop-types';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import DeleteSweepIcon from '@material-ui/icons/DeleteSweep';
import isEmpty from 'lodash.isempty';
import IconButton from '@material-ui/core/IconButton';
import ClearAllIcon from '@material-ui/icons/ClearAll';
import Tooltip from '@material-ui/core/Tooltip';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

/**
 *
 *
 * @param {*} props
 * @returns
 */
export default function OperationsSelector(props) {
    const { selectedOperations, setSelectedOperation, operations } = props;
    const [apiFromContext] = useAPI();

    // TODO: Following logic introduce a limitation in showing `indeterminate` icon state if user
    // select all -> unchecked one operation -> recheck same operation again ~tmkb
    const isIndeterminate = !isEmpty(selectedOperations);
    /**
     *
     *
     * @param {*} event
     */
    function handleSelector() {
        setSelectedOperation(isIndeterminate ? {} : operations);
    }
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item />
            <Grid item>
                <Box mr={17.25}>
                    <Tooltip title={isIndeterminate ? 'Clear selections' : 'Mark all for delete'}>
                        <div>
                            <IconButton
                                disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                onClick={handleSelector}
                                aria-label='delete all'
                                size='large'
                            >
                                {isIndeterminate ? <ClearAllIcon /> : <DeleteSweepIcon />}
                            </IconButton>
                        </div>
                    </Tooltip>
                </Box>
            </Grid>
        </Grid>
    );
}

OperationsSelector.defaultProps = {};

OperationsSelector.propTypes = {
    selectedOperations: PropTypes.shape({}).isRequired,
    setSelectedOperation: PropTypes.func.isRequired,
    operations: PropTypes.shape({}).isRequired,
};
