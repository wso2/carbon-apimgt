/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import FormGroup from '@material-ui/core/FormGroup';
import { makeStyles } from '@material-ui/core/styles';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';

const useStyles = makeStyles(theme => ({
    checkLabel: {
        paddingTop: 4,
        backgroundColor: theme.palette.background.paper,
    },
}));

/**
 * Generate the Depth Analysis UI under Query Depth tab within Query Analysis
 * @returns {*} Depth Analysis page UI
 */
function DepthAnalysis() {
    const classes = useStyles();
    const [state, setState] = React.useState({
        depthCheck: true,
    });

    const handleDepthToggle = name => (event) => {
        setState({ ...state, [name]: event.target.checked });
    };

    return (
        <React.Fragment>
            <Box>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.QueryAnalysis.DepthAnalysis.query.depth'
                        defaultMessage='Query Depth'
                    />
                </Typography>
            </Box>
            <FormGroup row>
                <Typography variant='subtitle2' className={classes.checkLabel}>
                    <FormattedMessage
                        id='Apis.Details.Depth.Analysis.DepthAnalysisCheck'
                        defaultMessage='Enable / Disable Depth Limitation'
                    />
                </Typography>
                <FormControlLabel
                    control={
                        <Switch
                            checked={state.depthCheck}
                            onChange={handleDepthToggle('depthCheck')}
                            value='depthCheck'
                            color='primary'
                        />
                    }
                />
            </FormGroup>
        </React.Fragment>
    );
}

export default DepthAnalysis;
