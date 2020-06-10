import React, { useState } from 'react';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';
import AddCircleIcon from '@material-ui/icons/AddCircle';
import ClearIcon from '@material-ui/icons/Clear';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage } from 'react-intl';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
}));

/**
 * Claim Mapping Creation Form
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function ClaimMappings(props) {
    const classes = useStyles();
    const [newRemoteClaim, setRemoteClaim] = useState('');
    const [newLocalClaim, setLocalClaim] = useState('');
    const { claimMappings, setClaimMapping } = props;
    const [validationError, setValidationError] = useState([]);

    const onChange = (e) => {
        const { id, value } = e.target;
        if (id === 'remoteClaim') {
            setRemoteClaim(value);
        } else if (id === 'localClaim') {
            setLocalClaim(value);
        }
    };
    const validate = (fieldName, value) => {
        let error = '';
        if (value === null || value === '') {
            error = 'Claim is Empty';
        } else {
            error = '';
        }
        setValidationError({ fieldName: error });
        return error;
    };

    const clearValues = () => {
        setLocalClaim(null);
        setRemoteClaim(null);
    };
    const handleAddToList = () => {
        const remoteClaimError = validate('remoteClaim', newRemoteClaim);
        const localClaimError = validate('localClaim', newLocalClaim);
        if (localClaimError !== '' || remoteClaimError !== '') {
            Alert.error(remoteClaimError);
            clearValues();
            return false;
        } else {
            let exist = false;
            claimMappings.map(({ remoteClaim }) => {
                if (remoteClaim === newRemoteClaim) {
                    Alert.error(<FormattedMessage
                        id='Claim.Mapping.already.exists'
                        defaultMessage='Claim Mapping Already Exist'
                    />);
                    clearValues();
                    exist = true;
                }
                return false;
            });
            if (!exist) {
                const claimMapping = {
                    remoteClaim: newRemoteClaim,
                    localClaim: newLocalClaim,
                };
                claimMappings.push(claimMapping);
                setClaimMapping(claimMappings);
                clearValues();
            }
            return true;
        }
    };
    const onDelete = (claimKey) => {
        const newMapping = claimMappings.filter(({ remoteClaim }) => remoteClaim !== claimKey);
        setClaimMapping(newMapping);
    };
    return (
        <Box mb={3}>
            <TableContainer component={Paper}>
                <Table className={classes.table} aria-label='simple table'>
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                <FormattedMessage id='Keymanager.Remote.Claim' defaultMessage='Remote Claim' />
                            </TableCell>
                            <TableCell align='right'>
                                <FormattedMessage id='Keymanager.Local.Claim' defaultMessage='Local Claim' />
                            </TableCell>
                            <TableCell align='right'>
                                <FormattedMessage id='Keymanager.Claim.Action' defaultMessage='Action' />
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow key='Add new'>
                            <TableCell component='th' scope='row'>
                                <TextField
                                    id='remoteClaim'
                                    label={(
                                        <FormattedMessage
                                            id='Keymanager.Remote.Claim'
                                            defaultMessage='Remote Claim'
                                        />
                                    )}
                                    variant='outlined'
                                    onChange={onChange}
                                    error={validationError.remoteClaim}
                                    helperText={validationError.remoteClaim && validationError.remoteClaim}
                                    value={newRemoteClaim === null ? '' : newRemoteClaim}
                                />
                            </TableCell>
                            <TableCell align='right'>
                                <TextField
                                    id='localClaim'
                                    label={(
                                        <FormattedMessage
                                            id='Keymanager.Local.Claim'
                                            defaultMessage='Local Claim'
                                        />
                                    )}
                                    value={newLocalClaim === null ? '' : newLocalClaim}
                                    onChange={onChange}
                                    error={validationError.localClaim}
                                    helperText={validationError.localClaim && validationError.localClaim}
                                    variant='outlined'
                                />
                            </TableCell>
                            <TableCell>
                                <Grid>
                                    <Grid item>
                                        <IconButton
                                            id='delete'
                                            aria-label='Remove'
                                            onClick={handleAddToList}
                                        >
                                            <AddCircleIcon />
                                        </IconButton>
                                    </Grid>
                                    <Grid item>
                                        <IconButton
                                            id='delete'
                                            aria-label='Clear'
                                            onClick={clearValues}
                                        >
                                            <ClearIcon />
                                        </IconButton>
                                    </Grid>
                                </Grid>
                            </TableCell>
                        </TableRow>
                        {claimMappings.map(({ remoteClaim, localClaim }) => (
                            <TableRow key={localClaim}>
                                <TableCell component='th' scope='row'>
                                    <TextField
                                        id={remoteClaim}
                                        defaultValue={remoteClaim}
                                        variant='outlined'
                                        disabled
                                    />
                                </TableCell>
                                <TableCell align='right'>
                                    <TextField
                                        id={localClaim}
                                        defaultValue={localClaim}
                                        variant='outlined'
                                        disabled
                                    />
                                </TableCell>
                                <TableCell align='right'>
                                    <IconButton
                                        id='delete'
                                        aria-label='Remove'
                                        onClick={() => { onDelete(remoteClaim); }}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
}

ClaimMappings.defaultProps = {
    claimMappings: [],
    required: false,
    helperText: (<FormattedMessage id='KeyManager.Claim.Helper.text' defaultMessage='Add Claim Mappings' />),
};
