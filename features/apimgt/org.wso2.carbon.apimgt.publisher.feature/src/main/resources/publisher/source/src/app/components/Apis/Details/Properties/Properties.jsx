/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/* eslint no-param-reassign: ["error", { "props": false }] */
/* eslint-disable react/jsx-no-bind */

import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import cloneDeep from 'lodash.clonedeep';
import isEmpty from 'lodash.isempty';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import AddCircle from '@material-ui/icons/AddCircle';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { FormattedMessage, injectIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import Checkbox from '@material-ui/core/Checkbox';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Box from '@material-ui/core/Box';
import APIContext, { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import API from 'AppData/api.js';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import { isRestricted } from 'AppData/AuthManager';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Configurations from 'Config';
import EditableRow from './EditableRow';

const propertyDisplaySuffix = Configurations.app.propertyDisplaySuffix || '__display';
const useStyles = makeStyles((theme) => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
        maxWidth: theme.custom.contentAreaWidth,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    button: {
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    buttonWrapper: {
        paddingTop: theme.spacing(3),
    },
    paperRoot: {
        padding: theme.spacing(3),
        marginTop: theme.spacing(3),
    },
    addNewHeader: {
        padding: theme.spacing(2),
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing(2),
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing(2),
    },
    addProperty: {
        marginRight: theme.spacing(2),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    link: {
        cursor: 'pointer',
    },
    messageBox: {
        marginTop: 20,
    },
    actions: {
        padding: '20px 0',
        '& button': {
            marginLeft: 0,
        },
    },
    head: {
        fontWeight: 200,
        marginBottom: 20,
    },
    marginRight: {
        marginRight: theme.spacing(1),
    },
    helpText: {
        paddingTop: theme.spacing(1),
    },
    checkBoxStyles: {
        whiteSpace: 'nowrap',
        marginLeft: 10,
    },
    tableHead: {
        fontWeight: 600,
    },
    table: {
        '& th': {
            fontWeight: 600,
        },
    },
}));

/**
 *
 *
 * @class Properties
 * @extends {React.Component}
 */
function Properties(props) {
    /**
     * @inheritdoc
     * @param {*} props properties
     */
    const { intl } = props;
    const classes = useStyles();
    const { api, updateAPI } = useContext(APIContext);
    const additionalPropertiesTemp = cloneDeep(api.additionalProperties);

    if (Object.prototype.hasOwnProperty.call(additionalPropertiesTemp, 'github_repo')) {
        delete additionalPropertiesTemp.github_repo;
    }
    if (Object.prototype.hasOwnProperty.call(additionalPropertiesTemp, 'slack_url')) {
        delete additionalPropertiesTemp.slack_url;
    }

    const [additionalProperties, setAdditionalProperties] = useState(additionalPropertiesTemp);
    const [showAddProperty, setShowAddProperty] = useState(false);
    const [propertyKey, setPropertyKey] = useState(null);
    const [propertyValue, setPropertyValue] = useState(null);
    const [updating, setUpdating] = useState(false);
    const [editing, setEditing] = useState(false);
    const [isAdditionalPropertiesStale, setIsAdditionalPropertiesStale] = useState(false);
    const [isVisibleInStore, setIsVisibleInStore] = useState(false);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    const keywords = ['provider', 'version', 'context', 'status', 'description',
        'subcontext', 'doc', 'lcstate', 'name', 'tags'];

    const toggleAddProperty = () => {
        setShowAddProperty(!showAddProperty);
    };
    const handleChange = (name) => (event) => {
        const { value } = event.target;
        if (name === 'propertyKey') {
            setPropertyKey(isVisibleInStore ? value + propertyDisplaySuffix : value);
        } else if (name === 'propertyValue') {
            setPropertyValue(value);
        }
    };

    /**
     *
     *
     * @param {*} itemValue
     * @returns
     * @memberof Properties
     */
    const validateEmpty = function (itemValue) {
        if (itemValue === null) {
            return false;
        } else if (!isVisibleInStore && itemValue === '') {
            return true;
        } else if (isVisibleInStore && itemValue.replace(propertyDisplaySuffix, '') === '') {
            return true;
        } else {
            return false;
        }
    };

    const isKeyword = (itemValue) => {
        if (itemValue === null) {
            return false;
        }
        return keywords.includes(itemValue.toLowerCase());
    };
    const hasWhiteSpace = (itemValue) => {
        if (itemValue === null) {
            return false;
        }
        const whitespaceChars = [' ', '\t', '\n'];
        return Array.from(itemValue).some((char) => whitespaceChars.includes(char));
    };
    /**
     *
     *
     * @param {*} oldAPI
     * @param {*} updateAPI
     * @memberof Properties
     */
    const handleSubmit = () => {
        setUpdating(true);
        if (Object.prototype.hasOwnProperty.call(additionalPropertiesTemp, 'github_repo')) {
            additionalProperties.github_repo = api.additionalProperties.github_repo;
        }
        if (Object.prototype.hasOwnProperty.call(additionalPropertiesTemp, 'slack_url')) {
            additionalProperties.slack_url = api.additionalProperties.slack_url;
        }
        const updatePromise = updateAPI({ additionalProperties });
        updatePromise
            .then(() => {
                setUpdating(false);
            })
            .catch((error) => {
                setUpdating(false);
                if (process.env.NODE_ENV !== 'production') console.log(error);
                const { status } = error;
                if (status === 401) {
                    doRedirectToLogin();
                }
            });
    };

    /**
     *
     *
     * @param {*} apiAdditionalProperties
     * @param {*} oldKey
     * @memberof Properties
     */
    const handleDelete = (apiAdditionalProperties, oldKey) => {
        const additionalPropertiesCopy = JSON.parse(JSON.stringify(additionalProperties));

        if (Object.prototype.hasOwnProperty.call(additionalPropertiesCopy, oldKey)) {
            delete additionalPropertiesCopy[oldKey];
        }
        setAdditionalProperties(additionalPropertiesCopy);

        if (additionalPropertiesCopy !== additionalProperties) {
            setIsAdditionalPropertiesStale(true);
        }
    };
    const validateBeforeAdd = (fieldKey, fieldValue, additionalPropertiesCopy, action = 'add') => {
        if ((additionalPropertiesCopy[fieldKey] != null || additionalPropertiesCopy[fieldKey + propertyDisplaySuffix]
            != null) && action === 'add') {
            Alert.warning(intl.formatMessage({
                id: `Apis.Details.Properties.Properties.
                    property.name.exists`,
                defaultMessage: 'Property name already exists',
            }));
            return false;
        } else if (validateEmpty(fieldKey) || validateEmpty(fieldValue)) {
            Alert.warning(intl.formatMessage({
                id: `Apis.Details.Properties.Properties.
                    property.name.empty.error`,
                defaultMessage: 'Property name/value can not be empty',
            }));
            return false;
        } else if (isKeyword(fieldKey)) {
            Alert.warning(intl.formatMessage({
                id:
                `Apis.Details.Properties.Properties.
                    property.name.keyword.error`,
                defaultMessage:
                'Property name can not be a system reserved keyword',
            }));
            return false;
        } else if (hasWhiteSpace(fieldKey)) {
            Alert.warning(intl.formatMessage({
                id:
                    `Apis.Details.Properties.Properties.
                    property.name.has.whitespaces`,
                defaultMessage:
                    'Property name can not have any whitespaces in it',
            }));
            return false;
        } else {
            return true;
        }
    };
    /**
     *
     *
     * @param {*} apiAdditionalProperties
     * @param {*} oldRow
     * @param {*} newRow
     * @memberof Properties
     */
    const handleUpdateList = (oldRow, newRow) => {
        // const additionalPropertiesCopy = JSON.parse(JSON.stringify(additionalProperties));

        const { oldKey, oldValue } = oldRow;
        const { newKey, newValue } = newRow;
        if (oldKey === newKey && oldValue === newValue) {
            Alert.warning(intl.formatMessage({
                id: `Apis.Details.Properties.Properties.
                    no.changes.to.save`,
                defaultMessage: 'No changes to save',
            }));
            return false;
        }
        if (!validateBeforeAdd(newKey, newValue, additionalProperties, 'update')) {
            return false;
        }

        if (Object.prototype.hasOwnProperty.call(additionalProperties, newKey) && oldKey === newKey) {
            // Only the value is updated
            if (newValue && oldValue !== newValue) {
                additionalProperties[oldKey] = newValue;
            }
        } else {
            delete additionalProperties[oldKey];
            additionalProperties[newKey] = newValue;
        }
        setAdditionalProperties(additionalProperties);
        return true;
    };
    /**
     *
     *
     * @param {*} apiAdditionalProperties
     * @memberof Properties
     */
    const handleAddToList = () => {
        const additionalPropertiesCopy = JSON.parse(JSON.stringify(additionalProperties));
        if (validateBeforeAdd(propertyKey, propertyValue, additionalPropertiesCopy, 'add')) {
            additionalPropertiesCopy[propertyKey] = propertyValue;
            setAdditionalProperties(additionalPropertiesCopy);
            setPropertyKey(null);
            setPropertyValue(null);
        }
    };

    /**
     *
     *
     * @memberof Properties
     */
    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            handleAddToList();
        }
    };

    const handleChangeVisibleInStore = (event) => {
        if (event.target.checked) {
            setPropertyKey(propertyKey + propertyDisplaySuffix);
        } else {
            setPropertyKey(propertyKey.indexOf(propertyDisplaySuffix) !== -1
                ? propertyKey.replace(propertyDisplaySuffix, '')
                : propertyKey);
        }
        setIsVisibleInStore(event.target.checked);
    };
    /**
     *
     *
     * @param {*} additionalProperties
     * @param {*} apiAdditionalProperties
     * @returns
     * @memberof Properties
     */
    const renderAdditionalProperties = () => {
        const items = [];
        for (const key in additionalProperties) {
            if (Object.prototype.hasOwnProperty.call(additionalProperties, key)) {
                items.push(<EditableRow
                    oldKey={key}
                    oldValue={additionalProperties[key]}
                    handleUpdateList={handleUpdateList}
                    handleDelete={handleDelete}
                    apiAdditionalProperties={additionalProperties}
                    {...props}
                    setEditing={setEditing}
                    isRestricted={isRestricted}
                    api={api}
                    validateEmpty={validateEmpty}
                    isKeyword={isKeyword}
                />);
            }
        }
        return items;
    };
    const getKeyValue = () => {
        if (propertyKey === null) {
            return '';
        } else if (propertyKey.indexOf(propertyDisplaySuffix) !== -1) {
            return propertyKey.replace(propertyDisplaySuffix, '');
        } else {
            return propertyKey;
        }
    };
    /**
     *
     *
     * @returns
     * @memberof Properties
     */
    return (
        <>
            <div className={classes.titleWrapper}>
                {api.apiType === API.CONSTS.APIProduct
                    ? (
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.Properties.Properties.api.product.properties'
                                defaultMessage='API Properties'
                            />
                        </Typography>
                    )
                    : (
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='Apis.Details.Properties.Properties.api.properties'
                                defaultMessage='API Properties'
                            />
                        </Typography>
                    )}

                {(!isEmpty(additionalProperties) || showAddProperty) && (
                    <Button
                        size='small'
                        className={classes.button}
                        onClick={toggleAddProperty}
                        disabled={showAddProperty || isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                    >
                        <AddCircle className={classes.buttonIcon} />
                        <FormattedMessage
                            id='Apis.Details.Properties.Properties.add.new.property'
                            defaultMessage='Add New Property'
                        />
                    </Button>
                )}
            </div>
            <Typography variant='caption' component='div' className={classes.helpText}>
                <FormattedMessage
                    id='Apis.Details.Properties.Properties.help.main'
                    defaultMessage={`Usually, APIs have a pre-defined set of properties such as 
                        the name, version, context, etc. API Properties allows you to 
                         add specific custom properties to the API.`}
                />
            </Typography>
            {isEmpty(additionalProperties) && !isAdditionalPropertiesStale && !showAddProperty && (
                <div className={classes.messageBox}>
                    <InlineMessage type='info' height={140}>
                        <div className={classes.contentWrapper}>
                            <Typography variant='h5' component='h3' className={classes.head}>
                                <FormattedMessage
                                    id='Apis.Details.Properties.Properties.add.new.property.message.title'
                                    defaultMessage='Create Additional Properties'
                                />
                            </Typography>
                            {api.apiType === API.CONSTS.APIProduct
                                ? (
                                    <Typography component='p' className={classes.content}>
                                        <FormattedMessage
                                            id='Apis.Details.Properties.Properties.APIProduct.
                                            add.new.property.message.content'
                                            defaultMessage={
                                                'Add specific custom properties to your '
                                        + 'API here.'
                                            }
                                        />
                                    </Typography>
                                )
                                : (
                                    <Typography component='p' className={classes.content}>
                                        <FormattedMessage
                                            id='Apis.Details.Properties.Properties.add.new.property.message.content'
                                            defaultMessage={
                                                'Add specific custom properties to your '
                                        + 'API here.'
                                            }
                                        />
                                    </Typography>
                                )}
                            <div className={classes.actions}>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    className={classes.button}
                                    onClick={toggleAddProperty}
                                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], api)}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Properties.Properties.add.new.property'
                                        defaultMessage='Add New Property'
                                    />
                                </Button>
                            </div>
                        </div>
                    </InlineMessage>
                </div>
            )}
            {(!isEmpty(additionalProperties) || showAddProperty || isAdditionalPropertiesStale) && (
                <Grid container spacing={7}>
                    <Grid item xs={12}>
                        <Paper className={classes.paperRoot}>
                            <Table className={classes.table}>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>
                                            <FormattedMessage
                                                id='Apis.Details.Properties.Properties.add.new.property.table'
                                                defaultMessage='Property Name'
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <FormattedMessage
                                                id='Apis.Details.Properties.Properties.add.new.property.value'
                                                defaultMessage='Property Value'
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <FormattedMessage
                                                id='Apis.Details.Properties.Properties.add.new.property.visibility'
                                                defaultMessage='Visibility'
                                            />
                                        </TableCell>
                                        <TableCell />
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {showAddProperty && (
                                        <>
                                            <TableRow>
                                                <TableCell>
                                                    <TextField
                                                        fullWidth
                                                        required
                                                        id='outlined-required'
                                                        label={intl.formatMessage({
                                                            id: `Apis.Details.Properties.Properties.
                                                                show.add.property.property.name`,
                                                            defaultMessage: 'Name',
                                                        })}
                                                        margin='dense'
                                                        variant='outlined'
                                                        className={classes.addProperty}
                                                        value={getKeyValue()}
                                                        onChange={handleChange('propertyKey')}
                                                        onKeyDown={handleKeyDown('propertyKey')}
                                                        helperText={validateEmpty(propertyKey) ? ''
                                                            : iff((isKeyword(propertyKey)
                                                                || hasWhiteSpace(propertyKey)), intl.formatMessage({
                                                                id: `Apis.Details.Properties.Properties.
                                                                    show.add.property.invalid.error`,
                                                                defaultMessage: 'Invalid property name',
                                                            }), '')}
                                                        error={validateEmpty(propertyKey) || isKeyword(propertyKey)
                                                        || hasWhiteSpace(propertyKey)}
                                                        disabled={isRestricted(
                                                            ['apim:api_create', 'apim:api_publish'],
                                                            api,
                                                        )}
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <TextField
                                                        fullWidth
                                                        required
                                                        id='outlined-required'
                                                        label={intl.formatMessage({
                                                            id: 'Apis.Details.Properties.Properties.property.value',
                                                            defaultMessage: 'Value',
                                                        })}
                                                        margin='dense'
                                                        variant='outlined'
                                                        className={classes.addProperty}
                                                        value={propertyValue === null ? '' : propertyValue}
                                                        onChange={handleChange('propertyValue')}
                                                        onKeyDown={handleKeyDown('propertyValue')}
                                                        error={validateEmpty(propertyValue)}
                                                        disabled={isRestricted(
                                                            ['apim:api_create', 'apim:api_publish'],
                                                            api,
                                                        )}
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormControlLabel
                                                        control={(
                                                            <Checkbox
                                                                checked={isVisibleInStore}
                                                                onChange={handleChangeVisibleInStore}
                                                                name='checkedB'
                                                                color='primary'
                                                            />
                                                        )}
                                                        label={intl.formatMessage({
                                                            id: `Apis.Details.Properties.
                                                            Properties.editable.show.in.devporal`,
                                                            defaultMessage: 'Show in devportal',
                                                        })}
                                                        className={classes.checkBoxStyles}
                                                    />
                                                </TableCell>
                                                <TableCell align='right'>
                                                    <Box display='flex'>
                                                        <Button
                                                            variant='contained'
                                                            color='primary'
                                                            disabled={
                                                                !propertyValue
                                                            || !propertyKey
                                                            || isRestricted(
                                                                ['apim:api_create', 'apim:api_publish'], api,
                                                            )
                                                            }
                                                            onClick={handleAddToList}
                                                            className={classes.marginRight}
                                                        >
                                                            <Typography variant='caption' component='div'>
                                                                <FormattedMessage
                                                                    id='Apis.Details.Properties.Properties.add'
                                                                    defaultMessage='Add'
                                                                />
                                                            </Typography>
                                                        </Button>

                                                        <Button onClick={toggleAddProperty}>
                                                            <Typography variant='caption' component='div'>
                                                                <FormattedMessage
                                                                    id='Apis.Details.Properties.Properties.cancel'
                                                                    defaultMessage='Cancel'
                                                                />
                                                            </Typography>
                                                        </Button>
                                                    </Box>
                                                </TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell colSpan={4}>
                                                    <Typography variant='caption'>
                                                        <FormattedMessage
                                                            id='Apis.Details.Properties.Properties.help'
                                                            defaultMessage={
                                                                'Property name should be unique, should not contain '
                                                                + 'spaces and cannot be any '
                                                                + 'of the following reserved keywords : '
                                                                + 'provider, version, context, status, description, '
                                                                + 'subcontext, doc, lcState, name, tags.'
                                                            }
                                                        />
                                                    </Typography>
                                                </TableCell>
                                            </TableRow>
                                        </>
                                    )}
                                    {renderAdditionalProperties()}
                                </TableBody>
                            </Table>
                        </Paper>
                        <div className={classes.buttonWrapper}>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={1}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    <div>
                                        <Button
                                            variant='contained'
                                            color='primary'
                                            onClick={handleSubmit}
                                            disabled={
                                                editing || updating || (isEmpty(additionalProperties)
                                                && !isAdditionalPropertiesStale)
                                                || isRestricted(['apim:api_create', 'apim:api_publish'], api)
                                            }
                                        >
                                            {updating && (
                                                <>
                                                    <CircularProgress size={20} />
                                                    <FormattedMessage
                                                        id='Apis.Details.Properties.Properties.updating'
                                                        defaultMessage='Updating ...'
                                                    />
                                                </>
                                            )}
                                            {!updating && (
                                                <FormattedMessage
                                                    id='Apis.Details.Properties.Properties.save'
                                                    defaultMessage='Save'
                                                />
                                            )}
                                        </Button>
                                    </div>
                                </Grid>
                                <Grid item>
                                    <Link to={'/apis/' + api.id + '/overview'}>
                                        <Button>
                                            <FormattedMessage
                                                id='Apis.Details.Properties.Properties.cancel'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    </Link>
                                </Grid>
                                {isRestricted(['apim:api_create', 'apim:api_publish'], api) && (
                                    <Grid item xs={12}>
                                        <Typography variant='body2' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Details.Properties.Properties.update.not.allowed'
                                                defaultMessage='*You are not authorized to update properties of
                                                    the API due to insufficient permissions'
                                            />
                                        </Typography>
                                    </Grid>
                                )}
                            </Grid>
                        </div>
                    </Grid>
                </Grid>
            )}
        </>
    );
}

Properties.propTypes = {
    state: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};
export default withAPI(injectIntl(Properties));
