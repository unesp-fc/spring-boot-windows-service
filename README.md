# Spring Boot Lancher for Windows Service

This project aims to make Spring Boot Loader compatible with Procrun (Apache Commons Daemon).

## Usage

To use thos project, include it as a dependency of the `spring-boot-maven-plugin`. Example:

```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>3.3.3</version>
        <executions>
            <execution>
                <goals>
                    <goal>repackage</goal>
                </goals>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>br.unesp.fc</groupId>
                <artifactId>spring-boot-windows-service</artifactId>
                <version>1.0.0</version>
            </dependency>
        </dependencies>
    </plugin>
```

To install the service, download prunsrv.exe [here](https://downloads.apache.org/commons/daemon/binaries/windows/), place it in the same directory as your package, and run the following command:


```
java -jar spring-boot-test install
```

By default, the installation will use `<name>` and `<description>` from your `pom.xml`. You can override theses values by passing the prunsrv parameters:


```
java -jar spring-boot-test.jar install --DisplayName "Spring Boot Test" --Description "Just a test"

```

For more prunsrv parameters, refer to the [Prorun documentation](https://commons.apache.org/proper/commons-daemon/procrun.html).

If you prefer to use the default Spring launcher, configure the plugin as follows:


```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>3.3.3</version>
        <configuration>
            <mainClass>br.unesp.fc.credential.UnespCredentialProvider</mainClass>
            <layoutFactory implementation="br.unesp.fc.spring.boot.loader.SpringLayoutFactory">
                <lancherClassName>spring_default</lancherClassName>
            </layoutFactory>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>repackage</goal>
                </goals>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>br.unesp.fc</groupId>
                <artifactId>spring-boot-windows-service</artifactId>
                <version>1.0.0</version>
            </dependency>
        </dependencies>
    </plugin>
```

The `lancherClassName` is the lancher class name. The special keyworkd `spring_default` is used to specify the default Spring launcher. To install it as service, run:

```
java -cp spring-boot-test.jar br.unesp.fc.spring.boot.loader.WindowsServiceJarLauncher install
```

Use `WindowsServiceWarLauncher` for War package. Only JAR e WAR are supported.


## How it works

During the repackage goal, Spring Boot Maven Plugin will look for a [LayoutFactory](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/loader/tools/LayoutFactory.html). This project sets up a `[spring.factories](src/main/resources/META-INF/spring.factories)` for auto-configuration. For more information, see [Custom Layout](https://docs.spring.io/spring-boot/docs/3.1.3/maven-plugin/reference/htmlsingle/#packaging.examples.custom-layout)) documentation.

