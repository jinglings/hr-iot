package cn.iocoder.yudao.module.report.dal.mysql.visualization;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardPageReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.visualization.VisualizationDashboardDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 可视化大屏 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VisualizationDashboardMapper extends BaseMapperX<VisualizationDashboardDO> {

    default PageResult<VisualizationDashboardDO> selectPage(VisualizationDashboardPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisualizationDashboardDO>()
                .eqIfPresent(VisualizationDashboardDO::getCreator, userId)
                .likeIfPresent(VisualizationDashboardDO::getName, reqVO.getName())
                .eqIfPresent(VisualizationDashboardDO::getCategory, reqVO.getCategory())
                .eqIfPresent(VisualizationDashboardDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VisualizationDashboardDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(VisualizationDashboardDO::getId));
    }

    default PageResult<VisualizationDashboardDO> selectPageByPublic(VisualizationDashboardPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VisualizationDashboardDO>()
                .eq(VisualizationDashboardDO::getIsPublic, true)
                .eq(VisualizationDashboardDO::getStatus, 1) // 已发布
                .likeIfPresent(VisualizationDashboardDO::getName, reqVO.getName())
                .eqIfPresent(VisualizationDashboardDO::getCategory, reqVO.getCategory())
                .orderByDesc(VisualizationDashboardDO::getCreateTime));
    }
}
