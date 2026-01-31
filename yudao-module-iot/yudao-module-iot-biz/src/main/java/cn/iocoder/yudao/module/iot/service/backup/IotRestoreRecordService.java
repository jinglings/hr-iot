package cn.iocoder.yudao.module.iot.service.backup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.backup.vo.restore.IotRestoreRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.backup.IotRestoreRecordDO;

import javax.validation.Valid;

/**
 * IoT TDengine 恢复记录 Service 接口
 *
 * @author claude
 */
public interface IotRestoreRecordService {

    /**
     * 创建恢复记录（执行恢复）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long executeRestore(@Valid IotRestoreRecordCreateReqVO createReqVO);

    /**
     * 获得恢复记录
     *
     * @param id 编号
     * @return 恢复记录
     */
    IotRestoreRecordDO getRestoreRecord(Long id);

    /**
     * 获得恢复记录分页
     *
     * @param pageReqVO 分页查询
     * @return 恢复记录分页
     */
    PageResult<IotRestoreRecordRespVO> getRestoreRecordPage(IotRestoreRecordPageReqVO pageReqVO);

}
