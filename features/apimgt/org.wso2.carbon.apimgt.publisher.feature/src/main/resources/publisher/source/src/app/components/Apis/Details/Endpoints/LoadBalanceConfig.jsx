/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useState, useContext } from 'react';
import {
    TextField, MenuItem, Grid, Button, withStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import APIContext from 'AppComponents/Apis/Details/components/ApiContext';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';

const algorithms = [
    {
        key: 'org.apache.synapse.endpoints.algorithms.RoundRobin',
        value: 'Round-Robin',
    },
    { key: 'other', value: 'Other' },
];
const sessionManagementOps = [
    { key: 'http', value: 'Transport' },
    { key: 'soap', value: 'SOAP' },
    { key: 'simpleClientSession', value: 'Client Id' },
    { key: 'none', value: 'None' },
];

const defaultTemplateObj = {
    algoClassName: algorithms[0].key,
    algoCombo: algorithms[0].key,
    sessionManagement: sessionManagementOps[0].key,
    sessionTimeOut: 300,
    failOver: false,
};

const styles = (theme) => ({
    configButtonContainer: {
        display: 'flex',
        justifyContent: 'flex-end',
        paddingTop: theme.spacing(1),
    },
});

/**
 * The component for loadbalance endpoint configuration.
 * @param {any} props The props that are being passed.
 * @returns {any} The HTML contents of the Configuration component.
 */
function LoadBalanceConfig(props) {
    const {
        algoClassName,
        algoCombo,
        sessionManagement,
        sessionTimeOut,
        failOver,
        handleLBConfigChange,
        closeLBConfigDialog,
        classes,
    } = props;
    const [lbConfig, setLbConfigObject] = useState(defaultTemplateObj);
    const [algoClassNameError, setAlgoClassNameError] = useState(false);
    const { api } = useContext(APIContext);

    useEffect(() => {
        setLbConfigObject(() => {
            const tmpLBConfig = { ...defaultTemplateObj };
            if (algoCombo) {
                tmpLBConfig.algoCombo = algoCombo;
            }
            if (sessionManagement) {
                tmpLBConfig.sessionManagement = sessionManagement;
            }
            if (algoClassName) {
                tmpLBConfig.algoClassName = algoClassName;
            }
            if (sessionTimeOut) {
                tmpLBConfig.sessionTimeOut = sessionTimeOut;
            }
            if (failOver) {
                tmpLBConfig.failOver = failOver;
            }
            return tmpLBConfig;
        });
    }, [props]);

    /**
     * Method to capture the alogrithm select changes.
     *
     * @param {any} event The event triggered by the element.
     * */
    const handleAlgorithmChange = (event) => {
        const { value } = event.target;
        setLbConfigObject({
            ...lbConfig,
            algoCombo: value,
            algoClassName: value === 'other' ? '' : defaultTemplateObj.algoClassName,
        });
    };

    /**
     * Method to capture the onChange event of the elements.
     *
     * @param {any} event The event triggered by the element.
     * @param {string} field The respective field which is being changed.
     * */
    const handleFieldChange = (event, field) => {
        setLbConfigObject({ ...lbConfig, [field]: event.target.value });
    };

    /**
     * Method to capture the onChange event of the elements.
     *
     * @param {any} event The event triggered by the element.
     * @param {string} field The respective field which is being changed.
     * */
    const handleFailoverFieldChange = (event, field) => {
        setLbConfigObject({ ...lbConfig, [field]: event.target.checked });
    };

    /**
     * Method to set the configuration changes to the original endpoints config object.
     * */
    const submitConfiguration = () => {
        handleLBConfigChange(lbConfig);
    };

    return (
        <>
            <Grid container direction='column'>
                <TextField
                    id='algorithmSelect'
                    select
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.LoadBalanceConfig.algorithm'
                            defaultMessage='Algorithm'
                        />
                    )}
                    value={lbConfig.algoCombo}
                    onChange={handleAlgorithmChange}
                    helperText='Please select the Loadbalance Algorithm.'
                    margin='normal'
                    disabled={isRestricted(['apim:api_create'], api)}
                >
                    {algorithms.map((algo) => (
                        <MenuItem key={algo.key} value={algo.key} selected={lbConfig.algoCombo}>
                            {algo.value}
                        </MenuItem>
                    ))}
                </TextField>
                {(lbConfig.algoCombo === 'other')
                    ? (
                        <TextField
                            id='customAlgoInput'
                            label={(
                                <FormattedMessage
                                    id='Apis.Details.Endpoints.LoadBalanceConfig.class.name.for.algorithm'
                                    defaultMessage='Class Name for Algorithm'
                                />
                            )}
                            required
                            error={algoClassNameError}
                            value={lbConfig.algoClassName}
                            onChange={(event) => handleFieldChange(event, 'algoClassName')}
                            onBlur={() => setAlgoClassNameError(lbConfig.algoClassName === '')}
                            helperText='Enter the class name of the loadbalance algorithm'
                            disabled={isRestricted(['apim:api_create'], api)}
                            margin='normal'
                        />
                    ) : <div /> }
                <TextField
                    id='sessionMgtSelect'
                    select
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.LoadBalanceConfig.session.management'
                            defaultMessage='Session Management'
                        />
                    )}
                    value={lbConfig.sessionManagement}
                    onChange={(event) => handleFieldChange(event, 'sessionManagement')}
                    helperText='Please select the Session Management mechanism.'
                    margin='normal'
                    disabled={isRestricted(['apim:api_create'], api)}
                >
                    {sessionManagementOps.map((option) => (
                        <MenuItem key={option.key} value={option.key}>
                            {option.value}
                        </MenuItem>
                    ))}
                </TextField>
                <TextField
                    id='sessionTimeout'
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.LoadBalanceConfig.session.timeout'
                            defaultMessage='Session Timeout (Millis)'
                        />
                    )}
                    value={lbConfig.sessionTimeOut}
                    onChange={(event) => handleFieldChange(event, 'sessionTimeOut')}
                    type='number'
                    placeholder='300'
                    margin='normal'
                    disabled={isRestricted(['apim:api_create'], api)}
                />
                <FormControlLabel
                    control={(
                        <Checkbox
                            id='failOver'
                            checked={lbConfig.failOver}
                            onChange={(event) => handleFailoverFieldChange(event, 'failOver')}
                            margin='normal'
                            disabled={isRestricted(['apim:api_create'], api)}
                        />
                    )}
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Endpoints.LoadBalanceConfig.failover'
                            defaultMessage='Enable Failover'
                        />
                    )}
                />
            </Grid>
            <Grid className={classes.configButtonContainer}>
                <Button
                    color='primary'
                    variant='contained'
                    autoFocus
                    onClick={submitConfiguration}
                    disabled={lbConfig.algoClassName === '' || isRestricted(['apim:api_create'], api)}
                    style={{ marginRight: '10px' }}
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.EndpointOverview.loadbalance.config.save.button'
                        defaultMessage='Save'
                    />
                </Button>
                <Button
                    onClick={closeLBConfigDialog}
                >
                    <FormattedMessage
                        id='Apis.Details.Endpoints.EndpointOverview.loadbalance.config.cancel.button'
                        defaultMessage='Close'
                    />
                </Button>
            </Grid>
        </>
    );
}

LoadBalanceConfig.propTypes = {
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    algoClassName: PropTypes.string.isRequired,
    algoCombo: PropTypes.string.isRequired,
    sessionManagement: PropTypes.string.isRequired,
    sessionTimeOut: PropTypes.string.isRequired,
    failOver: PropTypes.bool.isRequired,
    handleLBConfigChange: PropTypes.func.isRequired,
    closeLBConfigDialog: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(LoadBalanceConfig));
