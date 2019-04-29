import React from 'react';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import { Link } from 'react-router-dom';

/**
 * Row of the API Document in Doucment listing
 * @class DocRow @inheritdoc
 * @extends {React.Component} @inheritdoc
 */
export default class DocRow extends React.Component {

    render() {
        const { doc, api } = this.props;
        const { documentId } = doc;
        const { isSelected } = this.props;
        const overviewPath = `documents/${doc.documentId}/details`;
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
                    <Link to={overviewPath}>{doc.name}</Link>
                </TableCell>
                <TableCell>{doc.type}</TableCell>
                <TableCell>{doc.lastUpdatedTime}</TableCell>
            </TableRow>
        );
    }
}