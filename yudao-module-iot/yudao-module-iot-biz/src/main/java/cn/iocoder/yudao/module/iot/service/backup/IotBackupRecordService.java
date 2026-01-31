package cn.iocoder.yudao.module.iot.service.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupRecordDO;

import javax.validation.Valid;

/**
 * IoT TDengine 备份记录 Service 接口
 *
 * @author claude
 */
public interface IotBackupRecordService {

    /**
     * 创建备份记录（手动备份）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBackup(@Valid IotBackupRecordCreateReqVO createReqVO);

    /**
     * 删除备份记录
     *
     * @param id 编号
     */
    void deleteBackup(Long id);

    /**
     * 获得备份记录
     *
     * @param id 编号
     * @return 备份记录
     */
    IotBackupRecordDO getBackupRecord(Long id);

    /**
     * 获得备份记录分页
     *
     * @param pageReqVO 分页查询
     * @return 备份记录分页
     */
    PageResult<IotBackupRecordRespVO> getBackupRecordPage(IotBackupRecordPageReqVO pageReqVO);

    /**
     * 下载备份文件
     *
     * @param id 编号
     * @return 文件路径
     */
    String downloadBackup(Long id);

    /**
     * 验证备份文件
     *
     * @param id 编号
     * @return 验证结果
     */
    IotBackupExecutor.BackupMetadata validateBackup(Long id);

    /**
     * 清理过期备份
     *
     * @param retentionDays 保留天数
     * @param maxBackupCount 最大备份数量
     */
    void cleanExpiredBackups(Integer retentionDays, Integer maxBackupCount);

}
