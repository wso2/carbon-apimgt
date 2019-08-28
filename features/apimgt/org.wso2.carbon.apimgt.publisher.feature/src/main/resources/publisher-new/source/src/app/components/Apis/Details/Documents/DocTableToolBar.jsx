import React from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import CircularProgress from '@material-ui/core/CircularProgress';
import DeleteIcon from '@material-ui/icons/Delete';
import FilterListIcon from '@material-ui/icons/FilterList';
import { lighten } from '@material-ui/core/styles/colorManipulator';

const toolbarStyles = theme => ({
    root: {
        paddingRight: theme.spacing.unit,
    },
    highlight:
        theme.palette.type === 'light'
            ? {
                color: theme.palette.secondary.main,
                backgroundColor: lighten(theme.palette.secondary.light, 0.85),
            }
            : {
                color: theme.palette.text.primary,
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
        numSelected, classes, handleDeleteDocs, loading, totalDocCount,
    } = props;

    return (
        <Toolbar
            className={classNames(classes.root, {
                [classes.highlight]: numSelected > 0,
            })}
        >
            <div className={classes.title}>
                {numSelected > 0 ? (
                    <Typography color='inherit' variant='subheading'>
                        {numSelected} selected
                    </Typography>
                ) : (
                        <Typography variant='title' id='tableTitle'>
                            <FormattedMessage
                                id='docs'
                                defaultMessage='DOCS'
                            />
                            ({totalDocCount})
                        </Typography>
                    )}
            </div>
            <div className={classes.spacer} />
            <div className={classes.actions}>
                {numSelected > 0 ? (
                    <Tooltip title='Delete'>
                        <IconButton disabled={loading} onClick={handleDeleteDocs} aria-label='Delete'>
                            <DeleteIcon />
                            {loading && <CircularProgress className={classes.deleteProgress} />}
                        </IconButton>
                    </Tooltip>
                ) : (
                        <Tooltip title='Filter list'>
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
    totalDocCount: PropTypes.number.isRequired,
    handleDeleteDocs: PropTypes.func.isRequired,
};

export default withStyles(toolbarStyles)(EnhancedTableToolbar);
