package com.news.model.wemedia.dtos;

import lombok.Data;

@Data
public class ChannelDto {
    private String name;

    //private Boolean status;
    private Integer page;
    private Integer size;
    public void checkParam(){
        if(this.page == null || this.page < 1){
            setPage(1);
        }
        if(this.size == null || this.size < 0 || this.size > 100){
            setSize(10);
        }
    }
}
