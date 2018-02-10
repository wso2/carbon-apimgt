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

import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';

class SandboxKeys extends React.Component{
    constructor(props){
        super(props);
    }
    render(){
        return (
            <Paper>
                <Grid container className="tab-grid" spacing={0} >
                    <Grid item xs={12}>
                        <Typography type="display1" gutterBottom >
                            <span style={{fontSize: "50%"}}>SandboxKeys page</span>
                        </Typography>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default SandboxKeys