# Developer Portal Application Structure

1. Override folder  - If you want to override a certain React Component / file from source/src/ folder, this is the correct place to do it.
You do not have to copy the entire directory, only copy the desired file/files.

2. Services folder - Includes the files related to the functionalities of login, logout and refresh.

3. Site folder - Includes the configuration and public folders of the project.

4. jaggery.conf file - The file that is responsible to fetch the paths and handle server-side routing.

Note : Only the Services and Site folders along with the jaggery.conf file is required at the runtime.

5. Source and other files - Includes the react files (source code) of the Developer Portal React Application.
