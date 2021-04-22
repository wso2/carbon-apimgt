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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import ListParameters from './ListParameters';

/*
    Paramters will contain: name, description, type
*/

/**
 * Renders the operation parameters section
 * @export
 * @param {*} props
 * @returns
 */
export default function Parameters(props) {
    const {
        operation, spec, target, verb, operationsDispatcher, disableForSolace,
    } = props;
    let haveParameters = false;
    Object.entries(operation.parameters).forEach(() => {
        haveParameters = true;
    });
    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography variant='subtitle1'>
                    Topic Parameters
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                {haveParameters && (
                    <ListParameters
                        target={target}
                        verb={verb}
                        operationsDispatcher={operationsDispatcher}
                        operation={operation}
                        spec={spec}
                        disableForSolace={disableForSolace}
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
    resolvedSpec: PropTypes.shape({}).isRequired,
    disableForSolace: PropTypes.bool,
};

Parameters.defaultProps = {
    disableForSolace: false,
};
