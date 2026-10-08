package com.news.model.schedule.pojos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "taskinfo_logs")
public class TaskinfoLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private Date executeTime;

    @Lob
    private byte[] parameters;

    private Integer priority;

    private Integer taskType;

    @Version
    private Integer version;

    /**
     *  0=int 1=EXECUTED 2=CANCELLED
     */
    private Integer status;


}
