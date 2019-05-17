let argv = require('yargs').argv;
let shell = require('shelljs');
if (argv.skipTests === "true") {
    console.log('Skipping Tests');
} else {
    console.log('Running Tests');
    shell.exec("NODE_ENV=test NODE_TLS_REJECT_UNAUTHORIZED='0' mocha --recursive --require babel-register  \"./pages/test/**/!(runner).jsx\"", function (code) {
        shell.exit(code);
    });
}