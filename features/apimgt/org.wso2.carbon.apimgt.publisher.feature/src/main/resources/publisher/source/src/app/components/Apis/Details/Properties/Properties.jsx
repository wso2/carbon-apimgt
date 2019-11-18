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
import APIContext, { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import { isRestricted } from 'AppData/AuthManager';
import Alert from 'AppComponents/Shared/Alert';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import EditableRow from './EditableRow';

const useStyles = makeStyles(theme => ({
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
    const { api, updateAPI } = useContext(APIContext);
    const additionalPropertiesTemp = cloneDeep(api.additionalProperties);

    const [additionalProperties, setAdditionalProperties] = useState(additionalPropertiesTemp);
    const [showAddProperty, setShowAddProperty] = useState(false);
    const [propertyKey, setPropertyKey] = useState(null);
    const [propertyValue, setPropertyValue] = useState(null);
    const [updating, setUpdating] = useState(false);
    const [editing, setEditing] = useState(false);
    const [isAdditionalPropertiesStale, setIsAdditionalPropertiesStale] = useState(false);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    let isKeyWord = false;
    const keywords = ['provider', 'version', 'context', 'status', 'description',
        'subcontext', 'doc', 'lcState', 'name', 'tags'];
    if (keywords.includes(propertyKey)) {
        isKeyWord = true;
    } else {
        isKeyWord = false;
    }

    const toggleAddProperty = () => {
        setShowAddProperty(!showAddProperty);
    };
    const handleChange = name => (event) => {
        const { value } = event.target;
        if (name === 'propertyKey') {
            setPropertyKey(value);
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
    const validateEmpty = (itemValue) => {
        if (itemValue === null) {
            return false;
        } else if (itemValue === '') {
            return true;
        } else {
            return false;
        }
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

    /**
     *
     *
     * @param {*} apiAdditionalProperties
     * @param {*} oldRow
     * @param {*} newRow
     * @memberof Properties
     */
    const handleUpdateList = (oldRow, newRow) => {
        const additionalPropertiesCopy = JSON.parse(JSON.stringify(additionalProperties));

        const { oldKey, oldValue } = oldRow;
        const { newKey, newValue } = newRow;

        if (Object.prototype.hasOwnProperty.call(additionalPropertiesCopy, newKey) && oldKey === newKey) {
            // Only the value is updated
            if (newValue && oldValue !== newValue) {
                additionalPropertiesCopy[oldKey] = newValue;
            }
        } else {
            delete additionalPropertiesCopy[oldKey];
            additionalPropertiesCopy[newKey] = newValue;
        }
        setAdditionalProperties(additionalPropertiesCopy);
    };

    /**
     *
     *
     * @param {*} apiAdditionalProperties
     * @memberof Properties
     */
    const handleAddToList = () => {
        const additionalPropertiesCopy = JSON.parse(JSON.stringify(additionalProperties));
        if (additionalPropertiesCopy[propertyKey] != null) {
            Alert.warning('Property name already exists');
        } else {
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
                />);
            }
        }
        return items;
    };

    /**
     *
     *
     * @returns
     * @memberof Properties
     */
    const { intl } = props;
    const classes = useStyles();

    return (
        <React.Fragment>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.Properties.Properties.api.properties'
                        defaultMessage='API Properties'
                    />
                </Typography>
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
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Properties.Properties.add.new.property.message.content'
                                    defaultMessage={
                                        'Add specific custom properties to your ' +
                                        'API here.'
                                    }
                                />
                            </Typography>
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
                                        <TableCell />
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {showAddProperty && (
                                        <React.Fragment>
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
                                                        margin='normal'
                                                        variant='outlined'
                                                        className={classes.addProperty}
                                                        value={propertyKey === null ? '' : propertyKey}
                                                        onChange={handleChange('propertyKey')}
                                                        onKeyDown={handleKeyDown('propertyKey')}
                                                        helperText={validateEmpty(propertyKey) ? '' :
                                                            iff(isKeyWord, 'Invalid property name', '')}
                                                        error={validateEmpty(propertyKey) || isKeyWord}
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
                                                        margin='normal'
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
                                                <TableCell align='right'>
                                                    <Button
                                                        variant='contained'
                                                        color='primary'
                                                        disabled={
                                                            !propertyValue ||
                                                            !propertyKey ||
                                                            isRestricted(['apim:api_create', 'apim:api_publish'], api)
                                                            || isKeyWord
                                                        }
                                                        onClick={handleAddToList}
                                                        className={classes.marginRight}
                                                    >
                                                        <FormattedMessage
                                                            id='Apis.Details.Properties.Properties.add'
                                                            defaultMessage='Add'
                                                        />
                                                    </Button>

                                                    <Button onClick={toggleAddProperty}>
                                                        <FormattedMessage
                                                            id='Apis.Details.Properties.Properties.cancel'
                                                            defaultMessage='Cancel'
                                                        />
                                                    </Button>
                                                </TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell colSpan={3}>
                                                    <Typography variant='caption'>
                                                        <FormattedMessage
                                                            id='Apis.Details.Properties.Properties.help'
                                                            defaultMessage={
                                                                'Property name should be unique, should not contain ' +
                                                                'spaces and cannot be any ' +
                                                                'of the following reserved keywords : ' +
                                                                'provider, version, context, status, description, ' +
                                                                'subcontext, doc, lcState, name, tags.'
                                                            }
                                                        />
                                                    </Typography>
                                                </TableCell>
                                            </TableRow>
                                        </React.Fragment>
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
                                                editing || updating || (isEmpty(additionalProperties) &&
                                                !isAdditionalPropertiesStale)
                                                || isRestricted(['apim:api_create', 'apim:api_publish'], api)
                                            }
                                        >
                                            {updating && (
                                                <React.Fragment>
                                                    <CircularProgress size={20} />
                                                    <FormattedMessage
                                                        id='Apis.Details.Properties.Properties.updating'
                                                        defaultMessage='Updating ...'
                                                    />
                                                </React.Fragment>
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
        </React.Fragment>
    );
}

Properties.propTypes = {
    state: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};
export default withAPI(injectIntl(Properties));
