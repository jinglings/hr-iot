package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyDetailRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceWithEnergyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceWithEnergyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IoT 设备属性")
@RestController
@RequestMapping("/iot/device/property")
@Validated
public class IotDevicePropertyController {

    /**
     * 能耗属性标识符
     */
    private static final String ENERGY_PROPERTY_IDENTIFIER = "energy";

    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;

    @GetMapping("/get-latest")
    @Operation(summary = "获取设备属性最新属性")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyDetailRespVO>> getLatestDeviceProperties(
            @RequestParam("deviceId") Long deviceId) {
        // 1.1 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceId);
        Assert.notNull(device, "设备不存在");
        // 1.2 获取设备最新属性
        Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(deviceId);
        // 1.3 根据 productId + type 查询属性类型的物模型
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());

        // 2. 基于 thingModels 遍历，拼接 properties
        return success(convertList(thingModels, thingModel -> {
            ThingModelProperty thingModelProperty = thingModel.getProperty();
            Assert.notNull(thingModelProperty, "属性不能为空");
            IotDevicePropertyDetailRespVO result = new IotDevicePropertyDetailRespVO()
                    .setName(thingModel.getName()).setDataType(thingModelProperty.getDataType())
                    .setDataSpecs(thingModelProperty.getDataSpecs())
                    .setDataSpecsList(thingModelProperty.getDataSpecsList());
            result.setIdentifier(thingModel.getIdentifier());
            IotDevicePropertyDO property = properties.get(thingModel.getIdentifier());
            if (property != null) {
                result.setValue(property.getValue())
                        .setUpdateTime(LocalDateTimeUtil.toEpochMilli(property.getUpdateTime()));
            }
            return result;
        }));
    }

    @GetMapping("/history-list")
    @Operation(summary = "获取设备属性历史数据列表")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyRespVO>> getHistoryDevicePropertyList(
            @Valid IotDevicePropertyHistoryListReqVO listReqVO) {
        return success(devicePropertyService.getHistoryDevicePropertyList(listReqVO));
    }

    @GetMapping("/page-with-energy")
    @Operation(summary = "分页获取设备列表，附带能耗属性值")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<PageResult<IotDeviceWithEnergyRespVO>> getDevicePageWithEnergy(
            @Valid IotDeviceWithEnergyPageReqVO pageReqVO) {
        // 1. 构造分页查询参数，复用已有的设备分页查询
        IotDevicePageReqVO devicePageReqVO = new IotDevicePageReqVO();
        devicePageReqVO.setPageNo(pageReqVO.getPageNo());
        devicePageReqVO.setPageSize(pageReqVO.getPageSize());
        devicePageReqVO.setDeviceName(pageReqVO.getDeviceName());
        devicePageReqVO.setNickname(pageReqVO.getNickname());
        devicePageReqVO.setProductId(pageReqVO.getProductId());
        devicePageReqVO.setStatus(pageReqVO.getState());
        devicePageReqVO.setGroupId(pageReqVO.getGroupId());

        // 2. 获取设备分页数据
        PageResult<IotDeviceDO> devicePage = deviceService.getDevicePage(devicePageReqVO);
        if (devicePage.getList().isEmpty()) {
            return success(new PageResult<>(List.of(), devicePage.getTotal()));
        }

        // 3. 获取产品信息用于显示产品名称
        Set<Long> productIds = convertSet(devicePage.getList(), IotDeviceDO::getProductId);
        List<IotProductDO> allProducts = productService.getProductList();
        Map<Long, IotProductDO> productMap = allProducts.stream()
                .filter(p -> productIds.contains(p.getId()))
                .collect(Collectors.toMap(IotProductDO::getId, p -> p));

        // 4. 遍历设备，获取每个设备的 energy 属性值
        List<IotDeviceWithEnergyRespVO> resultList = devicePage.getList().stream()
                .map(device -> {
                    IotDeviceWithEnergyRespVO respVO = new IotDeviceWithEnergyRespVO();
                    respVO.setId(device.getId());
                    respVO.setDeviceName(device.getDeviceName());
                    respVO.setNickname(device.getNickname());
                    respVO.setProductId(device.getProductId());
                    respVO.setState(device.getState());
                    respVO.setLatitude(device.getLatitude());
                    respVO.setLongitude(device.getLongitude());
                    respVO.setOnlineTime(device.getOnlineTime());
                    respVO.setOfflineTime(device.getOfflineTime());
                    respVO.setCreateTime(device.getCreateTime());

                    // 设置产品名称
                    IotProductDO product = productMap.get(device.getProductId());
                    if (product != null) {
                        respVO.setProductName(product.getName());
                    }

                    // 获取设备的 energy 属性值
                    Map<String, IotDevicePropertyDO> properties = devicePropertyService
                            .getLatestDeviceProperties(device.getId());
                    IotDevicePropertyDO energyProperty = properties.get(ENERGY_PROPERTY_IDENTIFIER);
                    if (energyProperty != null && energyProperty.getValue() != null) {
                        try {
                            respVO.setEnergy(new BigDecimal(String.valueOf(energyProperty.getValue())));
                            respVO.setEnergyUpdateTime(energyProperty.getUpdateTime());
                        } catch (NumberFormatException e) {
                            // 如果转换失败，能耗值设为 null
                            respVO.setEnergy(null);
                        }
                    }
                    return respVO;
                })
                .collect(Collectors.toList());

        return success(new PageResult<>(resultList, devicePage.getTotal()));
    }

}