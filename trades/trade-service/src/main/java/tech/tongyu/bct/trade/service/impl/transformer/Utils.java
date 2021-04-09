package tech.tongyu.bct.trade.service.impl.transformer;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityFutureInstrument;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.EquityIndexFutureInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.EquityIndexInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.ListedEquityInstrument;
import tech.tongyu.bct.cm.reference.impl.*;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.market.dto.AssetClassEnum;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.dto.InstrumentTypeEnum;

import java.math.BigDecimal;

public class Utils {

    public static InstrumentOfValue fromDTO(InstrumentDTO instrumentDTO) {
        if (instrumentDTO.getInstrumentType().equals(InstrumentTypeEnum.STOCK) &&
                instrumentDTO.getAssetClass().equals(AssetClassEnum.EQUITY)) {
            return new ListedEquityInstrument(instrumentDTO.getInstrumentId());

        } else if (instrumentDTO.getInstrumentType().equals(InstrumentTypeEnum.FUTURES) &&
                instrumentDTO.getAssetClass().equals(AssetClassEnum.COMMODITY)) {
            return new CommodityFutureInstrument(instrumentDTO.getInstrumentId());

        } else if (instrumentDTO.getInstrumentType().equals(InstrumentTypeEnum.INDEX) &&
                instrumentDTO.getAssetClass().equals(AssetClassEnum.EQUITY)) {
            return new EquityIndexInstrument(instrumentDTO.getInstrumentId());

        } else if (instrumentDTO.getInstrumentType().equals(InstrumentTypeEnum.INDEX_FUTURES) &&
                instrumentDTO.getAssetClass().equals(AssetClassEnum.EQUITY)) {
            return new EquityIndexFutureInstrument(instrumentDTO.getInstrumentId());

        } else if (InstrumentTypeEnum.SPOT.equals(instrumentDTO.getInstrumentType()) &&
                AssetClassEnum.COMMODITY.equals(instrumentDTO.getAssetClass())){
            return new CommodityInstrument(instrumentDTO.getInstrumentId());

        }else {
            throw new CustomException(String.format("Not supported instrument type: %s", instrumentDTO.getInstrumentId()));
        }
    }


    public static UnitOfValue<BigDecimal> toUnitOfValue(UnitEnum unit, BigDecimal value) {
        switch (unit) {
            case PERCENT:
                return new UnitOfValue(Percent.get(), value);
            case CNY:
                return new UnitOfValue(CurrencyUnit.CNY, value);
            case LOT:
                return new UnitOfValue(Lot.get(), value);
            default:
                throw new CustomException(String.format("Not supported UnitType: %d", unit));
        }
    }

    public static InstrumentOfValuePartyRoleTypeEnum oppositePartyRole(InstrumentOfValuePartyRoleTypeEnum partyRole){
        switch (partyRole){
            case BUYER:
                return InstrumentOfValuePartyRoleTypeEnum.SELLER;
            case SELLER:
                return InstrumentOfValuePartyRoleTypeEnum.BUYER;
            default:
                throw new IllegalArgumentException(String.format("不支持的期权交易对手类型:%s", partyRole));
        }

    }
}

