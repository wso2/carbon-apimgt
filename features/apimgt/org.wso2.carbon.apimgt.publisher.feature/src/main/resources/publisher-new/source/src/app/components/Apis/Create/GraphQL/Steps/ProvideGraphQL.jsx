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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import API from 'AppData/api';
import DropZoneLocal from 'AppComponents/Shared/DropZoneLocal';

const useStyles = makeStyles(theme => ({
    mandatoryStar: {
        color: theme.palette.error.main,
    },
}));

/**
 * Sub component of API Create using GraphQL UI, This is handling the taking input of GraphQL file or URL from the user
 * In the create API using OpenAPI wizard first step out of 2 steps
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function ProvideGraphQL(props) {
    const { apiInputs, inputsDispatcher, onValidate } = props;
    const classes = useStyles();
    // If valid value is `null`,that means valid, else an error object will be there
    const [isValid, setValidity] = useState({ file: null });
    const [isValidating, setIsValidating] = useState(false);
    /**
     *
     *
     * @param {*} files
     */
    function onDrop(files) {
        setIsValidating(true);

        // Why `files.pop()` below is , We only handle one graphQL file at a time,
        // So if use provide multiple, We would only
        // accept the first file. This information is shown in the dropdown helper text
        const file = files.pop();
        let validFile = null;
        API.validateGraphQLFile(file)
            .then((response) => {
                const {
                    body: { isValid: isValidFile, info: graphQLInfo },
                } = response;
                if (isValidFile) {
                    validFile = file;
                    inputsDispatcher({ action: 'graphQLBean', value: graphQLInfo });
                    setValidity({ ...isValid, file: null });
                } else {
                    setValidity({ ...isValid, file: { message: 'GraphQL content validation failed!' } });
                }
            })
            .catch((error) => {
                setValidity({ ...isValid, file: { message: 'GraphQL content validation failed!' } });
                console.error(error);
            })
            .finally(() => {
                setIsValidating(false); // Stop the loading animation
                onValidate(validFile !== null); // If there is a valid file then validation has passed
                // If the given file is valid , we set it as the inputValue else set `null`
                inputsDispatcher({ action: 'inputValue', value: validFile });
            });
    }

    return (
        <React.Fragment>
            <Grid container spacing={5}>
                <Grid item md={12}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>
                            <React.Fragment>
                                <sup className={classes.mandatoryStar}>*</sup>{' '}
                                <FormattedMessage
                                    id='Apis.Create.GraphQL.Steps.ProvideGraphQL.Input.type'
                                    defaultMessage='Provide GraphQL File'
                                />
                            </React.Fragment>
                        </FormLabel>
                    </FormControl>
                </Grid>
                <Grid item md={7}>
                    <DropZoneLocal error={isValid.file} onDrop={onDrop} files={apiInputs.inputValue}>
                        {isValidating && <CircularProgress />}
                        {isValid.file ? (
                            isValid.file.message
                        ) : (
                            <FormattedMessage
                                id='Apis.Create.GraphQL.Steps.ProvideGraphQL.Input.file.dropzone'
                                defaultMessage='Select an GraphQL definition file'
                            />
                        )}
                    </DropZoneLocal>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}

ProvideGraphQL.defaultProps = {
    onValidate: () => {},
};
ProvideGraphQL.propTypes = {
    apiInputs: PropTypes.shape({
        type: PropTypes.string,
        inputType: PropTypes.string,
    }).isRequired,
    inputsDispatcher: PropTypes.func.isRequired,
    onValidate: PropTypes.func,
};
