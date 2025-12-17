package cn.fxbin.bubble.data.mybatisplus.mapper;

import cn.fxbin.bubble.core.dataobject.PageRequest;
import cn.fxbin.bubble.core.dataobject.PageResult;
import cn.fxbin.bubble.data.mybatisplus.util.PageUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * BaseMapperX
 *
 * <p>
 *     基于 {@link BaseMapper} 扩展
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/19 23:50
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    /**
     * 保存或更新
     * saveOrUpdate
     *
     * @param entity 实体
     * @return boolean
     * @since 2023/8/19 23:53
     */
    default boolean saveOrUpdate(T entity) {
        return Db.saveOrUpdate(entity);
    }

    /**
     * 批量保存或更新
     *
     * @param collection 集合
     */
    default void saveOrUpdateBatch(Collection<T> collection) {
        Db.saveOrUpdateBatch(collection);
    }

    /**
     * 批量插入，适合大量数据插入
     *
     * @param entities 实体们
     */
    default void insertBatch(Collection<T> entities) {
        Db.saveBatch(entities);
    }

    /**
     * 批量插入，适合大量数据插入
     *
     * @param entities 实体们
     * @param size     插入数量 Db.saveBatch 默认为 1000
     */
    default void insertBatch(Collection<T> entities, int size) {
        Db.saveBatch(entities, size);
    }

    /**
     * 根据 id 查询
     *
     * @param id id
     * @return {@link Optional}<{@link T}>
     */
    default Optional<T> findById(Serializable id) {
        return Optional.ofNullable(this.selectById(id));
    }

    /**
     * 分页查询
     *
     * @param pageRequest  页面请求
     * @param queryWrapper 查询包装
     * @return {@link PageResult}<{@link T}>
     */
    default PageResult<T> selectPage(PageRequest pageRequest, @Param("ew") Wrapper<T> queryWrapper) {
        // MyBatis Plus 查询
        IPage<T> mpPage = PageUtils.buildPage(pageRequest);
        selectPage(mpPage, queryWrapper);
        // 转换返回
        return PageUtils.buildPageResult(pageRequest, mpPage);
    }

    /**
     * 查询一条记录
     *
     * @param field 字段
     * @param value 值
     * @return {@link T}
     */
    default T selectOne(String field, Object value) {
        return selectOne(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询一条记录
     *
     * @param field 字段
     * @param value 值
     * @return {@link T}
     */
    default T selectOne(SFunction<T, ?> field, Object value) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询记录数
     *
     * @return {@link Long}
     */
    default Long selectCount() {
        return selectCount(new QueryWrapper<T>());
    }

    /**
     * 查询记录数
     *
     * @param field 字段
     * @param value 值
     * @return {@link Long}
     */
    default Long selectCount(String field, Object value) {
        return selectCount(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询记录数
     *
     * @param field 字段
     * @param value 值
     * @return {@link Long}
     */
    default Long selectCount(SFunction<T, ?> field, Object value) {
        return selectCount(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询列表
     *
     * @return {@link List}<{@link T}>
     */
    default List<T> selectList() {
        return selectList(new QueryWrapper<>());
    }

    /**
     * 查询列表
     *
     * @param field 字段
     * @param value 值
     * @return {@link List}<{@link T}>
     */
    default List<T> selectList(String field, Object value) {
        return selectList(new QueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询列表
     *
     * @param field 字段
     * @param value 值
     * @return {@link List}<{@link T}>
     */
    default List<T> selectList(SFunction<T, ?> field, Object value) {
        return selectList(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询列表
     *
     * @param field  字段
     * @param values 值
     * @return {@link List}<{@link T}>
     */
    default List<T> selectList(String field, Collection<?> values) {
        return selectList(new QueryWrapper<T>().in(field, values));
    }

    /**
     * 查询列表
     *
     * @param field  字段
     * @param values 值
     * @return {@link List}<{@link T}>
     */
    default List<T> selectList(SFunction<T, ?> field, Collection<?> values) {
        return selectList(new LambdaQueryWrapper<T>().in(field, values));
    }

}
