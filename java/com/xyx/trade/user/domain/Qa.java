package com.xyx.trade.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xyx_qa")
public class Qa {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String question;

    private String answer;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
