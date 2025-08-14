package org.example.mega_crew;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableJpaRepositories(
        basePackages = "org.example.mega_crew.domain.*.repository",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaRepository.class)
)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MegaCrewApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    // 실제 값이 잘 들어오는지 로그로 찍어보세요!
    System.out.println("DB_URL = " + System.getProperty("DB_URL"));
    System.out.println("DB_USERNAME = " + System.getProperty("DB_USERNAME"));
    System.out.println("DB_PASSWORD = " + System.getProperty("DB_PASSWORD"));

    SpringApplication.run(MegaCrewApplication.class, args);

  }

}
