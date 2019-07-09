/* eslint-disable no-debugger */
/* eslint-disable func-names */
const fs = require('fs');
const path = require('path');

module.exports = function (source, map) {
    const headerPath = path.resolve(this.rootContext + '/override' + this.resourcePath.split('/source')[1]);
    let newSource = source;
    if (fs.existsSync(headerPath)) {
        newSource = fs.readFileSync(headerPath, 'utf8');
        newSource = newSource.replace('AppOverride', this.rootContext + '/override/');
        
        this.addDependency(headerPath);
    }
    this.callback(null, newSource, map);
};
