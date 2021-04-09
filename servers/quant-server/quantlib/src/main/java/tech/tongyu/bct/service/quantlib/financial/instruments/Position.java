package tech.tongyu.bct.service.quantlib.financial.instruments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;

/**
 * A position consists of two parts: what's being traded (the product) and how much (the quantity).
 * @param <T> The product being traded
 */
@BctQuantSerializable
public class Position<T> {
    @JsonProperty("class")
    private final String type = Position.class.getSimpleName();
    private T product;
    private double quantity;
    private String id;

    @JsonCreator
    public Position(
            @JsonProperty("product") T product,
            @JsonProperty("quantity") double quantity,
            @JsonProperty("id") String id) {
        this.product = product;
        this.quantity = quantity;
        this.id = id;
    }

    public Position(T product, double quantity) {
        this.product = product;
        this.quantity = quantity;
        this.id = "";
    }

    public T getProduct() {
        return product;
    }

    public void setProduct(T product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*public static void main(String[] args) throws Exception {
        VanillaEuropean optn = VanillaEuropean.call(100.0,
                LocalDateTime.of(2017, 7, 1, 0, 0), LocalDateTime.of(2017, 7, 1, 0, 0));
        DigitalCash digi = new DigitalCash(OptionType.CALL, 100.0, 1.0,
                LocalDateTime.of(2018, 7, 1, 0, 0), LocalDateTime.of(2018, 7, 1, 0, 0));
        //String optnJson = JsonMapper.mapper.writeValueAsString(optn);
        Position<VanillaEuropean> p1 = new Position(optn, 1.0);
        Position<DigitalCash> p2 = new Position(digi, -1.0);
        // test a single position
        String s = JsonMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(p1);
        System.out.println(s);
        Position<VanillaEuropean> p1Restored = JsonMapper.mapper.readValue(s, new TypeReference<Position<VanillaEuropean>>(){});
        System.out.println(p1Restored.getQuantity());
        // test a vector of positions
        List<Position<?>> l = new ArrayList<>();
        l.add(p1);
        l.add(p2);
        s = JsonMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(l);
        System.out.println(s);
        List<Position<?>> lRestored = JsonMapper.mapper.readValue(s, new TypeReference<List<Position<?>>>(){});
        System.out.println(lRestored.get(1).getQuantity());
    }*/
}