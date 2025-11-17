package cn.iocoder.yudao.module.iot.service.energy.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportGenerateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.report.IotEnergyReportRecordMapper;
import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ENERGY_REPORT_RECORD_NOT_EXISTS;

/**
 * IoT 能源报表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyReportServiceImpl implements IotEnergyReportService {

    @Resource
    private IotEnergyReportRecordMapper reportRecordMapper;

    @Resource
    private IotEnergyStatisticsService statisticsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateReport(IotEnergyReportGenerateReqVO generateReqVO) {
        try {
            // 1. 查询统计数据
            String statPeriod = getStatPeriodByReportType(generateReqVO.getReportType());
            List<IotEnergyStatisticsDO> statistics = statisticsService.getStatisticsByTimeRange(
                    generateReqVO.getStartTime(),
                    generateReqVO.getEndTime(),
                    statPeriod,
                    generateReqVO.getBuildingId(),
                    generateReqVO.getEnergyTypeId()
            );

            // 2. 生成报表数据
            Map<String, Object> reportData = buildReportData(statistics, generateReqVO);

            // 3. 创建报表记录
            IotEnergyReportRecordDO record = IotEnergyReportRecordDO.builder()
                    .templateId(generateReqVO.getTemplateId())
                    .reportName(generateReqVO.getReportName())
                    .reportType(generateReqVO.getReportType())
                    .startTime(generateReqVO.getStartTime())
                    .endTime(generateReqVO.getEndTime())
                    .reportData(JSONUtil.toJsonStr(reportData))
                    .status(1) // 生成成功
                    .generateType(1) // 手动生成
                    .build();

            reportRecordMapper.insert(record);

            log.info("[generateReport][生成报表成功] recordId={}, reportName={}", record.getId(), record.getReportName());
            return record.getId();

        } catch (Exception e) {
            log.error("[generateReport][生成报表失败] reportName={}", generateReqVO.getReportName(), e);

            // 记录失败
            IotEnergyReportRecordDO record = IotEnergyReportRecordDO.builder()
                    .templateId(generateReqVO.getTemplateId())
                    .reportName(generateReqVO.getReportName())
                    .reportType(generateReqVO.getReportType())
                    .startTime(generateReqVO.getStartTime())
                    .endTime(generateReqVO.getEndTime())
                    .status(2) // 生成失败
                    .errorMessage(e.getMessage())
                    .generateType(1) // 手动生成
                    .build();

            reportRecordMapper.insert(record);
            throw new RuntimeException("报表生成失败", e);
        }
    }

    /**
     * 根据报表类型获取统计周期
     */
    private String getStatPeriodByReportType(String reportType) {
        switch (reportType) {
            case "day":
                return "hour";
            case "week":
            case "month":
                return "day";
            case "year":
                return "month";
            default:
                return "day";
        }
    }

    /**
     * 构建报表数据
     */
    private Map<String, Object> buildReportData(List<IotEnergyStatisticsDO> statistics, IotEnergyReportGenerateReqVO reqVO) {
        Map<String, Object> data = new HashMap<>();

        // 基本信息
        data.put("reportName", reqVO.getReportName());
        data.put("reportType", reqVO.getReportType());
        data.put("startTime", reqVO.getStartTime());
        data.put("endTime", reqVO.getEndTime());
        data.put("generateTime", LocalDateTime.now());

        // 统计汇总
        BigDecimal totalConsumption = statistics.stream()
                .map(IotEnergyStatisticsDO::getConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgConsumption = statistics.isEmpty() ? BigDecimal.ZERO :
                totalConsumption.divide(new BigDecimal(statistics.size()), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal maxConsumption = statistics.stream()
                .map(IotEnergyStatisticsDO::getConsumption)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minConsumption = statistics.stream()
                .map(IotEnergyStatisticsDO::getConsumption)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        data.put("totalConsumption", totalConsumption);
        data.put("avgConsumption", avgConsumption);
        data.put("maxConsumption", maxConsumption);
        data.put("minConsumption", minConsumption);
        data.put("dataCount", statistics.size());

        // 详细数据
        List<Map<String, Object>> details = new ArrayList<>();
        for (IotEnergyStatisticsDO stat : statistics) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("statTime", stat.getStatTime());
            detail.put("meterId", stat.getMeterId());
            detail.put("consumption", stat.getConsumption());
            detail.put("maxValue", stat.getMaxValue());
            detail.put("minValue", stat.getMinValue());
            detail.put("avgValue", stat.getAvgValue());
            details.add(detail);
        }
        data.put("details", details);

        return data;
    }

    @Override
    public void exportReportToExcel(Long recordId, HttpServletResponse response) {
        try {
            // 1. 获取报表记录
            IotEnergyReportRecordDO record = getReportRecord(recordId);
            if (record == null) {
                throw exception(ENERGY_REPORT_RECORD_NOT_EXISTS);
            }

            // 2. 解析报表数据
            Map<String, Object> reportData = JSONUtil.toBean(record.getReportData(), Map.class);

            // 3. 创建Excel工作簿
            Workbook workbook = createExcelWorkbook(reportData);

            // 4. 设置响应头
            String fileName = record.getReportName() + "_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 5. 写入响应
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
            outputStream.close();

            log.info("[exportReportToExcel][导出Excel成功] recordId={}, fileName={}", recordId, fileName);

        } catch (IOException e) {
            log.error("[exportReportToExcel][导出Excel失败] recordId={}", recordId, e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 创建Excel工作簿
     */
    private Workbook createExcelWorkbook(Map<String, Object> reportData) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("能源报表");

        // 创建标题样式
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 0;

        // 报表标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue((String) reportData.get("reportName"));
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        rowNum++; // 空行

        // 汇总信息
        createSummarySection(sheet, reportData, rowNum);
        rowNum += 6; // 汇总占5行 + 空行

        // 详细数据表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"统计时间", "计量点ID", "能耗值", "最大值", "最小值", "平均值"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // 详细数据
        List<Map<String, Object>> details = (List<Map<String, Object>>) reportData.get("details");
        if (CollUtil.isNotEmpty(details)) {
            for (Map<String, Object> detail : details) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(String.valueOf(detail.get("statTime")));
                dataRow.createCell(1).setCellValue(String.valueOf(detail.get("meterId")));
                dataRow.createCell(2).setCellValue(String.valueOf(detail.get("consumption")));
                dataRow.createCell(3).setCellValue(String.valueOf(detail.get("maxValue")));
                dataRow.createCell(4).setCellValue(String.valueOf(detail.get("minValue")));
                dataRow.createCell(5).setCellValue(String.valueOf(detail.get("avgValue")));
            }
        }

        return workbook;
    }

    /**
     * 创建汇总信息区域
     */
    private void createSummarySection(Sheet sheet, Map<String, Object> reportData, int startRow) {
        Row row1 = sheet.createRow(startRow++);
        row1.createCell(0).setCellValue("报表类型:");
        row1.createCell(1).setCellValue((String) reportData.get("reportType"));

        Row row2 = sheet.createRow(startRow++);
        row2.createCell(0).setCellValue("统计时间:");
        row2.createCell(1).setCellValue(reportData.get("startTime") + " 至 " + reportData.get("endTime"));

        Row row3 = sheet.createRow(startRow++);
        row3.createCell(0).setCellValue("总能耗:");
        row3.createCell(1).setCellValue(String.valueOf(reportData.get("totalConsumption")));

        Row row4 = sheet.createRow(startRow++);
        row4.createCell(0).setCellValue("平均能耗:");
        row4.createCell(1).setCellValue(String.valueOf(reportData.get("avgConsumption")));

        Row row5 = sheet.createRow(startRow);
        row5.createCell(0).setCellValue("数据条数:");
        row5.createCell(1).setCellValue(String.valueOf(reportData.get("dataCount")));
    }

    @Override
    public void exportReportToPdf(Long recordId, HttpServletResponse response) {
        // PDF导出功能（使用iText或其他PDF库）
        // 由于PDF生成较复杂，这里提供简化实现
        // 实际项目中建议使用专业的PDF库如iText、PDFBox等

        try {
            // 1. 获取报表记录
            IotEnergyReportRecordDO record = getReportRecord(recordId);
            if (record == null) {
                throw exception(ENERGY_REPORT_RECORD_NOT_EXISTS);
            }

            // 2. 暂时使用HTML转PDF的方式（需要额外配置wkhtmltopdf或使用其他库）
            // 这里仅记录日志，实际实现需要根据项目需求选择PDF生成方案
            log.warn("[exportReportToPdf][PDF导出功能待实现] recordId={}", recordId);

            // 临时方案：提示用户使用Excel导出
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("PDF导出功能正在开发中，请使用Excel导出。");

        } catch (Exception e) {
            log.error("[exportReportToPdf][导出PDF失败] recordId={}", recordId, e);
            throw new RuntimeException("导出PDF失败", e);
        }
    }

    @Override
    public IotEnergyReportRecordDO getReportRecord(Long id) {
        return reportRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotEnergyReportRecordDO> getReportRecordPage(IotEnergyReportRecordPageReqVO pageReqVO) {
        return reportRecordMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReportRecord(Long id) {
        // 校验存在
        if (reportRecordMapper.selectById(id) == null) {
            throw exception(ENERGY_REPORT_RECORD_NOT_EXISTS);
        }
        // 删除
        reportRecordMapper.deleteById(id);
    }

}
