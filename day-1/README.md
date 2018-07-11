# Restaurant Service Walkthrough: Day 1

![Logo](../Logo.png?raw=true "Architecture")

## Background

We have seen that usually in Spring Boot web service we get our project specific properties from the application.properties and when the service is dockerized properties are injected as environment variable. This needs restart of the service when the environment variale is changed.

In today's walkthrough, we will see how this can be managed better using Spring Config Server.

## What we will build

We will create a simple item service Spring Boot service. This will have few entries for item name and price in the database. The service will get the item name and its price in a GET request. We will be using the service only for getting the record so, we are not having the complete CRUD for the item service.

### Before

![Logo](ItemService1.png?raw=true "ItemService1")

### After

![Logo](ItemService2.png?raw=true "ItemService2")

## Step1: Create the item-service project

 - Go to http://start.spring.io/
 - Create a project using:
    - Group: *com.example.microservices*
    - Artifact: *item-service*
    - Dependencies: *Web, DevTools, Actuator*
 - Download, unzip the project.zip
 - From command line: ./gradlew eclipse
 - Import it to eclipse.
 - Add the following lines:
 
##### src/main/application.properties

```properties
 spring.application.name=item-service
 server.port=10000
```
##### src/main/java/ItemServiceController.java
 
```java
package com.example.microservices.itemservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemServiceController {
	
	@GetMapping("/items/{id}/type/{type}")
	public Item getItem(@PathVariable Long id, @PathVariable String type) {
		
		return new Item(id, "Mutton Biriyani", 220);
	}
}
```
 
##### src/main/java/Item.java

```java
package com.example.microservices.itemservice;

public class Item {
	
	private long id;
	private String name;
	private int price;
	
	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
	
	public Item() {}
	
	public Item(long id, String name, int price) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
	}
}


```

 - Run it and hit the following url in browser:

`http://localhost:10000/items/1001/type/restaurant`

## Step2: Read the price from database

- Add the required dependencies:

##### build.gradle

```groovy
...
dependencies {
	compile('org.springframework.boot:spring-boot-starter-actuator')
	compile('org.springframework.boot:spring-boot-starter-data-jpa')
	compile('org.springframework.boot:spring-boot-starter-web')
	runtime('org.springframework.boot:spring-boot-devtools')
	runtime('com.h2database:h2')
	testCompile('org.springframework.boot:spring-boot-starter-test')
}
...
```
- Run: ./gradlew cleanEclipse eclipse
- Refresh the eclipse project
- Add the following files:

##### src/main/resources/data.sql

```sql
Insert into item(id, name, price)
values(1001, 'Mutton Biriyani', 220);
Insert into item(id, name, price)
values(1002, 'Chicken Chaap', 180);
Insert into item(id, name, price)
values(1003, 'Galauti Kabab', 230);
```
##### src/main/java/Item.java

```java
package com.restaurant.itemservice;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Item {
	
	@Id
	private long id;
	...
```

##### src/main/java/ItemRepository.java

```java
package com.example.microservices.itemservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
	
}
```

##### src/main/java/ItemServiceController.java

```java
package com.example.microservices.itemservice;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemServiceController {
	
	@Autowired
	private ItemRepository repository;
	
	@GetMapping("/items/{id}/type/{type}")
	public Item getItem(@PathVariable Long id, @PathVariable String type) {
		
		Optional<Item> item = repository.findById(id);
		return item.get(); 
	}
}

```

##### src/main/resources/application.properties

```properties
...
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2
```
- Start the service
- Hit: `http://localhost:10000/h2` for h2 database
- Hit the following url in the browser:
`http://localhost:10000/items/1001/type/restaurant`
- Change the itemId to 1002 or 1003 to see the values coming dynamically

## Step3: Add a configuration value

##### src/main/resources/application.properties
```properties
...
item-service.factor=1.05
```
##### src/main/java/Configuration.java

```java
package com.example.microservices.itemservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("item-service")
public class Configuration {
	private float factor;

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}
}

```
##### src/main/java/ItemServiceController.java

```java
package com.example.microservices.itemservice;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemServiceController {
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ItemRepository repository;
	
	@GetMapping("/items/{id}/type/{type}")
	public Item getItem(@PathVariable Long id, @PathVariable String type) {
		
		Optional<Item> item = repository.findById(id);
		return getCalculatedItem(item.get(), type);
	}
	
	private Item getCalculatedItem(Item item, String type) {
		double factor = 1.0;
		if (type.equals("restaurant")) factor = configuration.getFactor();
		item.setPrice((int) (item.getPrice() * factor));
		return item;
	}
}
```
- Now, hit the URL in two different ways:

`http://localhost:10000/items/1001/type/restaurant`
`http://localhost:10000/items/1001/type/takeaway`

## Step 4: Setup the configuration for different environments

In reality, we have different environment like dev, qa, prod. So, we need to have different values for configuration per environment. In this section, we will do that.

##### src/main/resources/application.properties

```properties
spring.application.name=item-service
server.port=10000

spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2

spring.profiles.active=dev

item-service.factor=1
```


##### src/main/resources/application-dev.properties
```
item-service.factor=1.05
```

Now, you should see that the configuration is read from the `application-dev.properties` file.

## Step 5: Create the Configuration Server

- Create a git repo for the configuration service
[We already have one which is available at `http://localhost:3000/dipanjan/restaurant-config` - we will use that].

- Go to http://start.spring.io/
 - Create a project using:
    - Group: *com.example.microservices*
    - Artifact: *config-server*
    - Dependencies: *ConfigServer, DevTools, Actuator*
 - Download, unzip the project.zip
 - From command line: ./gradlew eclipse
 - Import it to eclipse.

- Put the following content in the files:

##### src/main/resources/application.properties

```properties
spring.application.name=config-server
server.port=9999
spring.cloud.config.server.git.uri=http://localhost:3000/dipanjan/restaurant-config
```
##### src/main/java/ConfigServerApplication.java

```java
...
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
```
- Run the Config Server and hit the following urls:

`http://localhost:9999/item-service/dev`
`http://localhost:9999/item-service-dev.properties`
`http://localhost:9999/item-service-dev.yaml`

## Step 6: Integrate Configuration Server with Item Service

Add Spring Cloud config client by changing the build.gradle of item service.

##### build.gradle

```groovy
buildscript {
	ext {
		springBootVersion = '2.0.3.RELEASE'
	}
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

group = 'com.example.microservices'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8

repositories {
	mavenCentral()
}

ext {
	springCloudVersion = 'Finchley.RELEASE'
}

dependencies {
	compile('org.springframework.boot:spring-boot-starter-actuator')
	compile('org.springframework.boot:spring-boot-starter-data-jpa')
	compile('org.springframework.boot:spring-boot-starter-web')
	compile('org.springframework.cloud:spring-cloud-starter-config')
	runtime('org.springframework.boot:spring-boot-devtools')
	runtime('com.h2database:h2')
	testCompile('org.springframework.boot:spring-boot-starter-test')
}

dependencyManagement {
	imports {
		mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
	}
}

```
- Run: `./gradlew cleanEclipse eclipse`
- Delete application-dev.properties
- Rename `application.properties` to `bootstrap.properties` and update it

##### src/main/resources/bootstrap.properties

```properties
spring.cloud.config.uri=http://localhost:9999
#item-service.factor=1
```

And make `application.properties` to have only the following line:

```properties
server.port=10000
```

## Step 7: Add refresh scope


In the config server, disable the management security.

##### src/main/resources/application.properties

```properties
...
management.security.enabled=false
``` 

And in the item service we add, `@RefreshScope` annotation.

##### src/main/java/ItemServiceController.java

```java
...
@RestController
@RefreshScope
public class ItemServiceController {
...

```

For item service, 

##### src/main/resources/application.properties

```properties
server.port=10000

management.endpoints.web.exposure.include=*
```

This will register the `/actuator/refresh` url with POST method. 

- Change the value in the config git repo
- Refresh the client and observe the value did not change
- Then send the POST request to /actuator/refresh url:

```bash
$ curl localhost:10000/actuator/refresh -d {} -H "Content-Type: application/json"

["config.client.version","item-service.factor"]
```

- Refresh the client, it will now show the revised value.

## Step 8: Add Spring Cloud bus for refresh all

- Add spring-cloud-starter-bus-amqp in the build.gradle of item-service

##### build.gradle
```groovy
...
compile('org.springframework.cloud:spring-cloud-starter-bus-amqp')
```
- Run: `./gradlew cleanEclipse eclipse`
- Refresh the project
- Create a docker-compose.yaml as follows:

```yaml
version: '2'

services:
  rabbitmq:
      image: 'rabbitmq:alpine'
      ports:
        - "5672:5672"
```
- Run: docker-compose up
- Update the configuration value
- Run the `bus-refresh` url

```
$ curl localhost:10000/actuator/bus-refresh -d {} -H "Content-Type: application/json"
```
- Now verify that configuration values in all the service instances are updated.