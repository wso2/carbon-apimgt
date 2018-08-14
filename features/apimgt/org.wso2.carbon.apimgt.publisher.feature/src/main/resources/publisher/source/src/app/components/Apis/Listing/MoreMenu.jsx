import React from 'react';
import { withStyles } from '@material-ui/core/styles';

const styles = {
    root: {
        display: 'flex',
        zIndex: 1202,
        position: 'absolute',
        marginTop: -20,
        left: 190,
    },
    popperClose: {
        pointerEvents: 'none',
    },
    moreButton: {
        backgroundColor: '#4c4c4c',
        minWidth: 55,
        minHeight: 20,
        color: '#fff',
        padding: 0,
    },
};

/**
 * @deprecated will be replaced by left side fixed nav bar
 * @returns {React.Component} @inheritDoc
 */
const MoreMenu = () => {
    return <div />;
};

export default withStyles(styles)(MoreMenu);
