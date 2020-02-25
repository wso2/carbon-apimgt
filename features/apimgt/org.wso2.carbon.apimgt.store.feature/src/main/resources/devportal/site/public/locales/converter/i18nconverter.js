const translate = require('translate');
const fs = require('fs');
let rawdata = fs.readFileSync('../en.json');


translate.engine = 'google';
translate.key = '<your-api-key>'; // Provide your google api key
const lanuageCode = 'si'; // lanuage code without region code

let langJSON = JSON.parse(rawdata);

let translatedJSON = {};
const jsonLength = Object.keys(langJSON).length;
Object.entries(langJSON).forEach(async ([key, value], index) => {
    console.log('converting ', key, value);
    translatedJSON[key] = await translate(value, lanuageCode);
    console.log('**********');
    console.log('translated', translatedJSON[key], index);
    if (jsonLength === index + 1) {
        fs.writeFile(`../${lanuageCode}.json`, JSON.stringify(translatedJSON, null, 1), (err) => {
            if (err) console.log(err);
            console.log('lanuage file saved');
        });
    }
});

