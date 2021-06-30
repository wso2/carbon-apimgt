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
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { app } from 'Settings';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
    },
    messageWrapper: {
        marginTop: theme.spacing(4),
        padding: theme.spacing(2),
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
}));

/**
 * Render no api section
 * @returns {void}
 */
export default function NoApi() {
    const classes = useStyles();
    const theme = useTheme();

    return (
        <div className={classes.root}>
            <Grid container spacing={3}>
                <Grid item xs={12} className={classes.messageWrapper}>
                    <img alt='API icon' src={app.context + theme.custom.noApiImage} className={classes.messageWrapper} />
                    <Typography id='itest-no-apis' variant='h5' gutterBottom>
                        <FormattedMessage id='Apis.Listing.NoApi.nodata.title' defaultMessage='No APIs Available' />
                    </Typography>
                    <Typography variant='subtitle1' gutterBottom>
                        <FormattedMessage
                            id='Apis.Listing.NoApi.nodata.content'
                            defaultMessage='There are no APIs to display right now.'
                        />
                    </Typography>
                </Grid>
            </Grid>
        </div>
    );
}
