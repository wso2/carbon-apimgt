import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {
    makeStyles,
    TableRow,
    TableCell,
    Button,
    TextField,
} from '@material-ui/core';
import Autocomplete from './Autocomplete.jsx';

const useStyles = makeStyles(theme => ({
    textField: {
        padding: theme.spacing(),
        width: '100%',
    },
}));

/**
 * The mapping component. This component holds the mapping of a resource and ARN.
 * @param {any} props The props that are being passed to the component.
 * @returns {any} HTML view of the mapping.
 */
export default function Mapping(props) {
    const classes = useStyles();
    const {
        api,
        resource,
        resources,
        setResources,
    } = props;
    const [name, setName] = useState(resource.target);
    const [arn, setArn] = useState(resource.arn);
    const [isExistingName, setIsExistingName] = useState(false);
    const [isEmptyName, setIsEmptyName] = useState(false);
    const [isEmptyArn, setIsEmptyArn] = useState(false);

    const addResource = () => {
        if (name === '' && arn === '') {
            setIsEmptyName(true);
            setIsEmptyArn(true);
        } else if (name === '') {
            setIsEmptyName(true);
        } else if (arn === '') {
            setIsEmptyArn(true);
        } else {
            const newResource = { target: name, arn, editable: false };
            const newResources = resources.slice();
            newResources.push(newResource);
            setResources(newResources);
            setName('');
            setArn('');
        }
    };

    const saveResource = () => {
        if (!isEmptyName && !isEmptyArn && !isExistingName) {
            const newResources = [];
            resources.forEach((element) => {
                if (element.target !== resource.target) {
                    newResources.push(element);
                } else {
                    newResources.push({ target: name, arn, editable: false });
                }
            });
            setResources(newResources);
            setName('');
            setArn('');
        }
    };

    const cancel = () => {
        const newResources = [];
        resources.forEach((element) => {
            if (element.target !== resource.target) {
                newResources.push(element);
            } else {
                const newResource = resource;
                newResource.editable = false;
                newResources.push(newResource);
            }
        });
        setResources(newResources);
    };

    return (
        <TableRow>
            <TableCell>
                <TextField
                    error={isEmptyName || isExistingName}
                    helperText={
                        // eslint-disable-next-line no-nested-ternary
                        isEmptyName ? 'Resource name should not be empty' :
                            isExistingName ? 'Resource name already exists' : ''
                    }
                    required
                    id='outlined-required'
                    placeholder='URL pattern *'
                    variant='outlined'
                    value={name}
                    onChange={(event) => {
                        if (event.target.value !== '') {
                            setIsEmptyName(false);
                        } else {
                            setIsEmptyName(true);
                        }
                        const resourceNames = [];
                        resources.forEach((element) => {
                            resourceNames.push(element.target);
                        });
                        if (resourceNames.includes(event.target.value)) {
                            setIsExistingName(true);
                        } else {
                            setIsExistingName(false);
                        }
                        setName(event.target.value);
                    }}
                    className={classes.textField}
                />
            </TableCell>
            <TableCell>
                <Autocomplete
                    api={api}
                    arn={arn}
                    setArn={setArn}
                    isEmptyArn={isEmptyArn}
                    setIsEmptyArn={setIsEmptyArn}
                />
            </TableCell>
            {resource.editable ?
                <TableCell>
                    <Button
                        onClick={saveResource}
                        disabled={isEmptyName || isEmptyArn || isExistingName}
                    >
                        Save
                    </Button>
                    <Button onClick={cancel}>Cancel</Button>
                </TableCell>
                :
                <TableCell>
                    <Button
                        onClick={addResource}
                        disabled={isEmptyName || isEmptyArn || isExistingName}
                    >
                        Add
                    </Button>
                </TableCell>
            }
        </TableRow>
    );
}

Mapping.propTypes = {
    api: PropTypes.shape({}).isRequired,
    resource: PropTypes.shape({}).isRequired,
    resources: PropTypes.shape([]).isRequired,
    setResources: PropTypes.func.isRequired,
};
