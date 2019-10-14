import React, { useContext } from 'react';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import { Box } from '@material-ui/core';
import { useHistory } from 'react-router-dom';

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
    const { currentStep, handleCurrentStep, handleReset } = props;
    const stepsLength = 5;
    const { api } = useContext(ApiContext);
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
        history.push(`/apis/${api.id}/credentials`);
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

export default ButtonPanel;
