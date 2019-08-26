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
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import SelectPolicies from './components/SelectPolicies';

const useStyles = makeStyles(theme => ({
    mandatoryStar: {
        color: theme.palette.error.main,
    },
}));

/**
 * Improved API create default form
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DefaultAPIForm(props) {
    const { onChange, api } = props;
    const classes = useStyles();

    return (
        <Grid item md={9}>
            <form noValidate autoComplete='off'>
                <TextField
                    autoFocus
                    fullWidth
                    id='outlined-name'
                    label={
                        <React.Fragment>
                            <sup className={classes.mandatoryStar}>*</sup>{' '}
                            <FormattedMessage id='Apis.Create.WSDL.Steps.DefaultAPIForm.name' defaultMessage='Name' />
                        </React.Fragment>
                    }
                    helperText='API name can not contain spaces or any special characters'
                    value={api.name}
                    name='name'
                    onChange={onChange}
                    margin='normal'
                    variant='outlined'
                />
                <Grid container spacing={2}>
                    <Grid item md={4}>
                        <TextField
                            fullWidth
                            id='outlined-name'
                            label={
                                <React.Fragment>
                                    <sup className={classes.mandatoryStar}>*</sup>{' '}
                                    <FormattedMessage
                                        id='Apis.Create.WSDL.Steps.DefaultAPIForm.version'
                                        defaultMessage='Version'
                                    />
                                </React.Fragment>
                            }
                            name='version'
                            value={api.version}
                            onChange={onChange}
                            margin='normal'
                            variant='outlined'
                        />
                    </Grid>
                    <Grid item md={8}>
                        <TextField
                            fullWidth
                            id='outlined-name'
                            label={
                                <React.Fragment>
                                    <sup className={classes.mandatoryStar}>*</sup>{' '}
                                    <FormattedMessage
                                        id='Apis.Create.WSDL.Steps.DefaultAPIForm.context'
                                        defaultMessage='Context'
                                    />
                                </React.Fragment>
                            }
                            helperText='API will be exposed in this context at the gateway'
                            name='context'
                            value={api.context}
                            onChange={onChange}
                            margin='normal'
                            variant='outlined'
                        />
                    </Grid>
                </Grid>

                <TextField
                    fullWidth
                    id='outlined-name'
                    label='Endpoint'
                    name='endpoint'
                    value={api.endpoint}
                    onChange={onChange}
                    margin='normal'
                    variant='outlined'
                />

                <SelectPolicies policies={api.policies} onChange={onChange} />
            </form>
            <Grid container direction='row' justify='flex-end' alignItems='center'>
                <Grid item>
                    <Typography variant='caption' display='block' gutterBottom>
                        <sup style={{ color: 'red' }}>*</sup> Mandatory fields
                    </Typography>
                </Grid>
            </Grid>
        </Grid>
    );
}
