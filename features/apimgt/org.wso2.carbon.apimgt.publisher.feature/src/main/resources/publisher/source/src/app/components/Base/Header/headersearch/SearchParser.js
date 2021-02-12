/**
 * This will parse the search query entered by the user into the expected format
 */
const reg = /(\S+:'(?:[^'\\]|\\.)*')|(\S+:"(?:[^"\\]|\\.)*")|(-?"(?:[^"\\]|\\.)*")|(-?'(?:[^'\\]|\\.)*')|\S+|\S+:\S+/g;

/**
 *
 *
 * @class SearchParser
 */
class SearchParser {
    /**
     *
     *
     * @static
     * @param {*} userQuery
     * @returns
     * @memberof SearchParser
     */
    static parse(userQuery) {
        let modifiedSearchQuery = '';
        // Get a list of search terms respecting single and double quotes
        let match;
        const contentArr = [];
        // eslint-disable-next-line no-cond-assign
        while ((match = reg.exec(userQuery)) !== null) {
            let term = match[0];
            const sepIndex = term.indexOf(':');
            if (sepIndex !== -1) {
                const key = term.slice(0, sepIndex);
                let val = term.slice(sepIndex + 1);
                // Strip backslashes respecting escapes
                val = (val + '').replace(/\\(.?)/g, (s, n1) => {
                    switch (n1) {
                        case '\\':
                            return '\\';
                        case '0':
                            return '\u0000';
                        case '':
                            return '';
                        default:
                            return n1;
                    }
                });
                if (key && val) {
                    modifiedSearchQuery += key + ':' + val + ' ';
                } else {
                    return '';
                }
            } else {
                // Strip backslashes respecting escapes
                term = (term + '').replace(/\\(.?)/g, (s, n1) => {
                    switch (n1) {
                        case '\\':
                            return '\\';
                        case '0':
                            return '\u0000';
                        case '':
                            return '';
                        default:
                            return n1;
                    }
                });
                contentArr.push(term);
            }
        }
        if (contentArr.length > 0) {
            modifiedSearchQuery = 'content:' + contentArr.join(' ') + ' ' + modifiedSearchQuery;
        }
        return modifiedSearchQuery.trim();
    }
}

export default SearchParser;
