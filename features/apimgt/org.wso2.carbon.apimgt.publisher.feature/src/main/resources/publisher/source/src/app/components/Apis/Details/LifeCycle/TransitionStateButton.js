import React from 'react';
import Button from '@material-ui/core/Button';

const TransitionStateButton = (props) => {
    return (
        <Button raised value={props.state.targetState}>{props.state.event}</Button>
    );
};

export default TransitionStateButton;
