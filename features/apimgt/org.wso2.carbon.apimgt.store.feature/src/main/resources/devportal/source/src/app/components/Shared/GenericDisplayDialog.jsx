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
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';

const useStyles = makeStyles((theme) => ({
    appContent: {
        margin: theme.spacing(2),
    },
    button: {
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
  }));

const genericDisplayDialog = (props) => {
    const {
        handleClick, heading, caption, buttonText,
    } = props;
    const classes = useStyles();
    return (
        <div className={classes.appContent}>
            <InlineMessage type='info' className={classes.dialogContainer}>
                <Typography variant='h5' component='h2'>
                    {heading}
                </Typography>
                <Typography variant="body2" gutterBottom>
                    {caption}
                </Typography>
                <ScopeValidation resourcePath={resourcePaths.APPLICATIONS} resourceMethod={resourceMethods.POST}>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.button}
                        onClick={handleClick}
                    >
                        {buttonText}
                    </Button>
                </ScopeValidation>
            </InlineMessage>
        </div>
    );
};

export default genericDisplayDialog;
