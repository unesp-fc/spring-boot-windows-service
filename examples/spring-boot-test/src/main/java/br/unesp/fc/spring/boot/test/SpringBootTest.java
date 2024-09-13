package br.unesp.fc.spring.boot.test;

import java.lang.reflect.Method;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringBootTest {

    private static ConfigurableApplicationContext context;

    // Regular main method
    // used for run command and to help Spring to find main class
    // not used when running as service
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringBootTest.class, args);
    }

    // Start service method
    public static void start(String args[]) {
        context = SpringApplication.run(SpringBootTest.class, args);
    }

    // Stop service method
    public static void stop(String args[]) {
        SpringApplication.exit(context);
    }

}
