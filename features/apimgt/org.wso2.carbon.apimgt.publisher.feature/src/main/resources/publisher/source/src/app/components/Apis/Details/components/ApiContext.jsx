import React from 'react';

const ApiContext = React.createContext({
    active: 'overview',
    setAPI: () => {},
    api: null,
});

export default ApiContext;
