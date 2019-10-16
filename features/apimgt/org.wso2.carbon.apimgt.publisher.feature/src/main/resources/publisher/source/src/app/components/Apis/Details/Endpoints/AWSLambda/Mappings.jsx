import React, { useState } from 'react';
import {
    Grid,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Button,
    makeStyles,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import Mapping from './Mapping.jsx';

const columns = [
    { id: 'name', label: 'Resource Name', minWidth: 100 },
    { id: 'arn', label: 'ARN', minWidth: 400 },
    { id: 'actions', label: 'Actions', minWidth: 100 },
];

const useStyles = makeStyles(theme => ({
    root: {
        paddingBottom: theme.spacing(),
        paddingTop: theme.spacing(),
        width: '100%',
        marginTop: theme.spacing(),
    },
    mappingsWrapper: {
        padding: theme.spacing(),
    },
    mappingTarget: {
        maxWidth: '300px',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
    mappingArn: {
        maxWidth: '800px',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
}));

/**
 * The mappings component. This component holds the mappings of resources and ARNs.
 * @param {any} props The props that are being passed to the function.
 * @returns {any} HTML view of the mappings.
 */
export default function Mappings(props) {
    const { api } = props;
    const [resources, setResources] = useState(api.operations);
    const classes = useStyles();

    /**
     * The function which enables the editing feature for a resource.
     * @param {any} resource The resource to be edited.
     */
    function editResource(resource) {
        const updatedResources = [];
        resources.forEach((element) => {
            if (element.target !== resource.target) {
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
        if (resources.length > 1) {
            const updatedResources = resources.filter((element) => {
                return element.target !== resource.target;
            });
            setResources(updatedResources);
        }
    }

    return (
        <Grid container item xs={12}>
            <Grid xs className={classes.mappingsWrapper}>
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
                                        api={api}
                                        resource={resource}
                                        resources={resources}
                                        setResources={setResources}
                                    />
                                );
                            } else {
                                return (
                                    <TableRow>
                                        <TableCell>
                                            <div className={classes.mappingTarget}>{resource.target}</div>
                                        </TableCell>
                                        <TableCell>
                                            <div className={classes.mappingArn}>{resource.amznResourceName}</div>
                                        </TableCell>
                                        <TableCell>
                                            <Button onClick={() => editResource(resource)}>Edit</Button>
                                            <Button onClick={() => deleteResource(resource)}>Delete</Button>
                                        </TableCell>
                                    </TableRow>
                                );
                            }
                        })}
                        <Mapping
                            api={api}
                            resource={{ name: '', arn: '', editable: false }}
                            resources={resources}
                            setResources={setResources}
                        />
                    </TableBody>
                </Table>
            </Grid>
        </Grid>
    );
}

Mappings.propTypes = {
    api: PropTypes.isRequired,
};
