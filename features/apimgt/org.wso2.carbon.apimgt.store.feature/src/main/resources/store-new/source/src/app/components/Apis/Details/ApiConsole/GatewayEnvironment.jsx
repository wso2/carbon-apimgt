import React from 'react';

const GatewayEnvironment = (props) => {
    const { environment } = props;
    return (
        <div>
            {environment.environmentName}
            {environment.environmentType}
            {environment.environmentURLs.http}
            {environment.environmentURLs.https}
        </div>
    );
};

export default GatewayEnvironment;
