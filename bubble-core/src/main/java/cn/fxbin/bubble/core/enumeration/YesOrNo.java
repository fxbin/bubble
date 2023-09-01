package cn.fxbin.bubble.core.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * YesOrNo
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 15:54
 */
@AllArgsConstructor
public enum YesOrNo implements IEnum {

    /**
     * Yes
     */
    YES(1, "Y"),

    /**
     * No
     */
    NO(0, "N");


    private final int value;

    @Getter
    private final String name;

    @Override
    public int value() {
        return this.value;
    }

}
