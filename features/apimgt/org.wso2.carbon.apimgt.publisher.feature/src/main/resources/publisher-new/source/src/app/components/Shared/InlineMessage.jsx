import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import InfoOutlined from '@material-ui/icons/InfoOutlined';
import VerticalDivider from './VerticalDivider';

const styles = theme => ({
    root: {
        display: 'flex',
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
            <Paper className={classes.root} elevation={1} {...this.props}>
                {type === 'info' && <InfoOutlined className={classes.iconItem} />}
                {type === 'warn' && <InfoOutlined className={classes.iconItem} />}
                <VerticalDivider height={height} />
                <div className={classes.content}>{this.props.children}</div>
            </Paper>
        );
    }
}

InlineMessage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    height: PropTypes.number,
    type: PropTypes.string,
    children: PropTypes.shape({}).isRequired,
};
InlineMessage.defaultProps = {
    height: 100,
    type: 'info',
};
export default withStyles(styles)(InlineMessage);
