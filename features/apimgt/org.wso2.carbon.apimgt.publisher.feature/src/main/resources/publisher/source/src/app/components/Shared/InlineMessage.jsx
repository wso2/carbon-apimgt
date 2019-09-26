import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Icon from '@material-ui/core/Icon';
import VerticalDivider from './VerticalDivider';

const styles = theme => ({
    root: {
        display: 'flex',
        alignItems: 'center',
        paddingLeft: theme.spacing.unit * 2,
        borderRadius: theme.shape.borderRadius,
        border: 'solid 1px ' + theme.palette.secondary.main,
        '& span.material-icons': {
            fontSize: 80,
            color: theme.palette.primary.main,
        },
    },
    button: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    content: {
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
        paddingRight: theme.spacing.unit,
    },
});

class InlineMessage extends React.Component {
    handleExpandClick = () => {
        this.setState(state => ({ expanded: !state.expanded }));
    };

    render() {
        const { classes, height, type } = this.props;
        return (
            <Paper className={classes.root} {...this.props}>
                {type === 'info' && <Icon>{type}</Icon>}
                <VerticalDivider height={height} />
                <div className={classes.content}>{this.props.children}</div>
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
