package utils;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import java.util.List;

/**
 * Wrapper of a single instance of {@link Chainr} object.
 * @author Alejandro.Arevalo
 *
 */
public class UserDataTransformation {
	// Should be coming from conf files -_-.
	private static String TRANSFORMATION_RULES = "[{\"operation\":\"modify-overwrite-beta\",\"spec\":{\"contracts\":{\"contract\":{\"*\":{\"assetLists\":{\"assetList\":{\"*\":{\"id\":\"@(1,camId)\",\"remotebookingtype\":\"@(1,remoteBookingType)\"}}},\"contractType\":\"=toLower\"}}}}},{\"operation\":\"shift\",\"spec\":{\"contracts\":{\"contract\":{\"*\":{\"assetLists\":{\"assetList\":{\"*\":{\"remoteBookingFLag\":{\"*\":{\"@2\":\"households[&6].viewingcards[]\"}}}}},\"bouquetId\":\"households[&1].bouquetid\",\"contractType\":\"households[&1].contracttype\",\"householdId\":\"households[&1].householdid\"}}},\"country\":\"country\",\"customerStatus\":\"customerStatus\",\"details\":{\"customerFlags\":{\"globalBookmarkingOptOut\":\"globalbookmarkingoptout\",\"kidsAutoplayOptOut\":\"kidsautoplayoptout\",\"marketingPermissionFlag\":\"marketingPermissionFlag\",\"personalisedRecsOptOut\":\"personalisedrecsoptout\",\"productAnalyticsOptOut\":\"productanalyticsoptout\"},\"firstname\":\"details.firstname\",\"lastname\":\"details.lastname\",\"birthDate\":\"details.birthdate\",\"presentation\":\"details.title\"},\"id\":{\"aliases\":{\"email\":\"id.aliases.email\"},\"partyId\":\"id.partyid\",\"profileId\":\"id.profileid\"},\"services\":{\"nowTv_de\":{\"flags\":{\"TCFlag\":{\"Y\":{\"#\\\\${currentDate}\":\"services.nowtv-de.termsandconditionsaccepted\"}},\"fullysignedup\":\"services.nowtv-de.fullysignedup\",\"globalaccess\":\"services.nowtv-de.globalaccess\"}},\"skyGo\":{\"flags\":{\"TCFlag\":{\"Y\":{\"#\\\\${currentDate}\":\"services.skygo.termsandconditionsaccepted\"}},\"globalaccess\":\"services.skygo.globalaccess\"}},\"skyKids\":{\"flags\":{\"TCFlag\":{\"Y\":{\"#\\\\${currentDate}\":\"services.skykids.termsandconditionsaccepted\"}},\"globalaccess\":\"services.skykids.globalaccess\"}},\"skyPlus\":{\"flags\":{\"TCFlag\":{\"Y\":{\"#\\\\${currentDate}\":\"services.skyplus.termsandconditionsaccepted\"}},\"globalaccess\":\"services.skyplus.globalaccess\"}},\"skyQ\":{\"flags\":{\"TCFlag\":{\"Y\":{\"#\\\\${currentDate}\":\"services.skyq.termsandconditionsaccepted\"}},\"globalaccess\":\"services.skyq.globalaccess\"}}},\"trackingid\":\"id.trackingid\"}},{\"operation\":\"remove\",\"spec\":{\"households\":{\"*\":{\"viewingcards\":{\"*\":{\"camId\":\"\",\"remoteBookingFLag\":\"\",\"remoteBookingType\":\"\",\"smcId\":\"\"}}}}}},{\"operation\":\"default\",\"spec\":{\"services\":{\"|nowtv-de\":{\"globalaccess\":false,\"servicename\":\"nowtv-de\"},\"|skygo\":{\"globalaccess\":false,\"servicename\":\"skygo\"},\"|skykids\":{\"globalaccess\":false,\"servicename\":\"skykids\"},\"|skyplus\":{\"globalaccess\":false,\"servicename\":\"skyplus\"},\"|skyq\":{\"globalaccess\":false,\"servicename\":\"skyq\"}}}}]";
	
	private final Chainr joltTransformer;

	public static UserDataTransformation create() {
		return new UserDataTransformation();
	}

	public UserDataTransformation() {
		List<Object> chainrSpecJSON = JsonUtils.jsonToList(TRANSFORMATION_RULES);
		this.joltTransformer = Chainr.fromSpec(chainrSpecJSON);
	}

	public String transform(String userDataJson, String currentDate) {
		Object jsonUserData = JsonUtils.jsonToObject(userDataJson);
		return JsonUtils.toJsonString(
				joltTransformer.transform(jsonUserData)
				).replace("${currentDate}", currentDate);
	}
}
