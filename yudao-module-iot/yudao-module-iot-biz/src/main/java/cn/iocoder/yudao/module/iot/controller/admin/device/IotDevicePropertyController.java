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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                result.setStale(property.getStale());
                result.setChangeTime(property.getChangeTime() == null ? null
                        : LocalDateTimeUtil.toEpochMilli(property.getChangeTime()));
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
                        // 数据冻结标识：方便在设备列表直接看出哪台设备数据长时间未变化
                        respVO.setStale(energyProperty.getStale());
                        respVO.setChangeTime(energyProperty.getChangeTime());
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
        // 各 Sheet 的标题（与模板保持一致）
        String[] titles = {"公共区域、照明动力用电", "商铺用电", "公区备用电表"};
        // sheetType: 1=公共区域格式, 2=商铺格式
        int[] sheetTypes = {1, 2, 1};
        // 各 Sheet 的列宽（Excel 字符宽度，与模板逐列保持一致）
        double[][] sheetWidths = {
                {25, 32.7, 30, 36.5, 35.6, 42.6, 54.6, 44.5, 53.4, 70.2}, // 公共区域
                {30, 34.8, 45, 30.8, 44.1, 43.8, 46.5, 43.3, 44.2, 75.8}, // 商铺
                {25, 24.7, 43.4, 37.7, 40, 34.8, 37.3, 40.5, 44.2, 49.7}, // 公区备用电表
        };

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
            buildEnergyCostSheet(workbook, sheetNames[i], titles[i], sheetTypes[i], sheetWidths[i], dataList);
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

            // 数据冻结标识：基于能耗属性的实时冻结状态，方便看出哪台设备数据长时间未变化
            IotDevicePropertyDO latestEnergy = devicePropertyService.getLatestDeviceProperties(device.getId())
                    .get(ENERGY_PROPERTY_IDENTIFIER);
            if (latestEnergy != null) {
                respVO.setStale(latestEnergy.getStale());
                respVO.setChangeTime(latestEnergy.getChangeTime());
            }

            // 先确定倍率（从设备名称解析或从数据库查询）
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

            // 查询开始/结束时间的能耗读数，除以倍率后作为显示读数
            IotDevicePropertyRespVO startProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, startTime);
            IotDevicePropertyRespVO endProperty = devicePropertyService
                    .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, endTime);

            if (startProperty != null && startProperty.getValue() != null) {
                BigDecimal rawStart = new BigDecimal(String.valueOf(startProperty.getValue()));
                respVO.setStartEnergy(rawStart.divide(ratio, 2, RoundingMode.HALF_UP));
                respVO.setStartEnergyTime(toLocalDateTime(startProperty.getUpdateTime()));
            }
            if (endProperty != null && endProperty.getValue() != null) {
                BigDecimal rawEnd = new BigDecimal(String.valueOf(endProperty.getValue()));
                respVO.setEndEnergy(rawEnd.divide(ratio, 2, RoundingMode.HALF_UP));
                respVO.setEndEnergyTime(toLocalDateTime(endProperty.getUpdateTime()));
            }

            // 计算实际消耗（基于除以倍率后的读数，再乘以倍率）
            if (respVO.getStartEnergy() != null && respVO.getEndEnergy() != null) {
                BigDecimal rawConsumption = respVO.getEndEnergy().subtract(respVO.getStartEnergy())
                        .setScale(2, RoundingMode.HALF_UP);
                respVO.setRawConsumption(rawConsumption);

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
     * 报表标题/表头字体（与模板一致：宋体）
     */
    private static final String REPORT_HEADER_FONT_NAME = "宋体";
    /**
     * 报表数据字体（与模板一致：Calibri）
     */
    private static final String REPORT_DATA_FONT_NAME = "Calibri";

    /**
     * 构建电费报表的 Sheet 页（样式与《抄表电费明细表》模板保持一致）
     *
     * @param workbook  工作簿
     * @param sheetName Sheet 名称
     * @param title     标题（如：公共区域、照明动力用电 / 商铺用电 / 公区备用电表）
     * @param sheetType Sheet 类型：1=公共区域格式, 2=商铺格式
     * @param columnWidths 各列宽度（Excel 字符宽度，与模板逐列一致）
     * @param dataList  数据列表
     */
    private void buildEnergyCostSheet(Workbook workbook, String sheetName, String title,
                                      int sheetType, double[] columnWidths,
                                      List<IotDeviceEnergyCostRespVO> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);
        final int lastCol = 9; // 共 10 列 A:J

        // ===== 字体 =====
        // 标题/表头：宋体 14 加粗
        Font boldFont = workbook.createFont();
        boldFont.setFontName(REPORT_HEADER_FONT_NAME);
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 14);

        // 数据/合计：Calibri 14
        Font normalFont = workbook.createFont();
        normalFont.setFontName(REPORT_DATA_FONT_NAME);
        normalFont.setFontHeightInPoints((short) 14);

        // ===== 样式 =====
        // 标题：宋体 14 加粗 居中 细边框（模板无底色填充）
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(boldFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorder(titleStyle);

        // 表头：与标题样式一致（宋体 14 加粗，无底色）
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.cloneStyleFrom(titleStyle);

        // 数据：Calibri 14 居中 细边框 通用格式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setFont(normalFont);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorder(dataStyle);

        // 合计行：与数据样式一致（Calibri 14，不加粗，无底色）
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.cloneStyleFrom(dataStyle);

        int rowNum = 0;

        // ===== 第1行：标题（合并 A1:J1） =====
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(40f);
        for (int c = 0; c <= lastCol; c++) {
            titleRow.createCell(c).setCellStyle(titleStyle);
        }
        titleRow.getCell(0).setCellValue(title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));

        // ===== 第2行：表头 =====
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(40f);
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
            dataRow.setHeightInPoints(30f);

            if (sheetType == 2) {
                // 商铺格式: 序号, 位置, 表号, 倍率, 上月表底, 表底, 实际消耗(度), 单价(元), 合计(元), 签字
                createCell(dataRow, 0, i + 1, dataStyle);
                createCell(dataRow, 1, firstNonBlank(data.getLocation(), data.getNickname()), dataStyle);
                createCell(dataRow, 2, data.getMeterNo(), dataStyle);
                createCellBigDecimal(dataRow, 3, ratioDisplayValue(data.getRatio()), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 4, data.getStartEnergy(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 5, data.getEndEnergy(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 6, data.getConsumption(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 7, data.getUnitPrice(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 8, data.getCost(), dataStyle, dataStyle);
                createCell(dataRow, 9, "", dataStyle); // 签字列留空
            } else {
                // 公共区域格式: 序号, 受电配电箱编号, 电表安装位置, 表号, 倍率, 上月表底, 表底, 实际消耗(度), 单价(元), 合计(元)
                createCell(dataRow, 0, i + 1, dataStyle);
                createCell(dataRow, 1, firstNonBlank(data.getNickname(), data.getLocation()), dataStyle);
                createCell(dataRow, 2, data.getLocation(), dataStyle);
                createCell(dataRow, 3, data.getMeterNo(), dataStyle);
                createCellBigDecimal(dataRow, 4, ratioDisplayValue(data.getRatio()), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 5, data.getStartEnergy(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 6, data.getEndEnergy(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 7, data.getConsumption(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 8, data.getUnitPrice(), dataStyle, dataStyle);
                createCellBigDecimal(dataRow, 9, data.getCost(), dataStyle, dataStyle);
            }

            if (data.getConsumption() != null) {
                totalConsumption = totalConsumption.add(data.getConsumption());
            }
            if (data.getCost() != null) {
                totalCost = totalCost.add(data.getCost());
            }
        }

        // ===== 合计行 =====
        Row totalRow = sheet.createRow(rowNum);
        totalRow.setHeightInPoints(30f);
        for (int c = 0; c <= lastCol; c++) {
            totalRow.createCell(c).setCellStyle(totalStyle);
        }
        totalRow.getCell(1).setCellValue("合计");
        if (sheetType == 2) {
            totalRow.getCell(6).setCellValue(totalConsumption.doubleValue());
            totalRow.getCell(8).setCellValue(totalCost.doubleValue());
        } else {
            totalRow.getCell(7).setCellValue(totalConsumption.doubleValue());
            totalRow.getCell(9).setCellValue(totalCost.doubleValue());
        }

        // ===== 列宽（与模板逐列一致，单位为 Excel 字符宽度） =====
        for (int c = 0; c < columnWidths.length; c++) {
            sheet.setColumnWidth(c, (int) Math.round(columnWidths[c] * 256));
        }
    }

    /**
     * 设置四周细边框
     */
    private void setThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    /**
     * 返回第一个非空字符串
     */
    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        return second != null ? second : "";
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
     * 表号匹配：连续 6 位及以上的数字（电表号一般约 12 位；倍率一般 1-3 位，不会被误匹配）
     */
    private static final Pattern METER_NO_PATTERN = Pattern.compile("\\d{6,}");
    /**
     * 倍率匹配：紧跟"倍率"之后的数字，允许下划线/空格分隔
     * 兼容：倍率30 / 倍率_80 / 倍率 40
     */
    private static final Pattern RATIO_PATTERN = Pattern.compile("倍率[_\\s]*(\\d+)");

    /**
     * 解析设备名称，提取位置、倍率、表号（表号统一为纯数字，与模板保持一致）
     *
     * 兼容多种命名格式，例如：
     * - B1_01B_倍率30_922101010083   → 位置=B1_01B,   倍率=30, 表号=922101010083
     * - L3_办公室_倍率_80_190712010005 → 位置=L3_办公室, 倍率=80, 表号=190712010005
     * - L1_17_210803000058_倍率20     → 位置=L1_17,    倍率=20, 表号=210803000058
     * - B1_01B_922101010083          → 位置=B1_01B,   倍率=1,  表号=922101010083
     */
    private void parseDeviceName(IotDeviceEnergyCostRespVO respVO, String deviceName) {
        respVO.setRatio(BigDecimal.ONE);
        if (deviceName == null || deviceName.trim().isEmpty()) {
            return;
        }
        String remaining = deviceName;

        // 1. 解析倍率：取"倍率"后的数字（兼容 倍率30 / 倍率_80 / 倍率 40），并从名称中移除该片段
        Matcher ratioMatcher = RATIO_PATTERN.matcher(remaining);
        if (ratioMatcher.find()) {
            try {
                BigDecimal ratio = new BigDecimal(ratioMatcher.group(1));
                if (ratio.compareTo(BigDecimal.ZERO) > 0) {
                    respVO.setRatio(ratio);
                }
            } catch (NumberFormatException ignored) {
                // 保持默认倍率 1
            }
            remaining = remaining.substring(0, ratioMatcher.start()) + "_" + remaining.substring(ratioMatcher.end());
        }

        // 2. 解析表号：取最后一段 6 位及以上的连续数字（去除倍率/空格等前后缀污染）
        Matcher meterMatcher = METER_NO_PATTERN.matcher(remaining);
        int meterStart = -1;
        int meterEnd = -1;
        while (meterMatcher.find()) {
            meterStart = meterMatcher.start();
            meterEnd = meterMatcher.end();
        }
        if (meterStart >= 0) {
            respVO.setMeterNo(remaining.substring(meterStart, meterEnd));
            remaining = remaining.substring(0, meterStart) + "_" + remaining.substring(meterEnd);
        }

        // 3. 位置：剩余部分，折叠多余下划线/空格，去除首尾下划线
        respVO.setLocation(cleanLocation(remaining));
    }

    /**
     * 清理位置字符串：折叠连续下划线/空格为单个下划线，并去除首尾下划线与空白
     */
    private String cleanLocation(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("[_\\s]+", "_");
        cleaned = cleaned.replaceAll("^_+", "").replaceAll("_+$", "");
        return cleaned.trim();
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
