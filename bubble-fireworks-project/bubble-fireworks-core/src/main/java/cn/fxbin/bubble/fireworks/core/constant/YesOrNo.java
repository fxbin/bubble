package cn.fxbin.bubble.fireworks.core.constant;

import lombok.AllArgsConstructor;
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
    YES(1, "Y"),

    /**
     *
     */
    NO(0, "N");


    final int value;

    final String desc;

}
