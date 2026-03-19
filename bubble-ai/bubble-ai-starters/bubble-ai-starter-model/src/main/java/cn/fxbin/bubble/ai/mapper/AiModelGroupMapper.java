package cn.fxbin.bubble.ai.mapper;

import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import cn.fxbin.bubble.data.mybatisplus.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型分组 Mapper
 * <p>负责模型分组元数据的数据库访问操作</p>
 *
 * @author fxbin
 * @since 2026/03/19 16:47
 */
@Mapper
public interface AiModelGroupMapper extends BaseMapperX<AiModelGroup> {
}
