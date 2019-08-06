import React, { Component } from 'react';
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';

import API from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import SubscriptionsTable from './SubscriptionsTable';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
});

/**
 * Subscriptions component
 *
 * @class Subscriptions
 * @extends {Component}
 */
class Subscriptions extends Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.api.id;
        this.state = {
            api: null,
            notFound: false,
        };
    }

    componentDidMount() {
        const api = new API();
        const promisedAPI = api.get(this.api_uuid);
        promisedAPI
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    render() {
        const { api, notFound } = this.state;
        const { resourceNotFoundMessage } = this.props;
        if (notFound) {
            return (<ResourceNotFound message={resourceNotFoundMessage} />);
        }

        if (!api) {
            return <Progress />;
        }

        return (
            <div>
                <SubscriptionsTable api={api} />
            </div>
        );
    }
}

Subscriptions.defaultProps = {
    resourceNotFoundMessage: 'Resource not found!',
};

Subscriptions.propTypes = {
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    resourceNotFoundMessage: PropTypes.string,
    intl: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Subscriptions);
