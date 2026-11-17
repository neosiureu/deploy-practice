package edu.kh.deploy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class DeployPracticeApplication{
    public static void main(String []args){
        System.exit(1); // step 5 test: dies right away
        SpringApplication.run(DeployPracticeApplication.class,args);
    }
}