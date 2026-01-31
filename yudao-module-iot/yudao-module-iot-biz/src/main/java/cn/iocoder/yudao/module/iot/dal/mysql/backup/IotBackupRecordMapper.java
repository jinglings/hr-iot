package cn.iocoder.yudao.module.iot.dal.mysql.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.record.IotBackupRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotBackupRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT TDengine 备份记录 Mapper
 *
 * @author claude
 */
@Mapper
public interface IotBackupRecordMapper extends BaseMapperX<IotBackupRecordDO> {

    /**
     * 分页查询备份记录
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    default PageResult<IotBackupRecordDO> selectPage(IotBackupRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotBackupRecordDO>()
                .likeIfPresent(IotBackupRecordDO::getBackupName, reqVO.getBackupName())
                .eqIfPresent(IotBackupRecordDO::getBackupType, reqVO.getBackupType())
                .eqIfPresent(IotBackupRecordDO::getBackupMode, reqVO.getBackupMode())
                .eqIfPresent(IotBackupRecordDO::getBackupStatus, reqVO.getBackupStatus())
                .betweenIfPresent(IotBackupRecordDO::getStartTime, reqVO.getStartTime())
                .orderByDesc(IotBackupRecordDO::getId));
    }

    /**
     * 根据状态查询备份记录列表
     *
     * @param backupStatus 备份状态
     * @return 备份记录列表
     */
    default List<IotBackupRecordDO> selectListByStatus(Integer backupStatus) {
        return selectList(IotBackupRecordDO::getBackupStatus, backupStatus);
    }

    /**
     * 查询指定时间之前的备份记录
     *
     * @param beforeTime 时间
     * @return 备份记录列表
     */
    default List<IotBackupRecordDO> selectListBeforeTime(LocalDateTime beforeTime) {
        return selectList(new LambdaQueryWrapperX<IotBackupRecordDO>()
                .lt(IotBackupRecordDO::getCreateTime, beforeTime)
                .orderByAsc(IotBackupRecordDO::getCreateTime));
    }

    /**
     * 查询指定配置的备份记录数量
     *
     * @param backupMode 备份模式
     * @param backupStatus 备份状态
     * @return 数量
     */
    default Long selectCountByModeAndStatus(Integer backupMode, Integer backupStatus) {
        return selectCount(new LambdaQueryWrapperX<IotBackupRecordDO>()
                .eq(IotBackupRecordDO::getBackupMode, backupMode)
                .eq(IotBackupRecordDO::getBackupStatus, backupStatus));
    }

    /**
     * 统计备份文件总大小
     *
     * @return 文件总大小（字节）
     */
    default Long selectTotalFileSize() {
        return selectObjs(new QueryWrapper<IotBackupRecordDO>()
                        .select("COALESCE(SUM(file_size), 0)"))
                .stream()
                .findFirst()
                .map(obj -> obj != null ? Long.parseLong(obj.toString()) : 0L)
                .orElse(0L);
    }

}
