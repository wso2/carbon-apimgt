import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Icon from '@material-ui/core/Icon';
import { amber } from '@material-ui/core/colors';
import VerticalDivider from './VerticalDivider';

const styles = (theme) => ({
    root: {
        display: 'flex',
        alignItems: 'center',
        paddingLeft: theme.spacing(2),
        borderRadius: theme.shape.borderRadius,
        border: 'solid 1px ' + theme.palette.secondary.main,
        '& span.material-icons.info': {
            fontSize: 80,
            color: theme.palette.primary.main,
        },
        '& span.material-icons.warning': {
            fontSize: 80,
            color: amber[700],
        },
    },
    button: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
    content: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
});


/**
 *
 *
 * @class InlineMessage
 * @extends {React.Component}
 */
class InlineMessage extends React.Component {
    handleExpandClick = () => {
        this.setState((state) => ({ expanded: !state.expanded }));
    };


    /**
     *
     *
     * @returns
     * @memberof InlineMessage
     * @inheritdoc
     */
    render() {
        const {
            classes, height, type, children,
        } = this.props;
        return (
            <Paper className={classes.root} {...this.props}>
                <Icon className={type}>{type}</Icon>
                <VerticalDivider height={height} />
                <div className={classes.content}>{children}</div>
            </Paper>
        );
    }
}

InlineMessage.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.string,
        iconItem: PropTypes.string,
        content: PropTypes.string,
    }).isRequired,
    height: PropTypes.number,
    type: PropTypes.string,
    children: PropTypes.shape({}).isRequired,
};
InlineMessage.defaultProps = {
    height: 100,
    type: 'info',
};
export default withStyles(styles)(InlineMessage);
