import Auth from "./Auth";

/**
 * Utility class for Publisher application
 */
class PublisherUtils {

    /**
     * TODO: Remove this method one the initial phase is done, This is used to continue the API class until the login page is create
     * @returns {promise}
     */
    static autoLogin() {
        let auth = new Auth();
        return auth.authenticateUser('admin', 'admin');
    }

}

export default PublisherUtils;