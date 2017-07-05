import React from 'react'
import {Radio} from 'antd'
const Button = Radio.Button;

const TransitionStateButton = (props) => {
    return (
        <Button value={props.state.targetState}>{props.state.event}</Button>
    );
};

export default TransitionStateButton