# json-validation-service

It's a REST service used to validate JSONs against uploaded schemas.


## Getting Started

### Running the app
In order to run DB below command should be executed:
```
cd docker
docker-compose up
```
and then from the root folder, to run the app itself:
```
sbt run
```

Then application should be app and running on: 
```
http://localhost:8090
```

### Swagger docs
Very simple endpoint documentation can be found under:

```
http://localhost:8090/docs
```

### In Memory mode
There's possibility to run the app without DB dependency. It will use simple Map as storage and won't be persistent between the runs.

```
sbt "run in_memory"
```

### Overview

### Architecture & decisions made

* Service is based on Tapir, htpp4s as interpreter, circe to parse JSONs and cats/cats-effect
* I used `circe-json-schema` for schema validation as it integrates nicely with rest of the stack
* Tapir was my library of choice because it allows distinguishing between endpoint definition and business logic which needs to be provided
* I tested ednpoints in unit test fashion, without depending on DB. That way tests are fast, though DB is not tested. It could be solved by providing simple E2E test which go through all layers or instead provide integration tests which could exercise endpoints along with the DB.
* I am falling back to default Tapir's behavior when it comes to malformed requests etc. This behavior is not tested. In production app I would either specify it explicitly or describe the behavior in swagger.
* I am using flyway to keep the schema and create DB

### Improvements/Disclaimers
I chose not to add every single improvement I had in mind, to develop the app quickly. 

I think it's in state which demonstrates my general approach to code and below considerations are for productions apps which are bigger and need more attention. 

* App is not creating specific thread pools for DB or http4s backend
* Logger was added but, there's almost no information logged
* No E2E or integration test
* Not using new types (like Refined) or Shapeless. There's very little data shuffled around, but I would definitely use stronger types in real world
* I am using auto codec derivation. It makes compile times longer
* Some classes should be extracted to separate files as it could grow and make things not easy to read. 
* I am leaning to use `IO` explicitly instead of `F[_]`. For this project it didn't make much difference, but it's something which should be considered in bigger projects. 
* Swagger is not a full-blown docs for the API. I wanted to demonstrated Tapir's capabilities to easily generate it based on the definition. Input/output examples should be provided
