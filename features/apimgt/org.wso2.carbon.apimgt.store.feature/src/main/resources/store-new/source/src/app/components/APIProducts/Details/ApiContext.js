import React from 'react';

export const ApiContext = React.createContext({
    active: 'overview',
    handleMenuSelect: () => {},
    api: null,
    applications: null,
    subscribedApplications: [],
    applicationsAvailable: [],
    updateSubscriptionData: () => {},
  });