package cn.iocoder.yudao.module.iot.controller.admin.thingmodel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelTemplateDO;
import cn.iocoder.yudao.module.iot.service.thingmodel.template.IotThingModelTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 物模型模板
 *
 * @author AI
 */
@Tag(name = "管理后台 - IoT 物模型模板")
@RestController
@RequestMapping("/iot/thing-model-template")
@Validated
public class IotThingModelTemplateController {

    @Resource
    private IotThingModelTemplateService templateService;

    // ========== 分类接口 ==========

    @GetMapping("/category/list")
    @Operation(summary = "获取所有分类列表")
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:query')")
    public CommonResult<List<IotThingModelTemplateCategoryRespVO>> getCategoryList() {
        List<IotThingModelTemplateCategoryDO> list = templateService.getCategoryList();
        return success(BeanUtils.toBean(list, IotThingModelTemplateCategoryRespVO.class));
    }

    // ========== 模板接口 ==========

    @PostMapping("/create")
    @Operation(summary = "创建自定义模板")
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:create')")
    public CommonResult<Long> createTemplate(@Valid @RequestBody IotThingModelTemplateCreateReqVO createReqVO) {
        return success(templateService.createTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模板")
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:update')")
    public CommonResult<Boolean> updateTemplate(@Valid @RequestBody IotThingModelTemplateCreateReqVO updateReqVO,
            @RequestParam("id") Long id) {
        templateService.updateTemplate(updateReqVO, id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:delete')")
    public CommonResult<Boolean> deleteTemplate(@RequestParam("id") Long id) {
        templateService.deleteTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取模板详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:query')")
    public CommonResult<IotThingModelTemplateRespVO> getTemplate(@RequestParam("id") Long id) {
        IotThingModelTemplateDO template = templateService.getTemplate(id);
        return success(BeanUtils.toBean(template, IotThingModelTemplateRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获取模板列表")
    @PreAuthorize("@ss.hasPermission('iot:thing-model-template:query')")
    public CommonResult<List<IotThingModelTemplateRespVO>> getTemplateList(
            @Valid IotThingModelTemplateListReqVO reqVO) {
        List<IotThingModelTemplateDO> list = templateService.getTemplateList(reqVO);
        return success(BeanUtils.toBean(list, IotThingModelTemplateRespVO.class));
    }

    // ========== 应用与导入导出 ==========

    @PostMapping("/apply")
    @Operation(summary = "应用模板到产品")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:create')")
    public CommonResult<Boolean> applyTemplate(@Valid @RequestBody IotThingModelTemplateApplyReqVO applyReqVO) {
        templateService.applyTemplateToProduct(applyReqVO);
        return success(true);
    }

    @GetMapping("/export")
    @Operation(summary = "导出产品物模型")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public void exportThingModel(@RequestParam("productId") Long productId, HttpServletResponse response)
            throws IOException {
        String tsl = templateService.exportThingModel(productId);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=thing_model.json");
        response.getOutputStream().write(tsl.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/import")
    @Operation(summary = "导入物模型到产品")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:create')")
    public CommonResult<Boolean> importThingModel(@RequestParam("productId") Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", required = false) Boolean overwrite) throws IOException {
        String tslJson = new String(file.getBytes(), StandardCharsets.UTF_8);
        templateService.importThingModel(productId, tslJson, overwrite != null ? overwrite : false);
        return success(true);
    }

}
