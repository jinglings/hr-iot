package cn.iocoder.yudao.module.iot.dal.mysql.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotRestoreRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT TDengine 恢复记录 Mapper
 *
 * @author claude
 */
@Mapper
public interface IotRestoreRecordMapper extends BaseMapperX<IotRestoreRecordDO> {

    /**
     * 分页查询恢复记录
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    default PageResult<IotRestoreRecordDO> selectPage(IotRestoreRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotRestoreRecordDO>()
                .eqIfPresent(IotRestoreRecordDO::getBackupId, reqVO.getBackupId())
                .eqIfPresent(IotRestoreRecordDO::getRestoreType, reqVO.getRestoreType())
                .eqIfPresent(IotRestoreRecordDO::getRestoreStatus, reqVO.getRestoreStatus())
                .betweenIfPresent(IotRestoreRecordDO::getStartTime, reqVO.getStartTime())
                .orderByDesc(IotRestoreRecordDO::getId));
    }

    /**
     * 根据备份ID查询恢复记录列表
     *
     * @param backupId 备份ID
     * @return 恢复记录列表
     */
    default List<IotRestoreRecordDO> selectListByBackupId(Long backupId) {
        return selectList(IotRestoreRecordDO::getBackupId, backupId);
    }

    /**
     * 根据状态查询恢复记录列表
     *
     * @param restoreStatus 恢复状态
     * @return 恢复记录列表
     */
    default List<IotRestoreRecordDO> selectListByStatus(Integer restoreStatus) {
        return selectList(IotRestoreRecordDO::getRestoreStatus, restoreStatus);
    }

    /**
     * 统计恢复成功次数
     *
     * @return 成功次数
     */
    default Long selectSuccessCount() {
        return selectCount(IotRestoreRecordDO::getRestoreStatus, 1);
    }

}
