import React from 'react';
import PageNavigation from '../APIsNavigation';
import PageContainer from '../../Base/container/';
import Listing from './Listing';

/**
 * Export the composed APIs Listing page
 * @returns {React.Component} @inheritdoc
 */
export default () => {
    return (
        <PageContainer pageNav={<PageNavigation />}>
            <Listing />
        </PageContainer>
    );
};
