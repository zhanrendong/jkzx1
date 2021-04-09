package tech.tongyu.bct.market.dto;

import org.springframework.beans.BeanUtils;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

public class InstrumentDTO {
    @BctField(name = "instrumentId", description = "标的物ID", type = "String")
    private String instrumentId;
    @BctField(name = "assetClass", description = "资产类别", type = "AssetClassEnum")
    private AssetClassEnum assetClass;
    @BctField(name = "assetSubClass", description = "资产子类别", type = "AssetSubClassEnum")
    private AssetSubClassEnum assetSubClass;
    @BctField(name = "instrumentType", description = "标的物合约类型", type = "InstrumentTypeEnum")
    private InstrumentTypeEnum instrumentType;
    @BctField(name = "instrumentInfo", description = "标的物信息", type = "InstrumentInfo")
    private InstrumentInfo instrumentInfo;

    public InstrumentDTO() {
    }

    public InstrumentDTO(String instrumentId, AssetClassEnum assetClass, AssetSubClassEnum assetSubClass,
                         InstrumentTypeEnum instrumentType, InstrumentInfo instrumentInfo) {
        this.instrumentId = instrumentId;
        this.assetClass = assetClass;
        this.assetSubClass = assetSubClass;
        this.instrumentType = instrumentType;
        this.instrumentInfo = instrumentInfo;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public AssetClassEnum getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClassEnum assetClass) {
        this.assetClass = assetClass;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentTypeEnum instrumentType) {
        this.instrumentType = instrumentType;
    }

    public InstrumentInfo getInstrumentInfo() {
        return instrumentInfo;
    }

    public void setInstrumentInfo(InstrumentInfo instrumentInfo) {
        this.instrumentInfo = instrumentInfo;
    }

    public AssetSubClassEnum getAssetSubClass() {
        return assetSubClass;
    }

    public void setAssetSubClass(AssetSubClassEnum assetSubClass) {
        this.assetSubClass = assetSubClass;
    }

    public InstrumentInfoDTO toInstrumentInfoDTO() {
        InstrumentInfoDTO dto = new InstrumentInfoDTO();
        dto.setInstrumentId(instrumentId);
        dto.setInstrumentType(instrumentType);
        dto.setAssetClass(assetClass);
        dto.setAssetSubClass(assetSubClass);
        if (instrumentInfo instanceof CommodityFuturesInfo) {
            CommodityFuturesInfo info = (CommodityFuturesInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof CommoditySpotInfo) {
            CommoditySpotInfo info = (CommoditySpotInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof EquityStockInfo) {
            EquityStockInfo info = (EquityStockInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof EquityIndexInfo) {
            EquityIndexInfo info = (EquityIndexInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof EquityIndexFuturesInfo) {
            EquityIndexFuturesInfo info = (EquityIndexFuturesInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof CommodityFuturesOptionInfo) {
            CommodityFuturesOptionInfo info = (CommodityFuturesOptionInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof EquityIndexOptionInfo) {
            EquityIndexOptionInfo info = (EquityIndexOptionInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else if (instrumentInfo instanceof EquityStockOptionInfo) {
            EquityStockOptionInfo info = (EquityStockOptionInfo) instrumentInfo;
            BeanUtils.copyProperties(info, dto);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "合约类型非法。检查输入是否为以下类型：" +
                    "CommodityFutures, CommoditySpot, EquityStock, EquityIndex, EquityIndexFutures");
        }
        return dto;
    }

    @Override
    public String toString() {
        return "InstrumentDTO{" +
                "instrumentId='" + instrumentId + '\'' +
                ", assetClass=" + assetClass +
                ", instrumentType=" + instrumentType +
                ", instrumentInfo=" + instrumentInfo +
                '}';
    }
}
