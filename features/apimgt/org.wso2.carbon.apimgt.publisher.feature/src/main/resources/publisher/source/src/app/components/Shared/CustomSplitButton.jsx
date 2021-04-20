import React from 'react';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import ButtonGroup from '@material-ui/core/ButtonGroup';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import Popper from '@material-ui/core/Popper';
import CircularProgress from '@material-ui/core/CircularProgress';
import MenuItem from '@material-ui/core/MenuItem';
import MenuList from '@material-ui/core/MenuList';

const options = ['Save and deploy', 'Save'];

/**
 *
 * @returns
 */
export default function CustomSplitButton(props) {
    const [open, setOpen] = React.useState(false);
    const { handleSave, handleSaveAndDeploy, isUpdating } = props;
    const anchorRef = React.useRef(null);
    const [selectedIndex, setSelectedIndex] = React.useState(1);

    const handleClick = (event, index) => {
        setSelectedIndex(index);
        setOpen(false);
        if (`${options[index]}` === 'Save') {
            handleSave();
        } else {
            handleSaveAndDeploy();
        }
    };

    const handleToggle = () => {
        setOpen((prevOpen) => !prevOpen);
    };

    const handleClose = (event) => {
        if (anchorRef.current && anchorRef.current.contains(event.target)) {
            return;
        }

        setOpen(false);
    };

    return (
        <Grid container direction='column' alignItems='center'>
            <Grid item xs={12}>
                <ButtonGroup
                    variant='contained'
                    color='primary'
                    ref={anchorRef}
                    aria-label='split button'
                    disabled={isUpdating}
                    style={{ width: '200px' }}
                >
                    <Button
                        onClick={(event) => handleClick(event, selectedIndex)}
                        disabled={isUpdating}
                        style={{ width: '200px' }}
                    >
                        {options[selectedIndex]}
                        {isUpdating && <CircularProgress size={24} />}
                    </Button>
                    <Button
                        color='primary'
                        size='small'
                        aria-controls={open ? 'split-button-menu' : undefined}
                        aria-expanded={open ? 'true' : undefined}
                        aria-label='select merge strategy'
                        aria-haspopup='menu'
                        onClick={handleToggle}
                    >
                        <ArrowDropDownIcon />
                    </Button>
                </ButtonGroup>
                <Popper open={open} anchorEl={anchorRef.current} role={undefined} transition disablePortal>
                    {({ TransitionProps, placement }) => (
                        <Grow
                            {...TransitionProps}
                            style={{
                                transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom',
                            }}
                        >
                            <Paper>
                                <ClickAwayListener onClickAway={handleClose}>
                                    <MenuList id='split-button-menu'>
                                        {options.map((option, index) => (
                                            <MenuItem
                                                key={option}
                                                selected={index === selectedIndex}
                                                onClick={(event) => handleClick(event, index)}
                                            >
                                                {option}
                                            </MenuItem>
                                        ))}
                                    </MenuList>
                                </ClickAwayListener>
                            </Paper>
                        </Grow>
                    )}
                </Popper>
            </Grid>
        </Grid>
    );
}

CustomSplitButton.propTypes = {
    handleSave: PropTypes.shape({}).isRequired,
    handleSaveAndDeploy: PropTypes.shape({}).isRequired,
};
