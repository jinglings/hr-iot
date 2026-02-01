package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceEnergyCostReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceEnergyCostRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyDetailRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceWithEnergyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceWithEnergyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    @Resource
    private IotEnergyMeterService energyMeterService;

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

    /**
     * 电费单价（元/度）
     */
    private static final BigDecimal ELECTRICITY_UNIT_PRICE = new BigDecimal("1.0616");

    @GetMapping("/energy-cost")
    @Operation(summary = "计算设备电费（根据时间段能耗差值）")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<PageResult<IotDeviceEnergyCostRespVO>> getDeviceEnergyCost(
            @Valid IotDeviceEnergyCostReqVO reqVO) {
        // 1. 构造分页查询参数，复用已有的设备分页查询
        IotDevicePageReqVO devicePageReqVO = new IotDevicePageReqVO();
        devicePageReqVO.setPageNo(reqVO.getPageNo());
        devicePageReqVO.setPageSize(reqVO.getPageSize());
        devicePageReqVO.setDeviceName(reqVO.getDeviceName());
        devicePageReqVO.setNickname(reqVO.getNickname());
        devicePageReqVO.setProductId(reqVO.getProductId());
        devicePageReqVO.setGroupId(reqVO.getGroupId());

        // 2. 获取设备分页数据
        PageResult<IotDeviceDO> devicePage = deviceService.getDevicePage(devicePageReqVO);
        if (devicePage.getList().isEmpty()) {
            return success(new PageResult<>(List.of(), devicePage.getTotal()));
        }

        // 3. 获取产品信息
        Set<Long> productIds = convertSet(devicePage.getList(), IotDeviceDO::getProductId);
        List<IotProductDO> allProducts = productService.getProductList();
        Map<Long, IotProductDO> productMap = allProducts.stream()
                .filter(p -> productIds.contains(p.getId()))
                .collect(Collectors.toMap(IotProductDO::getId, p -> p));

        // 4. 遍历设备，计算每个设备的电费
        List<IotDeviceEnergyCostRespVO> resultList = new ArrayList<>();
        for (IotDeviceDO device : devicePage.getList()) {
            IotDeviceEnergyCostRespVO respVO = new IotDeviceEnergyCostRespVO();
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setNickname(device.getNickname());
            respVO.setUnitPrice(ELECTRICITY_UNIT_PRICE);

            // 解析设备名称：格式如 B1_01B_倍率30_922101010083
            // 位置 = 倍率前的部分，倍率 = "倍率"后的数字，表号 = 最后一段
            parseDeviceName(respVO, device.getDeviceName());

            // 设置产品名称
            IotProductDO product = productMap.get(device.getProductId());
            if (product != null) {
                respVO.setProductName(product.getName());
            }

            // 4.1 查询开始时间的能耗读数（取开始时间之前最近的一条）
            IotDevicePropertyRespVO startProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, reqVO.getStartTime());
            // 4.2 查询结束时间的能耗读数（取结束时间之前最近的一条）
            IotDevicePropertyRespVO endProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, reqVO.getEndTime());

            if (startProperty != null && startProperty.getValue() != null) {
                respVO.setStartEnergy(new BigDecimal(String.valueOf(startProperty.getValue())));
                respVO.setStartEnergyTime(toLocalDateTime(startProperty.getUpdateTime()));
            }
            if (endProperty != null && endProperty.getValue() != null) {
                respVO.setEndEnergy(new BigDecimal(String.valueOf(endProperty.getValue())));
                respVO.setEndEnergyTime(toLocalDateTime(endProperty.getUpdateTime()));
            }

            // 4.3 计算实际消耗
            if (respVO.getStartEnergy() != null && respVO.getEndEnergy() != null) {
                BigDecimal rawConsumption = respVO.getEndEnergy().subtract(respVO.getStartEnergy());
                respVO.setRawConsumption(rawConsumption);

                // 4.4 获取倍率：优先从设备名称解析，其次从能源计量点表获取
                BigDecimal ratio = BigDecimal.ONE;
                if (respVO.getRatio() != null && respVO.getRatio().compareTo(BigDecimal.ONE) != 0) {
                    // 设备名称中已解析出倍率
                    ratio = respVO.getRatio();
                } else {
                    // 尝试从能源计量点表获取
                    List<IotEnergyMeterDO> meters = energyMeterService.getMeterListByDeviceId(device.getId());
                    if (meters != null && !meters.isEmpty()) {
                        IotEnergyMeterDO meter = meters.get(0);
                        if (meter.getRatio() != null && meter.getRatio().compareTo(BigDecimal.ZERO) > 0) {
                            ratio = meter.getRatio();
                        }
                    }
                    respVO.setRatio(ratio);
                }

                // 4.5 实际消耗 = 原始消耗 × 倍率
                BigDecimal consumption = rawConsumption.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                respVO.setConsumption(consumption);

                // 4.6 电费 = 实际消耗 × 单价
                BigDecimal cost = consumption.multiply(ELECTRICITY_UNIT_PRICE).setScale(2, RoundingMode.HALF_UP);
                respVO.setCost(cost);
            }

            resultList.add(respVO);
        }

        return success(new PageResult<>(resultList, devicePage.getTotal()));
    }

    /**
     * 解析设备名称，提取位置、倍率、表号
     *
     * 设备名称格式：
     * - 含倍率：B1_01B_倍率30_922101010083 → 位置=B1_01B, 倍率=30, 表号=922101010083
     * - 不含倍率：B1_01B_922101010083 → 位置=B1_01B, 倍率=1, 表号=922101010083
     */
    private void parseDeviceName(IotDeviceEnergyCostRespVO respVO, String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            respVO.setRatio(BigDecimal.ONE);
            return;
        }

        int ratioIdx = deviceName.indexOf("倍率");
        if (ratioIdx >= 0) {
            // 包含"倍率"：位置 = 倍率前面的部分（去掉末尾下划线）
            String beforeRatio = deviceName.substring(0, ratioIdx);
            if (beforeRatio.endsWith("_")) {
                beforeRatio = beforeRatio.substring(0, beforeRatio.length() - 1);
            }
            respVO.setLocation(beforeRatio);

            // "倍率"后面的内容
            String afterRatio = deviceName.substring(ratioIdx + 2); // 跳过"倍率"两个字
            // 找到下一个下划线分隔符，前面是倍率数字，后面是表号
            int sepIdx = afterRatio.indexOf("_");
            if (sepIdx >= 0) {
                String ratioStr = afterRatio.substring(0, sepIdx);
                String meterNo = afterRatio.substring(sepIdx + 1);
                try {
                    respVO.setRatio(new BigDecimal(ratioStr));
                } catch (NumberFormatException e) {
                    respVO.setRatio(BigDecimal.ONE);
                }
                respVO.setMeterNo(meterNo);
            } else {
                // 倍率后面没有下划线，整段当作倍率值
                try {
                    respVO.setRatio(new BigDecimal(afterRatio));
                } catch (NumberFormatException e) {
                    respVO.setRatio(BigDecimal.ONE);
                }
            }
        } else {
            // 不含"倍率"：最后一个下划线后是表号，前面是位置
            int lastSep = deviceName.lastIndexOf("_");
            if (lastSep >= 0) {
                respVO.setLocation(deviceName.substring(0, lastSep));
                respVO.setMeterNo(deviceName.substring(lastSep + 1));
            } else {
                respVO.setLocation(deviceName);
            }
            respVO.setRatio(BigDecimal.ONE);
        }
    }

    /**
     * 将毫秒时间戳转换为 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}