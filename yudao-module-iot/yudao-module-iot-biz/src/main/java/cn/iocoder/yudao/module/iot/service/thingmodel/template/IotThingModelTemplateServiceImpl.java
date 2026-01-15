package cn.iocoder.yudao.module.iot.service.thingmodel.template;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotThingModelTemplateCategoryMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotThingModelTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 物模型模板 Service 实现类
 *
 * @author AI
 */
@Service
@Validated
public class IotThingModelTemplateServiceImpl implements IotThingModelTemplateService {

    @Resource
    private IotThingModelTemplateCategoryMapper categoryMapper;

    @Resource
    private IotThingModelTemplateMapper templateMapper;

    // ========== 分类管理 ==========

    @Override
    public List<IotThingModelTemplateCategoryDO> getCategoryList() {
        return categoryMapper.selectList();
    }

    // ========== 模板管理 ==========

    @Override
    public Long createTemplate(IotThingModelTemplateCreateReqVO createReqVO) {
        // 1. 校验分类存在
        validateCategoryExists(createReqVO.getCategoryId());

        // 2. 校验编码唯一
        validateTemplateCodeUnique(null, createReqVO.getCode());

        // 3. 校验TSL格式
        validateTslFormat(createReqVO.getTsl());

        // 4. 插入
        IotThingModelTemplateDO template = BeanUtils.toBean(createReqVO, IotThingModelTemplateDO.class);
        template.setIsSystem(false);
        template.setUsageCount(0);
        template.setStatus(1);
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(IotThingModelTemplateCreateReqVO updateReqVO, Long id) {
        // 1. 校验存在
        IotThingModelTemplateDO template = validateTemplateExists(id);

        // 2. 系统模板不允许修改
        if (Boolean.TRUE.equals(template.getIsSystem())) {
            throw exception(THING_MODEL_TEMPLATE_SYSTEM_CANNOT_UPDATE);
        }

        // 3. 校验编码唯一
        if (updateReqVO.getCode() != null) {
            validateTemplateCodeUnique(id, updateReqVO.getCode());
        }

        // 4. 校验TSL格式
        if (updateReqVO.getTsl() != null) {
            validateTslFormat(updateReqVO.getTsl());
        }

        // 5. 更新
        IotThingModelTemplateDO updateObj = BeanUtils.toBean(updateReqVO, IotThingModelTemplateDO.class);
        updateObj.setId(id);
        templateMapper.updateById(updateObj);
    }

    @Override
    public void deleteTemplate(Long id) {
        // 1. 校验存在
        IotThingModelTemplateDO template = validateTemplateExists(id);

        // 2. 系统模板不允许删除
        if (Boolean.TRUE.equals(template.getIsSystem())) {
            throw exception(THING_MODEL_TEMPLATE_SYSTEM_CANNOT_DELETE);
        }

        // 3. 删除
        templateMapper.deleteById(id);
    }

    @Override
    public IotThingModelTemplateDO getTemplate(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public List<IotThingModelTemplateDO> getTemplateList(IotThingModelTemplateListReqVO reqVO) {
        return templateMapper.selectList(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyTemplateToProduct(IotThingModelTemplateApplyReqVO applyReqVO) {
        // 1. 校验模板存在
        IotThingModelTemplateDO template = validateTemplateExists(applyReqVO.getTemplateId());

        // 2. 增加使用次数
        templateMapper.incrementUsageCount(template.getId());

        // 3. 导入物模型到产品
        // TODO: 调用物模型Service创建属性/事件/服务
        // 此处需要与现有物模型模块集成
        importThingModel(applyReqVO.getProductId(), template.getTsl(),
                applyReqVO.getOverwrite() != null ? applyReqVO.getOverwrite() : false);
    }

    @Override
    public String exportThingModel(Long productId) {
        // TODO: 调用物模型Service获取产品物模型并转换为TSL格式
        // 此处返回模拟数据
        return "{\"profile\":{\"version\":\"1.0\"},\"properties\":[],\"events\":[],\"services\":[]}";
    }

    @Override
    public void importThingModel(Long productId, String tslJson, Boolean overwrite) {
        // 1. 校验TSL格式
        validateTslFormat(tslJson);

        // 2. TODO: 解析TSL并调用物模型Service创建属性/事件/服务
        // 此处需要与现有物模型模块集成
        // 可以解析TSL JSON，遍历properties/events/services，调用对应的创建接口
    }

    // ========== 校验方法 ==========

    private void validateCategoryExists(Long categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw exception(THING_MODEL_TEMPLATE_CATEGORY_NOT_EXISTS);
        }
    }

    private IotThingModelTemplateDO validateTemplateExists(Long id) {
        IotThingModelTemplateDO template = templateMapper.selectById(id);
        if (template == null) {
            throw exception(THING_MODEL_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateTemplateCodeUnique(Long id, String code) {
        IotThingModelTemplateDO existing = templateMapper.selectByCode(code);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(THING_MODEL_TEMPLATE_CODE_EXISTS);
        }
    }

    private void validateTslFormat(String tsl) {
        if (StrUtil.isBlank(tsl)) {
            throw exception(THING_MODEL_TEMPLATE_TSL_INVALID);
        }
        try {
            // 简单校验JSON格式
            if (!JSONUtil.isTypeJSON(tsl)) {
                throw exception(THING_MODEL_TEMPLATE_TSL_INVALID);
            }
        } catch (Exception e) {
            throw exception(THING_MODEL_TEMPLATE_TSL_INVALID);
        }
    }

}
