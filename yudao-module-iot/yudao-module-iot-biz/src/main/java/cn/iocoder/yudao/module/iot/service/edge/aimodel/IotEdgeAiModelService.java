package cn.iocoder.yudao.module.iot.service.edge.aimodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;

import javax.validation.Valid;

/**
 * IoT 边缘AI模型 Service 接口
 *
 * @author AI Assistant
 */
public interface IotEdgeAiModelService {

    /**
     * 创建AI模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModel(@Valid IotEdgeAiModelCreateReqVO createReqVO);

    /**
     * 更新AI模型
     *
     * @param updateReqVO 更新信息
     */
    void updateModel(@Valid IotEdgeAiModelUpdateReqVO updateReqVO);

    /**
     * 删除AI模型
     *
     * @param id 编号
     */
    void deleteModel(Long id);

    /**
     * 获得AI模型
     *
     * @param id 编号
     * @return AI模型
     */
    IotEdgeAiModelDO getModel(Long id);

    /**
     * 获得AI模型分页
     *
     * @param pageReqVO 分页查询
     * @return AI模型分页
     */
    PageResult<IotEdgeAiModelDO> getModelPage(IotEdgeAiModelPageReqVO pageReqVO);

}
