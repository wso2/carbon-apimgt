import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Icon from '@material-ui/core/Icon';
import VerticalDivider from './VerticalDivider';
/**
 * Main style object
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        display: 'flex',
        height: 100,
        alignItems: 'center',
        paddingLeft: theme.spacing(2),
        borderRadius: theme.shape.borderRadius,
        border: 'none',
        '& span.material-icons': {
            fontSize: 60,
            color: theme.palette.primary.main,
        }
    },
    iconItem: {
        paddingRight: theme.spacing(2),
        fontSize: 60,
    },
    button: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
    content: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
    },
});
/**
 *  Renders a inline massage
 *
 * @class InlineMessage
 * @extends {React.Component}
 */
class InlineMessage extends React.Component {
    state = {
        value: 0,
    };

    handleExpandClick = () => {
        this.setState(state => ({ expanded: !state.expanded }));
    };

    render() {
        const { classes, type } = this.props;
        const messgeType = type || 'info';
        return (
            <Paper className={classes.root} elevation={1}>
                {messgeType === 'info' && <Icon className={classes.iconItem}>info</Icon>}
                {messgeType === 'warn' && <Icon className={classes.iconItem}>warning</Icon>}
                <VerticalDivider height={100} />
                <div className={classes.content}>{this.props.children}</div>
            </Paper>
        );
    }
}
InlineMessage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    type: PropTypes.string.isRequired,
};

export default withStyles(styles)(InlineMessage);
