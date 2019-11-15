import React from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import CircularProgress from '@material-ui/core/CircularProgress';
import DeleteIcon from '@material-ui/icons/Delete';
import FilterListIcon from '@material-ui/icons/FilterList';
import { lighten } from '@material-ui/core/styles/colorManipulator';
import { FormattedMessage } from 'react-intl';

const toolbarStyles = theme => ({
    root: {
        paddingRight: theme.spacing(1),
    },
    highlight:
        theme.palette.type === 'light'
            ? {
                color: theme.palette.getContrastText(lighten(theme.palette.secondary.light, 0.85)),
                backgroundColor: lighten(theme.palette.secondary.light, 0.85),
            }
            : {
                color: theme.palette.getContrastText(theme.palette.secondary.dark),
                backgroundColor: theme.palette.secondary.dark,
            },
    spacer: {
        flex: '1 1 100%',
    },
    actions: {
        color: theme.palette.text.secondary,
    },
    title: {
        flex: '0 0 auto',
    },
    deleteProgress: {
        color: 'green',
        position: 'absolute',
    },
});

const EnhancedTableToolbar = (props) => {
    const {
        numSelected, classes, handleDeleteAPIs, loading, totalAPIsCount,
    } = props;

    return (
        <Toolbar
            className={classNames(classes.root, {
                [classes.highlight]: numSelected > 0,
            })}
        >
            <div className={classes.title}>
                {numSelected > 0 ? (
                    <Typography color='inherit' variant='subtitle1'>
                        {numSelected}
                        {' '}
                        <FormattedMessage
                            id='Apis.Listing.TableView.APITableToolBar.selected.number'
                            defaultMessage='selected'
                        />
                    </Typography>
                ) : (
                    <Typography variant='h6' id='tableTitle'>
                        <FormattedMessage
                            id='Apis.Listing.TableView.APITableToolBar.apis.title'
                            defaultMessage='APIS'
                        /> {' '} ({totalAPIsCount})
                    </Typography>
                )}
            </div>
            <div className={classes.spacer} />
            <div className={classes.actions}>
                {numSelected > 0 ? (
                    <Tooltip title={
                        <FormattedMessage
                            id='Apis.Listing.TableView.APITableToolBar.delete'
                            defaultMessage='Delete'
                        />}
                    >
                        <IconButton disabled={loading} onClick={handleDeleteAPIs} aria-label='Delete'>
                            <DeleteIcon />
                            {loading && <CircularProgress className={classes.deleteProgress} />}
                        </IconButton>
                    </Tooltip>
                ) : (
                    <Tooltip title={
                        <FormattedMessage
                            id='Apis.Listing.TableView.APITableToolBar.filter.list'
                            defaultMessage='Filter List'
                        />}
                    >
                        <IconButton aria-label='Filter list'>
                            <FilterListIcon />
                        </IconButton>
                    </Tooltip>
                )}
            </div>
        </Toolbar>
    );
};

EnhancedTableToolbar.defaultProps = {
    loading: false,
};
EnhancedTableToolbar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    numSelected: PropTypes.number.isRequired,
    loading: PropTypes.bool,
    totalAPIsCount: PropTypes.number.isRequired,
    handleDeleteAPIs: PropTypes.func.isRequired,
};

export default withStyles(toolbarStyles)(EnhancedTableToolbar);
