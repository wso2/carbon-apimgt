import React from 'react';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import Popper from '@material-ui/core/Popper';
import { withStyles } from '@material-ui/core/styles';

const styles = (theme) => ({
    root: {
        display: 'flex',
    },
    paper: {
        marginRight: theme.spacing(2),
    },
    position: {
        display: 'inherit',
        zIndex: theme.zIndex.apiCreateMenu,
    },
});

/**
 * Button with dropdown menu
 *
 * @class MenuButton
 * @extends {React.Component}
 */
class MenuButton extends React.Component {
    /**
     *Creates an instance of MenuButton.
     * @param {Object} props @inheritdoc
     * @memberof MenuButton
     */
    constructor(props) {
        super(props);
        this.state = {
            open: false,
        };
        this.handleToggle = this.handleToggle.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    /**
     *
     *
     * @memberof MenuButton
     */
    handleToggle(event) {
        this.setState((state) => ({ open: !state.open }));
        this.anchorEl = event.currentTarget;
    }

    /**
     *
     *
     * @param {React.SyntheticEvent} event
     * @memberof MenuButton
     */
    handleClose(event) {
        if (this.anchorEl.contains(event.target)) {
            return;
        }
        this.setState({ open: false });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof MenuButton
     */
    render() {
        const {
            classes, children, menuList, buttonProps,
        } = this.props;
        const { open } = this.state;
        return (
            <>
                <Button
                    id='itest-id-createapi'
                    aria-owns={open ? 'menu-list-grow' : null}
                    aria-haspopup='true'
                    onClick={this.handleToggle}
                    {...buttonProps}
                >
                    {children}
                    {' '}
                    ▼
                </Button>
                <Popper
                    open={open}
                    placement='bottom-start'
                    anchorEl={this.anchorEl}
                    transition
                    disablePortal
                    modifiers={{
                        flip: {
                            enabled: true,
                        },
                        preventOverflow: {
                            enabled: true,
                            boundariesElement: 'scrollParent',
                        },
                    }}
                    className={classes.position}
                >
                    {({ TransitionProps, placement }) => (
                        <Grow
                            {...TransitionProps}
                            id='menu-list-grow'
                            style={{ transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom' }}
                        >
                            <Paper>
                                <ClickAwayListener onClickAway={this.handleClose}>{menuList}</ClickAwayListener>
                            </Paper>
                        </Grow>
                    )}
                </Popper>
            </>
        );
    }
}

MenuButton.propTypes = {
    classes: PropTypes.shape({ position: PropTypes.shape({}) }).isRequired,
    menuList: PropTypes.element.isRequired,
    children: PropTypes.shape({}).isRequired,
    buttonProps: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(MenuButton);
