package com.news.mongo.test;

import com.news.mongo.pojo.ApAssociateWords;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class MongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void saveTest(){
        ApAssociateWords apAssociateWords = new ApAssociateWords();
        apAssociateWords.setAssociateWords("natures");
        apAssociateWords.setCreatedTime(new Date());
        mongoTemplate.save(apAssociateWords);
    }

    @Test
    public void saveFindOne(){
        ApAssociateWords apAssociateWords = mongoTemplate.findById("6537313839887f1d79c22207", ApAssociateWords.class);
        System.out.println(apAssociateWords.toString());
    }

    @Test
    public void find(){
        Query query = Query.query(Criteria.where("associateWords").is("schiff"))
                .with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(query, ApAssociateWords.class);
        System.out.println(apAssociateWords.toString());
    }

    @Test
    public void delete(){
        mongoTemplate.remove(Query.query(Criteria.where("associateWords").is("natures")),ApAssociateWords.class);
    }

}
