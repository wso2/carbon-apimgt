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
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import { getVersion } from 'AppComponents/Apis/Details/Resources/operationUtils';
import AddParameter from './AddParameter';
import ListParameters from './ListParameters';

/**
 * Renders the operation parameters section
 * @export
 * @param {*} props
 * @returns
 */
export default function Parameters(props) {
    const {
        operation, spec, target, verb, operationsDispatcher, disableUpdate, resolvedSpec,
    } = props;
    const specVersion = getVersion(spec);
    const haveParameters = (operation.parameters && operation.parameters.length !== 0) || operation.requestBody;
    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography variant='subtitle1'>
                    Parameters
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item xs={1} />
            <Grid item xs={11}>
                {!disableUpdate && (
                    <AddParameter
                        target={target}
                        verb={verb}
                        operationsDispatcher={operationsDispatcher}
                        operation={operation}
                        specVersion={specVersion}
                    />
                )}
            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                {haveParameters && (
                    <ListParameters
                        disableUpdate={disableUpdate}
                        target={target}
                        verb={verb}
                        operationsDispatcher={operationsDispatcher}
                        operation={operation}
                        spec={spec}
                        specVersion={specVersion}
                        resolvedSpec={resolvedSpec}
                    />
                )}
            </Grid>
        </>
    );
}

Parameters.propTypes = {
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    spec: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    operation: PropTypes.shape({}).isRequired,
    disableUpdate: PropTypes.bool,
    resolvedSpec: PropTypes.shape({}).isRequired,
};

Parameters.defaultProps = {
    disableUpdate: false,
};
