/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import React from 'react';
import { Component } from 'react';
import { Grid, Paper, Typography, Divider } from '@material-ui/core';
import EndpointsSelector from './EndpointsSelector';
import PropTypes from 'prop-types';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import { withStyles, MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import purple from '@material-ui/core/colors/purple';
import green from '@material-ui/core/colors/green';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import EndpointForm from './EndpointForm.jsx';


/**
 * API Details Endpoint page component
 * @class EndpointDetail
 * @extends {Component}
 */

const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    margin: {
        margin: theme.spacing.unit,
    },
    paper: {
        padding: theme.spacing.unit * 2,
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,

    },
    button: {
        margin: theme.spacing.unit,
    },
});
class EndpointDetail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isInline: this.props.isInline,
            selectedEndpointConfig: this.props.endpoint.endpointConfig[0],
        };
        this.handleEpClick = this.handleEpClick.bind(this);
    }

    handleEpClick(e) {
        this.setState({ selectedEndpointConfig: this.props.endpoint.endpointConfig[e.currentTarget.getAttribute('name')] });
    }

    render() {
        const { classes } = this.props;
        const flexContainer = {
            display: 'flex',
            flexDirection: 'row',
        };
        var epUrls = [];
        for (name in this.props.endpoint.endpointConfig) {
            epUrls.push(<Button variant="outlined" className={classes.button} onClick={this.handleEpClick} name={name}>
                {this.props.endpoint.endpointConfig[name].url}
            </Button>)
        };
        return (
            <div className={classes.root}>
                <Grid container spacing={24} justify='center'>
                    <Grid item md={12}>
                        <Typography variant='heading' gutterBottom>
                            {this.props.type} <FormattedMessage
                                id='endpoint'
                                defaultMessage='Endpoint'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={6}>

                        <Paper className={classes.paper}>
                            <div className={classes.container}>

                                {epUrls}
                            </div>
                        </Paper>

                    </Grid>
                    <EndpointForm
                        selectedEndpointConfig={this.state.selectedEndpointConfig} endpoint={this.props.endpoint} isInline={this.state.isInline} readOnly={this.props.readOnly}
                    />
                </Grid>
            </div>
        )
    }
}
EndpointDetail.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointDetail);
