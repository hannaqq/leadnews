package com.news.model.common.enums;

public enum AppHttpCodeEnum {
    SUCCESS(200,"success"),
    PARAM_INVALID(501,"Invalid parameter"),
    NEED_LOGIN(1,"please login"),
    LOGIN_PASSWORD_ERROR(2,"password error"),
    SERVER_ERROR(503,"server error"),
    DATA_EXIST(1000,"data exist"),
    AP_USER_DATA_NOT_EXIST(1001,"ApUser not exist"),
    DATA_NOT_EXIST(1002,"data not exist"),
    MATERIAL_REFERENCE_FAIL(3501,"materials reference fail"),

    MATERIAL_REFERENCED(3502,"material is used"),

    HAVE_FOLLOWED(2503,"have followed before");


    int code;
    String errorMessage;

    AppHttpCodeEnum(int code, String errorMessage) {
            this.code = code;





















































































            this.errorMessage = errorMessage;
        }

        public int getCode() {
            return code;
        }

        public String  getErrorMessage() {
            return errorMessage;
    }
}
