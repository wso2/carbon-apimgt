// @flow weak

import React, { Component } from 'react';
import BottomNavigation, { BottomNavigationButton } from 'material-ui/BottomNavigation';
import RestoreIcon from 'material-ui-icons/Restore';
import FavoriteIcon from 'material-ui-icons/Favorite';
import LocationOnIcon from 'material-ui-icons/LocationOn';


class Listing2 extends Component {
    state = {
        value: 0,
    };
    handleChange = (event, value) => {
        this.setState({ value });
    };


    render() {
        const classes = this.props.classes;
        const { data, order, orderBy, selected } = this.state;

        return (
            <Paper className={classes.paper}>
                <EnhancedTableToolbar numSelected={selected.length} />
                <Table>
                    <EnhancedTableHead
                        order={order}
                        orderBy={orderBy}
                        onSelectAllClick={this.handleSelectAllClick}
                        onRequestSort={this.handleRequestSort}
                    />
                    <TableBody>
                        {data.map(n => {
                            const isSelected = this.isSelected(n.id);
                            return (
                                <TableRow
                                    hover
                                    onClick={event => this.handleClick(event, n.id)}
                                    onKeyDown={event => this.handleKeyDown(event, n.id)}
                                    role="checkbox"
                                    aria-checked={isSelected}
                                    tabIndex="-1"
                                    key={n.id}
                                    selected={isSelected}
                                >
                                    <TableCell checkbox>
                                        <Checkbox checked={isSelected} />
                                    </TableCell>
                                    <TableCell disablePadding>
                                        {n.name}
                                    </TableCell>
                                    <TableCell numeric>
                                        {n.calories}
                                    </TableCell>
                                    <TableCell numeric>
                                        {n.fat}
                                    </TableCell>
                                    <TableCell numeric>
                                        {n.carbs}
                                    </TableCell>
                                    <TableCell numeric>
                                        {n.protein}
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </Paper>
        );
    }
}

export default Listing2;