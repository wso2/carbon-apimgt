import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridIcon from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';

import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
    },
    titleBar: {
        padding: '8px',
        display: 'flex',
        justifyContent: 'space-between',
    },
    buttonLeft: {
        alignSelf: 'flex-start',
    },
    buttonRight: {
        alignSelf: 'flex-end',
    },
    title: {
        display: 'inline-block',
        padding: '8px',
    },
});

const TopMenu = ({ classes, isCardView, toggleView }) => {
    return (
        <div className={classes.titleBar}>
            <div className={classes.buttonLeft}>
                <div className={classes.title}>
                    <Typography variant='display1' gutterBottom>
                        APIs
                    </Typography>
                </div>
                <APICreateMenu buttonProps={{ size: 'medium', color: 'secondary', variant: 'contained' }}>
                    Create API
                </APICreateMenu>
            </div>
            <div className={classes.buttonRight}>
                <IconButton className={classes.button} disabled={!isCardView} aria-label='List' onClick={toggleView}>
                    <List />
                </IconButton>
                <IconButton className={classes.button} disabled={isCardView} aria-label='Grid' onClick={toggleView}>
                    <GridIcon />
                </IconButton>
            </div>
        </div>
    );
};

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    toggleView: PropTypes.func.isRequired,
    isCardView: PropTypes.bool.isRequired,
};

export default withStyles(styles)(TopMenu);
