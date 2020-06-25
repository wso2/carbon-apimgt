import React, { useState } from 'react';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import DropZoneLocal from 'AppComponents/Shared/DropZoneLocal';
import { FormattedMessage, useIntl } from 'react-intl';
import SecurityIcon from '@material-ui/icons/Security';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Alert from 'AppComponents/Shared/Alert';

const cache = {};
/**
 * Renders the cetificate add/edit page.
 * @param {JSON} props Input props form parent components.
 * @returns {JSX} Cetificate upload/add UI.
 */
export default function Cetificates(props) {
    const intl = useIntl();
    const { certificates: { type, value }, dispatch } = props;
    const [selectedTab, setSelectedTab] = useState(0);
    const [file, setFile] = useState(null);
    cache[type] = value;

    const onDrop = (acceptedFile) => {
        const reader = new FileReader();
        setFile(acceptedFile[0]);
        reader.readAsText(acceptedFile[0], 'UTF-8');
        reader.onload = (evt) => {
            dispatch({
                field: 'certificates',
                value: {
                    type,
                    value: btoa(evt.target.result),
                },
            });
        };
        reader.onerror = () => {
            Alert.success(intl.formatMessage({
                id: 'KeyManagers.Cetificates.file.error',
                defaultMessage: 'Error reading file',
            }));
        };
    };

    const handleChange = (event) => {
        const { value: selected, name } = event.target;
        if (name === 'cetificateType') {
            dispatch({
                field: 'certificates',
                value: {
                    type: selected,
                    value: cache[selected],
                },
            });
        } else {
            dispatch({
                field: 'certificates',
                value: {
                    type,
                    value: name === 'cetificateValueUrl' ? selected : btoa(selected),
                },
            });
        }
    };
    const handleTabChange = (event, newValue) => {
        setSelectedTab(newValue);
    };
    return (
        <>
            <FormControl component='fieldset'>
                <RadioGroup
                    style={{ flexDirection: 'row' }}
                    aria-label='cetificate'
                    name='cetificateType'
                    value={type}
                    onChange={handleChange}
                >
                    <FormControlLabel value='PEM' control={<Radio />} label='PEM' />
                    <FormControlLabel value='JWKS' control={<Radio />} label='JWKS' />
                </RadioGroup>
            </FormControl>
            {type === 'JWKS' && (
                <TextField
                    label={intl.formatMessage(
                        {
                            id: 'KeyManagers.Cetificates.jwks.url',
                            defaultMessage: 'URL',
                        },
                    )}
                    fullWidth
                    variant='outlined'
                    value={value}
                    name='cetificateValueUrl'
                    onChange={handleChange}
                />
            )}
            {type === 'PEM' && (
                <>
                    <AppBar position='static' color='default'>
                        <Tabs value={selectedTab} onChange={handleTabChange}>
                            <Tab label='Paste' />
                            <Tab label='Upload' />
                        </Tabs>
                    </AppBar>
                    {selectedTab === 0
                        ? (
                            <TextField
                                label={intl.formatMessage(
                                    {
                                        id: 'KeyManagers.Cetificates.paste.label',
                                        defaultMessage: 'Paste the content of the PEM file',
                                    },
                                )}
                                multiline
                                fullWidth
                                rows={6}
                                variant='outlined'
                                value={atob(value)}
                                name='cetificateValue'
                                onChange={handleChange}
                            />
                        ) : (
                            <>
                                {(file && file.name) && (
                                    <Box m={1} display='flex' flexDirection='row' alignItems='center'>
                                        <SecurityIcon />
                                        <Box flex='1'>{file.name}</Box>
                                        <Typography variant='caption'>
                                            <FormattedMessage
                                                id='KeyManagers.Cetificates.override.message'
                                                defaultMessage='Upload new file to override the current certificate'
                                            />
                                        </Typography>
                                    </Box>
                                )}
                                <DropZoneLocal
                                    onDrop={onDrop}
                                    files={value && value.name}
                                    accept='.pem'
                                    baseStyle={{ padding: '16px 20px' }}
                                >
                                    <FormattedMessage
                                        id='KeyManagers.Cetificates.drag.and.drop.message'
                                        defaultMessage='Drag and Drop files here {break} or {break}'
                                        values={{ break: <br /> }}
                                    />
                                    <Button
                                        color='default'
                                        variant='contained'
                                    >
                                        <FormattedMessage
                                            id='KeyManagers.Cetificates.browse.files.to.upload'
                                            defaultMessage='Browse File to Upload'
                                        />
                                    </Button>
                                </DropZoneLocal>
                            </>
                        )}
                </>
            )}

        </>
    );
}
Cetificates.propTypes = {
    certificates: PropTypes.shape({}).isRequired,
    dispatch: PropTypes.func.isRequired,
};
