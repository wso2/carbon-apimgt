import React from 'react';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';

const ButtonPanel = (props) => {
    const {
        classes, currentStep, handleCurrentStep, handleRedirectTest, handleReset,
    } = props;
    const stepsLength = 5;
    return (
        <div className={classes.wizardButtons}>
            <Button
                disabled={currentStep < stepsLength - 1}
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
                variant='outlined'
                disabled={currentStep !== stepsLength - 1}
                onClick={handleReset}
                className={classes.button}
            >
                <FormattedMessage
                    id='Apis.Details.Credentials.Wizard.Wizard.rest'
                    defaultMessage='Reset'
                />
            </Button>
            <Button
                variant='contained'
                color='primary'
                onClick={handleCurrentStep}
                className={classes.button}
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
        </div>
    );
};

export default ButtonPanel;
