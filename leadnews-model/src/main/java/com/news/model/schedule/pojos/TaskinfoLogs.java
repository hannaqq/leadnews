package com.news.model.schedule.pojos;

import jakarta.persistence.Column;
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

/**
 * <p>
 * 
 * </p>
 *
 * @author itheima
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "taskinfo_logs")
public class TaskinfoLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "task_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    @Column(name = "execute_time")
    private Date executeTime;

    @Lob
    @Column(name = "parameters")
    private byte[] parameters;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "task_type")
    private Integer taskType;

    @Version
    @Column(name = "version")
    private Integer version;

    /**
     *  0=int 1=EXECUTED 2=CANCELLED
     */
    @Column(name = "status")
    private Integer status;


}
