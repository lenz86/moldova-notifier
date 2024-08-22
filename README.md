# Moldova Notifier

## Ports


## Build and deploy

### Profiles
There are 3 different profiles for building the project:
* debug - for running the project locally via IDE
* development - for running the project locally via docker-compose
* production - for running the project via docker-compose on remote server (UNIX-based)

### Build requires:
* Java 19

#### debug profile
* TG_BOT_NAME and TG_BOT_TOKEN environment variables on PATH or in IDEA Run Configuration variables

#### development profile
* TG_BOT_NAME and TG_BOT_TOKEN must be set in ./docker/container.env
* Bash must be installed and available on PATH (container starts by `./config/run-applications-local.sh`)

#### production profile
* TG_BOT_NAME and TG_BOT_TOKEN must be set in ./docker/container.env
* serverId must be configured in ./m2/settings.xml. Example:
```
    <servers>
        <server>
            <id>serverId</id>
            <username>username</username>
            <privateKey>X:\path\to\private\key\key-openssh</privateKey>
            <passphrase>passphrase-for-key-if-exist</passphrase>
			<filePermissions>775</filePermissions>
			<directoryPermissions>775</directoryPermissions>
        </server>
    </servers>
```
* All parameters in ./config/remote-deployment-properties must be defined
* If you build app on Windows, you must set correct access rights for you private key file 
to prevent error when run remote .sh via openssh (default for Windows)
