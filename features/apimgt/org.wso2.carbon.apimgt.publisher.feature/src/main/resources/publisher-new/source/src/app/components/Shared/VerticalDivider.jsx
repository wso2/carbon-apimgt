import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = {
    divider: {
        borderRight: 'solid 1px #ccc',
    },
};

function VerticalDivider(props) {
    const { classes } = props;
    const height = props.height ? props.height : 30;
    const marginLeft = props.marginLeft ? props.marginLeft : 10;
    const marginRight = props.marginRight ? props.marginRight : 10;

    return (
        <React.Fragment>
            <div className={classes.divider} style={{ height, marginLeft, marginRight }} />
        </React.Fragment>
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
