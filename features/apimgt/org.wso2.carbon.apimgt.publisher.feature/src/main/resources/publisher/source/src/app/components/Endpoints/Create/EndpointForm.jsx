import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Radio, { RadioGroup } from 'material-ui/Radio';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import { Link } from 'react-router-dom';
import { FormControlLabel, FormLabel } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import { withStyles } from 'material-ui/styles';

const styles = () => ({
    textField: {
        marginLeft: 20,
    },
    group: {
        display: 'flex',
    },
    legend: {
        marginBottom: 0,
        borderBottomStyle: 'none',
        marginTop: 20,
        fontSize: 12,
    },
    inputText: {
        marginTop: 20,
    },
    secured: {
        marginTop: 40,
    },
    button: {
        marginRight: 20,
    },
    buttonsWrapper: {
        marginTop: 40,
    },
});

/**
 * Endpoint input/Display form component
 * @class EndpointForm
 * @extends {Component}
 */
class EndpointForm extends Component {
    /**
     * Creates an instance of EndpointForm.
     * @param {any} props @inheritDoc
     * @memberof EndpointForm
     */
    constructor(props) {
        super(props);
        this.state = {};
    }

    /**
     * @inheritDoc
     * @returns {React.Component} component
     * @memberof EndpointForm
     */
    render() {
        const {
            classes, endpointSecurity, endpointType, endpointSecurityType,
        } = this.props;
        let { handleInputs } = this.props;
        const isReadOnly = !handleInputs; // Showing the endpoint details
        handleInputs = handleInputs || null;
        return (
            <div>
                <form className={classes.container} noValidate autoComplete='off'>
                    <TextField
                        label='Endpoint Name'
                        InputLabelProps={{
                            shrink: true,
                        }}
                        helperText='Enter a name to identify the endpoint. You will be able to pick this endpoint
                                        when creating/editing APIs '
                        fullWidth
                        name='endpointName'
                        onChange={handleInputs}
                        placeholder='Endpoint Name'
                        autoFocus
                    />
                    <FormLabel component='legend' className={classes.legend}>
                        Endpoint Type
                    </FormLabel>
                    <RadioGroup
                        row
                        aria-label='type'
                        className={classes.group}
                        value={endpointType}
                        name='endpointType'
                        onChange={handleInputs}
                    >
                        <FormControlLabel value='http' control={<Radio color='primary' />} label='HTTP' />
                        <FormControlLabel value='https' control={<Radio color='primary' />} label='HTTPS' />
                    </RadioGroup>
                    <TextField
                        className={classes.inputText}
                        label='Max TPS'
                        InputLabelProps={{
                            shrink: true,
                        }}
                        helperText='Max Transactions per second'
                        fullWidth
                        name='endpointMaxTPS'
                        onChange={handleInputs}
                        placeholder='100'
                    />
                    <TextField
                        className={classes.inputText}
                        name='endpointServiceUrl'
                        label='Service URL'
                        InputLabelProps={{
                            shrink: true,
                        }}
                        helperText='Provide Service URL'
                        onChange={handleInputs}
                        placeholder='https://forecast-v3.weather.gov'
                        fullWidth
                    />
                    <FormControlLabel
                        className={classes.inputText}
                        control={
                            <Switch
                                name='endpointSecurity'
                                checked={endpointSecurity}
                                onChange={handleInputs}
                                value='secured'
                                color='primary'
                            />
                        }
                        label='Secured'
                    />
                    {endpointSecurity && (
                        <div className={classes.secured}>
                            Type
                            <RadioGroup
                                row
                                name='endpointSecurityType'
                                value={endpointSecurityType}
                                onChange={handleInputs}
                            >
                                <FormControlLabel value='basic' control={<Radio />} label='Basic' />
                                <FormControlLabel value='digest' control={<Radio />} label='Digest' />
                            </RadioGroup>
                            <TextField
                                className={classes.inputText}
                                label='Username'
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                helperText='Enter the Username'
                                fullWidth
                                margin='normal'
                                name='endpointSecurityUsername'
                                onChange={handleInputs}
                                placeholder='Username'
                            />
                            <TextField
                                className={classes.inputText}
                                label='Password'
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                helperText='Enter the Password'
                                fullWidth
                                name='endpointSecurityPassword'
                                onChange={handleInputs}
                                placeholder='Password'
                            />
                        </div>
                    )}
                </form>
            </div>
        );
    }
}

EndpointForm.defaultProps = {
    handleInputs: false,
};
EndpointForm.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
};

export default withStyles(styles)(EndpointForm);
