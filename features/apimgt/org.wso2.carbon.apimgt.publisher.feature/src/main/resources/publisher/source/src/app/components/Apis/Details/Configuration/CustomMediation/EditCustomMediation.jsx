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

import React, { useState } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import AppThemes from 'Themes';
import IconButton from '@material-ui/core/IconButton';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import cloneDeep from 'lodash.clonedeep';
import Icon from '@material-ui/core/Icon';
import FormLabel from '@material-ui/core/FormLabel';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import classNames from 'classnames';

import MediatorProperties from './MediatorProperties';

function TabPanel(props) {
    const {
        children, value, index, ...other
    } = props;

    return (
        <Typography
            component='div'
            role='tabpanel'
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            <Box p={2}>{children}</Box>
        </Typography>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node.isRequired,
    index: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,

};

function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

function LinkTab(props) {
    return (
        <Tab
            component='a'
            onClick={(event) => {
                event.preventDefault();
            }}
            {...props}
        />
    );
}

const useStyles = makeStyles((theme) => ({
    paper: {
        padding: theme.spacing(1, 0),
    },
    textField: {
        width: 600,
    },
    mandatoryStar: {
        color: theme.palette.error.main,
    },
    tab: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.paper,
        height: 400,
        width: '100%',
        overflowY: 'auto',
        overflowX: 'hidden',
    },
    wrapper: {
        padding: theme.spacing(2, 0),
        display: 'flex',
    },
    input: {
        display: 'none',
    },
    mediators: {
        padding: theme.spacing(0, 4),
    },
    head: {
        color: theme.palette.common.black,
        fontSize: 14,
    },
    iconButtonOverride: {
        justifyContent: 'left',
        width: '100%',
        display: 'flex',
        borderRadius: 0,
    },
    box: {
        display: 'inline-flex',
        overflow: 'auto',
        width: '100%',
        height: 310,
        alignItems: 'center',
    },
    save: {
        paddingBottom: 10,
    },
    title: {
        flexGrow: 1,
    },
    deleteButtonOverride: {
        borderRadius: 0,
        height: 5,
        width: 2,
    },
    editButtonOverride: {
        borderRadius: 0,
        height: 5,
        width: 2,
    },
    swapButtonOverride: {
        borderRadius: 0,
        height: 5,
        width: 2,
    },
    card: {
        maxWidth: 150,
        height: 130,
    },
    swapCard: {
        maxWidth: 150,
        height: 170,
    },
    highlightedCard: {
        maxWidth: 150,
        height: 130,
        backgroundColor: theme.palette.grey['300'],
        border: 1,
        borderStyle: 'dashed',
    },
    swapHighlightedCard: {
        maxWidth: 150,
        height: 170,
        backgroundColor: theme.palette.grey['300'],
        border: 1,
        borderStyle: 'dashed',
    },
    arrowBtn: {
        justifyContent: 'center',
        height: 40,
        marginTop: 40,
    },
    clickedArrowBtn: {
        justifyContent: 'center',
        height: 40,
        marginTop: 40,
        variant: 'contained',
        color: theme.palette.primary.main,
        border: 1,
        borderStyle: 'dashed',
        borderColor: 'black',
        backgroundColor: theme.palette.grey['300'],
    },
}));

function EditCustomMediation(props) {
    const {
        intl,
    } = props;

    const classes = useStyles();
    const [value, setValue] = React.useState(0);
    const [addedMediators, setAddedMediators] = useState([]);
    const [mediationName, setMediationName] = useState(null);
    const [errors, setErrors] = useState(null);
    const [mediatorId, setMediatorId] = useState(null);
    const [mediatorLogo, setMediatorLogo] = useState(null);
    const [mediatorName, setMediatorName] = useState(null);
    const [editing, setEditing] = useState(false);
    const [mediatorArrowClicked, setMediatorArrowClicked] = useState(false);
    const [arrayWithClickedMediator, setArrayWithClickedMediator] = useState([]);
    const [arrayaWithoutClickedMediator, setArrayWithoutClickedMediators] = useState([]);
    const [clickedArrowID, setClickedArrowID] = useState(null);
    const [addedElementID, setAddedElementID] = useState(null);
    const [clickedAndAddedElementID, setClickedAndAddedElementID] = useState(null);
    const [highlightedStyle, setHighlightedStyle] = useState(false);
    const [pushedElementHighlightedStyle, setPushedElementHighlightedStyle] = useState(null);
    const [popViewShown, setPopViewShown] = useState(false);
    const [clickedMedID, setClickedMedID] = useState(null);
    const [clickedMediatorIndex, setClickedMediatorIndex] = useState(null);

    function startEditing(obj) {
        setMediatorId(obj.id);
        setMediatorLogo(obj.src);
        setMediatorName(obj.name);
        setEditing(true);
    }

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };

    const handleClick = (obj) => {
        const clickedID = obj.id;
        setClickedMedID(clickedID);
        setPopViewShown(true);
        setPushedElementHighlightedStyle(false);
        const clickedMedIndex = addedMediators.indexOf(obj);
        setClickedMediatorIndex(clickedMedIndex);
    };

    const { mediatorIcons } = AppThemes;

    // eslint-disable-next-line require-jsdoc
    function generateId() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            const r = Math.random() * 16 || 0; const
                v = c === 'x' ? r : (r && (0x3 || 0x8));
            return v.toString(16);
        });
    }
    const addMediator = (src, name) => {
        setPopViewShown(false);
        const mediatorObj = { src, id: '', name };
        if (mediatorArrowClicked === true) {
            arrayWithClickedMediator.push(mediatorObj);
            setHighlightedStyle(true);
            mediatorObj.id = generateId();
            const addedElement = arrayWithClickedMediator[arrayWithClickedMediator.length - 1];
            const addedID = addedElement.id;
            setClickedAndAddedElementID(addedID);
            const mergedArray = arrayWithClickedMediator.concat(arrayaWithoutClickedMediator);
            setAddedMediators(mergedArray);
            setMediatorArrowClicked(false);
        } else {
            const newAddedMediators = cloneDeep(addedMediators);
            mediatorObj.id = generateId();
            newAddedMediators.push(mediatorObj);
            setAddedMediators(newAddedMediators);
            setHighlightedStyle(false);
            const pushedID = newAddedMediators[newAddedMediators.length - 1].id;
            setAddedElementID(pushedID);
            setPushedElementHighlightedStyle(true);
        }
    };

    const addToSelectedMediator = (obj) => {
        setPopViewShown(false);
        setPushedElementHighlightedStyle(false);
        setMediatorArrowClicked(true);
        const arrowID = obj.id;
        setClickedArrowID(arrowID);
        const newArrayWithClickedMediator = addedMediators.slice(0, addedMediators.indexOf(obj) + 1);
        const newArrayWithoutClickedMediator = addedMediators.slice(addedMediators.indexOf(obj) + 1);
        setArrayWithClickedMediator(newArrayWithClickedMediator);
        setArrayWithoutClickedMediators(newArrayWithoutClickedMediator);
    };

    const swapWithBackElement = (obj) => {
        const clickedIndex = addedMediators.indexOf(obj);
        const swappingIndex = (addedMediators.indexOf(obj) - 1);
        if (clickedIndex > 0) {
            const swappingArray = cloneDeep(addedMediators);
            const temp = swappingArray[clickedIndex];
            swappingArray[clickedIndex] = swappingArray[swappingIndex];
            swappingArray[swappingIndex] = temp;
            setAddedMediators(swappingArray);
            setHighlightedStyle(false);
            setPopViewShown(false);
            setPushedElementHighlightedStyle(false);
        }
    };

    const swapWithFrontElement = (obj) => {
        const clickedIndex = addedMediators.indexOf(obj);
        const swappingIndex = (addedMediators.indexOf(obj) + 1);
        const lastIndex = (addedMediators.length - 1);
        if (clickedIndex !== lastIndex) {
            const swappingArray = cloneDeep(addedMediators);
            const temp = swappingArray[clickedIndex];
            swappingArray[clickedIndex] = swappingArray[swappingIndex];
            swappingArray[swappingIndex] = temp;
            setAddedMediators(swappingArray);
            setHighlightedStyle(false);
            setPopViewShown(false);
            setPushedElementHighlightedStyle(false);
        }
    };

    const deleteMediator = (id) => {
        setPopViewShown(false);
        const updatedMediators = cloneDeep(addedMediators);
        setAddedMediators(updatedMediators.filter((mediatorObj) => {
            return mediatorObj.id !== id;
        }));
        setHighlightedStyle(false);
        setPushedElementHighlightedStyle(false);
    };

    const handleNameChange = (event) => {
        const seqNames = event.target.value;
        let error = '';
        let formIsValid = true;

        if (seqNames.length === 0) {
            formIsValid = false;
            error = (intl.formatMessage({
                id: 'Apis.Details.Configuration.CustomMediation.EditCustomMediation.name.empty',
                defaultMessage: 'Mediation Sequence should not be empty.',
            }));
        } else if (typeof seqNames !== 'undefined') {
            if (!seqNames.match(/^[a-z_A-Z]+$/)) {
                formIsValid = false;
                error = (intl.formatMessage({
                    id: 'Apis.Details.Configuration.CustomMediation.EditCustomMediation.name.invalid',
                    defaultMessage: 'field name is not valid.name should be without spaces and special charectors.',
                }));
            }
        }
        setErrors(error);
        setMediationName(seqNames);
        return formIsValid;
    };

    return (

        <>
            <FormLabel component='designNewClicked' style={{ display: 'flex', marginTop: 10 }}>
                <FormattedMessage
                    id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.button.select'
                    defaultMessage='Create New Clicked'
                />
            </FormLabel>
            <Paper className={classes.paper} elevation={0}>
                <form noValidate autoComplete='off'>
                    <TextField
                        className={classes.textField}
                        autoFocus
                        id='outlined-mediation-name'
                        value={mediationName}
                        error={errors}
                        label={(
                            <>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.name'
                                    defaultMessage='Name'
                                />
                                <sup className={classes.mandatoryStar}>*</sup>
                            </>
                        )}
                        helperText={errors && `${errors}`}
                        name='mediation-name'
                        margin='normal'
                        variant='outlined'
                        onChange={handleNameChange}
                    />
                </form>
            </Paper>
            <div>
                <MediatorProperties
                    setEditing={setEditing}
                    editing={editing}
                    mediatorId={mediatorId}
                    mediatorLogo={mediatorLogo}
                    mediatorName={mediatorName}
                />
            </div>
            <Grid container className={classes.wrapper}>
                <Grid item xs={10}>
                    <Paper className={classes.tab}>
                        <AppBar position='relative' color='default'>
                            <Tabs
                                value={value}
                                onChange={handleChange}
                            >
                                <LinkTab label='Design' href='/drafts' {...a11yProps(0)} />
                                <LinkTab label='Source' href='/trash' {...a11yProps(1)} />
                            </Tabs>
                        </AppBar>

                        <TabPanel value={value} index={0}>
                            <div className={classes.box}>
                                {addedMediators.map((mediatorObj) => {
                                    return (addedMediators.length > 0)
                                        ? (
                                            <div style={{ display: 'flex' }}>
                                                {clickedAndAddedElementID === mediatorObj.id ? (
                                                    <Card
                                                        className={classNames(classes.card, {
                                                            [classes.highlightedCard]: highlightedStyle === true,
                                                            [classes.swapCard]: clickedMedID === mediatorObj.id
                                                            && popViewShown === true,
                                                            [classes.swapHighlightedCard]: highlightedStyle === true
                                                            && clickedMedID === mediatorObj.id && popViewShown === true,
                                                        })}
                                                    >
                                                        {popViewShown === true && clickedMedID === mediatorObj.id && (
                                                            <CardActions style={{ justifyContent: 'center' }}>
                                                                <IconButton
                                                                    onClick={() => swapWithBackElement(mediatorObj)}
                                                                    classes={{ root: classes.swapButtonOverride }}
                                                                    disabled={clickedMediatorIndex === 0}
                                                                >
                                                                    <Icon
                                                                        style={{ position: 'absolute' }}
                                                                    >
                                                                        swap_horizontol_circle
                                                                    </Icon>
                                                                </IconButton>
                                                                <IconButton
                                                                    onClick={() => swapWithFrontElement(mediatorObj)}
                                                                    classes={{ root: classes.swapButtonOverride }}
                                                                    disabled={
                                                                        clickedMediatorIndex
                                                                        === addedMediators.length - 1
                                                                    }
                                                                >
                                                                    <Icon
                                                                        style={{ position: 'absolute' }}
                                                                    >
                                                                        swap_horizontol_circle
                                                                    </Icon>
                                                                </IconButton>
                                                            </CardActions>
                                                        )}
                                                        <CardActionArea>
                                                            <Button onClick={() => handleClick(mediatorObj)}>
                                                                <img
                                                                    src={mediatorObj.src}
                                                                    alt={mediatorObj.name}
                                                                    style={{
                                                                        paddingRight: 10,
                                                                        height: 'fit-content',
                                                                    }}
                                                                />
                                                            </Button>
                                                        </CardActionArea>
                                                        <CardActions style={{ justifyContent: 'center' }}>
                                                            <IconButton
                                                                onClick={() => startEditing(mediatorObj)}
                                                                classes={{ root: classes.editButtonOverride }}
                                                            >
                                                                <Icon style={{ position: 'absolute' }}>edit</Icon>
                                                            </IconButton>
                                                            <IconButton
                                                                onClick={() => deleteMediator(mediatorObj.id)}
                                                                classes={{ root: classes.deleteButtonOverride }}
                                                            >
                                                                <Icon style={{ position: 'absolute' }}>delete</Icon>
                                                            </IconButton>
                                                        </CardActions>
                                                    </Card>
                                                ) : (
                                                    <Card
                                                        className={classNames(classes.card, {
                                                            [classes.swapCard]: popViewShown === true
                                                            && clickedMedID === mediatorObj.id,
                                                            [classes.highlightedCard]: addedElementID === mediatorObj.id
                                                            && pushedElementHighlightedStyle === true,
                                                            [classes.swapHighlightedCard]:
                                                            pushedElementHighlightedStyle === true
                                                            && clickedMedID === mediatorObj.id && popViewShown === true,
                                                        })}
                                                    >
                                                        {popViewShown === true && clickedMedID === mediatorObj.id && (
                                                            <CardActions style={{ justifyContent: 'center' }}>
                                                                <IconButton
                                                                    onClick={() => swapWithBackElement(mediatorObj)}
                                                                    classes={{ root: classes.swapButtonOverride }}
                                                                    disabled={clickedMediatorIndex === 0}
                                                                >
                                                                    <Icon
                                                                        style={{ position: 'absolute' }}
                                                                    >
                                                                        swap_horizontol_circle
                                                                    </Icon>
                                                                </IconButton>
                                                                <IconButton
                                                                    onClick={() => swapWithFrontElement(mediatorObj)}
                                                                    classes={{ root: classes.swapButtonOverride }}
                                                                    disabled={
                                                                        clickedMediatorIndex
                                                                        === addedMediators.length - 1
                                                                    }
                                                                >
                                                                    <Icon
                                                                        style={{ position: 'absolute' }}
                                                                    >
                                                                        swap_horizontol_circle
                                                                    </Icon>
                                                                </IconButton>
                                                            </CardActions>
                                                        )}
                                                        <CardActionArea>
                                                            <Button onClick={() => handleClick(mediatorObj)}>
                                                                <img
                                                                    src={mediatorObj.src}
                                                                    alt={mediatorObj.name}
                                                                    style={{
                                                                        paddingRight: 10,
                                                                        height: 'fit-content',
                                                                    }}
                                                                />
                                                            </Button>
                                                        </CardActionArea>
                                                        <CardActions style={{ justifyContent: 'center' }}>
                                                            <IconButton
                                                                onClick={() => startEditing(mediatorObj)}
                                                                classes={{ root: classes.editButtonOverride }}
                                                            >
                                                                <Icon style={{ position: 'absolute' }}>edit</Icon>
                                                            </IconButton>
                                                            <IconButton
                                                                onClick={() => deleteMediator(mediatorObj.id)}
                                                                classes={{ root: classes.deleteButtonOverride }}
                                                            >
                                                                <Icon style={{ position: 'absolute' }}>delete</Icon>
                                                            </IconButton>
                                                        </CardActions>
                                                    </Card>
                                                )}
                                                {clickedArrowID === mediatorObj.id ? (
                                                    <Button
                                                        onClick={() => addToSelectedMediator(mediatorObj)}
                                                        className={classNames(classes.arrowBtn, {
                                                            [classes.clickedArrowBtn]: mediatorArrowClicked === true,
                                                        })}
                                                    >
                                                        <Icon
                                                            style={{
                                                                paddingRight: 10,
                                                                fontSize: 60,
                                                                height: 'fit-content',
                                                                position: 'absolute',
                                                            }}
                                                        >
                                                            arrow_right_alt
                                                        </Icon>
                                                    </Button>
                                                ) : (
                                                    <Button
                                                        onClick={() => addToSelectedMediator(mediatorObj)}
                                                        className={classes.arrowBtn}
                                                    >
                                                        <Icon
                                                            style={{
                                                                paddingRight: 10,
                                                                fontSize: 60,
                                                                height: 'fit-content',
                                                                position: 'absolute',
                                                            }}
                                                        >
                                                            arrow_right_alt
                                                        </Icon>
                                                    </Button>
                                                )}
                                            </div>
                                        )
                                        : {};
                                })}
                            </div>
                        </TabPanel>
                        <TabPanel value={value} index={1}>
                                Source
                        </TabPanel>
                    </Paper>
                </Grid>
                <Grid item xs={2} className={classes.mediators}>
                    <Paper className={classes.tab}>
                        <Table stickyHeader>
                            <TableHead>
                                <TableRow>
                                    <TableCell align='center' className={classes.head}> Mediators</TableCell>
                                </TableRow>
                            </TableHead>

                            <TableBody>
                                {mediatorIcons.map((mediator) => {
                                    return (
                                        <TableRow>
                                            <TableCell component='th' align='left' style={{ padding: 0 }}>
                                                <IconButton
                                                    onClick={() => addMediator(mediator.src2, mediator.name)}
                                                    classes={{ root: classes.iconButtonOverride }}
                                                >
                                                    <img
                                                        src={mediator.src1}
                                                        alt={mediator.name}
                                                        style={{ paddingRight: 10 }}
                                                    />
                                                    <Typography>
                                                        { mediator.name }
                                                    </Typography>
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </Paper>
                </Grid>
            </Grid>
            <div className={classes.save}>
                <Button
                    color='primary'
                    variant='contained'
                    className={classes.save}
                >
                    <FormattedMessage
                        id='Apis.Details.Configuration.CustomMediation.EditCustomMediation.save.btn'
                        defaultMessage='Save'
                    />
                </Button>
            </div>
        </>
    );
}

EditCustomMediation.propTypes = {
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl((EditCustomMediation));
