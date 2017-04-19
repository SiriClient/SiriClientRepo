package com.nituv.mot.siri.client;

public class LbiSiriClientFactory {
	
	public static ILbiSiriWebServiceClient createLbiSiriWebServiceAsyncClient() {
		return new LbiSiriWebServiceAsyncClient();
	}

	public static ILbiSiriWebServiceClient createLbiSiriWebServiceClient() {
		return new LbiSiriWebServiceClient();
	}
}
