package cn.iocoder.yudao.module.iot.dal.mysql.ai;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiConversationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IotAiConversationMapper extends BaseMapperX<IotAiConversationDO> {

    default List<IotAiConversationDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<IotAiConversationDO>()
                .eq(IotAiConversationDO::getUserId, userId)
                .orderByDesc(IotAiConversationDO::getId));
    }

}
