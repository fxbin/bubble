package cn.fxbin.bubble.fireworks.data.mybatis.customizer;

import cn.fxbin.bubble.fireworks.core.util.IdGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * CustomizeIdentifierGenerator
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 19:11
 */
public class CustomizeIdentifierGenerator implements IdentifierGenerator {
    @Override
    public Number nextId(Object entity) {
        IdGenerator idGenerator = new IdGenerator();
        return idGenerator.nextId();
    }
}
