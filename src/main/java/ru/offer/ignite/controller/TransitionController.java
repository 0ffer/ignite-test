package ru.offer.ignite.controller;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.ignite.config.CacheNames;
import ru.offer.ignite.model.Transition;

/**
 * @author Stas Melnichuk
 */
@RestController
@RequestMapping("/transitions")
public class TransitionController {

    private IgniteCache<Long, Transition> transitionCache;

    public TransitionController(Ignite ignite) {
        transitionCache = ignite.cache(CacheNames.TRANSITION);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public Transition getTransitionById(@PathVariable Long id) {
        return transitionCache.get(id);
    }

}
