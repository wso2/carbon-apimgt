import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  divider: {
    borderRight: 'solid 1px #ccc',
  },
});

function VerticalDivider(props) {
  const { classes } = props;
  const height = props.height ? props.height : 30;
  const marginLeft = props.marginLeft ? props.marginLeft : 10;
  const marginRight = props.marginRight ? props.marginRight : 10;

  return (
    <React.Fragment>
        <div className={classes.divider} style={{height, marginLeft, marginRight}}></div>
    </React.Fragment>
  );
}

VerticalDivider.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(VerticalDivider);
