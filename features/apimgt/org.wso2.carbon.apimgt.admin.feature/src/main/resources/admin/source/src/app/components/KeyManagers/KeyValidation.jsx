import React, { useState } from 'react';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import FormControl from '@material-ui/core/FormControl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableBody from '@material-ui/core/TableBody';
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
 * Add KeyValidation Types Form
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function KeyValidation(props) {
    const classes = useStyles();

    const { tokenValidation, setTokenValidation } = props;
    const [jwtValue, setjwtValue] = useState({});
    const validate = (fieldName, value) => {
        let error = '';
        if (value === '') {
            error = 'Calim is Empty';
        } else {
            error = '';
        }
        return error;
    };
    const clearValues = () => {
        setjwtValue({ claimKey: '', claimValueRegex: '' });
    };
    const handleAddToList = () => {
        const claimKeyError = validate('claimKey', jwtValue.claimKey);
        const claimValueError = validate('claimValueRegex', jwtValue.claimValueRegex);
        if (claimKeyError !== '' || claimValueError !== '') {
            Alert.error(claimValueError);
            return false;
        } else {
            let exist = false;
            if (tokenValidation.value && tokenValidation.value.body) {
                Object.entries(tokenValidation.value.body).map((entry) => {
                    if (entry.key === jwtValue.claimKey) {
                        Alert.error(<FormattedMessage
                            id='Claim.Mapping.already.exists'
                            defaultMessage='Claim Mapping Already Exist'
                        />);
                        exist = true;
                        clearValues();
                    }
                    return false;
                });
                if (!exist) {
                    tokenValidation.value.body[jwtValue.claimKey] = jwtValue.claimValueRegex;
                    setTokenValidation(tokenValidation);
                }
            } else {
                const body = {};
                body[jwtValue.claimKey] = jwtValue.claimValueRegex;
                if (tokenValidation.value) {
                    tokenValidation.value.body = body;
                }
                setTokenValidation(tokenValidation);
            }
            clearValues();
            return true;
        }
    };
    const onChange = (e) => {
        const { name, value } = e.target;
        if (name === 'type') {
            tokenValidation.type = value;
        } else if (name === 'value') {
            tokenValidation.value = value;
        }
        if (value === 'JWT') {
            tokenValidation.value = { body: {} };
        }
        setTokenValidation(tokenValidation);
    };
    const onCreateJWTmapping = (e) => {
        const newjwt = { ...jwtValue };
        newjwt[e.target.name] = e.target.value;
        setjwtValue(newjwt);
    };
    const onDelete = (claimKey) => {
        const newMapping = { ...tokenValidation.value };
        if (newMapping.body) {
            delete newMapping.body[claimKey];
        }
        setTokenValidation(newMapping);
    };
    const getValueField = (type) => {
        if (type === 'REFERENCE') {
            return (
                <TextField
                    autoFocus
                    margin='dense'
                    name='value'
                    label=''
                    fullWidth
                    required
                    variant='outlined'
                    value={tokenValidation.value}
                    onChange={onChange}
                />
            );
        } else if (type === 'JWT') {
            return (
                <Box mb={3}>
                    <TableContainer component={Paper}>
                        <Table className={classes.table} aria-label='simple table'>
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Keymanager.KeyValidation.ClaimKey'
                                            defaultMessage='Claim Key'
                                        />
                                    </TableCell>
                                    <TableCell align='right'>
                                        <FormattedMessage
                                            id='Keymanager.KeyValidation.ClaimValue.Regex'
                                            defaultMessage='Claim Value Regex'
                                        />
                                    </TableCell>
                                    <TableCell align='right'>
                                        <FormattedMessage
                                            id='Keymanager.KeyValidation.Action'
                                            defaultMessage='Action'
                                        />
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow key='Add new'>
                                    <TableCell component='th' scope='row'>
                                        <TextField
                                            name='claimKey'
                                            label={(
                                                <FormattedMessage
                                                    id='Keymanager.KeyValidation.ClaimKey'
                                                    defaultMessage='Claim Key'
                                                />
                                            )}
                                            variant='outlined'
                                            onChange={onCreateJWTmapping}
                                            value={jwtValue.claimKey}
                                        />
                                    </TableCell>
                                    <TableCell align='right'>
                                        <TextField
                                            name='claimValueRegex'
                                            label={(
                                                <FormattedMessage
                                                    id='Keymanager.KeyValidation.ClaimValue.Regex'
                                                    defaultMessage='Claim Value Regex'
                                                />
                                            )}
                                            value={jwtValue.claimValueRegex}
                                            onChange={onCreateJWTmapping}
                                            variant='outlined'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Grid>
                                            <Grid item>
                                                <IconButton
                                                    id='add'
                                                    onClick={handleAddToList}
                                                >
                                                    <AddCircleIcon />
                                                </IconButton>
                                            </Grid>
                                            <Grid item>
                                                <IconButton
                                                    id='clear'
                                                    onClick={clearValues}
                                                >
                                                    <ClearIcon />
                                                </IconButton>
                                            </Grid>
                                        </Grid>
                                    </TableCell>
                                </TableRow>
                                {tokenValidation.value.body
                    && Object.entries(tokenValidation.value.body).map(([key, value]) => (
                        <TableRow key={key}>
                            <TableCell component='th' scope='row'>
                                <TextField
                                    id={key}
                                    defaultValue={key}
                                    variant='outlined'
                                    disabled
                                />
                            </TableCell>
                            <TableCell align='right'>
                                <TextField
                                    id={value}
                                    defaultValue={value}
                                    variant='outlined'
                                    disabled
                                />
                            </TableCell>
                            <TableCell align='right'>
                                <IconButton
                                    id='delete'
                                    onClick={() => { onDelete(key); }}
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
        } else {
            return (
                <TextField
                    autoFocus
                    margin='dense'
                    name='value'
                    label='Value'
                    fullWidth
                    required
                    variant='outlined'
                    value={tokenValidation.value}
                    onChange={onChange}
                />
            );
        }
    };
    return (
        <ExpansionPanel>
            <ExpansionPanelSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls='panel1bh-content'
                id='panel1bh-header'
            >
                <Typography className={classes.heading}>
                    Token Validation
                    {' '}
                    {tokenValidation.id}
                </Typography>
            </ExpansionPanelSummary>
            <Box display='flex' flexDirection='row'>
                <FormControl variant='outlined'>
                    <Select
                        name='type'
                        value={tokenValidation.type}
                        onChange={onChange}
                        classes={{ root: classes.slectRoot }}
                    >
                        <MenuItem value='REFERENCE'>
                            <FormattedMessage id='KeyManager.KeyValidation.REFERENCE' defaultMessage='REFERENCE' />
                        </MenuItem>
                        <MenuItem value='JWT'>
                            <FormattedMessage id='KeyManager.KeyValidation.JWT' defaultMessage='JWT' />
                        </MenuItem>
                        <MenuItem value='CUSTOM'>
                            <FormattedMessage id='KeyManager.KeyValidation.CUSTOM' defaultMessage='CUSTOM' />
                        </MenuItem>
                    </Select>
                </FormControl>
            </Box>
            <Box display='flex' flexDirection='row'>
                <FormControl variant='outlined'>
                    {getValueField(tokenValidation.type)}
                </FormControl>
            </Box>
        </ExpansionPanel>
    );
}
KeyValidation.defaultProps = {
    required: false,
};
