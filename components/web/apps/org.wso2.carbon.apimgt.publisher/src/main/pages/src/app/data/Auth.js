import axios from 'axios';
import qs from 'qs';

class Auth {
    constructor() {
        this.host = "https://localhost:9292";
        this.token = "/publisher/auth/apis/login/token";
    }

    getTokenEndpoint() {
        return this.host + this.token;
    }

    authenticateUser(username, password) {
        const headers = {
            'Authorization': 'Basic deidwe',
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        const data = {
            username: username,
            password: password,
            grant_type: 'password',
            validity_period: 3600,
            scopes: 'apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage apim:subscription_view apim:subscription_block apim:subscribe'
        };
        return axios.post(this.getTokenEndpoint(), qs.stringify(data), {headers: headers});
    }
}

export default Auth;