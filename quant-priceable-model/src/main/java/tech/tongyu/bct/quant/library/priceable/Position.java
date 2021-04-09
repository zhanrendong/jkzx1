package tech.tongyu.bct.quant.library.priceable;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.feature.Decomposable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Position {
    private final String positionId;
    private final double quantity;
    private final Priceable priceable;
    // this is a hack!
    private final boolean decomposed;
    private final double quantityBeforeDecomposition;

    public Position(String positionId, double quantity, Priceable priceable) {
        this.positionId = positionId;
        this.quantity = quantity;
        this.priceable = priceable;
        this.decomposed = false;
        this.quantityBeforeDecomposition = quantity;
    }

    @JsonCreator
    public Position(String positionId, double quantity, Priceable priceable,
                    boolean decomposed, double quantityBeforeDecomposition) {
        this.positionId = positionId;
        this.quantity = quantity;
        this.priceable = priceable;
        this.decomposed = decomposed;
        this.quantityBeforeDecomposition = quantityBeforeDecomposition;
    }

    public List<Position> decompose() {
        if (priceable instanceof Decomposable) {
            List<Position> decomposed = ((Decomposable) priceable).decompose(positionId);
            return decomposed.stream()
                    .map(p -> new Position(p.getPositionId(),
                            p.getQuantity() * this.quantity,
                            p.getPriceable(),
                            true,
                            this.quantity))
                    .collect(Collectors.toList());
        } else {
            List<Position> ret = new ArrayList<>();
            ret.add(this);
            return ret;
        }
    }

    public String getPositionId() {
        return positionId;
    }

    public double getQuantity() {
        return quantity;
    }

    public Priceable getPriceable() {
        return priceable;
    }

    public boolean decomposed() {
        return decomposed;
    }

    public double getQuantityBeforeDecomposition() {
        return quantityBeforeDecomposition;
    }
}
