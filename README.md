# Users API
A simple, RESTful web API for managing information about users.

## Running standalone (via docker)

This API can be run locally with only [docker and docker-compose installed](https://docs.docker.com/docker-for-mac/install/).
```
docker-compose -f docker-compose-standalone.yml up -d --build api
```

If the `build/` folder is missing, continue down to [Development Setup](/#development-setup) and then run
```
./gradlew clean build shadowJar -x test
```

The standalone API can be torn down with
```
docker-compose -f docker-compose-standalone.yml down
```

## Example requests
While the [FunctionalTestSuite](/src/test/kotlin/users/api/FunctionalTestSuite.kt) is primarily for performing black-box
tests against the API, it is also intended to be readable to help developers determine how to interact with the API. 

However, there is also an [OpenAPI "Swagger" spec](https://swagger.io/specification/) under `api-spec/`.
Swagger specs can be easily viewed at https://editor.swagger.io/, or you can use IDE plugins depending on your IDE.

If you're a fan of [Insomnia](https://insomnia.rest/) there is an insomnia collection under `insomnia/` that
you can import into your client.

Finally, there are tried-and-true curl commands as well.

#### Create User
```
curl --request POST --url http://localhost:8080/users --header 'Content-Type: application/json' --data '{
	"first_name": "Michael",
	"last_name": "Black",
	"is_test": true
}'
```

#### Get User by ID
```
curl --request GET --url http://localhost:8080/users/6c81c32a-b98f-4190-8317-26c5765b272d
```

#### Like User
```
curl --request POST --url http://localhost:8080/users/d175fc39-398e-4495-92a7-833344ab2b5e/likes --header 'Content-Type: application/json' --data '{
	"liked_by_user_id": "2cfc99a3-dd3c-4fe6-b5e7-010d3335d034"
}'
```

#### Get User's Likes
```
curl --request GET --url 'http://localhost:8080/users/38ac674a-a54b-4156-805f-b9834c39a553/likes?page=0&per_page=10
```

## Development Setup
To install make sure you have the following installed:
- Java 13 
- Kotlin
- Gradle
- Docker

If you don't have any of the top three installed you can use [Homebrew](https://brew.sh/) to easily install those.
```
brew install java
brew install kotlin
brew install gradle
```

Instructions for installing Docker for Mac can be found [here](https://docs.docker.com/docker-for-mac/install/).

### Spinning up dependencies
This application has external dependencies that must be spun up first
```
docker-compose up -d
```
_Note: this defaults to use `docker-compose.yml` in the same directory._

### Running Tests (and Linting)
```
./gradlew clean check
```

### Formatting
```
./gradlew ktlintFormat
```

### Running standalone (via gradle)
```
./gradlew run
```
