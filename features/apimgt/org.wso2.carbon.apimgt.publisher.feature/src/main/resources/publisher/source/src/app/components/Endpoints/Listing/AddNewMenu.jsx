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
                </Manager>
            </div>
        );
    }
}

AddNewMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(AddNewMenu);
