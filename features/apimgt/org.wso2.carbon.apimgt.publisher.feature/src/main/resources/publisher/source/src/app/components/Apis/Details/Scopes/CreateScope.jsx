
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import React from 'react';
import Card from '@material-ui/core/Card';
import APIPropertyField from 'AppComponents/Apis/Details/Overview/APIPropertyField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import TagsInput from 'react-tagsinput';
import Api from 'AppData/api';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { message } from 'antd/lib/index';
import { withRouter } from 'react-router';

// import Scope from './Scope';

class CreateScope extends React.Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
        };
        this.addScope = this.addScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    addScope() {
        const hideMessage = message.loading('Adding the Scope ...', 0);
        const api = new Api();
        const scope = this.state.apiScope;
        scope.bindings = {
            type: 'role',
            values: this.state.roles,
        };
        const promisedScopeAdd = api.addScope(this.props.match.params.api_uuid, scope);
        promisedScopeAdd.then((response) => {
            if (response.status !== 201) {
                console.log(response);
                message.error('Something went wrong while updating the ' + scope.name + ' Scope!');
                hideMessage();
                return;
            }
            message.success(scope.name + ' Scope added successfully!');
            const { apiScopes } = this.state;
            this.setState({
                apiScopes,
                apiScope: {},
                roles: [],
            });
            hideMessage();
        });
    }

    /**
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            this.setState({
                roles: event,
            });
        } else {
            const input = event.target;
            const { apiScope } = this.state;
            apiScope[input.id] = input.value;
            this.setState({
                apiScope,
            });
        }
    }

    render() {
        const { api } = this.props;
        return (
            <Grid container justify='center'>
                <Grid item lg={5}>
                    <Card
                        title='Add Scope'
                        style={{
                            width: '100%',
                            marginBottom: 20,
                        }}
                    >
                        <Typography className='create-scope' gutterBottom variant='headline' component='h2'>
                            <FormattedMessage
                                id='create.new.scopes'
                                defaultMessage='Create New Scope'
                            />
                        </Typography>
                        <APIPropertyField name='Name'>
                            <TextField
                                id='name'
                                type='text'
                                name='name'
                                margin='normal'
                                value={this.state.apiScope.name || ''}
                                onChange={this.handleInputs}
                            />
                        </APIPropertyField>
                        <APIPropertyField name='Description'>
                            <TextField
                                style={{
                                    width: '100%',
                                }}
                                id='description'
                                name='description'
                                helperText='Short description about the scope'
                                margin='normal'
                                type='text'
                                onChange={this.handleInputs}
                                value={this.state.apiScope.description || ''}
                            />
                        </APIPropertyField>
                        <APIPropertyField name='Roles'>
                            <TagsInput
                                value={this.state.roles}
                                onChange={this.handleInputs}
                                onlyUnique
                            />
                        </APIPropertyField>
                    </Card>
                    <Button variant='contained' color='primary' onClick={this.addScope}>
                        <FormattedMessage
                            id='save'
                            defaultMessage='Save'
                        />
                    </Button>
                    <Button variant='contained' color='primary' >
                        <FormattedMessage
                            id='cancel'
                            defaultMessage='Cancel'
                        />
                    </Button>
                </Grid>
            </Grid>
            // Object.keys(this.apiScopes).map((key) => {
            //     const scope = this.apiScopes[key];
            //     return (
            //         <Scope
            //             name={scope.name}
            //             description={scope.description}
            //             api_uuid={this.api_uuid}
            //             key={key}
            //         />
            //     );
            // })
        );
    }
}

CreateScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

CreateScope.defaultProps = {
    match: { params: {} },
};

export default withRouter(CreateScope);
