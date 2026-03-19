package cn.fxbin.bubble.ai.service;

import cn.fxbin.bubble.ai.domain.entity.AiModelGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI model group service.
 *
 * @author fxbin
 */
public interface AiModelGroupService extends IService<AiModelGroup> {

    /**
     * Resolve an enabled group by id or code.
     *
     * @param identifier group id or code
     * @return enabled group, or {@code null} when not found
     */
    AiModelGroup getEnabledGroup(String identifier);

    /**
     * List all enabled groups.
     *
     * @return enabled groups
     */
    List<AiModelGroup> listEnabledGroups();

    /**
     * Validate unique group code.
     *
     * @param groupCode group code
     * @param id current id for updates
     */
    void validateGroupCodeUnique(String groupCode, Long id);

    void createGroup(AiModelGroup aiModelGroup);

    void updateGroup(AiModelGroup aiModelGroup);

    void removeGroup(Long id);
}
