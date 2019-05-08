/**
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared';

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
        backgroundColor: theme.palette.grey[300],
    },
    textField: {
        marginLeft: theme.spacing.unit * 5,
        marginRight: theme.spacing.unit * 5,
    },
    button: {
        margin: theme.spacing.unit,
    },
    grid: {
        paddingLeft: '10px',
        paddingRight: '10px',
        minWidth: '50%',
    },
    suspendGrid: {
        paddingLeft: '10px',
        paddingRight: '10px',
        minWidth: '33%',
    },
});

/**
 *
 *
 * @class EndpointForm
 * @extends {Component}
 */
class EndpointForm extends Component {
    /**
     *Creates an instance of EndpointForm.
     * @param {*} props properies passed by the parent element
     * @memberof EndpointForm
     */
    constructor(props) {
        super(props);
        this.state = {
            serviceUrl: null,
            timeout: null,
            suspendInitialDuration: null,
            suspendMaixumumlDuration: null,
        };
        this.handleTextFieldChange = this.handleTextFieldChange.bind(this);
        this.handleEndpointConfigTextFieldChange = this.handleEndpointConfigTextFieldChange.bind(this);
        this.handleEndpointAttributeChange = this.handleEndpointAttributeChange.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof EndpointForm
     */
    componentDidMount() {
        this.setState({
            serviceUrl: this.props.selectedEndpointConfig.url,
            timeout: this.props.selectedEndpointConfig.timeout,
        });
    }

    /**
     * Set the endpoint url , timeout values to the state.
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointForm
     */
    handleEndpointConfigTextFieldChange(event) {
        this.props.selectedEndpointConfig[event.target.id] = event.currentTarget.value;
        this.setState({ [event.target.id]: event.currentTarget.value });
    }

    /**
     * Set the aditional endpoint config values to the state.
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointForm
     */
    handleTextFieldChange(event) {
        this.props.endpoint[event.target.id] = event.currentTarget.value;
        this.setState({ [event.target.id]: event.currentTarget.value });
    }

    /**
     * Set addditional attibutes like suspend information.
     *
     * @param {*} event Event that fired the function call
     * @memberof EndpointForm
     */
    handleEndpointAttributeChange(event) {
        let attribute;
        if (this.props.selectedEndpointConfig.attributes) {
            attribute = this.props.selectedEndpointConfig.attributes.find(item => item.name === event.target.id);
        }
        if (attribute) {
            attribute.value = event.currentTarget.value;
        } else {
            attribute = { name: event.target.id, value: event.currentTarget.value };
            this.props.selectedEndpointConfig.attributes.push(attribute);
        }
        this.setState({ [event.target.id]: event.currentTarget.value });
    }

    /**
     *
     *
     * @returns {React.Component} HTML content
     * @memberof EndpointForm
     */
    render() {
        const { classes } = this.props;
        const suspendInitialDuration = this.props.selectedEndpointConfig.attributes
            ? this.props.selectedEndpointConfig.attributes.find(item => item.name === 'suspendInitialDuration')
            : '';
        const suspendMaixumumlDuration = this.props.selectedEndpointConfig.attributes
            ? this.props.selectedEndpointConfig.attributes.find(item => item.name === 'suspendMaximumDuration')
            : '';
        const suspendFactor = this.props.selectedEndpointConfig.attributes
            ? this.props.selectedEndpointConfig.attributes.find(item => item.name === 'suspendFactor')
            : '';

        return (
            <React.Fragment>
                    <Grid item xs={12}>
                        <Paper className={classes.paper}>
                            <form className={classes.container} noValidate autoComplete='off'>
                                <Grid item xs={5} className={classes.grid}>
                                    <TextField
                                        id='url'
                                        label={<FormattedMessage id='service.url' defaultMessage='Service URL' />}
                                        value={this.props.selectedEndpointConfig.url}
                                        fullWidth
                                        margin='normal'
                                        onChange={this.handleEndpointConfigTextFieldChange}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    />
                                </Grid>
                                <Grid item xs={5} className={classes.grid}>
                                    <TextField
                                        id='timeout'
                                        label={<FormattedMessage id='timeout' defaultMessage='Timeout' />}
                                        value={this.props.selectedEndpointConfig.timeout}
                                        fullWidth
                                        margin='normal'
                                        onChange={this.handleEndpointConfigTextFieldChange}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    />
                                </Grid>
                            </form>
                        </Paper>
                        <Paper className={classes.paper}>
                            <Typography variant='subheading' gutterBottom>
                                <FormattedMessage id='endpoint.suspend.state' defaultMessage='Endpoint Suspend State' />
                            </Typography>
                            <form className={classes.container} noValidate autoComplete='off'>
                                <Grid item xs={4} className={classes.suspendGrid}>
                                    <TextField
                                        id='suspendInitialDuration'
                                        label={
                                            <FormattedMessage
                                                id='initial.duration.ms'
                                                defaultMessage='Initial Duration(ms)'
                                            />
                                        }
                                        value={suspendInitialDuration ? suspendInitialDuration.value : ''}
                                        margin='normal'
                                        fullWidth
                                        onChange={this.handleEndpointAttributeChange}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    />
                                </Grid>
                                <Grid item xs={4} className={classes.suspendGrid}>
                                    <TextField
                                        id='suspendMaximumDuration'
                                        label={
                                            <FormattedMessage
                                                id='maximum.duration.ms'
                                                defaultMessage='Maximium Duration(ms)'
                                            />
                                        }
                                        value={suspendMaixumumlDuration ? suspendMaixumumlDuration.value : ''}
                                        margin='normal'
                                        fullWidth
                                        onChange={this.handleEndpointAttributeChange}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    />
                                </Grid>
                                <Grid item xs={3} className={classes.suspendGrid}>
                                    <TextField
                                        id='suspendFactor'
                                        label={<FormattedMessage id='suspend.factor' defaultMessage='Suspend Factor' />}
                                        value={suspendFactor ? suspendFactor.value : ''}
                                        margin='normal'
                                        fullWidth
                                        onChange={this.handleEndpointAttributeChange}
                                        disabled={this.props.readOnly || (!this.props.readOnly && !this.props.isInline)}
                                    />
                                </Grid>
                            </form>
                        </Paper>
                        <Divider />
                    </Grid>
            </React.Fragment>
        );
    }
}

EndpointForm.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointForm);
