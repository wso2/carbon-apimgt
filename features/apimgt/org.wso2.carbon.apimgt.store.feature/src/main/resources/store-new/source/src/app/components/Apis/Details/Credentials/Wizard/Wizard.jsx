import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Grid from '@material-ui/core/Grid';
import AppBar from '@material-ui/core/AppBar';
import { Typography } from '@material-ui/core';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import CreateAppStep from './CreateAppStep';

const styles = theme => ({
    appBar: {
        background: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    toolbar: {
        marginLeft: theme.spacing.unit * 2,
    },
    subscribeTitle: {
        flex: 1,
    },
    plainContent: {
        paddingTop: 80,
        paddingLeft: theme.spacing.unit * 2,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    group: {
        display: 'flex',
        flexDirection: 'row',
    },
    instructions: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    root: {
        paddingLeft: theme.spacing.unit,
    },
    wizardContent: {
        paddingLeft: theme.spacing.unit,
    },
    wizardButtons: {
        paddingLeft: theme.spacing.unit * 2,
    },
});

/**
 * Class used for wizard
 */
class Wizard extends Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.steps = ['Create application', 'Subscribe to new application',
            'Generate Keys', 'Generate Access Token', 'Copy Access Token'];
        this.state = {
            currentStep: 0,
        };
    }

    handleNext = () => {
        this.setState(({ currentStep }) => {
            return { currentStep: currentStep + 1 };
        });
    }

    handleBack = () => {
        this.setState(({ currentStep }) => {
            return { currentStep: currentStep - 1 };
        });
    }

    handleReset = () => {
        this.setState({
            currentStep: 0,
        });
    };

    /**
     * @inheritdoc
     */
    render() {
        const {
            classes, updateSubscriptionData, apiId, handleClickToggle,
        } = this.props;
        const { currentStep } = this.state;
        return (
            <React.Fragment>
                <AppBar className={classes.appBar}>
                    <Grid container spacing={0}>
                        <Grid item xs={6}>
                            <Toolbar className={classes.toolbar}>
                                <IconButton
                                    color='inherit'
                                    onClick={() => handleClickToggle('openNew', updateSubscriptionData)}
                                    aria-label='Close'
                                >
                                    <CloseIcon />
                                </IconButton>
                                <div className={classes.subscribeTitle}>
                                    <Typography variant='h6'>
                                        Subscribe to new Application
                                    </Typography>
                                </div>
                            </Toolbar>
                        </Grid>
                    </Grid>
                </AppBar>
                <div className={classes.plainContent}>
                    <div className={classes.root}>
                        <Stepper activeStep={currentStep}>
                            {this.steps.map((label) => {
                                return (
                                    <Step key={label}>
                                        <StepLabel>{label}</StepLabel>
                                    </Step>
                                );
                            })}
                        </Stepper>
                    </div>
                    {/* <Wizard
                        apiId={apiId}
                        onClickFunction={(a, b) => this.handleClickToggle(a, b)}
                        updateSubscriptionData={updateSubscriptionData}
                    /> */}
                    <div>
                        {currentStep === this.steps.length ? (
                            <div>
                                <Typography className={classes.instructions}>
                                    All steps completed - you&quot;re finished
                                </Typography>
                                <Button
                                    onClick={this.handleReset}
                                    className={classes.button}
                                >
                                            Reset
                                </Button>
                            </div>
                        ) : (
                            <div className={classes.wizardContent}>
                                <CreateAppStep currentStep={currentStep} />
                                <div className={classes.wizardButtons}>
                                    <Button
                                        disabled={currentStep === 0}
                                        onClick={this.handleBack}
                                        className={classes.button}
                                        variant='outlined'
                                    >
                                                    Back
                                    </Button>
                                    <Button
                                        disabled={currentStep < this.steps.length - 1}
                                        onClick={this.handleRedirectTest}
                                        className={classes.button}
                                        variant='outlined'
                                    >
                                                    Test
                                    </Button>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        onClick={this.handleNext}
                                        className={classes.button}
                                    >
                                        {currentStep === this.steps.length - 1 ? 'Finish' : 'Next'}
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </React.Fragment>
        );
    }
}

Wizard.propTypes = {
    // classes: PropTypes.shpae({
    //     appBar: PropTypes.shape({}),
    //     toolbar: PropTypes.shape({}),
    //     subscribeTitle: PropTypes.shape({}),
    //     plainContent: PropTypes.shape({}),
    // }).isRequired,
    updateSubscriptionData: PropTypes.func.isRequired,
    handleClickToggle: PropTypes.func.isRequired,
    apiId: PropTypes.string.isRequired,
};

export default withStyles(styles)(Wizard);
