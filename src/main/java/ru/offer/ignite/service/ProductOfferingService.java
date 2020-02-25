package ru.offer.ignite.service;

import ru.offer.ignite.model.ProductOffering;

import java.util.Collection;

/**
 * @author Stas Melnichuk
 */
public interface ProductOfferingService {
    Collection<ProductOffering> getPossibleAlternatives(Long currentProductOfferingId);
}
