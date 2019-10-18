# Testing Implementation

We run mainly two types of tests

-   Unit tests
-   Integration test

# Prerequisites

Require NodeJs 8.0 or higher, and npm 5.0 or higher.You may use [`nvm`](https://github.com/nvm-sh/nvm) for installing/managing Node and NPM tools.

# How to run

Before running either unit tests or integration test, Go to the application root directory where `package.json` file is located (i:e `<carbon-apimgt-root>/features/apimgt/org.wso2.carbon.apimgt.publisher.feature/src/main/resources/publisher/`) and run `npm install` command to download all the dependencies.

## Unit tests

To run the unit tests, simply execute the `npm test` command in the application root directory (where the package.json file located). `npm test` is an alias to `jest` command defined in `package.json`.

## Integration tests

Test files are located in `<Product-APIM-Root>/modules/integration/tests-integration/tests-backend/src/test/resources/jest-integration-tests/`).
Go to the above location and run

```
npm test
```
for more information read the integration test [README.md](https://github.com/wso2/product-apim/blob/master/modules/integration/tests-integration/tests-backend/src/test/resources/jest-integration-tests/README.md) file


## Code coverage

Run the command `npm run test:coverage` to generate the code coverage for unit tests.Once generated, The coverage reports will be available in `<APP_ROOT>/coverage/` directory.

# Unit tests

We use [Jest framework](https://jestjs.io/en/) for managing the unit tests.it also been used as the assertion library as well. For DOM testing, out of the available libraries such as `react-testing-library`, `Enzyme`, and `React's TestUtils`, etc. . . we have mainly used `Enzyme`.

Test files naming convention is to add test of a component in the same name as the component's file name in the same location. For example if you write a new component name `MySampleComponent.jsx` then use `MySampleComponent.test.jsx` as the test file name for that component.
Jest will pickup the files for test which are ending with `.test.jsx` or `.test.js`


# Troubleshooting

> Feel free to update this guide , If you able to find better alternatives or if you find anything that is worth adding here

## Mounted wrapper does not contain the expected rendered react elements

This could happen due to failures in mounting child component, Or your test might take longer time to re-render after a state change.
In this case , We could do the suggested workaround in [here](https://github.com/airbnb/enzyme/issues/1587#issuecomment-416610674) Or [this workaround](https://github.com/facebook/jest/issues/2157#issuecomment-279171856)

## Wrapper state returns null

In this case, You might probably looking at wrong component instance (element), Use `wrapper.debug()` to find out which component is wrapped in

## setState does not update the rendered output in wrapper

You probably missed some `wrapper.update()` calls there. This is an asynchronous call, So make sure to use `await`.

For more info refer [this issue](https://github.com/airbnb/enzyme/issues/450#issuecomment-225075145)

## When mounting, not all components get rendered in component hierarchy

If you expect a component to be there in the rendered output but it's not there, That means you probably have check for the component before all the asynchronous calls get succeed (or get executed).

This could mostly happen if you have mocked an API response with a `Promise.resolve()` and haven't use `await` on the dom update or haven't flush the pending Promise resolves.

So in this case either you have to use
```javascript
await new Promise(resolve => setImmediate(resolve));
```
To wait for all the promises to get resolve (Exhausts all promises queued) ( source: [this comment](https://github.com/facebook/jest/issues/2157#issuecomment-279171856))

Or you need to use `await` for all the wrapper modification. i:e Event simulations etc.

## Error: Uncaught [TypeError: Cannot read property 'getPartialToken' of null]

This is exception is thrown if you haven't mocked the REST API calls. So use `Jest.fn()` or `Jest.mock('path/to/module')` to mock the relevant API calls.

## Mounting and testing components return by higher order components (HOC)

Most of the components int he Publisher app are decorated by one or more higher order components provided by various libraries, i:e most of the components are wrapped with `withStyle` higher order components to get the theming support from Material-UI, Similarly we have user `withRoute` to get routing details from react-router and `injectIntl` to get internationalization (i18n) support to the components.

for example in `Listing.jsx` we have used both `injectIntl` and `withStyles` higher order functions

```javascript
export default injectIntl(withStyles(styles)(Listing));
```

In this case, Enzyme shallow rendering will just render the outer most component , which is intl wrapper and will not actually render the intended Listing component, So we need to unwrap the HOC, before shallow rendering the component.

When doing Enzyme mount, These HOCs would require some values to be there in the context.
Do the following as required

-   For [React Intl](https://github.com/formatjs/react-intl/blob/master/docs/Testing-with-React-Intl.md) wrapper


    Use the [IntlHelper.js](source/Tests/Utils/IntlHelper) If we could not survive with this Util we might need to add [this package](https://github.com/joetidee/enzyme-react-intl) to get the full support
    i:e
    ```javascript
    import { mountWithIntl } from 'AppTests/Utils/IntlHelper';
    .
    .
    .
    let wrapper = await mountWithIntl(ThemedListing);
    ```

-   React router

Use `MemoryRouter` for wrapping the component, for example in [Listing.test.jsx](source/src/app/components/Apis/Listing/Listing.test.jsx)

```javascript
<MuiThemeProvider theme={createMuiTheme(light)}>
    <MemoryRouter>
        <Listing />
    </MemoryRouter>
</MuiThemeProvider>
```

- Material-UI [testing](https://material-ui.com/guides/testing/)

  - If you's test involves inserting, reading , modifying a value in material-ui component, checkout their test implementation for that component in [materia-ui git repo](https://github.com/mui-org/material-ui/tree/master/packages/material-ui/src). You will be able to get an idea of how to implement testing for your use case using material-ui components

Use `unwrap` util from material ui test utils to unwrap the `withStyle` decorations,

```javascript
import { unwrap } from '@material-ui/core/test-utils';

const UnwrappedMenuButton = unwrap(MenuButton);
```

When enzyme mounting use actual theme object and `MuiThemeProvider` component to wrap the target component.Like in the above (intl) example


## Clearing , Resting or Restoring mock implementations

If you want to change the mocked function behavior between different test cases within one test description(file), Use `mockReset` , `mockRestore` or `mockClear` accordingly. FOr more info refer this [issue](https://github.com/facebook/jest/issues/5143)

# For a quick lookup on Jest features
 - [Jest cheat sheet](https://github.com/sapegin/jest-cheat-sheet)
