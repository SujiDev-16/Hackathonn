package com.aws.poc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.core.env.Environment;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
@RequestMapping("/")
public class TestController {

	@Autowired
	public RestTemplate restTemplate;

	@Autowired
	public Environment environment;

	@GetMapping("/")
	public String getResponse() {
		return "Hello world Updated";
	}

	public void sendSMS(String lat, String lon, String num) {

		Twilio.init(environment.getProperty("TWILIO_ACCOUNT_SID"), environment.getProperty("TWILIO_AUTH_TOKEN"));

		Message.creator(new PhoneNumber(num), new PhoneNumber("+19706151450"),
				"EMERGENCY!!!" + "\n"
						+ "There is an accident observed at the below location.Please reach out and do the needful."
						+ "\n" + "Latitude : " + lat + "\n" + "Longitude : " + lon)
				.create();
		

		
	}

	public String getNearbyHospDtls(String latitude, String longitude, int radius, String keyword) throws Exception {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder()
				.url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "%2C"
						+ longitude + "&radius=" + radius + "&type=hospital&keyword=" + keyword
						+ "&key=AIzaSyCA0Rhg2V970ax4ZXmO9Hty1Yq_slq0qcw")
				.method("POST", body).build();

		Response response = client.newCall(request).execute();

		return response.body().string();

	}

	private int getRandomIndex(int size) {
		Random rand = new Random();
		return rand.nextInt(size);
	}

	@RequestMapping(value = "/hack/{latitude}/{longitude}")
	public @ResponseBody String getNearByLocationss(@PathVariable("latitude") String latitude,
			@PathVariable("longitude") String longitude) throws Exception {
		String phnNumbers[] = { "+919498036800", "+918867859507" };
		List<String> keywordList = new ArrayList<String>();
		keywordList.add("super%20speciality");
		keywordList.add("medicalcare");
		keywordList.add("24hours");
		keywordList.add("emergency");
		int radius = 1500;
		int index = getRandomIndex(keywordList.size());
		String nearlyHospDtls = getNearbyHospDtls(latitude, longitude, radius, keywordList.get(index));

		JSONObject jsonObject = new JSONObject(nearlyHospDtls);
		String value = jsonObject.getString("status"); // Here's status value
		while (value.equalsIgnoreCase("ZERO_RESULTS")) {
			index = getRandomIndex(keywordList.size());
			nearlyHospDtls = getNearbyHospDtls(latitude, longitude, radius + 500, keywordList.get(index));
			jsonObject = new JSONObject(nearlyHospDtls);
			value = jsonObject.getString("status");
		}
		 for(String num : phnNumbers) {
			sendSMS(latitude, longitude, num);
		 }

		return "SMS Sent Successfully to nearby hospital";
	}

}
