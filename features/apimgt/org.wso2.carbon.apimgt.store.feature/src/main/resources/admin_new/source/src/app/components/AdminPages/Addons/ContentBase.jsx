import React from 'react';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import { withStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';
import HelpIcon from '@material-ui/icons/Help';
import Tab from '@material-ui/core/Tab';
import Tabs from '@material-ui/core/Tabs';

const styles = (theme) => ({
  paper: {
    maxWidth: 936,
    margin: 'auto',
    overflow: 'hidden',
  },
  searchBar: {
    borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
  },
  searchInput: {
    fontSize: theme.typography.fontSize,
  },
  block: {
    display: 'block',
  },
  addUser: {
    marginRight: theme.spacing(1),
  },
  contentWrapper: {
    margin: '40px 16px',
  },
  secondaryBar: {
    zIndex: 0,
  },
  menuButton: {
    marginLeft: -theme.spacing(1),
  },
  iconButtonAvatar: {
    padding: 4,
  },
  link: {
    textDecoration: 'none',
    color: 'rgba(255, 255, 255, 0.7)',
    '&:hover': {
      color: theme.palette.common.white,
    },
  },
  button: {
    borderColor: 'rgba(255, 255, 255, 0.7)',
  },
  main: {
    flex: 1,
    padding: theme.spacing(6, 4),
    background: '#eaeff1',
},
});

function ContentBase(props) {
  const { classes, title, children, help, pageStyle } = props;

  return (
    <>
      <AppBar
        component="div"
        className={classes.secondaryBar}
        color="primary"
        position="static"
        elevation={0}
      >
        <Toolbar>
          <Grid container alignItems="center" spacing={1}>
            <Grid item xs>
              <Typography color="inherit" variant="h5" component="h1">
                {title}
              </Typography>
            </Grid>
            {/* <Grid item>
              <Button className={classes.button} variant="outlined" color="inherit" size="small">
                Web setup
              </Button>
            </Grid> */}
            <Grid item>
              {help}
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>
      <main className={classes.main}>
        {!pageStyle && (<Paper className={classes.paper}>
            {children}
        </Paper>)}
        {pageStyle && (pageStyle === 'no-paper') && (<>
            {children}
        </>)}
        {pageStyle && (pageStyle === 'full-page') && (<Paper>
            {children}
        </Paper>)}
      </main>
    </>
  );
}

ContentBase.propTypes = {
  classes: PropTypes.object.isRequired,
  help: PropTypes.element.isRequired,
};

export default withStyles(styles)(ContentBase);
