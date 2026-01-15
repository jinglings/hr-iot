package cn.iocoder.yudao.module.iot.dal.mysql.scada;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaDashboardDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SCADA 仪表板配置 Mapper
 *
 * Part of SCADA-011: Create SCADA Mappers
 *
 * @author HR-IoT Team
 */
@Mapper
public interface IotScadaDashboardMapper extends BaseMapperX<IotScadaDashboardDO> {

    /**
     * 根据仪表板 ID 查询
     *
     * @param dashboardId FUXA 仪表板 ID
     * @return 仪表板配置
     */
    default IotScadaDashboardDO selectByDashboardId(String dashboardId) {
        return selectOne(IotScadaDashboardDO::getDashboardId, dashboardId);
    }

    /**
     * 查询启用状态的仪表板列表
     *
     * @return 仪表板列表
     */
    default List<IotScadaDashboardDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotScadaDashboardDO>()
                .eqIfPresent(IotScadaDashboardDO::getStatus, status)
                .orderByAsc(IotScadaDashboardDO::getSortOrder)
                .orderByDesc(IotScadaDashboardDO::getId));
    }

    /**
     * 根据仪表板类型查询列表
     *
     * @param dashboardType 仪表板类型
     * @return 仪表板列表
     */
    default List<IotScadaDashboardDO> selectListByType(String dashboardType) {
        return selectList(new LambdaQueryWrapperX<IotScadaDashboardDO>()
                .eq(IotScadaDashboardDO::getDashboardType, dashboardType)
                .eq(IotScadaDashboardDO::getStatus, 1) // 只查询启用的
                .orderByAsc(IotScadaDashboardDO::getSortOrder));
    }

    /**
     * 查询默认仪表板
     *
     * @return 默认仪表板
     */
    default IotScadaDashboardDO selectDefault() {
        return selectOne(new LambdaQueryWrapperX<IotScadaDashboardDO>()
                .eq(IotScadaDashboardDO::getIsDefault, true)
                .eq(IotScadaDashboardDO::getStatus, 1));
    }

    /**
     * 分页查询仪表板
     *
     * @param name          仪表板名称（模糊匹配）
     * @param dashboardType 仪表板类型
     * @param status        状态
     * @param pageNo        页码
     * @param pageSize      每页数量
     * @return 分页结果
     */
    default PageResult<IotScadaDashboardDO> selectPage(String name, String dashboardType,
            Integer status, Integer pageNo, Integer pageSize) {
        cn.iocoder.yudao.framework.common.pojo.PageParam pageParam = new cn.iocoder.yudao.framework.common.pojo.PageParam();
        pageParam.setPageNo(pageNo != null ? pageNo : 1);
        pageParam.setPageSize(pageSize != null ? pageSize : 10);
        return selectPage(pageParam,
                new LambdaQueryWrapperX<IotScadaDashboardDO>()
                        .likeIfPresent(IotScadaDashboardDO::getName, name)
                        .eqIfPresent(IotScadaDashboardDO::getDashboardType, dashboardType)
                        .eqIfPresent(IotScadaDashboardDO::getStatus, status)
                        .orderByAsc(IotScadaDashboardDO::getSortOrder)
                        .orderByDesc(IotScadaDashboardDO::getId));
    }

    /**
     * 统计指定类型的仪表板数量
     *
     * @param dashboardType 仪表板类型
     * @return 数量
     */
    default Long selectCountByType(String dashboardType) {
        return selectCount(new LambdaQueryWrapperX<IotScadaDashboardDO>()
                .eq(IotScadaDashboardDO::getDashboardType, dashboardType));
    }

    /**
     * 清除默认仪表板标记（设置新默认前调用）
     */
    default void clearDefaultFlag() {
        update(IotScadaDashboardDO.builder().isDefault(false).build(),
                new LambdaQueryWrapperX<IotScadaDashboardDO>()
                        .eq(IotScadaDashboardDO::getIsDefault, true));
    }

}
