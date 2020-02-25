package ru.offer.ignite.service;

import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import ru.offer.ignite.config.CacheNames;
import ru.offer.ignite.model.ProductOffering;
import ru.offer.ignite.model.Transition;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для инкапсуляции бизнес логики работы с продуктовыми предложениями.
 *
 * @author Stas Melnichuk
 */
public class ProductOfferingServiceImpl implements Service, ProductOfferingService {

    @IgniteInstanceResource
    private Ignite ignite;

    private IgniteCache<Long, Transition> transitionCache;
    private IgniteCache<Long, ProductOffering> productOfferingCache;

    @Override
    public void init(ServiceContext ctx) throws Exception {
        transitionCache = ignite.cache(CacheNames.TRANSITION);
        productOfferingCache = ignite.cache(CacheNames.PRODUCT_OFFERING);
    }

    @Override public void cancel(ServiceContext ctx) { }

    @Override public void execute(ServiceContext ctx) throws Exception { }

    @Override
    public Collection<ProductOffering> getPossibleAlternatives(Long currentProductOfferingId) {
        // Консистентность поддерживается на уровне Cache. Cм. CacheAtomicityMode.TRANSACTIONAL_SNAPSHOT
        // https://apacheignite.readme.io/docs/multiversion-concurrency-control
        // Есть куча ограничений по использованию, но в рамки текущей задачи возможности функциональности вписываются.
        try (Transaction ignored = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {
            val requestTime = Instant.now();
            Set<Long> keys = transitionCache.query(new ScanQuery<Long, Transition>(
                            (k, tr) -> isTransitionSuitable(tr, requestTime, currentProductOfferingId)),
                            longTransitionEntry -> longTransitionEntry.getValue().getTo()
            ).getAll().stream().flatMap(Collection::stream).collect(Collectors.toSet());

            return productOfferingCache.getAll(keys).values();
        }
    }

    /**
     * Проверка {@link Transition} на применимость для перехода для данного продуктового предложения.
     */
    private boolean isTransitionSuitable(Transition tr, Instant requestTime, Long currentProductOfferingId) {
        return tr.getFromDate().isBefore(requestTime) &&
                requestTime.isBefore(tr.getToDate()) &&
                tr.getFrom().contains(currentProductOfferingId);
    }
}
