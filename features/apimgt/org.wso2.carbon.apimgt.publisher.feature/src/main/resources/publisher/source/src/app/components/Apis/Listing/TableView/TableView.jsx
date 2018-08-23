import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { ScopeValidation, resourceMethod, resourcePath } from '../../../data/ScopeValidation';

const columns = [
    {
        title: 'Name',
        dataIndex: 'name',
        key: 'name',
        render: (text, record) => <Link to={'/apis/' + record.id}>{text}</Link>,
    },
    {
        title: 'Context',
        dataIndex: 'context',
        key: 'context',
    },
    {
        title: 'Version',
        dataIndex: 'version',
        key: 'version',
    },
    {
        title: 'Action',
        key: 'action',
        render: record => (
            <ScopeValidation resourcePath={resourcePath.SINGLE_API} resourceMethod={resourceMethod.DELETE}>
                <Button
                    style={{ fontSize: 10, padding: '0px', margin: '0px' }}
                    color='primary'
                    onClick={() => this.handleApiDelete(record.id, record.name)}
                >
                    Delete
                </Button>
            </ScopeValidation>
        ),
    },
];
class TableView extends Component {
    render() {
        return (
            <Grid container spacing={8}>
                {this.state.apis.list.map((api, i) => {
                    return (
                        <ApiThumb key={api.id} listType={this.state.listType} api={api} updateApi={this.updateApi} />
                    );
                })}
            </Grid>
        );
    }
}

TableView.propTypes = {};

export default TableView;
