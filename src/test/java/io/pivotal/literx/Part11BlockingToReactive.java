package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.BlockingRepository;
import io.pivotal.literx.repository.BlockingUserRepository;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Iterator;

import static io.pivotal.literx.domain.User.JESSE;
import static io.pivotal.literx.domain.User.SKYLER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for
 * blocking code that produces or receives data.
 * <p>
 * For those who know RxJava:
 * - RxJava subscribeOn = Reactor subscribeOn
 * - RxJava observeOn = Reactor publishOn
 *
 * @author Sebastien Deleuze
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactive {

//========================================================================================

    @Test
    public void slowPublisherFastSubscriber() {
        BlockingUserRepository repository = new BlockingUserRepository();
        Flux<User> flux = blockingRepositoryToFlux(repository);
        assertEquals("The call to findAll must be deferred until the flux is subscribed", 0, repository.getCallCount());
        StepVerifier.create(flux)
                .expectNext(SKYLER, JESSE, User.WALTER, User.SAUL)
                .verifyComplete();
    }

    // Create a Flux for reading all users from the blocking repository deferred until the flux is subscribed, and run it with an elastic scheduler
    Flux<User> blockingRepositoryToFlux(BlockingRepository<User> repository) {
        return Flux.defer(() -> Flux.fromIterable(repository.findAll())).subscribeOn(Schedulers.elastic());
//        return Flux.<User>create(sink -> {
//            Iterable<User> users = repository.findAll();
//            for (User u : users) {
//                sink.next(u);
//            }
//            sink.complete();
//        }).subscribeOn(Schedulers.elastic());
    }

//========================================================================================

    @Test
    public void fastPublisherSlowSubscriber() {
        ReactiveRepository<User> reactiveRepository = new ReactiveUserRepository();
        BlockingUserRepository blockingRepository = new BlockingUserRepository(new User[]{});
        Mono<Void> complete = fluxToBlockingRepository(reactiveRepository.findAll(), blockingRepository);
        assertEquals(0, blockingRepository.getCallCount());
        StepVerifier.create(complete)
                .verifyComplete();
        Iterator<User> it = blockingRepository.findAll().iterator();
        assertEquals(SKYLER, it.next());
        assertEquals(JESSE, it.next());
        assertEquals(User.WALTER, it.next());
        assertEquals(User.SAUL, it.next());
        assertFalse(it.hasNext());
    }

    //  Insert users contained in the Flux parameter in the blocking repository using an parallel scheduler and return a Mono<Void> that signal the end of the operation
    Mono<Void> fluxToBlockingRepository(Flux<User> flux, BlockingRepository<User> repository) {
        return flux.publishOn(Schedulers.parallel()).doOnNext(u -> repository.save(u)).then();
    }

}
