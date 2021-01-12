import React from 'react';
import AsyncApiComponent from '@asyncapi/react-component';
import '@asyncapi/react-component/lib/styles/fiori.css';
// import './styles.css';

const AsyncApiUI = (props) => {
    const {
        spec,
    } = props;
    return <AsyncApiComponent schema={spec} />;
};

export default AsyncApiUI;
