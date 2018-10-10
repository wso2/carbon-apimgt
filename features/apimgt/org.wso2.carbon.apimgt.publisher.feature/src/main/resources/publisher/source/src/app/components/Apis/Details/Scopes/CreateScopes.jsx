
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';
import React, { Component } from 'react';


class CreateScopes extends Component {
    render() {
        return (
            <Grid item lg={5}>
                <h1>dkjnkwjnkjkn</h1>
                <h1>jdnfkjndwknfknwd</h1>
                <TextField
                    style={{
                        justifyContent: 'space-between',
                        marginRight: 25,
                    }}
                    id='api-property-key'
                    label='key'
                    placeholder='My Property'
                    margin='normal'
                />
                <TextField
                    style={{
                        justifyContent: 'space-between',
                        marginLeft: 25,
                    }}
                    id='api-property-value'
                    label='value'
                    placeholder='Property Value'
                    margin='normal'
                />
                <IconButton
                    id='delete'
                    aria-label='Remove'
                >
                    <DeleteIcon />
                </IconButton>
            </Grid>
        );
    }
}


export default CreateScopes;
