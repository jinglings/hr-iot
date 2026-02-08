package cn.iocoder.yudao.module.iot.dal.mysql.ai;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IotAiMessageMapper extends BaseMapperX<IotAiMessageDO> {

    default List<IotAiMessageDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<IotAiMessageDO>()
                .eq(IotAiMessageDO::getConversationId, conversationId)
                .orderByAsc(IotAiMessageDO::getId));
    }

}
