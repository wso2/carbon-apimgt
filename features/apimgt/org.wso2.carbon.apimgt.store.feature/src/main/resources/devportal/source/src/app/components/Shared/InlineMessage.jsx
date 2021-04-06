import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Icon from '@material-ui/core/Icon';
import VerticalDivider from './VerticalDivider';
import { Alert, AlertTitle } from '@material-ui/lab';

/**
 * Main style object
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        width: '100%',
        '& > * + *': {
            marginTop: theme.spacing(2),
        },
    },
});
/**
 *  Renders a inline massage
 *
 * @class InlineMessage
 * @extends {React.Component}
 */
function InlineMessage(props) {
    const { type, title } = props;
    const messageType = type || 'info';
    return (
        <Alert severity={messageType}>
            {title && (<AlertTitle>{title}</AlertTitle>)}
            {props.children}
        </Alert>

    );
}

InlineMessage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
};

export default InlineMessage;
