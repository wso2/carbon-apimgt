import React, { useState } from 'react';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import { updateUserLocalStorage, getUserLocalStorage } from 'AppData/UserStateUtils';

const WrappedExpansionPanel = (props) => {
    const { id } = props;
    const [expanded, setExpanded] = useState(getUserLocalStorage(`${id}-expanded`));
    const setExpandState = (event, expandedState) => {
        updateUserLocalStorage(`${id}-expanded`, expandedState);
        setExpanded(expandedState);
    };
    return (<ExpansionPanel expanded={expanded} {...props} onChange={setExpandState} />);
};
WrappedExpansionPanel.muiName = 'ExpansionPanel';

export default WrappedExpansionPanel;
