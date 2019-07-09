# Override root folder
If you want to override a certain React Component / File from source/src/ folder, this is the correct place to do it.
You do not have to copy the entire directory, only copy the desired file/files.

#### Example
Following will override the API Documentation component and Overview components.
```sh
override
└── src
    ├── Readme.txt
    └── app
        └── components
            └── Apis
                └── Details
                    ├── Documents
                    │   └── Documentation.jsx
                    └── Overview.jsx
```

### Development

When you are doing active development, the watch mode is working with the overriden files. But adding new files and directories will not triger a new webpack build.

```sh
npm run build:dev
```
