import React, {Component} from 'react'
import {Card} from 'antd'

import GenericEndpointInputs from './GenericEndpointInputs'

const Endpoint = (props) => {

    return (
        <div>
            <Card title="Production Endpoint" bordered={false} style={{width: '90%', marginBottom: '10px'}}>
                <GenericEndpointInputs/>
            </Card>
            <Card title="Sandbox Endpoint" bordered={false} style={{width: '90%'}}>
                <GenericEndpointInputs/>
            </Card>
        </div>
    );
};

export default Endpoint