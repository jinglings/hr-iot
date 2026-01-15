package cn.iocoder.yudao.module.iot.service.thingmodel.template;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IoT 物模型模板 Service 接口
 *
 * @author AI
 */
public interface IotThingModelTemplateService {

    // ========== 分类管理 ==========

    /**
     * 获取所有分类列表
     */
    List<IotThingModelTemplateCategoryDO> getCategoryList();

    // ========== 模板管理 ==========

    /**
     * 创建自定义模板
     */
    Long createTemplate(@Valid IotThingModelTemplateCreateReqVO createReqVO);

    /**
     * 更新模板
     */
    void updateTemplate(@Valid IotThingModelTemplateCreateReqVO updateReqVO, Long id);

    /**
     * 删除模板
     */
    void deleteTemplate(Long id);

    /**
     * 获取模板详情
     */
    IotThingModelTemplateDO getTemplate(Long id);

    /**
     * 获取模板列表
     */
    List<IotThingModelTemplateDO> getTemplateList(IotThingModelTemplateListReqVO reqVO);

    /**
     * 应用模板到产品
     */
    void applyTemplateToProduct(@Valid IotThingModelTemplateApplyReqVO applyReqVO);

    /**
     * 导出产品物模型为JSON
     */
    String exportThingModel(Long productId);

    /**
     * 从JSON导入物模型到产品
     */
    void importThingModel(Long productId, String tslJson, Boolean overwrite);

}
