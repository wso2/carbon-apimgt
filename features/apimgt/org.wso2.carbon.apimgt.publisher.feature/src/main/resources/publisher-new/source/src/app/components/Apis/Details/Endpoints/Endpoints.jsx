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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';
import FormControl from '@material-ui/core/FormControl';
import { FormattedMessage } from 'react-intl';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import API from 'AppData/api';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    FormControl: {
        padding: '0 20px',
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: '0 20px',
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
});
/**
 * @export @inheritDoc
 * @class InputForm
 * @extends {Component}
 */
class Endpoints extends Component {
    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof Endpoints
     */
    handleInputChange () {
        console.info('handle input change');
    }
    handleSubmit () {
        console.info('handle submit');
    }
    showEndpoint (api, type) {
        if(api.endpoint.length > 0){
            for(var i=0; i< api.endpoint.length; i++){
                if( type === "prod" && api.endpoint[i].type === "http"){
                    return api.endpoint[i].inline.endpointConfig.list[0].url;
                } else if( type === "sand" && api.endpoint[i].type === "sandbox_endpoints"){
                    return api.endpoint[i].inline.endpointConfig.list[0].url;
                }
            }
            
        }
    }
    render() {
        const { classes } = this.props;
        return (
            <ApiContext.Consumer>
                {({ api }) => (
                    <Grid container spacing={24} className={classes.root}>
                        <Grid item xs={12} md={8}>
                            <div className={classes.titleWrapper}>
                                <Typography variant='h4' align='left' className={classes.mainTitle}>
                                   Endpoints
                                </Typography>
                            </div>
                            <form onSubmit={this.handleSubmit}>
                                <FormControl margin='normal' className={classes.FormControl}>
                                    <TextField
                                        error={false}
                                        fullWidth
                                        id='prodEndpoint'
                                        placeholder='E.g: http://appserver/resource'
                                        helperText={
                                            false ? (
                                                <FormattedMessage
                                                    id='error.empty'
                                                    defaultMessage='This field can not be empty.'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='api.create.endpoint.help'
                                                    defaultMessage='This is the actual endpoint where the API implementation can be found'
                                                />
                                            )
                                        }
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        label='Production Endpoint'
                                        type='text'
                                        name='Production Endpoint'
                                        margin='normal'
                                        value={this.showEndpoint(api,'prod')}
                                        onChange={this.handleInputChange}
                                    />
                                </FormControl>
                                <FormControl margin='normal' className={classes.FormControlOdd}>
                                    <TextField
                                        error={false}
                                        fullWidth
                                        id='sandboxEndpoint'
                                        placeholder='E.g: http://appserver/resource'
                                        helperText={
                                            false ? (
                                                <FormattedMessage
                                                    id='error.empty'
                                                    defaultMessage='This field can not be empty.'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='api.create.endpoint.help'
                                                    defaultMessage='This is the actual endpoint where the API implementation can be found'
                                                />
                                            )
                                        }
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        label='Sandbox Endpoint'
                                        type='text'
                                        name='Sandbox Endpoint'
                                        margin='normal'
                                        value={this.showEndpoint(api,'sand')}
                                        onChange={this.handleInputChange}
                                    />
                                </FormControl>
                                <Grid
                                    container
                                    direction='row'
                                    alignItems='flex-start'
                                    spacing={16}
                                    className={classes.buttonSection}
                                >
                                    <Grid item>
                                        <Button type='submit' variant='contained' color='primary'>
                                            Save
                                        </Button>
                                    </Grid>
                                    <Grid item>
                                        <Button onClick={() => this.props.history.push('/apis')}>
                                            <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                        </Button>
                                    </Grid>
                                </Grid>
                            </form>
                        </Grid>
                    </Grid>
                )}
            </ApiContext.Consumer>
        );
    }
}

Endpoints.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Endpoints);
