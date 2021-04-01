import React, { useContext } from 'react';

const RuntimeConfigurationContext = React.createContext({ });
export const useRuntimeConfigurationContext = () => useContext(RuntimeConfigurationContext);
export const RuntimeConfigurationContextProvider = RuntimeConfigurationContext.Provider;
export default RuntimeConfigurationContext;
