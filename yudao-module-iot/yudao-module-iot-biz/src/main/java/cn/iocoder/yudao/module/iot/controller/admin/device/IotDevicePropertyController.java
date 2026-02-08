package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDeviceEnergyCostExportReqVO;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
@Slf4j
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

        // 3. 计算每个设备的电费
        List<IotDeviceEnergyCostRespVO> resultList = calculateEnergyCostList(
                devicePage.getList(), reqVO.getStartTime(), reqVO.getEndTime());

        return success(new PageResult<>(resultList, devicePage.getTotal()));
    }

    @GetMapping("/export-energy-cost")
    @Operation(summary = "导出电费报表 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public void exportEnergyCostExcel(@Valid IotDeviceEnergyCostExportReqVO reqVO,
                                      HttpServletResponse response) throws IOException {
        // 分组定义：groupId -> (sheetName, sheetType)
        // sheetType: 1=公共区域格式(10列), 2=商铺格式(10列不同表头)
        Long[][] groupConfigs = {
                {1L}, // 公共区域-动力
                {2L}, // 商铺
                {3L}, // 公区备用电表
        };
        String[] sheetNames = {"公共区域、动力电费计算", "商铺", "公区备用电表"};
        // sheetType: 1=公共区域格式, 2=商铺格式
        int[] sheetTypes = {1, 2, 1};

        Workbook workbook = new XSSFWorkbook();

        for (int i = 0; i < groupConfigs.length; i++) {
            Long groupId = groupConfigs[i][0];
            // 1. 查询该分组下所有设备（不分页）
            IotDevicePageReqVO devicePageReqVO = new IotDevicePageReqVO();
            devicePageReqVO.setPageNo(1);
            devicePageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
            devicePageReqVO.setGroupId(groupId);

            PageResult<IotDeviceDO> devicePage = deviceService.getDevicePage(devicePageReqVO);
            List<IotDeviceEnergyCostRespVO> dataList;
            if (devicePage.getList().isEmpty()) {
                dataList = new ArrayList<>();
            } else {
                dataList = calculateEnergyCostList(devicePage.getList(), reqVO.getStartTime(), reqVO.getEndTime());
            }

            // 2. 创建 Sheet
            buildEnergyCostSheet(workbook, sheetNames[i], sheetTypes[i], dataList);
        }

        // 3. 输出 Excel
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fileName = "能源报表" + reqVO.getStartTime().format(fmt) + "至" + reqVO.getEndTime().format(fmt) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 计算设备列表的电费
     */
    private List<IotDeviceEnergyCostRespVO> calculateEnergyCostList(List<IotDeviceDO> devices,
                                                                     LocalDateTime startTime,
                                                                     LocalDateTime endTime) {
        // 获取产品信息
        Set<Long> productIds = convertSet(devices, IotDeviceDO::getProductId);
        List<IotProductDO> allProducts = productService.getProductList();
        Map<Long, IotProductDO> productMap = allProducts.stream()
                .filter(p -> productIds.contains(p.getId()))
                .collect(Collectors.toMap(IotProductDO::getId, p -> p));

        List<IotDeviceEnergyCostRespVO> resultList = new ArrayList<>();
        for (IotDeviceDO device : devices) {
            IotDeviceEnergyCostRespVO respVO = new IotDeviceEnergyCostRespVO();
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setNickname(device.getNickname());
            respVO.setUnitPrice(ELECTRICITY_UNIT_PRICE);

            parseDeviceName(respVO, device.getDeviceName());

            IotProductDO product = productMap.get(device.getProductId());
            if (product != null) {
                respVO.setProductName(product.getName());
            }

            // 查询开始/结束时间的能耗读数
            IotDevicePropertyRespVO startProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, startTime);
            IotDevicePropertyRespVO endProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, endTime);

            if (startProperty != null && startProperty.getValue() != null) {
                respVO.setStartEnergy(new BigDecimal(String.valueOf(startProperty.getValue())));
                respVO.setStartEnergyTime(toLocalDateTime(startProperty.getUpdateTime()));
            }
            if (endProperty != null && endProperty.getValue() != null) {
                respVO.setEndEnergy(new BigDecimal(String.valueOf(endProperty.getValue())));
                respVO.setEndEnergyTime(toLocalDateTime(endProperty.getUpdateTime()));
            }

            // 计算实际消耗
            if (respVO.getStartEnergy() != null && respVO.getEndEnergy() != null) {
                BigDecimal rawConsumption = respVO.getEndEnergy().subtract(respVO.getStartEnergy());
                respVO.setRawConsumption(rawConsumption);

                BigDecimal ratio = BigDecimal.ONE;
                if (respVO.getRatio() != null && respVO.getRatio().compareTo(BigDecimal.ONE) != 0) {
                    ratio = respVO.getRatio();
                } else {
                    List<IotEnergyMeterDO> meters = energyMeterService.getMeterListByDeviceId(device.getId());
                    if (meters != null && !meters.isEmpty()) {
                        IotEnergyMeterDO meter = meters.get(0);
                        if (meter.getRatio() != null && meter.getRatio().compareTo(BigDecimal.ZERO) > 0) {
                            ratio = meter.getRatio();
                        }
                    }
                    respVO.setRatio(ratio);
                }

                BigDecimal consumption = rawConsumption.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                respVO.setConsumption(consumption);

                BigDecimal cost = consumption.multiply(ELECTRICITY_UNIT_PRICE).setScale(2, RoundingMode.HALF_UP);
                respVO.setCost(cost);
            }

            resultList.add(respVO);
        }
        return resultList;
    }

    /**
     * 构建电费报表的 Sheet 页
     *
     * @param workbook  工作簿
     * @param sheetName Sheet 名称
     * @param sheetType Sheet 类型：1=公共区域格式, 2=商铺格式
     * @param dataList  数据列表
     */
    private void buildEnergyCostSheet(Workbook workbook, String sheetName, int sheetType,
                                       List<IotDeviceEnergyCostRespVO> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);

        // ===== 样式 =====
        // 标题样式
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 数据样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 数字样式（保留2位小数）
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.cloneStyleFrom(dataStyle);
        DataFormat dataFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

        // 合计样式
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.cloneStyleFrom(headerStyle);

        int rowNum = 0;

        // ===== 第1行：标题（合并单元格） =====
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetName);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // ===== 第2行：表头 =====
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers;
        if (sheetType == 2) {
            // 商铺格式
            headers = new String[]{"序号", "位置", "表号", "倍率", "上月表底", "表底", "实际消耗(度)", "单价(元)", "合计(元)", "签字"};
        } else {
            // 公共区域格式
            headers = new String[]{"序号", "受电配电箱编号", "电表安装位置", "表号", "倍率", "上月表底", "表底", "实际消耗(度)", "单价(元)", "合计(元)"};
        }
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ===== 数据行 =====
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (int i = 0; i < dataList.size(); i++) {
            IotDeviceEnergyCostRespVO data = dataList.get(i);
            Row dataRow = sheet.createRow(rowNum++);

            if (sheetType == 2) {
                // 商铺格式: 序号, 位置, 表号, 倍率, 上月表底, 表底, 实际消耗(度), 单价(元), 合计(元), 签字
                createCell(dataRow, 0, (double) (i + 1), dataStyle);
                createCell(dataRow, 1, data.getNickname() != null ? data.getNickname() : data.getLocation(), dataStyle);
                createCell(dataRow, 2, data.getMeterNo(), dataStyle);
                createCellBigDecimal(dataRow, 3, ratioDisplayValue(data.getRatio()), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 4, data.getStartEnergy(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 5, data.getEndEnergy(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 6, data.getConsumption(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 7, data.getUnitPrice(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 8, data.getCost(), numberStyle, dataStyle);
                createCell(dataRow, 9, "", dataStyle); // 签字列留空
            } else {
                // 公共区域格式: 序号, 受电配电箱编号, 电表安装位置, 表号, 倍率, 上月表底, 表底, 实际消耗(度), 单价(元), 合计(元)
                createCell(dataRow, 0, (double) (i + 1), dataStyle);
                createCell(dataRow, 1, data.getNickname() != null ? data.getNickname() : data.getLocation(), dataStyle);
                createCell(dataRow, 2, data.getLocation(), dataStyle);
                createCell(dataRow, 3, data.getMeterNo(), dataStyle);
                createCellBigDecimal(dataRow, 4, ratioDisplayValue(data.getRatio()), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 5, data.getStartEnergy(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 6, data.getEndEnergy(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 7, data.getConsumption(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 8, data.getUnitPrice(), numberStyle, dataStyle);
                createCellBigDecimal(dataRow, 9, data.getCost(), numberStyle, dataStyle);
            }

            // 累加合计
            if (data.getConsumption() != null) {
                totalConsumption = totalConsumption.add(data.getConsumption());
            }
            if (data.getCost() != null) {
                totalCost = totalCost.add(data.getCost());
            }
        }

        // ===== 合计行 =====
        Row totalRow = sheet.createRow(rowNum);
        if (sheetType == 2) {
            createCell(totalRow, 1, "合计", totalStyle);
            createCellBigDecimal(totalRow, 6, totalConsumption, numberStyle, totalStyle);
            createCellBigDecimal(totalRow, 8, totalCost, numberStyle, totalStyle);
        } else {
            createCell(totalRow, 1, "合计", totalStyle);
            createCellBigDecimal(totalRow, 7, totalConsumption, numberStyle, totalStyle);
            createCellBigDecimal(totalRow, 9, totalCost, numberStyle, totalStyle);
        }

        // ===== 设置列宽 =====
        sheet.setColumnWidth(0, 2000);  // 序号
        if (sheetType == 2) {
            sheet.setColumnWidth(1, 5000);  // 位置
            sheet.setColumnWidth(2, 5000);  // 表号
            sheet.setColumnWidth(3, 2500);  // 倍率
            sheet.setColumnWidth(4, 4000);  // 上月表底
            sheet.setColumnWidth(5, 4000);  // 表底
            sheet.setColumnWidth(6, 4000);  // 实际消耗
            sheet.setColumnWidth(7, 3000);  // 单价
            sheet.setColumnWidth(8, 4000);  // 合计
            sheet.setColumnWidth(9, 3000);  // 签字
        } else {
            sheet.setColumnWidth(1, 5500);  // 受电配电箱编号
            sheet.setColumnWidth(2, 4500);  // 电表安装位置
            sheet.setColumnWidth(3, 5000);  // 表号
            sheet.setColumnWidth(4, 2500);  // 倍率
            sheet.setColumnWidth(5, 4000);  // 上月表底
            sheet.setColumnWidth(6, 4000);  // 表底
            sheet.setColumnWidth(7, 4000);  // 实际消耗
            sheet.setColumnWidth(8, 3000);  // 单价
            sheet.setColumnWidth(9, 4000);  // 合计
        }
    }

    /**
     * 倍率显示值：倍率为1时不显示
     */
    private BigDecimal ratioDisplayValue(BigDecimal ratio) {
        if (ratio == null || ratio.compareTo(BigDecimal.ONE) == 0) {
            return null;
        }
        return ratio;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCellBigDecimal(Row row, int col, BigDecimal value, CellStyle numberStyle, CellStyle emptyStyle) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(numberStyle);
        } else {
            cell.setCellValue("");
            cell.setCellStyle(emptyStyle);
        }
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
