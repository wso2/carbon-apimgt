import React, { useState } from 'react';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import KeyConfiguration from 'AppComponents/Shared/AppsAndKeys/KeyConfiguration';

const generateKeysStep = (props) => {
    const [tab, setTab] = useState(0);
    const [keyRequest, setKeyRequest] = useState({
        keyType: 'PRODUCTION',
        supportedGrantTypes: ['client_credentials'],
        callbackUrl: 'https://wso2.am.com',
    });

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
    const handleTabChange = (event, currentTab) => {
        setTab(currentTab);
    };

    const { currentStep } = props;

    if (currentStep === 3) {
        console.log('keys generated');
    } else if (currentStep === 2) {
        return (
            <React.Fragment>
                <Tabs value={tab} onChange={handleTabChange} fullWidth indicatorColor='secondary' textColor='secondary'>
                    <Tab label='PRODUCTION' />
                    <Tab label='SANDBOX' />
                </Tabs>
                {tab === 0 && (
                    <div>
                        <KeyConfiguration
                            updateKeyRequest={setKeyRequest}
                            keyRequest={keyRequest}
                            keyType='PRODUCTION'
                        />
                    </div>
                )}
                {tab === 1 && (
                    <div>
                        <KeyConfiguration
                            updateKeyRequest={setKeyRequest}
                            keyRequest={keyRequest}
                            keyType='SANDBOX'
                        />
                    </div>
                )}
            </React.Fragment>
        );
    }
    return '';
};

export default generateKeysStep;
