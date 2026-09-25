package com.news.freemarker.controller;


import com.news.freemarker.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Controller
public class HelloController {
    public static final Student STUDENT = new Student();

    @GetMapping("/basic")
    public String hello(Model model){
        model.addAttribute("name","freemarker");
        Student student = new Student();
        student.setName("aa");
        student.setAge(18);
        model.addAttribute("stu",student);
        return "01-basic";
    }

    @GetMapping("/list")
    public String list(Model model){
        Student stu1 = new Student();
        stu1.setName("aa");
        stu1.setAge(18);
        stu1.setMoney(2000.1f);
        stu1.setBirthday(new Date());

        Student stu2 = new Student();
        stu2.setName("bb");
        stu2.setAge(19);
        stu2.setMoney(200.1f);
        //stu2.setBirthday(new Date());

        ArrayList<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        model.addAttribute("stus",stus);

        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        model.addAttribute("stuMap",stuMap);
        model.addAttribute("today",new Date());
        return "02-list";
    }



}
