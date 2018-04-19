import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Typography from 'material-ui/Typography';
import { withStyles } from 'material-ui/styles';
import Card, { CardContent, CardActions, CardHeader } from 'material-ui/Card';
import { ErrorOutline, Refresh } from '@material-ui/icons/';
import IconButton from 'material-ui/IconButton';
import Avatar from 'material-ui/Avatar';
import red from 'material-ui/colors/red';
import Divider from 'material-ui/Divider';

const styles = () => ({
    cardContent: { color: 'pink', backgroundColor: 'black' },
    avatar: {
        margin: 10,
        color: '#fff',
        backgroundColor: red[500],
    },
    subheader: {
        color: red[500],
    },
});
/**
 * Error boundary for the application.catch JavaScript errors anywhere in their child component tree,
 * log those errors, and display a fallback UI instead of the component tree that crashed.
 * Error boundaries catch errors during rendering, in lifecycle methods,
 * and in constructors of the whole tree below them.
 * @class AppErrorBoundary
 * @extends {Component}
 */
class AppErrorBoundary extends Component {
    /**
     * Creates an instance of AppErrorBoundary.
     * @param {any} props @inheritDoc
     * @memberof AppErrorBoundary
     */
    constructor(props) {
        super(props);
        this.state = {
            hasError: false,
        };
    }

    /**
     * The componentDidCatch() method works like a JavaScript catch {} block, but for components.
     * @param {Error} error is an error that has been thrown
     * @param {Object} info info is an object with componentStack key. The property has information about component
     * stack during thrown error.
     * @memberof AppErrorBoundary
     */
    componentDidCatch(error, info) {
        this.setState({ hasError: true, error, info });
    }

    /**
     * Return error handled UI
     * @returns {React.Component} return react component
     * @memberof AppErrorBoundary
     */
    render() {
        const { hasError, error, info } = this.state;
        const { children, appName, classes } = this.props;
        if (hasError) {
            return (
                <div>
                    <Card>
                        <CardHeader
                            avatar={
                                <Avatar aria-label='Error' className={classes.avatar}>
                                    <ErrorOutline />
                                </Avatar>
                            }
                            title={
                                <Typography variant='headline'>
                                    Aaaah! Something went wrong while rendering the {appName}
                                </Typography>
                            }
                            subheader={<Typography color='error'>{error.message}</Typography>}
                        />
                        <Divider />
                        <CardContent className={classes.cardContent}>
                            <pre>
                                <u>{error.stack}</u>
                            </pre>
                            <pre>
                                <u>{info.componentStack}</u>
                            </pre>
                        </CardContent>
                        <CardActions className={classes.actions} disableActionSpacing>
                            <Typography color='primary'>You may refresh the page or try again later</Typography>
                            <IconButton
                                onClick={() => {
                                    window.location.reload(true);
                                }}
                                aria-label='Refresh'
                            >
                                <Refresh />
                            </IconButton>
                        </CardActions>
                    </Card>
                </div>
            );
        } else {
            return children;
        }
    }
}

AppErrorBoundary.defaultProps = {
    appName: 'Application',
};

AppErrorBoundary.propTypes = {
    children: PropTypes.node.isRequired,
    appName: PropTypes.string,
    classes: PropTypes.shape({
        root: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(AppErrorBoundary);
