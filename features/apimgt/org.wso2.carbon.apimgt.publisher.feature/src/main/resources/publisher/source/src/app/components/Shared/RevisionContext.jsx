import React, { useContext } from 'react';

const RevisionContext = React.createContext({ });
export const useRevisionContext = () => useContext(RevisionContext);
export const RevisionContextProvider = RevisionContext.Provider;
export default RevisionContext;
