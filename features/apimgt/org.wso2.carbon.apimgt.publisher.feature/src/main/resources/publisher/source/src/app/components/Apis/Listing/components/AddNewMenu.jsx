import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Button from '@material-ui/core/Button';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import { withStyles } from '@material-ui/core/styles';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import ArrowDropDownCircle from '@material-ui/icons/ArrowDropDownCircle';
import { List, ListItem, ListItemText } from '@material-ui/core';
import { Link } from 'react-router-dom';
import Popper from '@material-ui/core/Popper';

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

/**
 * Create APIs menu in API Listing
 * @class AddNewMenu
 * @extends {React.Component}
 */
class AddNewMenu extends React.Component {
    /**
     *Creates an instance of AddNewMenu.
     * @param {*} props @inheritDoc
     * @memberof AddNewMenu
     */
    constructor(props) {
        super(props);
        this.state = {
            open: false,
        };
        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    /**
     *
     *
     * @memberof AddNewMenu
     */
    handleClick() {
        this.setState({ open: true });
    }

    /**
     *
     *
     * @memberof AddNewMenu
     */
    handleClose() {
        this.setState({ open: false });
    }

    /**
     *
     *
     * @returns {React.Component} @inheritDoc
     * @memberof AddNewMenu
     */
    render() {
        const { classes } = this.props;
        const { open } = this.state;

        return (
            <div className={classes.root}>
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
                <Popper
                    placement='bottom-start'
                    open={open}
                    className={classNames({ [classes.popperClose]: !open })}
                >
                    <ClickAwayListener onClickAway={this.handleClose}>
                        <Grow in={open} id='menu-list'>
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
            </div>
        );
    }
}

AddNewMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(AddNewMenu);
