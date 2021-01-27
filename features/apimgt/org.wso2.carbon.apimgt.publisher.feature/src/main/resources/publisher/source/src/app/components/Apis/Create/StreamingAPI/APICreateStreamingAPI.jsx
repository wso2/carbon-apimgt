/**
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { useReducer, useState } from 'react';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withRouter } from 'react-router';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import { makeStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';
import Banner from 'AppComponents/Shared/Banner';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import { useAppContext } from 'AppComponents/Shared/AppContext';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
}));

const APICreateStreamingAPI = () => {
    // parameter: props
    const { settings } = useAppContext();
    const [pageError, setPageError] = useState(null);
    const [isCreating, setIsCreating] = useState();
    const classes = useStyles();

    /**
     *
     * Reduce the events triggered from API input fields to current state
     */
    function apiInputsReducer(currentState, inputAction) {
        const { action, value } = inputAction;
        switch (action) {
            case 'name':
            case 'version':
            case 'context':
            case 'endpoint':
            case 'protocol':
            case 'policies':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            default:
                return currentState;
        }
    }
    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        formValidity: false,
    });

    const isAPICreatable = apiInputs.name && apiInputs.context && apiInputs.version && !isCreating;

    const protocols = [
        {
            name: 'websub',
            displayName: 'WebSub',
            description: 'WebSub',
        },
    ];

    /**
     *
     *
     * @param {*} event
     */
    function handleOnChange(event) {
        const { name: action, value } = event.target;
        inputsDispatcher({ action, value });
    }

    /**
     *
     * Set the validity of the API Inputs form
     * @param {*} isValidForm
     * @param {*} validationState
     */
    function handleOnValidate(isFormValid) {
        inputsDispatcher({
            action: 'isFormValid',
            value: isFormValid,
        });
    }

    function createAPI() {
        const {
            name, version, context, endpoint, protocol, policies,
        } = apiInputs;
        const apiData = {
            name,
            version,
            context,
            endpoint,
            type: protocol.toUpperCase(),
            policies,
        };

        if (endpoint) {
            apiData.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: endpoint,
                },
                production_endpoints: {
                    url: endpoint,
                },
            };
        }

        apiData.gatewayEnvironments = settings.environment.map((env) => env.name);

        const newAPI = new API(apiData);
        const promisedCreatedAPI = newAPI
            .saveStreamingAPI()
            .then((api) => {
                Alert.info('API created successfully');
                return api;
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                    setPageError(error.response.body);
                } else {
                    const message = 'Something went wrong while adding the API';
                    Alert.error(message);
                    setPageError(message);
                }
                console.error(error);
            })
            .finally(() => {
                setIsCreating(false);
            });
        return promisedCreatedAPI.finally(() => setIsCreating(false));
    }

    function createAPIOnly() {
        createAPI().then((api) => {
            window.history.push(`/apis/${api.id}/overview`);
        });
    }

    const pageTitle = (
        <>
            <Typography variant='h5'>
                <FormattedMessage
                    id='Apis.Create.StreamingAPI.APICreateStreamingAPI.api.heading'
                    defaultMessage='Create a Streaming API'
                />
            </Typography>
            <Typography variant='caption'>
                <FormattedMessage
                    id='Apis.Create.StreamingAPI.APICreateStreamingAPI.api.sub.heading'
                    defaultMessage={
                        'Create an API by providing a Name, a Version, a Context, Backend Endpoint(s) (optional), '
                        + 'and Business Plans (optional).'
                    }
                />
            </Typography>
        </>
    );

    return (
        <APICreateBase title={pageTitle}>
            <Grid container direction='row' justify='center' alignItems='center' spacing={3}>
                {/* Page error banner */}
                {pageError && (
                    <Grid item xs={11}>
                        <Banner
                            onClose={() => setPageError(null)}
                            disableActions
                            dense
                            paperProps={{ elevation: 1 }}
                            type='error'
                            message={pageError}
                        />
                    </Grid>
                )}
                {/* end of Page error banner */}
                <Grid item xs={12} />
                <Grid item md={1} xs={0} />
                <Grid item md={11} xs={12}>
                    <DefaultAPIForm
                        onValidate={handleOnValidate}
                        onChange={handleOnChange}
                        api={apiInputs}
                        endpointPlaceholderText='Streaming Provider'
                        appendChildrenBeforeEndpoint
                        hideEndpoint
                    >
                        <TextField
                            fullWidth
                            select
                            label={(
                                <>
                                    <FormattedMessage
                                        id='Apis.Create.Components.SelectPolicies.business.plans'
                                        defaultMessage='Protocol'
                                    />
                                    <sup className={classes.mandatoryStar}>*</sup>
                                </>
                            )}
                            value={apiInputs.protocol}
                            name='protocol'
                            SelectProps={{
                                multiple: false,
                                renderValue: (selected) => (selected),
                            }}
                            margin='normal'
                            variant='outlined'
                            InputProps={{
                                id: 'itest-id-apipolicies-input',
                            }}
                            onChange={handleOnChange}
                        >
                            {protocols.map((protocol) => (
                                <MenuItem
                                    dense
                                    disableGutters={false}
                                    id={protocol.name}
                                    key={protocol.name}
                                    value={protocol.displayName}
                                >
                                    <ListItemText primary={protocol.displayName} secondary={protocol.description} />
                                </MenuItem>
                            ))}
                        </TextField>
                    </DefaultAPIForm>
                </Grid>
                <Grid item md={1} xs={0} />
                <Grid item md={11} xs={12}>
                    <Grid container direction='row' justify='flex-start' alignItems='center' spacing={2}>
                        <Grid item>
                            <Button
                                variant='contained'
                                color='primary'
                                disabled={!(isAPICreatable && apiInputs.isFormValid)}
                                onClick={createAPIOnly}
                            >
                                Create
                                {' '}
                                {isCreating && <CircularProgress size={24} />}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Link to='/apis/'>
                                <Button variant='text'>
                                    <FormattedMessage
                                        id='Apis.Create.Default.APICreateDefault.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </APICreateBase>
    );
};
APICreateStreamingAPI.WORKFLOW_STATUS = {
    CREATED: 'CREATED',
};

export default withRouter(injectIntl(APICreateStreamingAPI));
