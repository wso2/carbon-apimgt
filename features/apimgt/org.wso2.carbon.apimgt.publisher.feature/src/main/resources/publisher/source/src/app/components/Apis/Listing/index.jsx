import React from 'react';
import PageNavigation from '../APIsNavigation';
import PageContainer from '../../Base/container/';
import Listing from './Listing';

export default (_) => {
    return (
        <PageContainer pageNav={<PageNavigation />}>
            <Listing />
        </PageContainer>
    );
};
