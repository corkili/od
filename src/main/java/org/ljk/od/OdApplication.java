package org.ljk.od;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OdApplication {

//    @Bean
//    InitializingBean saveData(ODRepository repository) {
//        return () -> {
//            repository.save(new OpticalDevice("name1", "type1", "firstCategory1",
//                    "secondCategory1", 1.0, "characteristics1", "describe1",
//                    "factory1"));
//            repository.save(new OpticalDevice("name2", "type2", "firstCategory2",
//                    "secondCategory2", 2.0, "characteristics2", "describe2",
//                    "factory2"));
//            repository.save(new OpticalDevice("name3", "type3", "firstCategory3",
//                    "secondCategory3", 3.0, "characteristics3", "describe3",
//                    "factory3"));
//            repository.save(new OpticalDevice("name4", "type4", "firstCategory4",
//                    "secondCategory4", 4.0, "characteristics4", "describe4",
//                    "factory4"));
//        };
//    }

    public static void main(String[] args) {
        SpringApplication.run(OdApplication.class, args);
    }
}
