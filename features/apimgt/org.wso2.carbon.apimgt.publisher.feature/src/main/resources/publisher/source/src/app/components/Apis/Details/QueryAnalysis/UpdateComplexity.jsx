/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TextField from '@material-ui/core/TextField';
import TableCell from '@material-ui/core/TableCell';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import TableRow from '@material-ui/core/TableRow';
import PropTypes from 'prop-types';
import TableBody from '@material-ui/core/TableBody';
import TableHead from '@material-ui/core/TableHead';
import Box from '@material-ui/core/Box';

const useStyles = makeStyles((theme) => ({
    searchWrapper: {
        width: '100%',
        marginBottom: theme.spacing(2),
    },
}));

/**
 *
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
export default function UpdateComplexity(props) {
    const classes = useStyles();
    const [filterKeyWord, setFilter] = useState('');
    const {
        setList, typelist, list,
    } = props;

    /**
     * Filter the information by Types.
     */

    const setFilterByKeyWord = (event) => {
        setFilter(event.target.value.toLowerCase());
    };

    return (
        <>
            <Grid item md={2}>
                <Box mt={4} pb={2}>
                    <div className={classes.searchWrapper}>
                        <TextField
                            id='outlined-full-width'
                            label='Type'
                            placeholder='Search By Types'
                            onChange={(e) => setFilterByKeyWord(e, typelist)}
                            fullWidth
                            variant='outlined'
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </div>
                </Box>
            </Grid>
            <Grid item md={12}>
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                <Typography variant='subtitle2'>
                                    <FormattedMessage
                                        id='Apis.Details.QueryAnalysis.UpdateComplexity.typeName'
                                        defaultMessage='Type'
                                    />
                                </Typography>
                            </TableCell>
                            <TableCell>
                                <Typography variant='subtitle2'>
                                    <FormattedMessage
                                        id='Apis.Details.QueryAnalysis.UpdateComplexity.fieldcomplexity'
                                        defaultMessage='Fields'
                                    />
                                </Typography>
                            </TableCell>
                            <TableCell>
                                <Typography variant='subtitle2'>
                                    <FormattedMessage
                                        id='Apis.Details.QueryAnalysis.UpdateComplexity.fieldcomplexitysum'
                                        defaultMessage='Sum of the Complexity'
                                    />
                                </Typography>
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {typelist.filter(
                            (item) => item.type.toLowerCase().includes(filterKeyWord),
                        ).map((typename) => {
                            return (
                                <TableRow style={{ borderStyle: 'hidden' }}>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            {typename.type}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <ExpansionPanel>
                                            <ExpansionPanelSummary
                                                expandIcon={<ExpandMoreIcon />}
                                                aria-controls='panel1a-content'
                                                id='panel1a-header'
                                            />
                                            <ExpansionPanelDetails>
                                                <Grid item md={12}>
                                                    <Table>
                                                        <TableRow>
                                                            <TableCell>
                                                                <b>Field</b>
                                                            </TableCell>
                                                            <TableCell>
                                                                <b>ComplexityValue</b>
                                                            </TableCell>
                                                        </TableRow>
                                                        {list.map((respond, index) => ((respond.type === typename.type)
                                                     && (
                                                         <TableRow>
                                                             <TableCell>
                                                                 {respond.field}
                                                             </TableCell>
                                                             <TableCell>
                                                                 <TextField
                                                                     id='complexityValue'
                                                                     label='complexityValue'
                                                                     margin='normal'
                                                                     variant='outlined'
                                                                     value={respond.complexityValue}
                                                                     onChange={(event) => {
                                                                         const newArr = [...list];
                                                                         newArr[index] = {
                                                                             type: respond.type,
                                                                             field: respond.field,
                                                                             complexityValue: +event.target.value,
                                                                         };
                                                                         setList(newArr);
                                                                     }}
                                                                 />
                                                             </TableCell>
                                                         </TableRow>
                                                     )))}
                                                    </Table>
                                                </Grid>
                                            </ExpansionPanelDetails>
                                        </ExpansionPanel>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant='body1'>
                                            {typename.summation}
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </Grid>
        </>
    );
}

UpdateComplexity.propTypes = {
    setList: PropTypes.func.isRequired,
    list: PropTypes.arrayOf(
        PropTypes.shape({
            type: PropTypes.string,
            field: PropTypes.string,
            complexityValue: PropTypes.number,
        }),
    ).isRequired,
    typelist: PropTypes.arrayOf(
        PropTypes.shape({
            type: PropTypes.string,
            summation: PropTypes.number,
        }),
    ).isRequired,
};
