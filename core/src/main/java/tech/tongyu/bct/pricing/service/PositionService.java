package tech.tongyu.bct.pricing.service;

import tech.tongyu.bct.quant.library.priceable.Position;

import java.util.List;
import java.util.Optional;

interface PositionService {
    Position savePosition(Position position);

    Optional<Position> getPosition(String positionId);

    Optional<Position> deletePosition(String positionId);

    List<Position> listAllPositions();

    List<Position> listPositions(List<String> positionIds);

    List<Position> decompose(List<Position> positions);
}
