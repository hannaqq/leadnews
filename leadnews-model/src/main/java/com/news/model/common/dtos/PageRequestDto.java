package com.news.model.common.dtos;

import lombok.Data;

@Data
public class PageRequestDto {
    protected Integer size;
    protected Integer page;
    public void checkParam(){
        if(this.page == null || this.page < 0){
            setPage(1);
        }
        if(this.size == null || this.size < 0 || this.size > 100){
            setSize(10);
        }
    }
}
