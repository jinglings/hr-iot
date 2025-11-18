package cn.iocoder.yudao.module.report.convert.visualization;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardCreateReqVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardRespVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardUpdateReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.visualization.VisualizationDashboardDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 可视化大屏 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface VisualizationDashboardConvert {

    VisualizationDashboardConvert INSTANCE = Mappers.getMapper(VisualizationDashboardConvert.class);

    VisualizationDashboardDO convert(VisualizationDashboardCreateReqVO bean);

    VisualizationDashboardDO convert(VisualizationDashboardUpdateReqVO bean);

    VisualizationDashboardRespVO convert(VisualizationDashboardDO bean);

    List<VisualizationDashboardRespVO> convertList(List<VisualizationDashboardDO> list);

    PageResult<VisualizationDashboardRespVO> convertPage(PageResult<VisualizationDashboardDO> page);
}
