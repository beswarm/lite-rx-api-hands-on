/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Learn how to use StepVerifier to test Mono, Flux or any other kind of Reactive Streams Publisher.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/docs/test/release/api/reactor/test/StepVerifier.html">StepVerifier Javadoc</a>
 */
public class Part03StepVerifier {

//========================================================================================

    @Test
    public void expectElementsThenComplete() {
        expectFooBarComplete(Flux.just("foo", "bar"));
    }

    // Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then completes successfully.
    void expectFooBarComplete(Flux<String> flux) {
        StepVerifier.create(flux).expectNext("foo").expectNext("bar").verifyComplete();
    }

//========================================================================================

    @Test
    public void expect2ElementsThenError() {
        expectFooBarError(Flux.just("foo", "bar").concatWith(Mono.error(new RuntimeException())));
    }

    // Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then a RuntimeException error.
    void expectFooBarError(Flux<String> flux) {
        StepVerifier.create(flux).expectNext("foo", "bar").verifyError(RuntimeException.class);
    }

//========================================================================================

    @Test
    public void expectElementsWithThenComplete() {
        expectSkylerJesseComplete(Flux.just(new User("swhite", null, null), new User("jpinkman", null, null)));
    }

    // Use StepVerifier to check that the flux parameter emits a User with "swhite" username and another one with "jpinkman" then completes successfully.
    void expectSkylerJesseComplete(Flux<User> flux) {
        StepVerifier.create(flux).expectNextMatches(user -> "swhite".equals(user.getUsername()))
                .expectNextMatches(user -> "jpinkman".equals(user.getUsername()))
                .verifyComplete();
    }

//========================================================================================

    @Test
    public void count() {
        expect10Elements(Flux.interval(Duration.ofSeconds(1)).take(10));
    }

    // Expect 10 elements then complete and notice how long it takes for running the test
    void expect10Elements(Flux<Long> flux) {
        Duration duration =
                StepVerifier.create(flux).expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L).verifyComplete();
        System.out.println(duration.getSeconds());
    }

//========================================================================================

    @Test
    public void countWithVirtualTime() {
        expect3600Elements(() -> Flux.interval(Duration.ofSeconds(1)).take(3600));
    }

    // Expect 3600 elements then complete using the virtual time capabilities provided via StepVerifier.withVirtualTime() and notice how long it takes for running the test
    void expect3600Elements(Supplier<Flux<Long>> supplier) {
        List<Long> longList = new ArrayList<>();
        for (int i = 0; i < 3600; i++) {
            longList.add((long) i);
        }
        StepVerifier.withVirtualTime(supplier)
                .thenAwait(Duration.ofSeconds(3600))
                .expectNextCount(3600)
                .verifyComplete();
    }

}
