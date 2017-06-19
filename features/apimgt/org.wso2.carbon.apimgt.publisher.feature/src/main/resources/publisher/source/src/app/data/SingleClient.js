import SwaggerClient from 'swagger-client'

/**
 * This class expose single swaggerClient instance created using the given swagger URL (Publisher, Store, ect ..)
 * it's highly unlikely to change the REST API Swagger definition (swagger.json) file on the fly,
 * Hence this singleton class help to preserve consecutive swagger client object creations saving redundant IO operations.
 */
class SingleClient {
    /**
     * Check for already created instance of the class, in the `SingleClient._instance` variable,
     * and return single instance if already exist, else assign `SingleClient._instance` to current instance and return
     * @param {{}} args : Accept as an optional argument for SwaggerClient constructor.Merge the given args with default args.
     * @returns {SingleClient|*|null}
     */
    constructor(args = {}) {
        if (SingleClient._instance) {
            return SingleClient._instance;
        }
        // instance variable _client is meant to be served as pvt variable
        this._client = new SwaggerClient(Object.assign(args, {
            url: this._getSwaggerURL(),
            usePromise: true
        }));
        this._client.then(
            (swagger) => {
                swagger.setHost("localhost:9292");
                /* TODO: Set hostname according to the APIM environment selected by user*/
                swagger.setSchemes(["https"]);
            }
        );
        this._client.catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
            }
        );
        SingleClient._instance = this;
    }

    /**
     * Expose the private _client property to public
     * @returns {SwaggerClient} an instance of SwaggerClient class
     */
    get client() {
        return this._client;
    }

    _getSwaggerURL() {
        /* TODO: Read this from configuration ~tmkb*/
        return "https://localhost:9292/api/am/publisher/v1.0/apis/swagger.json";
    }
}

SingleClient._instance = null; // A private class variable to preserve the single instance of a swaggerClient

export default SingleClient