package src.client;

@FunctionalInterface
public interface ResponseCallback {
    void onResponseReceived(String response);
}
