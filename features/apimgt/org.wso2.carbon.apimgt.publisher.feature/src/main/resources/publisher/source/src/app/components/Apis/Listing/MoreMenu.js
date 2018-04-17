import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Button from 'material-ui/Button';
import Grow from 'material-ui/transitions/Grow';
import Paper from 'material-ui/Paper';
import { withStyles } from 'material-ui/styles';
import { Manager, Target, Popper } from 'react-popper';
import ClickAwayListener from 'material-ui/utils/ClickAwayListener';
import MoreHoriz from '@material-ui/icons/MoreHoriz';
import List, {ListItem, ListItemText} from 'material-ui/List';
import {Link} from 'react-router-dom'



const styles = {
    root: {
        display: 'flex',
        zIndex: 1202,
        position: 'absolute',
        marginTop: -20,
        left: 190,

    },
    popperClose: {
        pointerEvents: 'none',
    },
    moreButton: {
        backgroundColor: '#4c4c4c',
        minWidth: 55,
        minHeight: 20,
        color: '#fff',
        padding: 0,
    }
};


class MoreMenu extends React.Component {
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
        const tabs = [
            "overview",
            "lifecycle",
            "endpoints",
            "resources",
            "scopes",
            "documents",
            "permission",
            "mediation",
            "scripting",
            "subscriptions",
            "security"
        ];
        return (
            <div className={classes.root}>
                <Manager>
                    <Target>
                        <Button
                            aria-owns={open ? 'menu-list' : null}
                            aria-haspopup="true"
                            onClick={this.handleClick}
                            className={classes.moreButton}
                            variant="raised"
                        >
                           <MoreHoriz />
                        </Button>
                    </Target>
                    <Popper
                        placement="bottom-start"
                        eventsEnabled={open}
                        className={classNames({ [classes.popperClose]: !open })}
                    >
                        <ClickAwayListener onClickAway={this.handleClose}>
                            <Grow in={open} id="menu-list" style={{ transformOrigin: '0 0 0' }}>
                                <Paper>
                                    <List>
                                        {tabs.map(tab =>
                                            (<ListItem key={tab}>
                                                <Link name={tab} to={"/apis/" + this.props.api_uuid + "/" + tab}>
                                                    <ListItemText primary={tab}/></Link>
                                            </ListItem>)
                                        )}
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

MoreMenu.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(MoreMenu);