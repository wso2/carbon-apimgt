import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = {
    divider: {
        borderRight: 'solid 1px #ccc',
    },
};

/**
 *
 *
 * @param {*} props
 * @returns
 */
function VerticalDivider(props) {
    const {
        classes, height = 30, marginLeft = 10, marginRight = 10,
    } = props;

    return (
        <>
            <div className={classes.divider} style={{ height, marginLeft, marginRight }} />
        </>
    );
}

VerticalDivider.propTypes = {
    classes: PropTypes.shape({
        divider: PropTypes.string,
    }).isRequired,
    height: PropTypes.shape({}).isRequired,
    marginLeft: PropTypes.shape({}).isRequired,
    marginRight: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(VerticalDivider);
