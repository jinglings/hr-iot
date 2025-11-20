package cn.iocoder.yudao.module.iot.convert.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-18T13:59:21+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
public class IotEdgeGatewayConvertImpl implements IotEdgeGatewayConvert {

    @Override
    public IotEdgeGatewayDO convert(IotEdgeGatewayCreateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeGatewayDO.IotEdgeGatewayDOBuilder iotEdgeGatewayDO = IotEdgeGatewayDO.builder();

        iotEdgeGatewayDO.name( bean.getName() );
        iotEdgeGatewayDO.serialNumber( bean.getSerialNumber() );
        iotEdgeGatewayDO.deviceType( bean.getDeviceType() );
        iotEdgeGatewayDO.hardwareVersion( bean.getHardwareVersion() );
        iotEdgeGatewayDO.location( bean.getLocation() );
        iotEdgeGatewayDO.cpuCores( bean.getCpuCores() );
        iotEdgeGatewayDO.memoryTotal( bean.getMemoryTotal() );
        iotEdgeGatewayDO.diskTotal( bean.getDiskTotal() );
        iotEdgeGatewayDO.config( bean.getConfig() );
        iotEdgeGatewayDO.description( bean.getDescription() );

        return iotEdgeGatewayDO.build();
    }

    @Override
    public IotEdgeGatewayDO convert(IotEdgeGatewayUpdateReqVO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeGatewayDO.IotEdgeGatewayDOBuilder iotEdgeGatewayDO = IotEdgeGatewayDO.builder();

        iotEdgeGatewayDO.id( bean.getId() );
        iotEdgeGatewayDO.name( bean.getName() );
        iotEdgeGatewayDO.deviceType( bean.getDeviceType() );
        iotEdgeGatewayDO.hardwareVersion( bean.getHardwareVersion() );
        iotEdgeGatewayDO.softwareVersion( bean.getSoftwareVersion() );
        iotEdgeGatewayDO.location( bean.getLocation() );
        iotEdgeGatewayDO.config( bean.getConfig() );
        iotEdgeGatewayDO.description( bean.getDescription() );

        return iotEdgeGatewayDO.build();
    }

    @Override
    public IotEdgeGatewayRespVO convert(IotEdgeGatewayDO bean) {
        if ( bean == null ) {
            return null;
        }

        IotEdgeGatewayRespVO iotEdgeGatewayRespVO = new IotEdgeGatewayRespVO();

        iotEdgeGatewayRespVO.setId( bean.getId() );
        iotEdgeGatewayRespVO.setName( bean.getName() );
        iotEdgeGatewayRespVO.setSerialNumber( bean.getSerialNumber() );
        iotEdgeGatewayRespVO.setGatewayKey( bean.getGatewayKey() );
        iotEdgeGatewayRespVO.setGatewaySecret( bean.getGatewaySecret() );
        iotEdgeGatewayRespVO.setDeviceType( bean.getDeviceType() );
        iotEdgeGatewayRespVO.setHardwareVersion( bean.getHardwareVersion() );
        iotEdgeGatewayRespVO.setSoftwareVersion( bean.getSoftwareVersion() );
        iotEdgeGatewayRespVO.setIpAddress( bean.getIpAddress() );
        iotEdgeGatewayRespVO.setMacAddress( bean.getMacAddress() );
        iotEdgeGatewayRespVO.setLocation( bean.getLocation() );
        iotEdgeGatewayRespVO.setStatus( bean.getStatus() );
        iotEdgeGatewayRespVO.setLastOnlineTime( bean.getLastOnlineTime() );
        iotEdgeGatewayRespVO.setLastOfflineTime( bean.getLastOfflineTime() );
        iotEdgeGatewayRespVO.setActiveTime( bean.getActiveTime() );
        iotEdgeGatewayRespVO.setCpuCores( bean.getCpuCores() );
        iotEdgeGatewayRespVO.setMemoryTotal( bean.getMemoryTotal() );
        iotEdgeGatewayRespVO.setDiskTotal( bean.getDiskTotal() );
        iotEdgeGatewayRespVO.setConfig( bean.getConfig() );
        iotEdgeGatewayRespVO.setDescription( bean.getDescription() );
        iotEdgeGatewayRespVO.setCreateTime( bean.getCreateTime() );

        return iotEdgeGatewayRespVO;
    }

    @Override
    public List<IotEdgeGatewayRespVO> convertList(List<IotEdgeGatewayDO> list) {
        if ( list == null ) {
            return null;
        }

        List<IotEdgeGatewayRespVO> list1 = new ArrayList<IotEdgeGatewayRespVO>( list.size() );
        for ( IotEdgeGatewayDO iotEdgeGatewayDO : list ) {
            list1.add( convert( iotEdgeGatewayDO ) );
        }

        return list1;
    }

    @Override
    public PageResult<IotEdgeGatewayRespVO> convertPage(PageResult<IotEdgeGatewayDO> page) {
        if ( page == null ) {
            return null;
        }

        PageResult<IotEdgeGatewayRespVO> pageResult = new PageResult<IotEdgeGatewayRespVO>();

        pageResult.setTotal( page.getTotal() );
        pageResult.setList( convertList( page.getList() ) );

        return pageResult;
    }
}
