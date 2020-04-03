import React from 'react';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';

const LeftMenu = (props) => {
    // const { classes, intl } = props;
    console.log('props obj from side nav bar: ', props);
    return (
        <div className='black'>
            <div className='Details-LeftMenu-649'>
                <LeftMenuItem
                    // text={intl.formatMessage({
                    //     id: 'Apis.Details.index.overview',
                    //     defaultMessage: 'overview',
                    // })}
                    text='overview'
                    to='/overview'
                />
            </div>
        </div>
    );
};

export default LeftMenu;
