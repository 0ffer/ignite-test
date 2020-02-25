package ru.offer.ignite.model;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * @author Stas Melnichuk
 */
@Data
public class Transition {
    private Long id;
    private List<Long> from;
    private List<Long> to;
    private Instant fromDate;
    private Instant toDate;
}
