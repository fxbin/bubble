package cn.fxbin.bubble.fireworks.core.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * YesOrNo
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 15:54
 */
@Getter
@AllArgsConstructor
public enum YesOrNo {

    /**
     *
     */
    YES(0, "Y"),

    /**
     *
     */
    NO(1, "N");


    final int value;

    final String desc;

}
