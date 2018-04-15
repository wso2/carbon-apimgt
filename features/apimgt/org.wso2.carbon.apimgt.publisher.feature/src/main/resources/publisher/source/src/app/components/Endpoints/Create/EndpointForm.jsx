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
     * @inheritDoc
     * @returns {React.Component}
     * @memberof EndpointForm
     */
    render() {
        const { classes, handleInputs } = this.props;
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
                        name='name'
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
                        value={this.state.endpointType}
                        onChange={(e) => {
                            e.target.name = 'endpointType';
                            handleInputs(e);
                        }}
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
                        name='name'
                        onChange={(e) => {
                            e.target.name = 'maxTPS';
                            handleInputs(e);
                        }}
                        placeholder='100'
                    />
                    <TextField
                        className={classes.inputText}
                        name='serviceUrl'
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
                                checked={this.state.secured}
                                onChange={this.handleChange('secured')}
                                value='secured'
                                color='primary'
                            />
                        }
                        label='Secured'
                    />
                    {this.state.secured && (
                        <div className={classes.secured}>
                            Type
                            <RadioGroup
                                row
                                value={this.state.securityType}
                                onChange={(e) => {
                                    e.target.name = 'securityType';
                                    handleInputs(e);
                                }}
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
                                name='username'
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
                                name='password'
                                onChange={handleInputs}
                                placeholder='Password'
                            />
                        </div>
                    )}
                    <div className={classes.buttonsWrapper}>
                        <Button variant='raised' color='primary' className={classes.button} onClick={this.handleSubmit}>
                            Create
                        </Button>
                        <Link to='/endpoints/'>
                            <Button variant='raised' className={classes.button}>
                                Cancel
                            </Button>
                        </Link>
                    </div>
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
