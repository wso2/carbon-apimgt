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
import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import { useIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Api from 'AppData/api';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';
import { Progress } from 'AppComponents/Shared';
import { isRestricted } from 'AppData/AuthManager';

const useStyles = makeStyles(() => ({
    content: {
        flexGrow: 1,
    },
    itemWrapper: {
        width: 'auto',
        display: 'flex',
    },
    FormControl: {
        padding: 10,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    subTitle: {
        marginTop: 20,
    },
    subTitleDescription: {
        marginBottom: 10,
    },
    flowWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
    heading: {
        margin: 'auto',
        color: 'rgba(0, 0, 0, 0.40)',
    },
    paper: {
        padding: '10px 24px',
        width: 'auto',
    },
    editIcon: {
        position: 'absolute',
        top: 8,
        right: 0,
    },
}));

/**
 * The componet add deafult complexty values for all fields
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
export default function AddDefaultComplexity(props) {
    const classes = useStyles();
    const [api, updateAPI] = useAPI();
    const intl = useIntl();
    const {
        complexity, setState, findSummation,
    } = props;
    const [defaultList, setDefaultList] = useState(null);


    useEffect(() => {
        const apiId = api.id;
        const apiClient = new Api();
        const promisedComplexityType = apiClient.getGraphqlPoliciesComplexityTypes(apiId);
        promisedComplexityType
            .then((res) => {
                const array = [];
                res.typeList.map((respond) => {
                    respond.fieldList.map((ob) => {
                        const obj = {};
                        obj.type = respond.type;
                        obj.field = ob;
                        obj.complexityValue = 1;
                        array.push(obj);
                        return ob;
                    });
                    return array;
                });
                setDefaultList(array);
            });
    }, []);

    /**
     * Assign default complexity value of 1 for all fields.
     */
    function addDefaultComplexity() {
        const apiId = api.id;
        const apiClient = new Api();
        const promised = apiClient.addGraphqlPoliciesComplexity(
            apiId, {
                list: defaultList,
            },
        );
        updateAPI({ complexity });
        setState(defaultList);
        findSummation(defaultList);
        promised
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.QueryAnalysis.AddDefaultComplexity.default.complexity.added.successfully',
                    defaultMessage: 'Default Complexity Added successfully',
                }));
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    const { description } = response.body;
                    Alert.error(description);
                }
            });
    }

    if (defaultList === null) {
        return <Progress />;
    }

    return (
        <>
            {defaultList === null ? (
                <CircularProgress size={16} classes={{ root: classes.progress }} />
            ) : (
                <Button
                    onClick={addDefaultComplexity}
                    disabled={
                        isRestricted(['apim:api_create'], api)
                    }
                    className={classes.editIcon}
                    size='small'
                >
                    <SaveAltIcon />
                </Button>
            )}
        </>
    );
}

AddDefaultComplexity.propTypes = {
    complexity: PropTypes.shape({}).isRequired,
    setState: PropTypes.func.isRequired,
    findSummation: PropTypes.func.isRequired,
};
