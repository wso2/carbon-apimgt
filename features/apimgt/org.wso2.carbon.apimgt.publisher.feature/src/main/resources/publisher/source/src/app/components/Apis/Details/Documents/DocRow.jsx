import React from 'react';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';

/**
 * Row of the API Document in Doucment listing
 * @class DocRow @inheritdoc
 * @extends {React.Component} @inheritdoc
 */
export default class DocRow extends React.Component {

    render() {
        const doc = this.props.doc;
        const { documentId } = doc;
        const {isSelected} = this.props;
        return (
            <TableRow
                hover
                role='checkbox'
                aria-checked={isSelected}
                tabIndex={-1}
                key={documentId}
                selected={isSelected}
            >
                <TableCell onClick={this.props.handleSelectADoc} id={documentId} padding='checkbox'>
                    <Checkbox checked={isSelected} />
                </TableCell>

                <TableCell component='th' scope='row' padding='none'>
                    {doc.name}
                </TableCell>
                <TableCell>{doc.type}</TableCell>
                <TableCell>{doc.lastUpdatedTime}</TableCell>
            </TableRow>
        );
    }
}