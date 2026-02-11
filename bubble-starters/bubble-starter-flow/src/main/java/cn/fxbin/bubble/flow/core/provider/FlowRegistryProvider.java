
package cn.fxbin.bubble.flow.core.provider;

import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.flow.core.builder.FlowExpressionBuilder;
import cn.fxbin.bubble.flow.core.enums.FlowPublishStatus;
import cn.fxbin.bubble.flow.core.exception.FlowNotFoundException;
import cn.fxbin.bubble.flow.core.mapper.FlowDefinitionMapper;
import cn.fxbin.bubble.flow.core.model.entity.FlowDefinition;
import cn.fxbin.bubble.flow.core.util.FlowUtils;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.flow.FlowBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * FlowRegistryProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/22 10:32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowRegistryProvider {

    private final FlowExpressionBuilder expressionBuilder;

    private final FlowDefinitionMapper flowDefinitionMapper;

    /**
     * 发布流程（动态注册到LiteFlow）
     */
    @Transactional(rollbackFor = {FlowNotFoundException.class, Exception.class})
    public String registry(Long flowId) {
        return this.registry(flowId, FlowUtils.generateExecutionId(flowId));
    }


    @Transactional(rollbackFor = {FlowNotFoundException.class, Exception.class})
    public String registry(Long flowId, String executeId) {
        try {
            FlowDefinition flow = flowDefinitionMapper.findById(flowId)
                    .orElseThrow(() -> new FlowNotFoundException(StringUtils.utf8Str(flowId)));

            String chainId = StringUtils.isNotEmpty(executeId) ? executeId : FlowUtils.generateExecutionId(flowId);

            // 1. 删除旧流程
            FlowBus.removeChain(chainId);

            // 2. 生成新表达式
            String expr = expressionBuilder.buildExpression(flowId);
            log.info("发布流程: {}, 表达式: {}", flowId, expr);

            // 4. 更新状态
            flow.setStatus(FlowPublishStatus.PUBLISHED);
            // 5. 更新表达式
            flow.setEl(expr);
            flowDefinitionMapper.saveOrUpdate(flow);

            // 3. 动态注册（事务提交后执行，避免 DB 回滚但内存链已生效）
            registerAfterCommit(chainId, expr);

            return chainId;
        } catch (FlowNotFoundException e) {
            log.error("流程发布失败，未找到对应流程ID: {}", flowId, e);
            // 确保异常被抛出以触发回滚
            throw e;
        } catch (Exception e) {
            log.error("流程发布失败，发生未知错误: {}", flowId, e);
            // 确保异常被抛出以触发回滚
            throw e;
        }
    }


    public void unRegistry(String chainId) {
        FlowBus.removeChain(chainId);
        log.info("取消注册流程: {}", chainId);
    }

    /**
     * 在事务提交后注册链。
     * <p>若当前无事务，立即注册；若有事务，则在 {@code afterCommit} 阶段注册。</p>
     *
     * @param chainId 链ID
     * @param expr LiteFlow EL 表达式
     */
    private void registerAfterCommit(String chainId, String expr) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            doRegister(chainId, expr);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                doRegister(chainId, expr);
            }
        });
    }

    /**
     * 实际执行 LiteFlow 链注册。
     *
     * @param chainId 链ID
     * @param expr LiteFlow EL 表达式
     */
    private void doRegister(String chainId, String expr) {
        LiteFlowChainELBuilder.createChain()
                .setChainId(chainId)
                .setEL(expr)
                .build();
    }

}
