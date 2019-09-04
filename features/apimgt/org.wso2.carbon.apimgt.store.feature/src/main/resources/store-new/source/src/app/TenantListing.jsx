import React, { useEffect, useState, useContext } from 'react';
import Tenants from 'AppData/Tenants';
import { Link } from 'react-router-dom';
import Settings from 'AppComponents/Shared/SettingsContext';

const tenantListing = (props) => {
    // const [tenantList, setTenantList] = useState([]);
    const settingContext = useContext(Settings);
    const { tenantList } = props;
    useEffect(() => {
        // const tenantApi = new Tenants();
        // tenantApi.getTenantsByState().then((response) => {
        //     setTenantList(response.body);
        // }).catch((error) => {
        //     console.log('error when getting tenants ' + error);
        // });
    }, []);
    console.log(tenantList);
    console.log(settingContext);
    return (
        <div>
            {tenantList.map(({ domain }) => {
                return (
                    <Link to={`/apis?tenant=${domain}`} onClick={() => settingContext.setTenantDomain(domain)}>
                        {domain}
                        {' '}
                    </Link>
                );
            })}

        </div>
    );
};

export default tenantListing;
