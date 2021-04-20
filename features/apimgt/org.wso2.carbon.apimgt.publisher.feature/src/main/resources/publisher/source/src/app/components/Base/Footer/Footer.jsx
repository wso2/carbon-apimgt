/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import Configurations from 'Config';

import FeedbackForm from './FeedbackForm';

const useStyles = makeStyles((theme) => ({
    footer: {
        background: theme.custom.footer.background,
        paddingLeft: theme.spacing(3),
        height: theme.custom.footer.height,
        alignItems: 'center',
        display: 'flex',
        color: theme.custom.footer.color,
    },
}));

/**
 *
 *
 * @param {*} props
 * @returns
 */
function Footer() {
    const classes = useStyles();
    const theme = useTheme();

    return (
        <footer className={classes.footer}>
            <Grid container direction='row' justify='space-between' alignItems='center'>
                <Grid item>
                    {theme.custom.footer.text ? theme.custom.footer.text : (
                        <Typography noWrap>
                            <FormattedMessage
                                id='Base.Footer.Footer.product_details'
                                defaultMessage='WSO2 API-M v4.0.0 | © 2021 WSO2 Inc'
                            />
                        </Typography>
                    )}
                </Grid>
                {Configurations.app.feedback.enable && (
                    <Grid item>
                        <FeedbackForm />
                    </Grid>
                )}
                <Grid item />
            </Grid>
        </footer>
    );
}

export default Footer;
