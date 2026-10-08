package com.news.model.search.dtos;

import lombok.Data;

import java.util.Date;


@Data
public class UserSearchDto {

    String searchWords;
    int pageNum;

    int pageSize;
    Date minBehotTime;

    public int getFromIndex(){
        if(this.pageNum<1)return 0;
        if(this.pageSize<1) this.pageSize = 10;
        return this.pageSize * (pageNum-1);
    }
}