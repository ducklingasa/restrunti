# RestRunTI
RestRunTI provides remote and local TI execution using the IBM Planning Analytics (TM1) REST API

## Installation
Deploy the compiled fat JAR file into the folder: `<TM1 Install>\bin64\javaextensions\user\`

## Usage
```ruby
# RestRunTI (synchronous, CAM)
return = ExecuteJavaN('cognos.tm1.tiext.BasicRestRunTI', 'https://localhost:8030', '1', '}CAMNamespace', 'QURNSU46YXBwbGU6TVM=', 'z_fix');

# RestRunTI (asynchronous, user & password, paramater)
return = ExecuteJavaN('cognos.tm1.tiext.BasicRestRunTI', 'https://localhost:8030', '0', 'ADMIN', 'apple', 'z_fix', 'pType', 'async');
```
You can find more samples in the [Documentation](https://www.misc-soft.eu.org/restrunti).

## Task lists
- [x] Explicit authentication (Native & CAM)
- [ ] Implicit authentication (Windows SSO)

## ChangeLog 
See the [Change Log](CHANGELOG.md) for recent changes.

## Documentation
Our [Documentation](https://www.misc-soft.eu.org/restrunti) 

## Issues
If you find issues, sign up in Github and open an Issue in this repository

## Contribution
RestRunTI is open source, if you find a bug or feel like you can contribute please fork the repository, update the code and then create a pull request so I can merge in the changes.
