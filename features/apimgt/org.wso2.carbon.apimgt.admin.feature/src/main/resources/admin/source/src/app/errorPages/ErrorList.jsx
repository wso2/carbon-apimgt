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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
    errorIcon: {
        marginBottom: theme.spacing(-1),
        marginRight: theme.spacing(1),
    },
}));

const ErrorList = (props) => {
    const { errorCode } = props;
    const classes = useStyles();
    switch (errorCode) {
        case '500':
            return (
                <>
                    <Typography variant='h5' gutterBottom>
                        <ErrorOutlineIcon fontSize='large' color='error' className={classes.errorIcon} />
                        <FormattedMessage
                            id='error.list.500'
                            defaultMessage='500 : The page cannot be displayed.'
                        />
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='error.list.500.description'
                            defaultMessage={'The server encountered an internal error or misconfiguration and'
                            + ' was unable to complete your request.'}
                        />
                    </Typography>
                </>
            );
        case '401':
            return (
                <>
                    <Typography variant='h5' gutterBottom>
                        <ErrorOutlineIcon fontSize='large' color='error' className={classes.errorIcon} />
                        <FormattedMessage
                            id='error.list.401'
                            defaultMessage='401 : Authorization Required.'
                        />
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='error.list.401.description'
                            defaultMessage={'The server could not verify that you are authorized to '
                            + 'access the requested resource'}
                        />
                    </Typography>
                </>
            );
        case '403':
            return (
                <>
                    <Typography variant='h5' gutterBottom>
                        <ErrorOutlineIcon fontSize='large' color='error' className={classes.errorIcon} />
                        <FormattedMessage
                            id='error.list.403'
                            defaultMessage='403 : Forbidden.'
                        />
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='error.list.403.description'
                            defaultMessage={'You do not have permission to access anything with that '
                            + 'kind of request.'}
                        />
                    </Typography>
                </>
            );
        case '404':
            return (
                <>
                    <Typography variant='h5' gutterBottom>
                        <ErrorOutlineIcon fontSize='large' color='error' className={classes.errorIcon} />
                        <FormattedMessage
                            id='error.list.404'
                            defaultMessage='404 : The page cannot be found.'
                        />
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='error.list.404.description'
                            defaultMessage={'The page you are looking for might have been removed,  '
                            + 'had its name changed or is temporarily unavailable.'}
                        />
                    </Typography>
                </>
            );
        default:
            break;
    }
    return errorCode;
};
export default ErrorList;
