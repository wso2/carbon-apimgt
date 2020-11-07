import React from 'react';
import AsyncApiComponent from '@kyma-project/asyncapi-react';
import '@kyma-project/asyncapi-react/lib/styles/fiori.css';
// import './styles.css';

const AsyncApiUI = (props) => {
    const {
        spec,
    } = props;
    return <AsyncApiComponent schema={spec} />;
};

export default AsyncApiUI;
