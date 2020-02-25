package ru.offer.ignite;

import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import ru.offer.ignite.config.CacheNames;
import ru.offer.ignite.model.ProductOffering;
import ru.offer.ignite.model.Transition;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IgniteApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private Ignite ignite;

	// TODO Перезапись перед каждым тестом.
	@BeforeEach
	public void before() {
		val transitionCache = ignite.<Long, Transition>cache(CacheNames.TRANSITION);
		val transitionSeq = ignite.atomicSequence("TransitionIdSeq", 0, true);

		val transition1 = new Transition();
		val transition1Id = transitionSeq.incrementAndGet();
		transition1.setId(transition1Id);
		transition1.setFrom(Arrays.asList(1L,2L,3L));
		transition1.setTo(Arrays.asList(4L,5L,6L));
		transition1.setFromDate(Instant.now().minus(5, ChronoUnit.MINUTES));
		transition1.setToDate(Instant.now().plus(5, ChronoUnit.MINUTES));
		transitionCache.put(transition1Id, transition1);

		val transition2 = new Transition();
		val transition2Id = transitionSeq.incrementAndGet();
		transition2.setId(transition2Id);
		transition2.setFrom(Arrays.asList(2L,3L,4L));
		transition2.setTo(Arrays.asList(5L,6L,7L));
		transition2.setFromDate(Instant.now().minus(5, ChronoUnit.MINUTES));
		transition2.setToDate(Instant.now().plus(5, ChronoUnit.MINUTES));
		transitionCache.put(transition2Id, transition2);

		val productOfferingCache = ignite.<Long, ProductOffering>cache(CacheNames.PRODUCT_OFFERING);
		val productOfferingSeq = ignite.atomicSequence("ProductOfferingIdSeq", 0, true);

		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "first");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "second");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "third");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "fourth");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "fifth");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "sixth");
		generateProductOffering(productOfferingCache, productOfferingSeq.incrementAndGet(), "seventh");
	}

	private void generateProductOffering(IgniteCache<Long,ProductOffering> productOfferingCache, long id, String name) {
		val productOffering = new ProductOffering();
		productOffering.setId(id);
		productOffering.setName(name);
		productOfferingCache.put(id, productOffering);
	}


	@Test
	void simpleTest() throws URISyntaxException {
		ResponseEntity<List<ProductOffering>> result = restTemplate.exchange(
				RequestEntity.get(new URI("http://localhost:" + port + "/product-offerings/1/available-transitions")).build(),
				new ParameterizedTypeReference<List<ProductOffering>>() {});

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).hasSize(3);

		result = restTemplate.exchange(
				RequestEntity.get(new URI("http://localhost:" + port + "/product-offerings/2/available-transitions")).build(),
				new ParameterizedTypeReference<List<ProductOffering>>() {});

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).hasSize(4);

		result = restTemplate.exchange(
				RequestEntity.get(new URI("http://localhost:" + port + "/product-offerings/100500/available-transitions")).build(),
				new ParameterizedTypeReference<List<ProductOffering>>() {});

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).hasSize(0);
	}

}
