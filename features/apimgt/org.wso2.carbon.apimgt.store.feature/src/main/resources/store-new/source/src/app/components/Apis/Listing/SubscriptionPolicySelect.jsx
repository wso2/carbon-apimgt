import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Button from '@material-ui/core/Button';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        display: 'flex',
    },
    buttonGap: {
        marginRight: 10,
    },
});

/**
 *
 *
 * @class SubscriptionPolicySelect
 * @extends {React.Component}
 */
class SubscriptionPolicySelect extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedPolicy: null,
        };
    }

    /**
     *
     *
     * @returns
     * @memberof SubscriptionPolicySelect
     */
    componentDidMount() {
        const { policies } = this.props;

        this.setState({ selectedPolicy: policies[0] });
    }

    /**
     *
     *
     * @returns
     * @memberof SubscriptionPolicySelect
     */
    render() {
        const {
            classes, policies, apiId, handleSubscribe, applicationId,
        } = this.props;
        const { selectedPolicy } = this.state;

        return (
            policies &&
            <div className={classes.root}>
                <Button
                    variant='contained'
                    size='small'
                    color='primary'
                    className={classes.buttonGap}
                    onClick={() => {
                        handleSubscribe(applicationId, apiId, selectedPolicy);
                    }}
                >
                    Subscribe
                </Button>
                <Select
                    value={selectedPolicy}
                    onChange={(e) => {
                        this.setState({ selectedPolicy: e.target.value });
                    }}
                >
                    {policies.map(policy => (
                        <MenuItem value={policy}>
                            {policy}
                        </MenuItem>
                    ))}

                </Select>
            </div>
        );
    }
}

SubscriptionPolicySelect.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(SubscriptionPolicySelect);

