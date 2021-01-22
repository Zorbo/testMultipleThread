import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.couchbase.client.java.document.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import utils.CustomerDataConfiguration;
import utils.UserDataTransformation;

//#test-top


public class GetProfileTest extends CustomerDataConfiguration {
	


	@Test
	public void okTestWithConcurrency() throws IOException, InterruptedException {

		LOG.info("\n=========================== okTestWithConcurrency ===========================\n");
		final UserDataTransformation userDataTransformation = UserDataTransformation.create();
		final Collection<String> exceptionResults = Collections.synchronizedCollection(new ArrayList<>());

		final JsonObject expectedJson = readDoc("ExcpectedUserDataTransformation").content();
		String userDataContent = readDoc("CompleteDoc").content().toString();
		String date ="13-11-2019";
		int numberOfThreads = 5;
		ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			final int counter = i;
			service.submit(() -> {
				try {
					LOG.info("++++++++ Starting Thread: {} +++++++++", counter);
					for (int x = 0; x < 10; x++) {
						String result = userDataTransformation.transform(userDataContent, date);
						assertEquals(expectedJson, JsonObject.fromJson(result));
					}
					LOG.info("++++++++ Finishing Thread: {} +++++++++", counter);
				} catch (Throwable e) {
					exceptionResults.add(ExceptionUtils.getMessage(e));
				}
				latch.countDown();
			});
		}
		latch.await();
		assertTrue("The exception list should be empty: " + exceptionResults, exceptionResults.isEmpty());
	}
}

