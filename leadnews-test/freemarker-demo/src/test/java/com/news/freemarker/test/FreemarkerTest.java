package com.news.freemarker.test;

import com.news.freemarker.FreemarkerDemoApplication;
import com.news.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = FreemarkerDemoApplication.class)
@ExtendWith(SpringExtension.class)
public class FreemarkerTest {
   @TempDir
   Path tempDirectory;

   @Autowired
   private Configuration configuration;

   @Test
   public void test() throws IOException, TemplateException {
    Template template = configuration.getTemplate("02-list.ftl");
    try (var writer = Files.newBufferedWriter(tempDirectory.resolve("list.html"))) {
        template.process(getData(), writer);
    }

   }

   private Map getData(){
    Map<String,Object> map = new HashMap<>();
    Student stu1 = new Student();
    stu1.setName("aa");
    stu1.setAge(18);
    stu1.setMoney(2000.1f);
    stu1.setBirthday(new Date());

    Student stu2 = new Student();
    stu2.setName("bb");
    stu2.setAge(19);
    stu2.setMoney(200.1f);

    ArrayList<Student> stus = new ArrayList<>();
    stus.add(stu1);
    stus.add(stu2);

    map.put("stus",stus);

    HashMap<String, Student> stuMap = new HashMap<>();
    stuMap.put("stu1",stu1);
    stuMap.put("stu2",stu2);
    map.put("stuMap",stuMap);
    map.put("today",new Date());
    return map;

   }
}
