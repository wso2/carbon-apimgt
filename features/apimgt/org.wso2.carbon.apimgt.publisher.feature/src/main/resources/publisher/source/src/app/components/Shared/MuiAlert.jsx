/**
 * Note: This component was taken from MUI Lab until it's moved to core components and we migrate to 5.x
 * https://github.com/mui-org/material-ui/tree/master/packages/material-ui-lab/src/Alert
 * For component documentation see https://material-ui.com/components/alert
 * `outlinedWaiting` Style added separately
 */
import * as React from 'react';
import clsx from 'clsx';
import { withStyles, lighten, darken } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import IconButton from '@material-ui/core/IconButton';
import { capitalize } from '@material-ui/core/utils';
import SuccessOutlinedIcon from '@material-ui/icons/CheckCircleOutlineOutlined';
import ReportProblemOutlinedIcon from '@material-ui/icons/ReportProblemOutlined';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutlineOutlined';
import InfoOutlinedIcon from '@material-ui/icons/InfoOutlined';
import MoreHorizIcon from '@material-ui/icons/MoreHoriz';
import CloseIcon from '@material-ui/icons/Close';

export const styles = (theme) => {
    const getColor = theme.palette.type === 'light' ? darken : lighten;
    const getBackgroundColor = theme.palette.type === 'light' ? lighten : darken;

    return {
        /* Styles applied to the root element. */
        root: {
            ...theme.typography.body2,
            borderRadius: theme.shape.borderRadius,
            backgroundColor: 'transparent',
            display: 'flex',
            padding: '6px 16px',
        },
        /* Styles applied to the root element if `variant="standard"` and `color="success"`. */
        standardSuccess: {
            color: getColor(theme.palette.success.main, 0.6),
            backgroundColor: getBackgroundColor(theme.palette.success.main, 0.9),
            '& $icon': {
                color: theme.palette.success.main,
            },
        },
        /* Styles applied to the root element if `variant="standard"` and `color="info"`. */
        standardInfo: {
            color: getColor(theme.palette.info.main, 0.6),
            backgroundColor: getBackgroundColor(theme.palette.info.main, 0.9),
            '& $icon': {
                color: theme.palette.info.main,
            },
        },
        /* Styles applied to the root element if `variant="standard"` and `color="warning"`. */
        standardWarning: {
            color: getColor(theme.palette.warning.main, 0.6),
            backgroundColor: getBackgroundColor(theme.palette.warning.main, 0.9),
            '& $icon': {
                color: theme.palette.warning.main,
            },
        },
        /* Styles applied to the root element if `variant="standard"` and `color="error"`. */
        standardError: {
            color: getColor(theme.palette.error.main, 0.6),
            backgroundColor: getBackgroundColor(theme.palette.error.main, 0.9),
            '& $icon': {
                color: theme.palette.error.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="success"`. */
        outlinedSuccess: {
            color: getColor(theme.palette.success.main, 0.6),
            border: `1px solid ${theme.palette.success.main}`,
            '& $icon': {
                color: theme.palette.success.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="info"`. */
        outlinedInfo: {
            color: getColor(theme.palette.info.main, 0.6),
            border: `1px solid ${theme.palette.info.main}`,
            '& $icon': {
                color: theme.palette.info.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="warning"`. */
        outlinedWarning: {
            color: getColor(theme.palette.warning.main, 0.6),
            border: `1px solid ${theme.palette.warning.main}`,
            '& $icon': {
                color: theme.palette.warning.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="error"`. */
        outlinedError: {
            color: getColor(theme.palette.error.main, 0.6),
            border: `1px solid ${theme.palette.error.main}`,
            '& $icon': {
                color: theme.palette.error.main,
            },
        },
        /* Styles applied to the root element if `variant="filled"` and `color="success"`. */
        filledSuccess: {
            color: '#fff',
            fontWeight: theme.typography.fontWeightMedium,
            backgroundColor: theme.palette.success.main,
        },
        /* Styles applied to the root element if `variant="filled"` and `color="info"`. */
        filledInfo: {
            color: '#fff',
            fontWeight: theme.typography.fontWeightMedium,
            backgroundColor: theme.palette.info.main,
        },
        /* Styles applied to the root element if `variant="filled"` and `color="warning"`. */
        filledWarning: {
            color: '#fff',
            fontWeight: theme.typography.fontWeightMedium,
            backgroundColor: theme.palette.warning.main,
        },
        /* Styles applied to the root element if `variant="filled"` and `color="error"`. */
        filledError: {
            color: '#fff',
            fontWeight: theme.typography.fontWeightMedium,
            backgroundColor: theme.palette.error.main,
        },
        /* Styles applied to the icon wrapper element. */
        icon: {
            marginRight: 12,
            padding: '7px 0',
            display: 'flex',
            fontSize: 22,
            opacity: 0.9,
        },
        /* Styles applied to the message wrapper element. */
        message: {
            padding: '8px 0',
        },
        /* Styles applied to the action wrapper element if `action` is provided. */
        action: {
            display: 'flex',
            alignItems: 'center',
            marginLeft: 'auto',
            paddingLeft: 16,
            marginRight: -8,
        },
        plainWaiting: {
            color: getColor(theme.palette.text.secondary, 0.6),
            '& $icon': {
                color: theme.palette.text.secondary,
            },
        },
        plainSuccess: {
            color: getColor(theme.palette.success.main, 0.6),
            '& $icon': {
                color: theme.palette.success.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="info"`. */
        plainInfo: {
            color: getColor(theme.palette.info.main, 0.6),
            '& $icon': {
                color: theme.palette.info.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="warning"`. */
        plainWarning: {
            color: getColor(theme.palette.warning.main, 0.6),
            '& $icon': {
                color: theme.palette.warning.main,
            },
        },
        /* Styles applied to the root element if `variant="outlined"` and `color="error"`. */
        plainError: {
            color: getColor(theme.palette.error.main, 0.6),
            '& $icon': {
                color: theme.palette.error.main,
            },
        },
    };
};

const defaultIconMapping = {
    success: <SuccessOutlinedIcon fontSize='inherit' />,
    warning: <ReportProblemOutlinedIcon fontSize='inherit' />,
    error: <ErrorOutlineIcon fontSize='inherit' />,
    info: <InfoOutlinedIcon fontSize='inherit' />,
    waiting: <MoreHorizIcon fontSize='inherit' />,
};

const Alert = React.forwardRef((props, ref) => {
    const {
        action,
        children,
        classes,
        className,
        closeText = 'Close',
        color,
        icon,
        iconMapping = defaultIconMapping,
        onClose,
        role = 'alert',
        severity = 'success',
        variant = 'standard',
        ...other
    } = props;

    return (
        <Paper
            role={role}
            square
            elevation={0}
            className={clsx(
                classes.root,
                classes[`${variant}${capitalize(color || severity)}`],
                className,
            )}
            ref={ref}
            {...other}
        >
            {icon !== false ? (
                <div className={classes.icon}>
                    {icon || iconMapping[severity] || defaultIconMapping[severity]}
                </div>
            ) : null}
            <div className={classes.message}>{children}</div>
            {action != null ? <div className={classes.action}>{action}</div> : null}
            {action == null && onClose ? (
                <div className={classes.action}>
                    <IconButton
                        size='small'
                        aria-label={closeText}
                        title={closeText}
                        color='inherit'
                        onClick={onClose}
                    >
                        <CloseIcon fontSize='small' />
                    </IconButton>
                </div>
            ) : null}
        </Paper>
    );
});
export default withStyles(styles, { name: 'MuiAlert' })(Alert);
