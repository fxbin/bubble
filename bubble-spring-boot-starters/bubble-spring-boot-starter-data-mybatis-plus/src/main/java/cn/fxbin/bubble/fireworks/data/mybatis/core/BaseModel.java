package cn.fxbin.bubble.fireworks.data.mybatis.core;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BaseModel
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/23 10:46
 */
@Data
public class BaseModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Boolean deleted;

}
