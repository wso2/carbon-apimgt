import Auth from "../src/app/data/Auth.js";
class TestUtils {
    static setupMockEnviroment() {
        global.window = {
            location: {
                hash: "",
                host: "localhost:9292",
                hostname: "localhost",
                origin: "https://localhost:9292",
                pathname: "/",
                port: "9292",
                protocol: "https:"
            }
        };
        global.document = {
            value_: '',

            get cookie() {
                return this.value_;
            },

            set cookie(value) {
                this.value_ += value + '; ';
            }
        };
    }

    static userLogin(username = 'admin', password = 'admin') {
        let authenticator = new Auth();
        return authenticator.authenticateUser(username, password);
    }
}

export default TestUtils