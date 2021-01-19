/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React, { useEffect, useState, useContext } from 'react';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import Node from 'AppComponents/Apis/Details/History/Node';
import NodeStartEnd from 'AppComponents/Apis/Details/History/NodeStartEnd';
import Typography from '@material-ui/core/Typography';
import Link from '@material-ui/core/Link';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import Progress from 'AppComponents/Shared/Progress';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import DateTime from 'AppComponents/Apis/Details/History/DateTime';
import moment from 'moment';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
        maxWidth: theme.custom.contentAreaWidth,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    historyHead: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    contentWrapper: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'left',
        alignItems: 'center',
        paddingTop: 50,
    },
    moreLink: {
        background: 'linear-gradient(90deg, rgba(255,255,255,1) 0%, rgba(231,231,231,1) 50%, rgba(255,255,255,1) 100%)',
        padding: 10,
        marginTop: 20,
        display: 'block',
        width: '100%',
        textAlign: 'center',
    },
    filterBar: {
        padding: 10,
        background: '#fefefe',
        border: 'solid 1px #efefef',
        borderRadius: 5,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
    },
    formControl: {
        minWidth: 200,
        marginRight: 10,
    },
}));

/**
 * Renders a single node.
 * @returns {JSX} Rendered jsx output.
 */
const HistoryTree = () => {
    const classes = useStyles();
    const { api } = useContext(APIContext);
    const [history, setHistory] = useState([]);
    const [pageCount, setPageCount] = useState(1);
    const today = new Date();
    const lastYear = new Date();
    lastYear.setFullYear(lastYear.getFullYear() - 1);
    const [startTime, setStartTime] = useState(moment(lastYear).format('YYYY-MM-DD') + 'T00:00');
    const [endTime, setEndTime] = useState(moment(today).format('YYYY-MM-DD') + 'T00:00');
    const [page, setPage] = useState(0);
    const [revision, setRevision] = React.useState('all');

    const handleChangeRevision = (event) => {
        setRevision(event.target.value);
    };
    const limit = 10;

    const loadPage = (currentPage) => {
        Api.getAllHistory(currentPage, limit, api.id, moment(startTime).toISOString(),
            moment(endTime).toISOString()).then((response) => {
            const { body: { list, pagination: { total } } } = response;
            if (total > limit) {
                // Need pagination logic.
                setPageCount(Math.ceil(total / limit));
                setPage(page + 1);
            }
            setHistory(history.concat(list));
        })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    const { description } = response.body;
                    Alert.error(description);
                }
            });
    };
    useEffect(() => {
        loadPage(page * limit);
    }, [startTime, endTime]);
    const loadMore = () => {
        loadPage(page * limit);
    };
    const isRevisionNode = (des) => {
        return des.indexOf('/revisions') !== -1;
    };
    let isLeft = true;
    return (
        <>
            <Typography variant='h4' gutterBottom>
                <FormattedMessage id='Apis.Details.History.HistoryTree.title' defaultMessage='History' />
            </Typography>
            <div className={classes.filterBar}>
                <FormControl
                    classes={{ root: classes.formControl }}
                    margin='normal'
                >
                    <InputLabel id='demo-simple-select-label'>Revision</InputLabel>
                    <Select
                        labelId='demo-simple-select-label'
                        id='demo-simple-select'
                        value={revision}
                        onChange={handleChangeRevision}
                        margin='normal'
                    >
                        <MenuItem value='all'>All</MenuItem>
                        <MenuItem value='revision1'>Revision 1</MenuItem>
                        <MenuItem value='revision2'>Revision 2</MenuItem>
                    </Select>
                </FormControl>
                <DateTime
                    label={
                        <FormattedMessage id='Apis.Details.History.HistoryTree.startTime' defaultMessage='Start Time' />
                    }
                    time={startTime}
                    setTime={setStartTime}
                />
                <DateTime
                    time={endTime}
                    setTime={setEndTime}
                    label={
                        <FormattedMessage id='Apis.Details.History.HistoryTree.endTime' defaultMessage='End Time' />
                    }
                />
            </div>

            {history ? (
                <>
                    <div className={classes.contentWrapper}>
                        <NodeStartEnd isLeft={isLeft} isTop />
                        {history.map((entry) => {
                            const isRevision = isRevisionNode(entry.description);
                            isLeft = isRevision ? !isLeft : isLeft;
                            return (
                                <>
                                    <Node
                                        entry={entry}
                                        isLeft={isLeft}
                                        isRevisionNode={isRevision}
                                    />
                                </>
                            );
                        })}
                        {(pageCount === page) && (
                            <>
                                <NodeStartEnd isLeft={isLeft} isTop={false} />
                            </>
                        )}
                        {(pageCount > 1 && pageCount !== page) && (
                            <Link href='#' onClick={loadMore} className={classes.moreLink}>
                                <FormattedMessage
                                    id='Apis.Details.History.HistoryTree.show.more'
                                    defaultMessage='Show More'
                                />
                            </Link>
                        )}
                    </div>
                </>
            ) : (
                <Progress
                    per={5}
                    message={(
                        <FormattedMessage
                            id='Apis.Details.History.HistoryTree.loading'
                            defaultMessage='Loading history data...'
                        />
                    )}
                />
            )}
        </>
    );
};

export default HistoryTree;
