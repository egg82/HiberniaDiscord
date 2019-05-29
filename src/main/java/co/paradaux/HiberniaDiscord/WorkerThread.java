package co.paradaux.HiberniaDiscord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WorkerThread implements Runnable {

    private String a;
    private String b;
    private String c;
    private String webhook_url;
    private String crafatar_url;
    private String crafatar_options;

    public WorkerThread(String aa, String bb, String cc, String dd, String ee, String ff) {
        webhook_url = dd;
        crafatar_url = ee;
        crafatar_options = ff;

        a = crafatar_url + aa + "?" + crafatar_options;
        b = bb;
        c = cc;
    }

    public void run() {
        try {
            sendWebhook(a, b, c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWebhook(String headURL, String userName, String message) throws IOException {
        URL url = new URL(webhook_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Sets type
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Add additional headers

        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String jsonInputString = "{\"content\": \"" + message + "\", \"username\": \"" + userName + "\", \"avatar_url\": \"" + headURL + "\"}";
        // Write the body

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
    }
}
