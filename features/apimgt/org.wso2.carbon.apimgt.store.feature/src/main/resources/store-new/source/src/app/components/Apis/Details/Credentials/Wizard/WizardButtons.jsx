import React from 'react';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';

const wizardButtons = (props) => {
    const {
        currentStep, handleRedirectTest, steps, handleNext,
    } = props;
    return (
        <div className={classes.wizardButtons}>
            <Button
                disabled={currentStep < steps.length - 1}
                onClick={handleRedirectTest}
                className={classes.button}
                variant='outlined'
            >
                <FormattedMessage
                    id='Apis.Details.Credentials.Wizard.Wizard.test'
                    defaultMessage='Test'
                />
            </Button>
            <Button
                variant='contained'
                color='primary'
                onClick={() => handleNext('next')}
                className={classes.button}
            >
                {currentStep === steps.length - 1
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
        </div>
    );
};

export default wizardButtons;
