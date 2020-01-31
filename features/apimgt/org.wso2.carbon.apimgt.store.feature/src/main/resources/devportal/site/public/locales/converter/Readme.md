### i18N Converter Node JS Application

i18nconverter.js is a simple node app to automatically convert the lanuage keys in to any given lauage.

## How to run

You need to have nodejs in your enviorment.



Open a terminal and navigate to the /devportal/site/public/locales/converter/
1. Run the following

```js
npm install
```
2. Obtain an API key for google translate API. https://cloud.google.com/translate/docs/basic/setup-basic
3. Open i18nconverter.js and change the lanuage key ( without region code. Meaning that if it's en-US, you need to put only 'en' ) and add the API key obtained at the step 2.
```js
translate.key = 'your-api-key'; 
const lanuageCode = 'fr'; // lanuage code without region code
```
4. Open a terminal and navigate to **devportal/site/public/locales/converter** folder. Build the node js project by running the following command.
```
npm install
```

5. Run the following commands to convert the en.json to your language.
```js
node i18nconverter.js 
```


