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

import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DescriptionAndSummary(props) {
    const { operation, operationActionsDispatcher } = props;
    return (
        <Fragment>
            <Grid item md={12}>
                <Typography variant='subtitle1'>
                    Summary {'&'} Description
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} />
            <Grid item md={6}>
                <TextField
                    margin='dense'
                    fullWidth
                    label='Description'
                    multiline
                    rows='4'
                    value={operation.spec.description}
                    variant='outlined'
                    onChange={({ target: { value } }) =>
                        operationActionsDispatcher({ action: 'description', event: { operation, value } })
                    }
                />
            </Grid>
            <Grid item md={5}>
                <TextField
                    id='outlined-dense'
                    label='Summary'
                    margin='dense'
                    variant='outlined'
                    fullWidth
                    value={operation.spec.summary}
                    onChange={({ target: { value } }) =>
                        operationActionsDispatcher({ action: 'summary', event: { operation, value } })
                    }
                />
            </Grid>
        </Fragment>
    );
}

DescriptionAndSummary.propTypes = {
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
        spec: PropTypes.shape({}).isRequired,
    }).isRequired,
    operationActionsDispatcher: PropTypes.func.isRequired,
};

DescriptionAndSummary.defaultProps = {};
