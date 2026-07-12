package c4f.vannang.vaops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication(scanBasePackages = "c4f.vannang.vaops", nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class VaopsApplication {

  public static void main(String[] args) {
    SpringApplication.run(VaopsApplication.class, args);
  }
}
