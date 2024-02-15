[[_TOC_]]

# PHMS Provincial Registry Service

REST service serving as ambassador proxy to receive requests response from provincial systems.

### Understanding provincial registry service

* [Where service fits in Solution Design](https://confluence.lblw.cloud/display/VP/PHMS+Solution+Approach)
* [Technical Documentation](https://confluence.lblw.cloud/display/VP/Technical+Documentation)
* [DSQ, SGOEM and DIS Transactions](https://confluence.lblw.cloud/pages/viewpage.action?pageId=513052168)
* [Different Error Codes used in service response](https://confluence.lblw.cloud/display/VP/Error+Message+Standards)
* [Sample Test Data for request/response](https://confluence.lblw.cloud/pages/viewpage.action?spaceKey=VP&title=Test+data+for+interacting+with+Ministries)
* [XML XPath Usage Guide](https://confluence.lblw.cloud/pages/viewpage.action?spaceKey=VP&title=XML+XPath+Documentation)


### Dependencies

The service relies on various internal and third-party dependencies. Following are the internal (VPHX/PE) dependencies
which are used, and requires to be upgraded on timely fashion:

  ```xml

<dependencies>
    <dependency>
        <groupId>com.sdm.ehealth</groupId>
        <artifactId>rds-client</artifactId>
        <version>${rds-client.version}</version>
    </dependency>
    <dependency>
        <groupId>com.lblw.vphx.iams</groupId>
        <artifactId>security-audit-engine</artifactId>
        <version>${security-audit-engine.version}</version>
    </dependency>
</dependencies>

```

There are a number of third-party dependencies used in the project. Browse the Maven pom.xml file for details of
libraries and versions used.

## Installation and Getting Started

These instructions will get you a copy of the project up and help you build the project.

### Prerequisites

You will need:

* JDK 11
* Maven 3.5+, download [here](https://maven.apache.org/download.cgi)
* GIT.
  See [Git setup guide](https://confluence.lblw.cloud/pages/viewpage.action?pageId=595878004#PHMSLocalSetup-GitSetup)
* Docker, download [here](https://docs.docker.com/desktop/install/windows-install/)
* MongoDB Compass, download [here](https://www.mongodb.com/products/compass)
* GCP CLI. For access instructions and more,
  check [here](https://confluence.lblw.cloud/display/VP/Gitlab+and+GCP+access)
* GlobalVPN Connect. Find download
  instructions [I](https://confluence.lblw.cloud/display/SuperSpace/0.+Guide+for+getting+Support+Accesses?preview=/475340710/475341175/Steps%20to%20install%20GlobalProtect%20VPN.pdf)
  and [II](https://confluence.lblw.cloud/pages/viewpage.action?pageId=574401139#TechnicalDocumentation-ConnecttoGlobalProtectVPN)

## Working with local copy in your IDE

#### [Code Formats and Standard guidelines](https://confluence.lblw.cloud/pages/viewpage.action?pageId=535121402#NamingconventionsandStandards-IntelliJformattingandautosave(PHMSteamspecificones))

#### [Feature branch and merge request guidelines](https://confluence.lblw.cloud/pages/viewpage.action?pageId=535121402#NamingconventionsandStandards-Branchnamingconvention)

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
  git clone https://gitlab.lblw.ca/health-watch/domains/PHMS/phms-docker-setup.git
   ```

   ```shell
  docker compose up --quite-pull -d 
   ```

#### 3) Building the service

  ```shell
  git clone https://gitlab.lblw.ca/health-watch/domains/PHMS/phms-provincial-registry-service.git
  ```

  ```shell
  mvn clean install
  OR
  mvn clean install -DskipTests
   ```

#### 4) Importing project in IntelliJ

  ```
  File -> Open -> {project_location}/phms-provincial-registry-service
  OR 
  In the main menu, choose `File -> Open` and select the /phms-provincial-registry-service/pom.xml. Click on the `Open` button.
  ```

#### 5) Configure build profile to `local`, and set following Environment Variables:

  ```
  SPRING_PROFILES_ACTIVE=local
  ```

#### 6) Running the service

   ```shell
   java -jar /app/target/phms-provincial-registry-service*.jar
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
* Pending Tech Debt/ Backlog
  ```
  https://jira.lblw.cloud/browse/LTPHVPHXQC-2960

  https://jira.lblw.cloud/browse/LTPHVPHXQC-2963

  https://jira.lblw.cloud/browse/LTPHVPHXQC-2901
  ```
