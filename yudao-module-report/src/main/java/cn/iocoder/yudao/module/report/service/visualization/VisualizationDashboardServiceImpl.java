package cn.iocoder.yudao.module.report.service.visualization;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardCreateReqVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardPageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.VisualizationDashboardUpdateReqVO;
import cn.iocoder.yudao.module.report.convert.visualization.VisualizationDashboardConvert;
import cn.iocoder.yudao.module.report.dal.dataobject.visualization.VisualizationDashboardDO;
import cn.iocoder.yudao.module.report.dal.mysql.visualization.VisualizationDashboardMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;

/**
 * 可视化大屏 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class VisualizationDashboardServiceImpl implements VisualizationDashboardService {

    @Resource
    private VisualizationDashboardMapper dashboardMapper;

    @Override
    public Long createDashboard(VisualizationDashboardCreateReqVO createReqVO) {
        // 插入
        VisualizationDashboardDO dashboard = VisualizationDashboardConvert.INSTANCE.convert(createReqVO);
        dashboardMapper.insert(dashboard);
        // 返回
        return dashboard.getId();
    }

    @Override
    public void updateDashboard(VisualizationDashboardUpdateReqVO updateReqVO) {
        // 校验存在
        validateDashboardExists(updateReqVO.getId());
        // 更新
        VisualizationDashboardDO updateObj = VisualizationDashboardConvert.INSTANCE.convert(updateReqVO);
        dashboardMapper.updateById(updateObj);
    }

    @Override
    public void deleteDashboard(Long id) {
        // 校验存在
        validateDashboardExists(id);
        // 删除
        dashboardMapper.deleteById(id);
    }

    private void validateDashboardExists(Long id) {
        if (dashboardMapper.selectById(id) == null) {
            throw exception(VISUALIZATION_DASHBOARD_NOT_EXISTS);
        }
    }

    @Override
    public VisualizationDashboardDO getDashboard(Long id) {
        return dashboardMapper.selectById(id);
    }

    @Override
    public PageResult<VisualizationDashboardDO> getDashboardPage(VisualizationDashboardPageReqVO pageReqVO, Long userId) {
        return dashboardMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public PageResult<VisualizationDashboardDO> getPublicDashboardPage(VisualizationDashboardPageReqVO pageReqVO) {
        return dashboardMapper.selectPageByPublic(pageReqVO);
    }

    @Override
    public Long copyDashboard(Long id) {
        // 校验存在
        VisualizationDashboardDO sourceDashboard = getDashboard(id);
        if (sourceDashboard == null) {
            throw exception(VISUALIZATION_DASHBOARD_NOT_EXISTS);
        }

        // 复制并插入
        VisualizationDashboardDO newDashboard = VisualizationDashboardDO.builder()
                .name(sourceDashboard.getName() + " - 副本")
                .description(sourceDashboard.getDescription())
                .picUrl(sourceDashboard.getPicUrl())
                .thumbnail(sourceDashboard.getThumbnail())
                .config(sourceDashboard.getConfig())
                .width(sourceDashboard.getWidth())
                .height(sourceDashboard.getHeight())
                .backgroundColor(sourceDashboard.getBackgroundColor())
                .backgroundImage(sourceDashboard.getBackgroundImage())
                .scaleMode(sourceDashboard.getScaleMode())
                .status(0) // 未发布
                .isPublic(false) // 默认私有
                .category(sourceDashboard.getCategory())
                .tags(sourceDashboard.getTags())
                .remark(sourceDashboard.getRemark())
                .build();

        dashboardMapper.insert(newDashboard);
        return newDashboard.getId();
    }

    @Override
    public void updateDashboardStatus(Long id, Integer status) {
        // 校验存在
        validateDashboardExists(id);

        // 更新状态
        VisualizationDashboardDO updateObj = new VisualizationDashboardDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        dashboardMapper.updateById(updateObj);
    }
}
