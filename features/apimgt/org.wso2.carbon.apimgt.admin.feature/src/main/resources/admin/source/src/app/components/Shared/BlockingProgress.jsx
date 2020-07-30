import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Progress from 'AppComponents/Shared/Progress';

const useStyles = makeStyles(() => ({
    root: {
        position: 'fixed',
        background: '#000',
        left: 0,
        top: 0,
        width: '100%',
        height: '100%',
        opacity: 0.6,
        zIndex: 2,
        '& .apim-dual-ring span': {
            color: '#fff',
            marginLeft: 0,
            paddingBottom: 10,
        },
    },
}));

/**
 * Renders a blocking loading animation
 * @returns {JSX} The progress react component.
 */
function BlockingProgress({ message }) {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <Progress message={message} />
        </div>
    );
}

BlockingProgress.propTypes = {
    message: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default BlockingProgress;
