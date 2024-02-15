[[_TOC_]]

# Provincial Registry Service

REST service serving as ambassador proxy to receive requests response from provincial systems.

### Understanding provincial registry

### Dependencies

The service relies on various internal and third-party dependencies. Following are the internal dependencies which are used, and requires to be upgraded on timely fashion:

```xml

```

There are a number of third-party dependencies used in the project. Browse the Maven pom.xml file for details of libraries and versions used.

## Installation and Getting Started

These instructions will get you a copy of the project up and help you build the project.

### Prerequisites

You will need:

* JDK 11
* Maven 3.5+, download [here](https://maven.apache.org/download.cgi)
* GIT.
* Docker, download [here](https://docs.docker.com/desktop/install/windows-install/)
* MongoDB Compass, download [here](https://www.mongodb.com/products/compass)
* GCP CLI. For access instructions and more,

## Working with local copy in your IDE

### Steps

#### 1) Setting GCP configurations and credentials

   ```shell
   gcloud components update
   gcloud components install kubectl
   gcloud auth application-default login
   gcloud auth login --brief
   gcloud config set project lt-sre-shipyard
   gcloud config set compute/region northamerica-northeast1
   gcloud container clusters get-credentials banting
   ```

#### 2) Configure docker container

   ```shell
  git clone #docker-setup-link
   ```

   ```shell
  docker compose up --quite-pull -d 
   ```

#### 3) Building the service

  ```shell
  git clone #code-link
  ```

  ```shell
  mvn clean install
  OR
  mvn clean install -DskipTests
   ```

#### 4) Importing project in IntelliJ

  ```
  File -> Open -> {project_location}/provincial-registry
  OR 
  In the main menu, choose `File -> Open` and select the /provincial-registry/pom.xml. Click on the `Open` button.
  ```

#### 5) Configure build profile to `local`, and set following Environment Variables:

  ```
  SPRING_PROFILES_ACTIVE=local
  ```

#### 6) Running the service

   ```shell
   java -jar /app/target/provincial-registry*.jar
   OR 
   Run com/lblw/vphx/phms/registry/ProvincialRegistryServiceApplication.java
   ```

#### 7) Navigate to following Swagger URL to verify service availability.

[Swagger OpenAPI Specification](http://localhost:8085/local/phms/swagger-ui.html)

## Deployment

For cloud deployment, follow the `Gitlab -> Pipeline`

## Future Enhancements

* Module common-utils to be deprecated and removed.
* Integrate with common libraries.
* Resolve pending //TODO:
* Enhance code coverage.
