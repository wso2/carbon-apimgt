import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { Link } from 'react-router-dom';
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
        if (api.apiType === API.CONSTS.APIProduct) {
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
        if (api.apiType === API.CONSTS.APIProduct) {
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
            <form method='post' onSubmit={this.handleSubmit}>
                <Grid container xs={6} spacing={2}>
                    <Grid item xs={12}>
                        <Typography id='itest-api-details-api-monetization-head' variant='h4'>
                            <FormattedMessage
                                id='Apis.Details.Monetization.Index.monetization'
                                defaultMessage='Monetization'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12}>
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
                    </Grid>
                    <Grid item xs={12}>
                        <Paper className={classes.root}>
                            <Grid item xs={12} className={classes.grid}>
                                <Typography className={classes.heading} variant='h6'>
                                    <FormattedMessage
                                        id='Apis.Details.Monetization.Index.monetization.properties'
                                        defaultMessage='Monetization Properties'
                                    />
                                </Typography>
                                {
                                    (monetizationAttributes.length > 0) ? (
                                        (monetizationAttributes.map((monetizationAttribute, i) => (
                                            <TextField
                                                disabled={!monStatus || isRestricted(['apim:api_publish'], api)}
                                                fullWidth
                                                id={'attribute' + i}
                                                label={monetizationAttribute.displayName}
                                                placeholder={monetizationAttribute.displayName}
                                                name={monetizationAttribute.name}
                                                type='text'
                                                margin='normal'
                                                variant='outlined'
                                                required={monetizationAttribute.required}
                                                onChange={this.handleInputChange}
                                                autoFocus
                                            />
                                        )))
                                    ) : (
                                        <Typography>
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
                    <Grid item xs={12}>
                        <Paper className={classes.root}>
                            <Grid item xs={12} className={classes.grid}>
                                <BusinessPlans api={api} monStatus={monStatus} />
                            </Grid>
                        </Paper>
                    </Grid>
                    <Grid item xs={12}>
                        <Button
                            onClick={this.handleSubmit}
                            color='primary'
                            variant='contained'
                            className={classes.button}
                            disabled={api.isRevision}
                        >
                            <FormattedMessage
                                id='Apis.Details.Monetization.Index.save'
                                defaultMessage='Save'
                            />
                        </Button>
                        <Link to={'/apis/' + api.id + '/overview'}>
                            <Button>
                                <FormattedMessage
                                    id='Apis.Details.Monetization.Index.cancel'
                                    defaultMessage='Cancel'
                                />
                            </Button>
                        </Link>
                    </Grid>
                </Grid>
            </form>
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
