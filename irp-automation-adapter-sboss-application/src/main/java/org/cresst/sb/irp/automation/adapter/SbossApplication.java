package org.cresst.sb.irp.automation.adapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = { "classpath:application-context.xml" })
public class SbossApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SbossApplication.class, args);
    }

}
