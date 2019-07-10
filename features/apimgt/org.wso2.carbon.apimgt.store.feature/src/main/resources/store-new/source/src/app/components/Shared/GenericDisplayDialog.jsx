import React from 'react';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';

const genericDisplayDialog = (props) => {
    const {
        classes, handleClick, heading, caption, buttonText,
    } = props;
    return (
        <div className={classes.appContent}>
            <InlineMessage type='info' className={classes.dialogContainer}>
                <Typography variant='headline' component='h3'>
                    {heading}
                </Typography>
                <Typography component='p'>
                    {caption}
                </Typography>
                <ScopeValidation resourcePath={resourcePaths.APPLICATIONS} resourceMethod={resourceMethods.POST}>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.button}
                        onClick={handleClick}
                    >
                        {buttonText}
                    </Button>
                </ScopeValidation>
            </InlineMessage>
        </div>
    );
};

export default genericDisplayDialog;
