package test.web.dto;

import cn.fxbin.bubble.fireworks.web.validator.EqualField;
import lombok.Data;

/**
 * TestEqualDTO
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/16 1:09
 */
@Data
@EqualField(source = "a", target = "b")
public class TestEqualDTO {

    private String a;

    private String b;
}
