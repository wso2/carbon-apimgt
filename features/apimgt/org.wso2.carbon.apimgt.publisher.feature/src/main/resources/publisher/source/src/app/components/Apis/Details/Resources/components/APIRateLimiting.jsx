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
import React, { useState, useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Radio from '@material-ui/core/Radio';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import Paper from '@material-ui/core/Paper';
import TextField from '@material-ui/core/TextField';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import MenuItem from '@material-ui/core/MenuItem';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import CircularProgress from '@material-ui/core/CircularProgress';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

const RateLimitingLevels = {
    API: 'api',
    RESOURCE: 'resource',
};

const useStyles = makeStyles((theme) => ({
    focusLabel: {
        boxShadow: '1px 1px 1px 1px #efefef',
        paddingRight: theme.spacing(1),
    },
}));

/**
 *
 * Handles the resource level and API level throttling UI switch
 * @export
 * @param {*} props
 * @returns
 */
function APIRateLimiting(props) {
    const {
        updateAPI, operationRateLimits, onChange, value: currentApiThrottlingPolicy, isAPIProduct,
        setFocusOperationLevel, focusOperationLevel,
    } = props;
    const classes = useStyles();
    const [apiThrottlingPolicy, setApiThrottlingPolicy] = useState(currentApiThrottlingPolicy);
    const [isSaving, setIsSaving] = useState(false);

    const isResourceLevel = apiThrottlingPolicy === null;
    const rateLimitingLevel = isResourceLevel ? RateLimitingLevels.RESOURCE : RateLimitingLevels.API;
    const [apiFromContext] = useAPI();

    // Following effect is used to handle the controlled component case, If user provide onChange handler to
    // control this component, Then we accept the props as the valid input and update the current state value from props
    useEffect(() => {
        if (onChange) {
            if (currentApiThrottlingPolicy === '' && apiFromContext.apiThrottlingPolicy) {
                setApiThrottlingPolicy(apiFromContext.apiThrottlingPolicy);
            } else {
                setApiThrottlingPolicy(currentApiThrottlingPolicy);
            }
        }
    }, [onChange, currentApiThrottlingPolicy]); // Do not expect to change the onChange during the runtime

    /**
     *
     *
     * @param {*} event
     */
    function updateRateLimitingPolicy(event) {
        // If the selected option is resource, we set the api level rate limiting to null
        const userSelection = event.target.value === RateLimitingLevels.RESOURCE
            ? null : '';
        if (onChange) {
            // Assumed controlled component
            onChange(userSelection);
        } else {
            setApiThrottlingPolicy(userSelection);
        }
        if (event.target.value === RateLimitingLevels.RESOURCE) {
            setFocusOperationLevel(false);
        }
    }
    /**
     *
     *
     */
    function saveChanges() {
        setIsSaving(true);
        updateAPI({ apiThrottlingPolicy }).finally(() => setIsSaving(false));
    }

    /**
     *
     *
     */
    function resetChanges() {
        setApiThrottlingPolicy(currentApiThrottlingPolicy);
    }

    let operationRateLimitMessage = (
        <Typography variant='body1' gutterBottom>
            You may change the rate limiting policies per operation
            <Typography variant='caption' display='block' gutterBottom>
                Expand an operation below to select a rate limiting policy for an operation
            </Typography>
        </Typography>
    );
    if (isAPIProduct) {
        operationRateLimitMessage = (
            <Typography variant='body1' gutterBottom>
                Rate limiting polices of the source operation will be applied
                <Typography variant='caption' display='block' gutterBottom>
                    Rate limiting policy of an individual operation will be govern by the policy specified in the source
                    operation
                </Typography>
            </Typography>
        );
    }
    return (
        <Paper>
            <Grid container direction='row' spacing={3} justify='flex-start' alignItems='flex-start'>
                <Grid item md={12} xs={12}>
                    <Box ml={1}>
                        <Typography variant='subtitle1' gutterBottom>
                            Operations Configuration
                            <Tooltip
                                fontSize='small'
                                title='Configurations that affects on all the resources'
                                aria-label='common configurations'
                                placement='right-end'
                                interactive
                            >
                                <HelpOutline />
                            </Tooltip>
                        </Typography>
                    </Box>
                    <Divider light variant='middle' />
                </Grid>
                <Grid item md={1} xs={1} />
                <Grid item md={5} xs={11}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>Rate limiting level</FormLabel>
                        <RadioGroup
                            aria-label='Apply rate limiting in'
                            value={rateLimitingLevel}
                            onChange={updateRateLimitingPolicy}
                            row
                        >
                            <FormControlLabel
                                value={RateLimitingLevels.API}
                                control={(
                                    <Radio
                                        color='primary'
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                    />
                                )}
                                label='API Level'
                                labelPlacement='end'
                            />
                            <FormControlLabel
                                value={RateLimitingLevels.RESOURCE}
                                control={(
                                    <Radio
                                        color='primary'
                                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                    />
                                )}
                                className={focusOperationLevel && classes.focusLabel}
                                label='Operation Level'
                                labelPlacement='end'
                            />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item md={6} xs={12}>
                    <Box minHeight={70} borderLeft={1} pl={10}>
                        {isResourceLevel ? (
                            operationRateLimitMessage
                        ) : (
                            <TextField
                                disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                id='operation_throttling_policy'
                                select
                                label='Rate limiting policies'
                                value={apiThrottlingPolicy}
                                onChange={({ target: { value } }) => (
                                    onChange ? onChange(value) : setApiThrottlingPolicy(value))}
                                helperText='Selected rate limiting policy will be applied to whole API'
                                margin='dense'
                                variant='outlined'
                            >
                                {operationRateLimits.map((rateLimit) => (
                                    <MenuItem key={rateLimit.name} value={rateLimit.name}>
                                        {rateLimit.displayName}
                                    </MenuItem>
                                ))}
                            </TextField>
                        )}
                    </Box>
                </Grid>
                {/* If onChange handler is provided we assume that component is getting controlled by its parent
                so that, hide the save cancel action */}
                {!onChange && (
                    <>
                        <Grid item md={12}>
                            <Divider />
                        </Grid>
                        <Grid item>
                            <Box ml={1}>
                                <Button
                                    onClick={saveChanges}
                                    disabled={false}
                                    variant='outlined'
                                    size='small'
                                    color='primary'
                                >
                                    Save
                                    {isSaving && <CircularProgress size={24} />}
                                </Button>
                                <Box display='inline' ml={1}>
                                    <Button size='small' onClick={resetChanges}>
                                        Reset
                                    </Button>
                                </Box>
                            </Box>
                        </Grid>
                    </>
                )}
            </Grid>
        </Paper>
    );
}
APIRateLimiting.defaultProps = {
    onChange: null,
    isAPIProduct: false,
};
APIRateLimiting.propTypes = {
    updateAPI: PropTypes.func.isRequired,
    onChange: PropTypes.oneOf([null, PropTypes.func]),
    operationRateLimits: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
    disabledAction: PropTypes.shape({}).isRequired,
    value: PropTypes.string.isRequired,
    isAPIProduct: PropTypes.bool,
};

export default React.memo(APIRateLimiting);
