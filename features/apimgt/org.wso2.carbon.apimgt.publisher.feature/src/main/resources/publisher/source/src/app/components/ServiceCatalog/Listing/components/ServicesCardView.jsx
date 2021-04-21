/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState, useEffect } from 'react';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import TablePagination from '@material-ui/core/TablePagination';
import ServiceCard from './ServiceCard';

/**
 *
 * @returns
 */
export default function ServicesCardView(props) {
    const {
        serviceList, onDelete, getData, pagination,
    } = props;
    const numberOfServices = serviceList.length;
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    useEffect(() => {
        const offset = page > 0 ? rowsPerPage * page : page;
        getData(rowsPerPage, offset);
    }, [page, rowsPerPage]);
    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };
    return (
        <>
            <Grid
                container
                direction='row'
                justify={numberOfServices > 5 ? 'center' : 'flex-start'}
                alignItems='flex-start'
                spacing={4}
            >
                {serviceList.map((service) => (
                    <Grid item>
                        <ServiceCard onDelete={onDelete} service={service} />
                    </Grid>
                ))}
            </Grid>
            <Box justifyContent='flex-end' mx={3} display='flex'>
                {pagination.total > 10 && (
                    <TablePagination
                        component='div'
                        count={pagination.total}
                        page={page}
                        onChangePage={handleChangePage}
                        rowsPerPage={rowsPerPage}
                        onChangeRowsPerPage={handleChangeRowsPerPage}
                    />
                )}
            </Box>
        </>
    );
}
