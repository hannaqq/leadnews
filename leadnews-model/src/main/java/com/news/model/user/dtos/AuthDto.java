package com.news.model.user.dtos;

import lombok.Data;

@Data
public class AuthDto {
    private Integer id;
    private String msg;
    private Integer page;
    private Integer size;
    private Integer status;

    public void checkParam(){
        if(this.page == null || this.page < 0){
            setPage(1);
        }
        if(this.size == null || this.size < 0 || this.size > 100){
            setSize(10);
        }
    }
}
