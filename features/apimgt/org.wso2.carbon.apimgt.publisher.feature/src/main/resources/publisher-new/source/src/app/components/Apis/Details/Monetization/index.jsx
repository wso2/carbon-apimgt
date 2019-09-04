import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { withStyles } from '@material-ui/core/styles';
import { Grid, Paper, Typography, Divider } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import BusinessPlans from './BusinessPlans';
import AuthManager from '../../../../data/AuthManager';

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
        margin: theme.spacing.unit,
    },
});

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
        this.isNotCreator = AuthManager.isNotCreator();
        this.isNotPublisher = AuthManager.isNotPublisher();
    }

    componentDidMount() {
        this.getMonetizationData();
    }

    getMonetizationData() {
        const { api } = this.props;
        api.getSettings().then((settings) => {
            if (settings.monetizationAttributes != null) {
                this.setState({ monetizationAttributes: settings.monetizationAttributes });
            }
        });
        api.getMonetization(this.props.api.id).then((status) => {
            this.setState({ monStatus: status.enabled });
        });
    }

    /**
     * Handles the submit action for configuring monetization
     */
    handleSubmit() {
        const { api, intl } = this.props;
        const properties = this.state.property;
        const enabled = this.state.monStatus;
        const body = {
            enabled,
            properties,
        };
        const promisedMonetizationConf = api.configureMonetizationToApi(this.props.api.id, body);
        promisedMonetizationConf.then((response) => {
            if (response.status !== 200) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Monetization.Index.something.went.wrong.while.configuring.monetization',
                    defaultMessage: 'Something went wrong while configuring monetization',
                }));
                return;
            }
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.Monetization.Index.monetization.configured.successfully',
                defaultMessage: 'Monetization Configured Successfully',
            }));
            this.setState({ monStatus: !this.state.monStatus });
        }).catch((error) => {
            console.error(error);
            if (error.response) {
                Alert.error(error.response.body.message);
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Monetization.Index.something.went.wrong.while.configuring.monetization',
                    defaultMessage: 'Something went wrong while configuring monetization',
                }));
            }
        });
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

    render() {
        const { api, classes } = this.props;
        const { monetizationAttributes, monStatus } = this.state;
        if (!monetizationAttributes || monStatus === null) {
            return <Progress />;
        }
        return (
            <Grid item xs={6}>
                <Typography variant='title' gutterBottom>
                    <FormattedMessage id='Apis.Details.Monetization.Index.monetization' defaultMessage='Monetization' />
                </Typography>
                <form method='post' onSubmit={this.handleSubmit}>
                    <FormControlLabel
                        control={
                            <Checkbox
                                disabled={this.isNotCreator && this.isNotPublisher}
                                id='monStatus'
                                name='monStatus'
                                checked={monStatus}
                                onChange={this.handleChange}
                                value={monStatus}
                            />
                        }
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
                                    (monetizationAttributes.length > 0) ?
                                        (monetizationAttributes.map((monetizationAttribute, i) => (
                                            <TextField
                                                disabled={this.isNotCreator && this.isNotPublisher}
                                                fullWidth
                                                id={'attribute' + i}
                                                label={monetizationAttribute.name}
                                                name={monetizationAttribute.displayName}
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
                                                    id='Apis.Details.Monetization.Index.there.are.no
                                                        .monetization.properties.configured'
                                                    defaultMessage='There are no monetization properties configured'
                                                />
                                            </Typography>
                                        )
                                }
                            </Grid>
                        </Paper>
                    </Grid>
                    <Divider className={classes.grid} />
                    <Grid>
                        <Paper className={classes.paper}>
                            <Grid item xs={12} className={classes.grid}>
                                {<BusinessPlans api={api} monStatus={monStatus} />}
                            </Grid>
                        </Paper>
                    </Grid>
                    <Divider className={classes.grid} />
                    <Button onClick={this.handleSubmit} color='primary' variant='contained' className={classes.button} >
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

export default withStyles(styles)(Monetization);
