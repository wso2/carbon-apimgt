import React, { useState } from 'react';
import TextField from '@material-ui/core/TextField';
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Typography from '@material-ui/core/Typography';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableBody from '@material-ui/core/TableBody';
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
    outlined: {
        padding: '11px 14px',
    },
    textField: {
        marginTop: '14px !important',
    },
    formControlSelect: {
        width: 200,
        marginRight: 20,
        paddingLeft: 14,
    },
    labelRoot: {
        position: 'relative',
    },
    table: {
        margin: 10,
        '& tr td, & tr th': {
            padding: 5,
            margin: 0,
        },
    },
    acitonColumn: {
        width: 50,
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
    const [preValues, setPreValues] = useState({
        JWT: { body: {} },
        REFERENCE: '',
        CUSTOM: '',
    });
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
                            defaultMessage='Claim Mapping already exists.'
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
            const newPreValues = { ...preValues };
            newPreValues[tokenValidation.type] = tokenValidation.value;
            setPreValues(newPreValues);
            tokenValidation.type = value;
            if (tokenValidation.type === 'JWT') {
                tokenValidation.value = preValues[value];
            } else {
                tokenValidation.value = '';
            }
        } else if (name === 'value') {
            tokenValidation.value = value;
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
    return (
        <>
            <Box display='flex' flexDirection='row' mt={2}>
                <FormControl variant='outlined' className={classes.formControlSelect}>
                    <InputLabel classes={{ root: classes.labelRoot }}>
                        <FormattedMessage
                            id='KeyManager.KeyValidation.token.validation.type'
                            defaultMessage='Type'
                        />
                    </InputLabel>
                    <Select
                        name='type'
                        value={tokenValidation.type}
                        onChange={onChange}
                        classes={{ outlined: classes.outlined }}
                        fullWidth
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
                { (tokenValidation.type === 'REFERENCE' || tokenValidation.type === 'CUSTOM') && (
                    <TextField
                        margin='dense'
                        name='value'
                        label=''
                        fullWidth
                        required
                        variant='outlined'
                        value={tokenValidation.value}
                        classes={{ root: classes.textField }}
                        onChange={onChange}
                    />
                )}
            </Box>
            <Box display='flex' flexDirection='row'>
                { (tokenValidation.type === 'JWT') && (
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
                            <TableRow>
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
                                        margin='dense'
                                        fullWidth
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
                                        margin='dense'
                                        fullWidth
                                    />
                                </TableCell>
                                <TableCell className={classes.acitonColumn}>
                                    <Box display='flex'>
                                        <IconButton
                                            id='delete'
                                            aria-label={(
                                                <FormattedMessage
                                                    id='Keymanager.Local.Claim.remove'
                                                    defaultMessage='Remove'
                                                />
                                            )}
                                            onClick={handleAddToList}
                                        >
                                            <AddCircleIcon />
                                        </IconButton>
                                        <IconButton
                                            id='delete'
                                            aria-label='Clear'
                                            onClick={clearValues}
                                        >
                                            <ClearIcon />
                                        </IconButton>
                                    </Box>
                                </TableCell>
                            </TableRow>
                            {tokenValidation.value.body
                                    && Object.entries(tokenValidation.value.body).map(([key, value]) => (
                                        <TableRow key={key}>
                                            <TableCell component='th' scope='row'>
                                                <Typography variant='body1' gutterBottom>
                                                    {key}
                                                </Typography>

                                            </TableCell>
                                            <TableCell align='right'>
                                                <Typography variant='body1' gutterBottom>
                                                    {value}
                                                </Typography>

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
                )}
            </Box>
        </>
    );
}
KeyValidation.defaultProps = {
    required: false,
};
