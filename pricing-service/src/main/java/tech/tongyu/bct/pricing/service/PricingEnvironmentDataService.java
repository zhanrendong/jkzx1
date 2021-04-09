package tech.tongyu.bct.pricing.service;

import tech.tongyu.bct.pricing.dao.dto.PricingEnvironmentDTO;

import java.util.List;

public interface PricingEnvironmentDataService {
    PricingEnvironmentDTO create(PricingEnvironmentDTO dto);
    PricingEnvironmentDTO load(String pricingEnvironmentId);
    String delete(String pricingEnvironmentId);
    List<String> list();
}
