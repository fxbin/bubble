package cn.fxbin.bubble.ai.mapper;

import cn.fxbin.bubble.ai.domain.entity.AiModelConfig;
import cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型配置 Mapper
 * <p>负责AI模型配置的数据库操作</p>
 *
 * @author fxbin
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapperX<AiModelConfig> {
}
