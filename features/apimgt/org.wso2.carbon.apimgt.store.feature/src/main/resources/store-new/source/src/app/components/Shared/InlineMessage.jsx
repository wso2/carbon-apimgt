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
        paddingLeft: theme.spacing.unit * 2,
        borderRadius: theme.shape.borderRadius,
        border: 'solid 1px ' + theme.palette.secondary.main,
    },
    iconItem: {
        paddingRight: theme.spacing.unit * 2,
        fontSize: 60,
    },
    button: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    content: {
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
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
            <Paper className={classes.root} elevation={1} {...this.props}>
                {messgeType === 'info' && <Icon className={classes.iconItem}>info</Icon>}
                {messgeType === 'warn' && <Icon className={classes.iconItem}>warning</Icon>}
                <VerticalDivider height={100} />
                <div className={classes.content}>{this.props.children}</div>
            </Paper>
        );
    }
}

InlineMessage.propTypes = {
    classes: PropTypes.instanceOf(Object).isRequired,
    type: PropTypes.instanceOf(Object).isRequired,
};

export default withStyles(styles)(InlineMessage);
