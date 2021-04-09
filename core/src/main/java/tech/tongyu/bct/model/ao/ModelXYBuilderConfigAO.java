package tech.tongyu.bct.model.ao;

import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ModelXYBuilderConfigAO {
    public static class Slice {
        private final LocalDateTime timestamp;
        private final List<Double> spots;
        private final List<Double> prices;
        private final List<Double> deltas;
        private final List<Double> gammas;
        private final List<Double> vegas;
        private final List<Double> thetas;

        public Slice(LocalDateTime timestamp,
                     List<Double> spots,
                     List<Double> prices,
                     List<Double> deltas,
                     List<Double> gammas,
                     List<Double> vegas,
                     List<Double> thetas) {
            this.timestamp = timestamp;
            this.spots = spots;
            this.prices = prices;
            this.deltas = deltas;
            this.gammas = gammas;
            this.vegas = vegas;
            this.thetas = thetas;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public List<Double> getSpots() {
            return spots;
        }

        public List<Double> getPrices() {
            return prices;
        }

        public List<Double> getDeltas() {
            return deltas;
        }

        public List<Double> getGammas() {
            return gammas;
        }

        public List<Double> getVegas() {
            return vegas;
        }

        public List<Double> getThetas() {
            return thetas;
        }
    }

    private String modelName;
    private LocalDate valuationDate;
    private ZoneId timezone;
    private InstanceEnum instance;
    private List<Slice> slices;

    public ModelXYBuilderConfigAO(String modelName,
                                  LocalDate valuationDate,
                                  ZoneId timezone,
                                  InstanceEnum instance,
                                  List<Slice> slices) {
        this.modelName = modelName;
        this.valuationDate = valuationDate;
        this.timezone = timezone;
        this.instance = instance;
        this.slices = slices;
    }

    public String getModelName() {
        return modelName;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public List<Slice> getSlices() {
        return slices;
    }
}
