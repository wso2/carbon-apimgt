import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { withStyles } from '@material-ui/core/styles';
import { Grid, Paper, Typography } from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withRouter } from 'react-router';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import APIProduct from 'AppData/APIProduct';
import { isRestricted } from 'AppData/AuthManager';

import BusinessPlans from './BusinessPlans';

const styles = (theme) => ({
    root: {
        flexGrow: 1,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    margin: {
        margin: theme.spacing(),
    },
    paper: {
        padding: theme.spacing(2),
        textAlign: 'left',
        color: theme.palette.text.secondary,
        paddingBottom: '10px',
    },
    grid: {
        paddingLeft: '10px',
        paddingRight: '10px',
        paddingBottom: '10px',
        minWidth: '50%',
    },
    button: {
        margin: theme.spacing(),
    },
});

/**
 *
 *
 * @class Monetization
 * @extends {Component}
 */
class Monetization extends Component {
    constructor(props) {
        super(props);
        this.state = {
            monetizationAttributes: [],
            monStatus: null,
            property: {},
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.getMonetizationData();
    }

    getMonetizationData() {
        const { api } = this.props;
        if (api.apiType === 'APIProduct') {
            const apiProduct = new APIProduct(api.name, api.context, api.policies);
            apiProduct.getSettings().then((settings) => {
                if (settings.monetizationAttributes != null) {
                    this.setState({ monetizationAttributes: settings.monetizationAttributes });
                }
            });
            apiProduct.getMonetization(this.props.api.id).then((status) => {
                this.setState({ monStatus: status.enabled });
            });
        } else {
            api.getSettings().then((settings) => {
                if (settings.monetizationAttributes != null) {
                    this.setState({ monetizationAttributes: settings.monetizationAttributes });
                }
            });
            api.getMonetization(this.props.api.id).then((status) => {
                this.setState({ monStatus: status.enabled });
            });
        }
    }

    handleChange = (event) => {
        this.setState({ monStatus: event.target.checked });
    };

    handleInputChange = (event) => {
        const { name, value } = event.target;
        this.setState((oldState) => {
            const { property } = oldState;
            property[name] = value;
            return { property };
        });
    };

    /**
     * Handles the submit action for configuring monetization
     */
    handleSubmit() {
        const { api, intl } = this.props;
        if (api.apiType === 'APIProduct') {
            const properties = this.state.property;
            const enabled = this.state.monStatus;
            const body = {
                enabled,
                properties,
            };
            const apiProduct = new APIProduct(api.name, api.context, api.policies);
            const promisedMonetization = apiProduct.configureMonetizationToApiProduct(api.id, body);
            promisedMonetization.then((response) => {
                const status = JSON.parse(response.data);
                if (status.enabled) {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.monetization.configured.successfully',
                        defaultMessage: 'Monetization Enabled Successfully',
                    }));
                } else {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.monetization.disabled.successfully',
                        defaultMessage: 'Monetization Disabled Successfully',
                    }));
                }
                this.setState((cState) => ({ monStatus: !cState.monStatus }));
            }).catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.something.went.wrong.while.configuring.monetization',
                        defaultMessage: 'Something went wrong while configuring monetization',
                    }));
                }
            });
        } else {
            const properties = this.state.property;
            const enabled = this.state.monStatus;
            const body = {
                enabled,
                properties,
            };
            const promisedMonetizationConf = api.configureMonetizationToApi(this.props.api.id, body);
            promisedMonetizationConf.then((response) => {
                const status = JSON.parse(response.data);
                if (status.enabled) {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.monetization.configured.successfully',
                        defaultMessage: 'Monetization Enabled Successfully',
                    }));
                } else {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.monetization.disabled.successfully',
                        defaultMessage: 'Monetization Disabled Successfully',
                    }));
                }
                this.setState((cState) => ({ monStatus: !cState.monStatus }));
            }).catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Monetization.Index.something.went.wrong.while.configuring.monetization',
                        defaultMessage: 'Something went wrong while configuring monetization',
                    }));
                }
            });
        }
    }

    render() {
        const { api, classes } = this.props;
        const { monetizationAttributes, monStatus } = this.state;
        if (api && isRestricted(['apim:api_publish'], api)) {
            return (
                <Grid
                    container
                    direction='row'
                    alignItems='center'
                    spacing={4}
                    style={{ marginTop: 20 }}
                >
                    <Grid item>
                        <Typography variant='body2' color='primary'>
                            <FormattedMessage
                                id='Apis.Details.Monetization.Index.update.not.allowed'
                                defaultMessage={'* You are not authorized to update API monetization'
                                    + ' due to insufficient permissions'}
                            />
                        </Typography>
                    </Grid>
                </Grid>
            );
        }
        if (!monetizationAttributes || monStatus === null) {
            return <Progress />;
        }
        return (
            <Grid item xs={6}>
                <Typography variant='h4' gutterBottom>
                    <FormattedMessage id='Apis.Details.Monetization.Index.monetization' defaultMessage='Monetization' />
                </Typography>
                <form method='post' onSubmit={this.handleSubmit}>
                    <FormControlLabel
                        control={(
                            <Checkbox
                                disabled={isRestricted(['apim:api_publish'], api)}
                                id='monStatus'
                                name='monStatus'
                                checked={monStatus}
                                onChange={this.handleChange}
                                value={monStatus}
                                color='primary'
                            />
                        )}
                        label='Enable Monetization'
                    />
                    <Grid>
                        <Paper className={classes.paper}>
                            <Grid item xs={5} className={classes.grid}>
                                <Typography variant='subtitle' gutterBottom>
                                    <FormattedMessage
                                        id='Apis.Details.Monetization.Index.monetization.properties'
                                        defaultMessage='Monetization Properties'
                                    />
                                </Typography>
                                {
                                    (monetizationAttributes.length > 0)
                                        ? (monetizationAttributes.map((monetizationAttribute, i) => (
                                            <TextField
                                                disabled={isRestricted(['apim:api_publish'], api)}
                                                fullWidth
                                                id={'attribute' + i}
                                                label={monetizationAttribute.displayName}
                                                name={monetizationAttribute.name}
                                                type='text'
                                                margin='normal'
                                                required={monetizationAttribute.required}
                                                onChange={this.handleInputChange}
                                                autoFocus
                                            />
                                        )))
                                        : (
                                            <Typography gutterBottom>
                                                <FormattedMessage
                                                    id={'Apis.Details.Monetization.Index.there.are.no'
                                                        + ' .monetization.properties.configured'}
                                                    defaultMessage='There are no monetization properties configured'
                                                />
                                            </Typography>
                                        )
                                }
                            </Grid>
                        </Paper>
                    </Grid>
                    <Grid>
                        <Paper className={classes.paper}>
                            <Grid item xs={12} className={classes.grid}>
                                <BusinessPlans api={api} monStatus={monStatus} />
                            </Grid>
                        </Paper>
                    </Grid>
                    <Button onClick={this.handleSubmit} color='primary' variant='contained' className={classes.button}>
                        <FormattedMessage
                            id='Apis.Details.Monetization.Index.save'
                            defaultMessage='Save'
                        />
                    </Button>
                </form>
            </Grid>
        );
    }
}

Monetization.propTypes = {
    api: PropTypes.instanceOf(API).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withRouter(withStyles(styles)(Monetization)));
