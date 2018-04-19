import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Button from 'material-ui/Button';
import Grow from 'material-ui/transitions/Grow';
import Paper from 'material-ui/Paper';
import { withStyles } from 'material-ui/styles';
import { Manager, Target, Popper } from 'react-popper';
import ClickAwayListener from 'material-ui/utils/ClickAwayListener';
import ArrowDropDownCircle from '@material-ui/icons/ArrowDropDownCircle';
import List, { ListItem, ListItemText } from 'material-ui/List';
import { Link } from 'react-router-dom';

const styles = theme => ({
    root: {
        display: 'flex',
        zIndex: 1203,
    },
    popperClose: {
        pointerEvents: 'none',
    },
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
});

class AddNewMenu extends React.Component {
    state = {
        open: false,
    };

    handleClick = () => {
        this.setState({ open: true });
    };

    handleClose = () => {
        this.setState({ open: false });
    };

    render() {
        const { classes } = this.props;
        const { open } = this.state;

        return (
            <div className={classes.root}>
                <Manager>
                    <Target>
                        <Button
                            aria-owns={open ? 'menu-list' : null}
                            aria-haspopup='true'
                            onClick={this.handleClick}
                            variant='raised'
                            color='secondary'
                        >
                            Create
                            <ArrowDropDownCircle className={classes.rightIcon} />
                        </Button>
                    </Target>
                    <Popper
                        placement='bottom-start'
                        eventsEnabled={open}
                        className={classNames({ [classes.popperClose]: !open })}
                    >
                        <ClickAwayListener onClickAway={this.handleClose}>
                            <Grow in={open} id='menu-list' style={{ transformOrigin: '0 0 0' }}>
                                <Paper>
                                    <List>
                                        <ListItem>
                                            <Link to='/api/create/rest'>
                                                <ListItemText
                                                    primary='Design a New REST API'
                                                    secondary='Design and prototype a new REST API'
                                                />
                                            </Link>
                                        </ListItem>
                                        <ListItem>
                                            <Link to='/api/create/swagger'>
                                                <ListItemText
                                                    primary='I Have an Existing REST API'
                                                    secondary='Use an existing REST endpoint or Swagger definition'
                                                />
                                            </Link>
                                        </ListItem>
                                        <ListItem>
                                            <Link to='/api/create/wsdl'>
                                                <ListItemText
                                                    primary='I Have a SOAP Endpoint'
                                                    secondary='Use an existing SOAP or Import the WSDL'
                                                />
                                            </Link>
                                        </ListItem>
                                        <ListItem>
                                            <Link to='/api/create/rest'>
                                                <ListItemText
                                                    primary='Design New Websocket API'
                                                    secondary='Design and prototype a new WebSocket API'
                                                />
                                            </Link>
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Grow>
                        </ClickAwayListener>
                    </Popper>
                </Manager>
            </div>
        );
    }
}

AddNewMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(AddNewMenu);
