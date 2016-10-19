package cn.sznxkj.push;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.PushPayload;

public class Push {
	private static final String appKey = "e947108d8c35f05b6fe85674";
	private static final String masterSecret = "8f3c358c2ca4e4a6c16731a3";
	private Push() {}
	/*
	public static void pushAll() {
		JPushClient jpushClient = new JPushClient(masterSecret, appKey);

        // For push, all you need do is to build PushPayload object.
        PushPayload payload = buildPushObject_all_all_alert();

        try {
            PushResult result = jpushClient.sendPush(payload);
            LOG.info("Got result - " + result);

        } catch (APIConnectionException e) {
            // Connection error, should retry later
            LOG.error("Connection error, should retry later", e);

        } catch (APIRequestException e) {
            // Should review the error, and fix the request
            LOG.error("Should review the error, and fix the request", e);
            LOG.info("HTTP Status: " + e.getStatus());
            LOG.info("Error Code: " + e.getErrorCode());
            LOG.info("Error Message: " + e.getErrorMessage());
        }
	}*/
	private static PushPayload buildPushObject_all_all_alert() {
        return PushPayload.alertAll("");
    }
}
