import React, { Component } from 'react';
import Button from 'material-ui/Button';
import AddIcon from '@material-ui/icons/Add';
import UpdateIcon from '@material-ui/icons/Update';
import PropTypes from 'prop-types';

import API from '../../../data/api';

export default class ButtonCell extends Component {
    constructor(props) {
        super(props);
        this.state = {
            record: this.props.record,
            storedEndpoints: this.props.storedEndpoints,
            actionButton: (
                <Button raised color='primary' className='ed-button'>
                    Loading...
                </Button>
            ),
        };
    }

    componentDidMount() {
        if (this.state.storedEndpoints != null) {
            if (this.checkIfEndpointExists()) {
                this.setState({
                    actionButton: this.getUpdateButton(),
                });
            } else {
                this.setState({
                    actionButton: this.getAddButton(),
                });
            }
        }
    }

    getAddButton() {
        return (
            <Button raised color='primary' className='ed-button' onClick={() => this.handleAddEndpointToDB()}>
                <AddIcon />&nbsp; Add
            </Button>
        );
    }

    getUpdateButton() {
        return (
            <Button raised className='ed-button' onClick={() => this.handleUpdateEndpoint()}>
                <UpdateIcon />&nbsp; Update
            </Button>
        );
    }

    handleAddEndpointToDB = () => {
        this.props.changeMessage('loading', 'Adding the Endpoint ...');
        const { record } = this.state;
        const configObject = JSON.parse(record.endpointConfig);

        const endpointDefinition = {
            name: configObject.namespace + '-' + record.name + '-' + record.type + '-' + configObject.urlType,
            type: record.type,
            endpointConfig: record.endpointConfig,
            endpointSecurity: record.endpointSecurity,
            maxTps: record.maxTps,
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint
            .then((response) => {
                const { name } = response.obj;
                this.state.storedEndpoints.push(response.obj);

                this.setState({
                    actionButton: this.getUpdateButton(),
                });
                this.props.changeMessage('success', 'New endpoint ' + name + ' created successfully');
            })
            .catch(() => {
                this.props.changeMessage('error', 'Error occurred while creating the endpoint!');
            });
    };

    handleUpdateEndpoint = () => {
        this.props.changeMessage('loading', 'Updating the Endpoint ...');
        const { record } = this.state;
        const configObject = JSON.parse(record.endpointConfig);
        const endpointName =
            configObject.namespace + '-' + record.name + '-' + record.type + '-' + configObject.urlType;
        const storedEndpoint = this.state.storedEndpoints.find(el => el.name === endpointName);
        if (storedEndpoint === null) {
            this.props.changeMessage(
                'error',
                'Error while updating. Could not find the ' + endpointName + ' Endpoint!',
            );
            return;
        }
        const endpointDefinition = {
            id: storedEndpoint.id,
            name: endpointName,
            type: record.type,
            endpointConfig: record.endpointConfig,
            endpointSecurity: record.endpointSecurity,
            maxTps: record.maxTps,
        };
        const api = new API();
        const promisedUpdate = api.updateEndpoint(endpointDefinition);
        promisedUpdate
            .then((response) => {
                if (response.status !== 200) {
                    this.props.changeMessage(
                        'error',
                        'Something went wrong while updating the ' + endpointName + ' Endpoint!',
                    );
                    return;
                }
                this.props.changeMessage('success', 'Endpoint ' + endpointName + ' updated successfully!');
            })
            .catch((error) => {
                console.error(error);
                this.props.changeMessage('error', 'Error occurred while trying to update the endpoint!');
            });
    };

    checkIfEndpointExists = () => {
        const { record } = this.state;
        const endpointConfig = JSON.parse(record.endpointConfig);
        const endpointName =
            endpointConfig.namespace + '-' + record.name + '-' + record.type + '-' + endpointConfig.urlType;
        return this.state.storedEndpoints.some(el => el.name === endpointName);
    };

    render() {
        return this.state.actionButton;
    }
}

ButtonCell.propTypes = {
    record: PropTypes.string.isRequired,
    changeMessage: PropTypes.string.isRequired,
    storedEndpoints: PropTypes.objectOf(Promise).isRequired,
};
