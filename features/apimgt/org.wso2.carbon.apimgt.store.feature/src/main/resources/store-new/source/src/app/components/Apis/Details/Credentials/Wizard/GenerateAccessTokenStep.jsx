import React, { useState } from 'react';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Tokens from 'AppComponents/Shared/AppsAndKeys/Tokens';

const generateAccessTokenStep = (props) => {
    const [tab, setTab] = useState(0);

    /**
    * @param {*} event event
    * @param {*} currentTab current tab
    * @memberof Wizard
    */
    const handleTabChange = (event, currentTab) => {
        setTab(currentTab);
    };

    const { currentStep, createdApp } = props;

    if (currentStep === 4) {
        console.log('acctoken generated');
    } else if (currentStep === 3) {
        return (
            <React.Fragment>
                <Tabs
                    value={tab}
                    onChange={handleTabChange}
                    fullWidth
                    indicatorColor='secondary'
                    textColor='secondary'
                >
                    <Tab label='PRODUCTION' />
                    <Tab label='SANDBOX' />
                </Tabs>
                {tab === 0 && (
                    <div>
                        <Tokens
                            innerRef={node => (this.tokens = node)}
                            selectedApp={createdApp}
                            keyType='PRODUCTION'
                        />
                    </div>
                )}
                {tab === 1 && (
                    <div>
                        <Tokens
                            innerRef={node => (this.tokens = node)}
                            selectedApp={createdApp}
                            keyType='SANDBOX'
                        />
                    </div>
                )}
            </React.Fragment>
        );
    }
    return '';
};

export default generateAccessTokenStep;
