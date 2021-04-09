package tech.tongyu.bct.document.dto;

/**
 *
 * 补充协议条款
 */
public class SuppleRider {

    private Integer id;
    private String productType;

    public SuppleRider() {
    }

    public SuppleRider(Integer id, String productType) {
        this.id = id;
        this.productType = productType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }
}
