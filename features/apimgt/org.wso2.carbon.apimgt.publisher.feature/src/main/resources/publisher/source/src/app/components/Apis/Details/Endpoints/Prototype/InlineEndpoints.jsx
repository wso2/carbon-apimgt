/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React, {
    useState, useEffect, useContext, useCallback,
} from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core';
import GenericResource from 'AppComponents/Apis/Details/Endpoints/Prototype/GenericResource';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';

const xMediationScriptProperty = 'x-mediation-script';

const defaultScript = '/* mc.setProperty(\'CONTENT_TYPE\', \'application/json\');\n\t'
    + 'mc.setPayloadJSON(\'{ "data" : "sample JSON"}\');*/\n'
    + '/*Uncomment the above comment block to send a sample response.*/';

/**
 * The inline endpoints component.
 * This component lists the api resources to add custom mediation scripts.
 *
 * @param {any} props The input props.
 * @return {any} The HTML representation of the component.
 * */
function InlineEndpoints(props) {
    const { api } = useContext(APIContext);
    const {
        paths, updatePaths,
    } = props;
    const [mockValueDetails, setMockValueDetails] = useState({ resourcePath: '', resourceMethod: '' });

    /**
     * Handles the onChange event of the script editor.
     *
     * @param {string} value The editor content
     * @param {string} path The path value of the resource.
     * @param {string} method The resource method.
     * */
    const onScriptChange = useCallback(
        (value, path, method) => {
            const tmpPaths = JSON.parse(JSON.stringify(paths));
            tmpPaths[path][method][xMediationScriptProperty] = value;
            updatePaths(tmpPaths);
        },
        [mockValueDetails.resourcePath, mockValueDetails.resourceMethod],
    );

    const [mockScripts, setMockScripts] = useState([]);

    useEffect(() => {
        const promisedResponse = api.getGeneratedMockScriptsOfAPI(api.id);
        promisedResponse.then((response) => {
            setMockScripts(response.obj.list);
        });
    }, []);

    function getGeneratedMockScriptOfAPI(resourcePath, resourceMethod) {
        for (let i = 0; i < mockScripts.length; i++) {
            if (mockScripts[i].verb.toLowerCase() === resourceMethod.toLowerCase()
                && mockScripts[i].path === resourcePath) {
                return mockScripts[i].content;
            }
        }
        return null;
    }

    return (
        <>
            <Grid container spacing={1} direction='column'>
                {Object.keys(paths).map((path) => {
                    return (
                        Object.keys(paths[path]).map((method) => {
                            const mediationScript = paths[path][method][xMediationScriptProperty];
                            const script = mediationScript === undefined ? defaultScript : mediationScript;
                            const originalScript = getGeneratedMockScriptOfAPI(path, method);
                            return (
                                <GenericResource
                                    resourcePath={path}
                                    resourceMethod={method}
                                    onChange={onScriptChange}
                                    scriptContent={script}
                                    originalScript={originalScript}
                                    setMockValueDetails={setMockValueDetails}
                                />
                            );
                        })
                    );
                })}
            </Grid>
        </>
    );
}

InlineEndpoints.propTypes = {
    paths: PropTypes.shape({}).isRequired,
    updatePaths: PropTypes.func.isRequired,
};

export default React.memo(InlineEndpoints);
