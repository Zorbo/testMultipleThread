package utils;

import static org.junit.Assert.assertEquals;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CustomerDataConfiguration extends JUnitRouteTest {

	protected static final Logger LOG = LoggerFactory.getLogger(CustomerDataConfiguration.class);


	private static final Configuration conf = Configuration.getInstance();

	private static final String URI_CUSTOMER_DATA = "/api/customer/%s";
	private static final String REASOURCE_PATH = "testCaseDocuments";
	private static final String IN_CORRECT_CUSTOMER_ID = "customer-id-in_correct";
	protected static final String CORRECT_PASSWORD = "7923";
	private static final int EXCEPTION = 1;
	private static final int NORMAL = 0;
	private static final int UPDATE_IN_PROGRESS = 2;
	private static CouchbaseConnector cbConnectormock;
	protected volatile AtomicInteger TEST_CASE = new AtomicInteger(NORMAL);

	private TestProbe testProbe;
	private CustomerDataServer server;
	protected static final String CORRECT_CUSTOMER_ID = "customeridtest";
	protected static final ObjectMapper MAPPER = new ObjectMapper();

	protected static final String GET = "GET";
	protected static final String POST = "POST";
	protected static final String PUT = "PUT";


	protected static final String TIMESTAMP_FORMAT="dd-MMM-yyyy";
	protected static final DateTimeFormatter sdf = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
	protected volatile AtomicReference<Consumer<Pair<Collection<Header>, GenericRecord>>> TEST_CONSUMER =  new AtomicReference<>();

	protected static CouchbaseAsynchDao asyncDaoMock;
	protected static String application_json_MediaType;
	protected static ActorSystem system;

	protected TestRoute appRoute = null;
	private ActorRef healthActor;
	protected ActorRef getProfileActor;
	protected ActorRef managePersonalDataActor;
	protected ActorRef kafkaNotificationActor;





	@BeforeClass
	public static void startup() {
		cbConnectormock = mock(CouchbaseConnector.class);		
		application_json_MediaType = "application/json";
		system = ActorSystem.create("getProfileTestAkkaSystem");
	}

	//#set-up
	@Before
	public void init() {
		// Start Mock couchbase

		asyncDaoMock = mock(CouchbaseAsynchDao.class);
		when(cbConnectormock.getCouchbaseAsynchDao()).thenReturn(asyncDaoMock);
		// END Mock couchbase

		// START Mock KafkaNotificationActor
		Consumer<Pair<Collection<Header>, GenericRecord>> testCaseConsumer = (record) -> {
			if(TEST_CONSUMER.get() != null) {
				TEST_CONSUMER.get().accept(record);
			}
		};

		TEST_CONSUMER.set(null);
		TEST_CASE.set(NORMAL);

		testProbe = TestProbe.apply(system);
		Runnable command = () -> {
			switch (TEST_CASE.get()) {
			case EXCEPTION:
				throw new IllegalStateException("MOCKED EXCEPTION");
			case UPDATE_IN_PROGRESS:
				throw new TimeoutException("KAFKA TIMEOUT ARRIVED");
			case NORMAL:
			default:
				return;
			}
		};
		// END Mock KafkaNotificationActor

		// Init test actors
		ActorRef kafkaNotificationsActor = system.actorOf(TestKafkaProducerActor.props(testProbe, command, testCaseConsumer));
		managePersonalDataActor = system.actorOf(ManagePersonalDataActor.props(kafkaNotificationsActor), "managePersonalDataActor");
		getProfileActor = system.actorOf(GetProfileActor.props(asyncDaoMock, UserDataTransformation.create(), this::getMockedLocalDateTime), "getProfileActor");
		healthActor = system.actorOf(HealthStatusActor.props(Arrays.asList("getProfileActor", "managePersonalDataActor")), "healthActor");

		server = new CustomerDataServer(system, getProfileActor,managePersonalDataActor, healthActor);
		appRoute = testRoute(server.createRoute());
	}
	private LocalDateTime getMockedLocalDateTime() {
		return LocalDateTime.parse("13-11-2019 00:00", DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
	}

	@After
	public void clean() {
		managePersonalDataActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
		getProfileActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
		healthActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
	}

	@AfterClass
	public static void teardown() {
		system.terminate();
	}

	protected akka.http.javadsl.testkit.TestRouteResult createFailureRequestAndAssertResponse(String method, String customerId, ResponseCodesEnum responseCodesEnum, String payload){
		return appRoute.run(createHttpRequest(method, customerId, payload))

				.assertStatusCode(extractExcpetedStatusCodeValue(responseCodesEnum))
				.assertMediaType(application_json_MediaType)
				.assertEntityAs(Jackson.unmarshaller(CustomerDataResponse.class), getSimpleResponseEntity(responseCodesEnum));
	}

	protected void createCorrectRequestAndAssertResponse(String method, String customerId, JsonNode excpectedBodyResponse, String payload) {
		JsonNode actualResponseBody = appRoute.run(createHttpRequest(method, customerId, payload))
				.assertStatusCode(extractExcpetedStatusCodeValue(ResponseCodesEnum.OK))
				.assertMediaType(application_json_MediaType)
				.entity(Jackson.unmarshaller(JsonNode.class));

		assertEquals(excpectedBodyResponse, actualResponseBody);
	}
	/**
	 * 
	 * @param method
	 * @param customerId
	 * @param expectedCode
	 * @param payload
	 * @param expecteddataResponse
	 */
	protected void createRequestAndAssertResponse(String method, String customerId, ResponseCodesEnum expectedCode,
                                                  String payload, CustomerDataResponse expecteddataResponse) {
		CustomerDataResponse actualResponseBody = appRoute.run(createHttpRequest(method, customerId, payload))
				.assertStatusCode(extractExcpetedStatusCodeValue(expectedCode))
				.assertMediaType(application_json_MediaType)
				.entity(Jackson.unmarshaller(CustomerDataResponse.class));

		assertEquals(expecteddataResponse, actualResponseBody);
	}

	protected String createManDataBody(String firstName, String lastName) {
		return JsonObject.create()
				.put("first-name", firstName)
				.put("last-name", lastName)
				.toString();
	}

	protected String createPersDataBody(String oldPass, String newPass) {
		return JsonObject.create()
				.put("old-password", oldPass)
				.put("new-password", newPass)
				.toString();
	}


	}
	// ***************************************
	// * utility to read docs for test cases *
	// ***************************************
	protected static JsonDocument readDoc(String id) {
		String jsonData = readTestFile(id);

		JsonDocument doc = JsonDocument.create(id);
		JsonObject obj = JsonObject.fromJson(jsonData);
		doc = JsonDocument.from(doc, id, obj);

		LOG.info("MOCKED DOCUMENT: {}", doc);

		return doc;
	}

	protected static String readTestFile(String id) {
		return readTestFile(id, "json");
	}

	private static String readTestFile(String id, String extension) {
		LOG.info("looking for file in " + REASOURCE_PATH + "/" + id + "." + extension);
		String jsonData = null;
		try {
			ClassLoader classLoader = GetProfileTest.class.getClassLoader();
			File parentDirectory = new File(classLoader.getResource(REASOURCE_PATH).getFile());

			File jsonFile = new File(parentDirectory, id + "." + extension);


			jsonData = new String(Files.readAllBytes(jsonFile.toPath()));
		} catch (IOException e) {
		}
		return jsonData;
	}

}
