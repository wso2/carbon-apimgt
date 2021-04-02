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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import InsertDriveFile from '@material-ui/icons/InsertDriveFile';
import DeleteIcon from '@material-ui/icons/Delete';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/ListItemText';
import API from 'AppData/api';
import DropZoneLocal, { humanFileSize } from 'AppComponents/Shared/DropZoneLocal';
import Banner from 'AppComponents/Shared/Banner';

const useStyles = makeStyles((theme) => ({
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
    const { inputValue } = apiInputs;
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
                    body: { isValid: isValidFile, graphQLInfo },
                } = response;
                if (isValidFile) {
                    validFile = file;
                    inputsDispatcher({ action: 'graphQLInfo', value: graphQLInfo });
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

    useEffect(() => {
        if (inputValue) {
            onDrop([inputValue]);
        }
    }, [inputValue]);
    const accept = '.graphql,text/plain';
    return (
        <>
            <Grid container spacing={5}>
                {!apiInputs.inputValue && (
                    <Grid item md={12}>
                        <FormControl component='fieldset'>
                            <FormLabel component='legend'>
                                <>
                                    <sup className={classes.mandatoryStar}>*</sup>
                                    {' '}
                                    <FormattedMessage
                                        id='Apis.Create.GraphQL.Steps.ProvideGraphQL.Input.type'
                                        defaultMessage='Provide GraphQL File'
                                    />
                                </>
                            </FormLabel>
                        </FormControl>
                    </Grid>
                )}
                {isValid.file
                    && (
                        <Grid item md={11}>
                            <Banner
                                onClose={() => setValidity({ file: null })}
                                disableActions
                                dense
                                paperProps={{ elevation: 1 }}
                                type='error'
                                message={isValid.file.message}
                            />
                        </Grid>
                    )}
                <Grid item md={11}>
                    {apiInputs.inputValue ? (
                        <List>
                            <ListItem key={apiInputs.inputValue.path}>
                                <ListItemAvatar>
                                    <Avatar>
                                        <InsertDriveFile />
                                    </Avatar>
                                </ListItemAvatar>
                                <ListItemText
                                    primary={`${apiInputs.inputValue.path} - 
                                    ${humanFileSize(apiInputs.inputValue.size)}`}
                                />
                                <ListItemSecondaryAction>
                                    <IconButton
                                        edge='end'
                                        aria-label='delete'
                                        onClick={() => {
                                            inputsDispatcher({ action: 'inputValue', value: null });
                                            inputsDispatcher({ action: 'isFormValid', value: false });
                                        }}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </ListItemSecondaryAction>
                            </ListItem>
                        </List>
                    ) : (
                        <DropZoneLocal
                            error={isValid.file}
                            onDrop={onDrop}
                            files={apiInputs.inputValue}
                            accept={accept}
                        >
                            {isValidating ? (<CircularProgress />)
                                : ([
                                    <FormattedMessage
                                        id='Apis.Create.GraphQL.Steps.ProvideGraphQL.Input.file.dropzone'
                                        defaultMessage={'Drag & Drop files here {break} or {break} '
                                        + 'Browse files{break}({accept})'}
                                        values={{ break: <br />, accept }}
                                    />,
                                    <Button
                                        color='primary'
                                        variant='contained'
                                    >
                                        <FormattedMessage
                                            id='Apis.Create.GraphQL.Steps.ProvideGraphQL.Input.file.upload'
                                            defaultMessage='Browse File to Upload'
                                        />
                                    </Button>,
                                ]
                                )}
                        </DropZoneLocal>
                    )}
                </Grid>
            </Grid>
        </>
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
