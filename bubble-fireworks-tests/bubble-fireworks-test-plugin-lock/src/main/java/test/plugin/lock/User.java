package test.plugin.lock;

import lombok.Data;

/**
 * User
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/7 17:28
 */
@Data
public class User {

    private Integer id;

    private Test test;

    @Data
    public static class Test{
        private String number;
    }

}
