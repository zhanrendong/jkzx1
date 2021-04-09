package tech.tongyu.bct.exchange.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.exchange.dao.dbo.local.SerialNo;
import tech.tongyu.bct.exchange.dao.repo.intel.local.SerialNoRepo;

import java.util.List;
import java.util.UUID;

@Service
public class SerialNoApis {
    private SerialNoRepo serialNoRepo;

    @Autowired
    public SerialNoApis(SerialNoRepo serialNoRepo){
        this.serialNoRepo = serialNoRepo;
    }

    @BctMethodInfo(
            description = "save max serialNo",
            retName = "status",
            retDescription = "result of the operation",
            service = "exchange-service"
    )
    public String excSerialNoSave(
            @BctMethodArg(description = "serial") Integer serial){
        serialNoRepo.save(new SerialNo(serial));
        return "success";
    }

    @BctMethodInfo(
            description = "find max serialNo",
            retName = "serialNo",
            retDescription = "serialNo",
            returnClass = SerialNo.class,
            service = "exchange-service"
    )
    public SerialNo excSerialNofind(){
        List<SerialNo> all = serialNoRepo.findAll();
        if(all.size()==0){
            serialNoRepo.save(new SerialNo(0));
            return serialNoRepo.findAll().get(0);
        }
        return all.get(0);
    }

    @BctMethodInfo(
            description = "update max serialNo",
            retName = "status",
            retDescription = "result of the operation",
            service = "exchange-service"
    )
    public String excSerialNoUpdate(
            @BctMethodArg(description = "uuid") String uuid,
            @BctMethodArg(description = "serial") Integer serial){
        SerialNo serialNo = new SerialNo();
        serialNo.setUuid(UUID.fromString(uuid));
        serialNo.setSerialNoMax(serial);
        serialNoRepo.save(serialNo);
        return "success";
    }
}
