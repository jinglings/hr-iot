package cn.iocoder.yudao.module.report.service.visualization;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardCreateReqVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardPageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardUpdateReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.visualization.VisualizationDashboardDO;

import javax.validation.Valid;

/**
 * 可视化大屏 Service 接口
 *
 * @author 芋道源码
 */
public interface VisualizationDashboardService {

    /**
     * 创建可视化大屏
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDashboard(@Valid VisualizationDashboardCreateReqVO createReqVO);

    /**
     * 更新可视化大屏
     *
     * @param updateReqVO 更新信息
     */
    void updateDashboard(@Valid VisualizationDashboardUpdateReqVO updateReqVO);

    /**
     * 删除可视化大屏
     *
     * @param id 编号
     */
    void deleteDashboard(Long id);

    /**
     * 获得可视化大屏
     *
     * @param id 编号
     * @return 可视化大屏
     */
    VisualizationDashboardDO getDashboard(Long id);

    /**
     * 获得我的可视化大屏分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 可视化大屏分页
     */
    PageResult<VisualizationDashboardDO> getDashboardPage(VisualizationDashboardPageReqVO pageReqVO, Long userId);

    /**
     * 获得公开的可视化大屏分页
     *
     * @param pageReqVO 分页查询
     * @return 可视化大屏分页
     */
    PageResult<VisualizationDashboardDO> getPublicDashboardPage(VisualizationDashboardPageReqVO pageReqVO);

    /**
     * 复制可视化大屏
     *
     * @param id 编号
     * @return 新大屏编号
     */
    Long copyDashboard(Long id);

    /**
     * 发布/取消发布可视化大屏
     *
     * @param id 编号
     * @param status 状态
     */
    void updateDashboardStatus(Long id, Integer status);
}
