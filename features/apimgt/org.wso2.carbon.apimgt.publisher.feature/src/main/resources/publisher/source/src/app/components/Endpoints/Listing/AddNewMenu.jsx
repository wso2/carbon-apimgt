import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Button from '@material-ui/core/Button';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import { withStyles } from '@material-ui/core/styles';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import ArrowDropDownCircle from '@material-ui/icons/ArrowDropDownCircle';
import List, { ListItem, ListItemText } from '@material-ui/core/List';
import { Link } from 'react-router-dom';
import Popper from '@material-ui/core/Popper';

import { ScopeValidation, resourceMethod, resourcePath } from '../../../data/ScopeValidation';

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
 *
 *
 * @class CreateNewMenu
 * @extends {React.Component}
 */
class CreateNewMenu extends React.Component {
    state = {
        open: false,
    };

    handleClick = () => {
        this.setState({ open: true });
    };

    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     *
     *
     * @returns
     * @memberof CreateNewMenu
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
                    eventsEnabled={open}
                    className={classNames({ [classes.popperClose]: !open })}
                >
                    <ClickAwayListener onClickAway={this.handleClose}>
                        <Grow in={open} id='menu-list' style={{ transformOrigin: '0 0 0' }}>
                            <Paper>
                                <List>
                                    <ListItem>
                                        <Link to='/endpoints/create'>
                                            <ListItemText
                                                primary='Create new Endpoint'
                                                secondary='Create a new global endpoint'
                                            />
                                        </Link>
                                    </ListItem>
                                    <ScopeValidation
                                        resourcePath={resourcePath.SERVICE_DISCOVERY}
                                        resourceMethod={resourceMethod.GET}
                                    >
                                        <ListItem>
                                            <Link to='/endpoints/discover'>
                                                <ListItemText
                                                    primary='Service Discovery'
                                                    secondary='Add Global Endpoints Via Service Discovery'
                                                />
                                            </Link>
                                        </ListItem>
                                    </ScopeValidation>
                                </List>
                            </Paper>
                        </Grow>
                    </ClickAwayListener>
                </Popper>
            </div>
        );
    }
}

CreateNewMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(CreateNewMenu);
