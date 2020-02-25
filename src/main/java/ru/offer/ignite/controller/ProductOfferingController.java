package ru.offer.ignite.controller;

import org.apache.ignite.Ignite;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.ignite.config.ServiceNames;
import ru.offer.ignite.model.ProductOffering;
import ru.offer.ignite.service.ProductOfferingService;

import java.util.Collection;

/**
 * @author Stas Melnichuk
 */
@RestController
@RequestMapping("product-offerings/")
public class ProductOfferingController {

    private final ProductOfferingService productOfferingService;

    public ProductOfferingController(Ignite ignite) {
        productOfferingService = ignite.services().
                serviceProxy(ServiceNames.PRODUCT_OFFERING_SERVICE, ProductOfferingService.class, false);
    }

    @GetMapping(path = "/{id}/available-transitions", produces = "application/json")
    public Collection<ProductOffering> getTransitionById(@PathVariable Long id) {
        return productOfferingService.getPossibleAlternatives(id);
    }

}
