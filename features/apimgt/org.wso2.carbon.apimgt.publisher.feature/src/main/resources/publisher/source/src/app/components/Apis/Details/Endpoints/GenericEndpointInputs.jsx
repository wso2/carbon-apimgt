import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Grid } from 'material-ui';
import Input, { InputLabel } from 'material-ui/Input';
import { FormControl, FormHelperText } from 'material-ui/Form';
import Select from 'material-ui/Select';

/**
 * Define common Endpoint definition form to be used both in API details and global endpoint creation.
 * @export
 * @class GenericEndpointInputs
 * @extends {Component}
 */
export default class GenericEndpointInputs extends Component {
    /**
     * Creates an instance of GenericEndpointInputs.
     * @param {any} props @inheritDoc
     * @memberof GenericEndpointInputs
     */
    constructor(props) {
        super(props);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.handleSecurityType = this.handleSecurityType.bind(this);
        this.handleEndpointType = this.handleEndpointType.bind(this);
        this.state = { url: this.props.endpoint.url };
    }

    handleSecurityType(type) {
        let isSecured = false;
        if (type !== 'nonsecured') {
            isSecured = true;
        }
        this.setState({ isSecured });
    }

    handleEndpointType(endpointUUID) {
        let ep = null;
        let isGlobalEPSelected = false;
        const e = {
            target: {
                name: 'uuid',
                value: endpointUUID,
            },
        };

        if (endpointUUID !== 'custom') {
            ep = JSON.parse(this.props.endpointsMap.get(endpointUUID).endpointConfig).serviceUrl;
            isGlobalEPSelected = true;
        } else {
            // custom
            e.target.value = {};
        }
        this.props.handleInputs(e);
        this.setState({ url: ep, isGlobalEPSelected });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Common endpoint component
     * @memberof GenericEndpointInputs
     */
    render() {
        const { endpoint, endpointsMap } = this.props;
        return (
            <Grid container>
                <Grid item>
                    <FormControl error>
                        <InputLabel htmlFor='endpoint-select'>Endpoint</InputLabel>
                        <Select
                            native
                            value={endpoint.selectedEP || 'Custom...'}
                            onChange={this.handleEndpointType}
                            input={<Input id='endpoint-select' />}
                        >
                            <option value='in-line'>In-Line</option>
                            <optgroup label='Global'>
                                {[...endpointsMap].map(ep => <option key={ep.id}>{ep.name}</option>)}
                            </optgroup>
                        </Select>
                        <FormHelperText>Global or In-line</FormHelperText>
                    </FormControl>
                </Grid>
                <Grid>
                    <Grid>Endpoint URL</Grid>
                    <Grid>
                        <Input
                            disabled={endpoint.isGlobalEPSelected || this.state.isGlobalEPSelected}
                            value={endpoint.url || this.state.url}
                            name='url'
                            onChange={this.props.handleInputs}
                            placeholder='https://sample.wso2.org/api/endpoint'
                        />
                    </Grid>
                </Grid>
                <Grid>
                    <Grid>Security Scheme</Grid>
                    <Grid>
                        <Select
                            disabled={endpoint.isGlobalEPSelected || this.state.isGlobalEPSelected}
                            name='security'
                            onChange={this.handleSecurityType}
                            defaultValue='None Secured'
                            style={{ width: '40%' }}
                        >
                            <Option value='nonsecured'>None Secured</Option>
                            <Option value='basic'>Basic Auth</Option>
                            <Option value='digest'>Digest Auth</Option>
                        </Select>
                    </Grid>
                </Grid>
                <div hidden={!this.state.isSecured}>
                    <Grid>
                        <Grid>Username</Grid>
                        <Grid>
                            <Input
                                name='username'
                                defaultValue={endpoint.username}
                                onChange={this.props.handleInputs}
                                placeholder='Enter Username'
                            />
                        </Grid>
                    </Grid>
                    <Grid>
                        <Grid>Password</Grid>
                        <Grid>
                            <Input name='password' onChange={this.handleTextInputs} placeholder='Basic usage' />
                        </Grid>
                    </Grid>
                </div>
            </Grid>
        );
    }
}

GenericEndpointInputs.propTypes = {
    endpoint: PropTypes.shape({
        url: PropTypes.string,
        username: PropTypes.string,
        selectedEP: PropTypes.string,
        isGlobalEPSelected: PropTypes.bool,
    }).isRequired,
    endpointsMap: PropTypes.instanceOf(Map).isRequired,
};
