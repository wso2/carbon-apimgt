import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import InfoOutlined from '@material-ui/icons/InfoOutlined';
import Button from '@material-ui/core/Button';
import VerticalDivider from './VerticalDivider';

const styles = theme => ({
  root: {
    display: 'flex',
    height: 100,
    alignItems: 'center',
    paddingLeft: theme.spacing.unit*2,
    borderRadius: theme.shape.borderRadius,
    border: 'solid 1px ' + theme.palette.secondary.main,
  },
  iconItem: {
    paddingRight: theme.spacing.unit*2,
    fontSize: 60,
  },
  button: {
    marginTop: theme.spacing.unit,
    marginBottom: theme.spacing.unit,
  },
});

class Credentials extends React.Component {
  state = {
    value: 0,
  };

  handleExpandClick = () => {
    this.setState(state => ({ expanded: !state.expanded }));
  };
  

  render() {
    const { classes } = this.props;
    const type = this.props.type ? this.props.type : 'info';
    const handleMenuSelect = this.props.handleMenuSelect ? this.props.handleMenuSelect : () => {};
    if( type === "info") {
      return (
        <Paper className={classes.root} elevation={1}>
            <InfoOutlined className={classes.iconItem} />
            <VerticalDivider height={100} />
            <div>
                <Typography variant="headline" component="h3">
                    Generate Credentials
                </Typography>
                <Typography component="p">
                    You need to generate credentials to access this API
                </Typography>
                  <Button variant="contained" color="primary" className={classes.button} onClick={handleMenuSelect}>
                      GENERATE
                  </Button>
            </div>
        </Paper>
      );
    } else if ( type === "warn" ){
      return (
        <Paper className={classes.root} elevation={1}>
            <InfoOutlined className={classes.iconItem} />
            <VerticalDivider height={100} />
            <div>
                <Typography variant="headline" component="h3">
                    Please Copy the Access Token
                </Typography>
                <Typography component="p">
                Please copy this generated token value as it will be displayed only for the current browser session.
( After a page refresh, the token is not visible in the UI )
                </Typography>
            </div>
        </Paper>
      );
    }
    
  }
}

Credentials.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Credentials);
