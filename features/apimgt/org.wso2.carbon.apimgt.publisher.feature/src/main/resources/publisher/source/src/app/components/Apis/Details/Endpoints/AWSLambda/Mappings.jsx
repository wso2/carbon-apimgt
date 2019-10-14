import React, { useState } from 'react';
import {
    Grid,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
    Button,
    makeStyles,
    Paper,
} from '@material-ui/core';
import Mapping from './Mapping.jsx';
import API from '../../../../../data/api'; // TODO: Use webpack aliases instead of relative paths ~tmkb

const columns = [
    { id: 'name', label: 'Resource Name', minWidth: 100 },
    { id: 'arn', label: 'ARN', minWidth: 400 },
    { id: 'actions', label: 'Actions', minWidth: 100 },
];

const defaultResources = API.getResources();

const useStyles = makeStyles(theme => ({
    root: {
        paddingBottom: theme.spacing(),
        paddingTop: theme.spacing(),
        width: '100%',
        marginTop: theme.spacing(),
    },
    mappingsWrapper: {
        padding: theme.spacing(),
        borderRight: '#c4c4c4',
        borderRightStyle: 'solid',
        borderRightWidth: 'thin',
    },
    mappingsHeading: {
        display: 'flex',
        alignItems: 'center',
    },
    mappingsTitle: {
        paddingLeft: theme.spacing(),
        fontSize: '1rem',
        paddingTop: theme.spacing(),
        paddingBottom: theme.spacing(),
    },
}));

/**
 * The mappings component. This component holds the mappings of resources and ARNs.
 * @returns {any} HTML view of the mappings.
 */
export default function Mappings() {
    const classes = useStyles();
    const [resources, setResources] = useState(defaultResources);

    /**
     * The function which enables the editing feature for a resource.
     * @param {any} resource The resource to be edited.
     */
    function editResource(resource) {
        const updatedResources = [];
        resources.forEach((element) => {
            if (element.name !== resource.name) {
                updatedResources.push(element);
            } else {
                const editableResource = element;
                editableResource.editable = true;
                updatedResources.push(editableResource);
            }
        });
        setResources(updatedResources);
    }

    /**
     * The function which deletes the given resource from the list.
     * @param {any} resource The resource.
     */
    function deleteResource(resource) {
        const updatedResources = resources.filter((element) => {
            return element.name !== resource.name;
        });
        setResources(updatedResources);
    }

    return (
        <Paper className={classes.root}>
            <Grid container item xs={12}>
                <Grid xs className={classes.mappingsWrapper}>
                    <div className={classes.mappingsHeading}>
                        <Typography className={classes.mappingsTitle}>
                            Mappings
                        </Typography>
                    </div>
                    <Table stickyHeader>
                        <TableHead>
                            <TableRow>
                                {columns.map(column => (
                                    <TableCell
                                        key={column.id}
                                        align={column.align}
                                        style={{ minWidth: column.minWidth }}
                                    >
                                        {column.label}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {resources.map((resource) => {
                                if (resource.editable) {
                                    return (
                                        <Mapping
                                            resource={resource}
                                            resources={resources}
                                            setResources={setResources}
                                        />
                                    );
                                } else {
                                    return (
                                        <TableRow>
                                            <TableCell>{resource.name}</TableCell>
                                            <TableCell>{resource.arn}</TableCell>
                                            <TableCell>
                                                <Button onClick={() => editResource(resource)}>Edit</Button>
                                                <Button onClick={() => deleteResource(resource)}>Delete</Button>
                                            </TableCell>
                                        </TableRow>
                                    );
                                }
                            })}
                            <Mapping
                                resource={{ name: '', arn: '', editable: false }}
                                resources={resources}
                                setResources={setResources}
                            />
                        </TableBody>
                    </Table>
                </Grid>
            </Grid>
        </Paper>
    );
}
