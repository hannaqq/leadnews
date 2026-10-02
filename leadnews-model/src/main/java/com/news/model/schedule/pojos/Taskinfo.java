package com.news.model.schedule.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
@Table(name = "taskinfo")
public class Taskinfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


}
