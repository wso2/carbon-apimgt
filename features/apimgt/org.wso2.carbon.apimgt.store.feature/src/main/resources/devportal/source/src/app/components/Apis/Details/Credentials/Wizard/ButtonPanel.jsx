import React, { useContext } from 'react';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import { Box } from '@material-ui/core';
import { useHistory } from 'react-router-dom';
import PropTypes from 'prop-types';

const styles = {
    button: {
        mt: 2,
        mr: 1,
    },
    wizardButtons: {
        pl: 2,
    },
};

const ButtonPanel = (props) => {
    const {
        currentStep, handleCurrentStep, handleReset, nextActive,
    } = props;
    const stepsLength = 5;
    const { api, updateSubscriptionData } = useContext(ApiContext);
    const history = useHistory();

    /**
     * Redirect  to the API console page
     */
    const handleTest = () => {
        history.push(`/apis/${api.id}/test`);
    };

    /**
     * Redirect back to credentials page
     */
    const handleCancel = () => {
        updateSubscriptionData(history.push('credentials'));
    };

    return (
        <Box display='flex' {...styles.wizardButtons}>
            {currentStep < stepsLength - 1 && (
                <Box {...styles.button}>
                    <Button
                        onClick={handleCancel}
                        variant='text'
                    >
                        <FormattedMessage
                            id='Apis.Details.Credentials.Wizard.Wizard.Cancel'
                            defaultMessage='CANCEL'
                        />
                    </Button>
                </Box>
            )}
            {currentStep >= stepsLength - 1 && (
                <Box {...styles.button}>
                    <Button
                        onClick={handleTest}
                        {...styles.button}
                        variant='outlined'
                    >
                        <FormattedMessage
                            id='Apis.Details.Credentials.Wizard.Wizard.test'
                            defaultMessage='Test'
                        />
                    </Button>
                </Box>
            )}
            {currentStep >= stepsLength - 1 && (
                <Box {...styles.button}>
                    <Button
                        variant='outlined'
                        onClick={handleReset}
                    >
                        <FormattedMessage
                            id='Apis.Details.Credentials.Wizard.Wizard.rest'
                            defaultMessage='Reset'
                        />
                    </Button>
                </Box>
            )}
            <Box {...styles.button}>
                <Button
                    variant='contained'
                    color='primary'
                    onClick={handleCurrentStep}
                    disabled={!nextActive}
                >
                    {currentStep === stepsLength - 1
                        ? (
                            <FormattedMessage
                                id='Apis.Details.Credentials.Wizard.Wizard.finish'
                                defaultMessage='Finish'
                            />
                        )
                        : (
                            <FormattedMessage
                                id='Apis.Details.Credentials.Wizard.Wizard.next'
                                defaultMessage='Next'
                            />
                        )}
                </Button>
            </Box>
        </Box>
    );
};

ButtonPanel.defaultProps = {
    nextActive: true,
};

ButtonPanel.propTypes = {
    currentStep: PropTypes.func.isRequired,
    handleCurrentStep: PropTypes.func.isRequired,
    handleReset: PropTypes.func.isRequired,
    nextActive: PropTypes.bool,
};

export default ButtonPanel;
