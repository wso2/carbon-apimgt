import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';

/**
 *
 *
 * @class AdditionalProperty
 * @extends {Component}
 */
class AdditionalProperty extends Component {

    render() {
        return (
            <Grid item lg={5}>
                <TextField
                    style={{
                        justifyContent: 'space-between',
                        marginRight: 25,
                    }}
                    id='api-property'
                    label={this.props.isEditable && 'name'}
                    defaultValue={this.props.property.name}
                    placeholder='My Property'
                    margin='normal'
                    InputProps={{
                        readOnly: !this.props.isEditable,
                    }}
                />
                <TextField
                    style={{
                        justifyContent: 'space-between',
                        marginLeft: 25,
                    }}
                    id='api-property-value'
                    label={this.props.isEditable && 'value'}
                    defaultValue={this.props.property.value}
                    placeholder='Property Value'
                    margin='normal'
                    InputProps={{
                        readOnly: !this.props.isEditable,
                    }}
                />

                <IconButton
                    id='delete'
                    aria-label='Remove'
                    onClick={() => { this.props.onDelete(this.props.property.name);}}
                >
                    <DeleteIcon />
                </IconButton>

            </Grid>
        );
    }
}

AdditionalProperty.propTypes = {
    property: PropTypes.shape({
        name: PropTypes.string,
        value: PropTypes.string,
    }).isRequired,
    isEditable: PropTypes.bool,
    onDelete: PropTypes.func.isRequired,
};

AdditionalProperty.defaultProps = {
    isEditable: false,
};

export default AdditionalProperty;
