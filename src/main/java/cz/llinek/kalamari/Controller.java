package cz.llinek.kalamari;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import cz.llinek.kalamari.dataTypes.Hour;
import cz.llinek.kalamari.dataTypes.RequestCallback;

public class Controller {
    private static String url;
    private static String token;
    private static SimpleDateFormat timestampFormatter;

    public static void updateTimetable(Context context) {
        performRequest(context, "/api/3/timetable/permanent", new RequestCallback() {
            @Override
            public void run(String response) {
                if (response != null) {
                    try {
                        FileManager.fileWrite(Constants.PERMANENT_TIMETABLE_FILENAME, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
            }

            @Override
            public int join() {
                return 5000;
            }
        });
    }

    public static void runOnUiThread(Context context, Runnable run) {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(run);
    }


    public static Hour[][] parsePermanentHours(Context context) {
        String response;
        Hour[][] hours = null;
        if (FileManager.exists(Constants.PERMANENT_TIMETABLE_FILENAME)) {
            response = FileManager.readFile(Constants.PERMANENT_TIMETABLE_FILENAME);
        } else {
            updateTimetable(context);
            response = FileManager.readFile(Constants.PERMANENT_TIMETABLE_FILENAME);
        }
        try {
            JSONObject rozvrh = new JSONObject(response);
            int minHours = -1;
            int maxHours = -1;
            int days = 0;
            for (int i = rozvrh.getJSONArray("Days").length() - 1; i >= 0; i--) {
                if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length() > 0) {
                    days++;
                    for (int j = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length() - 1; j >= 0; j--) {
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") < minHours || minHours == -1) {
                            minHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") > maxHours || maxHours == -1) {
                            maxHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                    }
                }
            }
            hours = new Hour[days][maxHours - minHours + 1];
            for (int dayId = 0; dayId < hours.length; dayId++) {
                /*for (int hourId = 0; hourId < hours[dayId].length; hourId++) {
                    JSONObject hour = rozvrh.getJSONArray("Days").getJSONObject(dayId).getJSONArray("Atoms").getJSONObject(hourId);
                    String[] groupIds = new String[hour.getJSONArray("GroupIds").length()];
                    String[] cycleIds = new String[hour.getJSONArray("CycleIds").length()];
                    for (int k = 0; k < groupIds.length; k++) {
                        groupIds[k] = hour.getJSONArray("GroupIds").getString(k);
                    }
                    for (int k = 0; k < cycleIds.length; k++) {
                        cycleIds[k] = hour.getJSONArray("CycleIds").getString(k);
                    }
                    try {
                        JSONObject c = hour.getJSONObject("Change");
                        Change change = new Change(c.getString("ChangeSubject"), getTimestampFormatter().parse(c.getString("Day")), c.getString("Hours"), c.getString("ChangeType"), c.getString("Description"), c.getString("Time"), c.getString("TypeAbbrev"), c.getString("TypeName"));
                        hours[dayId][hourId] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, change, hour.getString("Theme"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        hours[dayId][hourId] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        hours[dayId][hourId] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    }
                }*/
                JSONArray atoms = rozvrh.getJSONArray("Days").getJSONObject(dayId).getJSONArray("Atoms");
                for (int i = 0; i < atoms.length(); i++) {
                    JSONObject hour = atoms.getJSONObject(i);
                    hours[dayId][hour.getInt("HourId") - minHours] = new Hour(context, hour, Constants.PERMANENT_TIMETABLE_FILENAME);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return hours;
    }

    public static int dpToPx(Context context, float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static void performRequest(Context context, String appendix, RequestCallback runLater) {
        login(context, () -> {
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(getUrl() + appendix).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Authorization", "Bearer " + getToken());
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        runLater.run(response.toString());
                        connection.disconnect();
                        input.close();
                    } else {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        if (connection.getResponseMessage().equalsIgnoreCase("Unauthorized")) {
                            login(context, () -> Toast.makeText(context, "Auth failed, try to reload once more, then login.", Toast.LENGTH_SHORT).show());
                        }
                        response.append("Error: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        System.err.println(response.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                    runLater.run(null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        if (runLater.join() != -1) {
            try {
                thread.join(runLater.join());
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
    }

    public static void performRequest(String url, String appendix, RequestCallback runLater) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url + appendix).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Authorization", "Bearer " + getToken());
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        runLater.run(response.toString());
                        connection.disconnect();
                        input.close();
                    } else {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        response.append("Error: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        System.err.println(response.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                    runLater.run(null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        if (runLater.join() != -1) {
            try {
                thread.join(runLater.join());
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        Controller.url = url;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Controller.token = token;
    }

    public static SimpleDateFormat getTimestampFormatter() {
        return timestampFormatter;
    }

    public static void setTimestampFormatter(SimpleDateFormat timestampFormatter) {
        Controller.timestampFormatter = timestampFormatter;
    }

    public static void loginRefreshToken() {

    }

    public static void clearCredentials() {
        setToken(null);
        setUrl(null);
    }

    public static void updateCredentials() {

    }

    public static void logout() {
        clearCredentials();
        FileManager.deleteDirContents(Constants.USERDATA_DIRECTORY);
    }

    public static boolean login(Context context, String url, String user, String pwd) {
        final boolean[] result = {false};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url + "/api/login").openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    PrintWriter output = new PrintWriter(connection.getOutputStream());
                    output.print("client_id=ANDR&grant_type=password&username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(pwd, "utf-8"));
                    output.close();
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        connection.disconnect();
                        input.close();
                    } else {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        response.append("Error: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        System.err.println(response.toString());
                    }

                    if (connection.getResponseCode() == 200) {
                        JSONObject res = new JSONObject(response.toString());
                        setUrl(url);
                        setToken(res.getString("access_token"));
                        FileManager.fileWrite(Constants.CREDENTIALS_FILENAME, url + '\n' + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                        /*runOnUiThread(context, () -> {
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });*/
                        result[0] = true;
                    }
                } catch (Throwable e) {
                    runOnUiThread(context, () -> {
                        Toast.makeText(context, "Exception", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    });
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        try {
            thread.join(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result[0];
    }

    public static boolean checkSchoolUrl(String url) {
        final boolean[] result = {false};
        performRequest(url, "/api", new RequestCallback() {
            @Override
            public void run(String response) {
                if (response != null) {
                    try {
                        JSONArray apis = new JSONArray(response);
                        if (apis.length() > 0) {
                            if (apis.getJSONObject(0).getString("ApiVersion") != null) {
                                result[0] = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
            }

            @Override
            public int join() {
                return 3000;
            }
        });
        return result[0];
    }

    public static String parseUrl(String url) {
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    public static boolean tokenExpired() {
        String[] credentials = FileManager.readFile(Constants.CREDENTIALS_FILENAME).split("\n");
        if (credentials.length <= 3) {
            if (System.currentTimeMillis() < Long.parseLong(credentials[2]) - 5000) {
                return false;
            }
        }
        return true;
    }

    public static void login(Context context, Runnable runAfter) {
        if (FileManager.exists(Constants.CREDENTIALS_FILENAME)) {
            if (tokenExpired()) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALS_FILENAME)));
                            String url = bufferedReader.readLine();
                            bufferedReader.readLine();
                            long expirationTime = Long.parseLong(bufferedReader.readLine());
                            if (System.currentTimeMillis() < expirationTime - 100000) {
                                runOnUiThread(context, runAfter);
                                return;
                            }
                            bufferedReader.readLine();
                            String user = bufferedReader.readLine();
                            String pwd = bufferedReader.readLine();
                            StringBuilder response = new StringBuilder();
                            HttpsURLConnection connection = (HttpsURLConnection) new URL(getUrl() + "/api/login").openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            PrintWriter output = new PrintWriter(connection.getOutputStream());
                            output.print("client_id=ANDR&grant_type=password&username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(pwd, "utf-8"));
                            output.close();
                            connection.connect();
                            if (connection.getErrorStream() == null) {
                                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String temp = input.readLine();
                                while (temp != null) {
                                    response.append(temp);
                                    temp = input.readLine();
                                }
                                connection.disconnect();
                                input.close();
                            } else {
                                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                                String temp = input.readLine();
                                while (temp != null) {
                                    response.append(temp);
                                    temp = input.readLine();
                                }
                                response.append("Error: ");
                                response.append(connection.getResponseCode());
                                response.append(", ");
                                response.append(connection.getResponseMessage());
                                runOnUiThread(context, () -> {
                                    Toast.makeText(context, "Wrong login", Toast.LENGTH_LONG).show();
                                    System.err.println(response.toString());
                                    context.startActivity(new Intent(context, LoginScreen.class));
                                });
                            }

                            if (connection.getResponseCode() == 200) {
                                runOnUiThread(context, () -> {
                                    Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                                });
                                JSONObject res = new JSONObject(response.toString());
                                setUrl(url);
                                setToken(res.getString("access_token"));
                                FileManager.fileWrite(Constants.CREDENTIALS_FILENAME, url + "\n" + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                                runOnUiThread(context, runAfter);
                            }
                        } catch (Throwable e) {
                            runOnUiThread(context, () -> {
                                Toast.makeText(context, "Exception", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                System.err.println(e.getMessage());
                            });
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        } else {
            Toast.makeText(context, "else", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginScreen.class));
        }
    }

    public static void onActivityStart() {
        setTimestampFormatter(new SimpleDateFormat(Constants.TIMESTAMP));
        if (!FileManager.exists(Constants.USERDATA_DIRECTORY)) {
            FileManager.mkDir(Constants.USERDATA_DIRECTORY);
        }
        if (FileManager.exists(Constants.CREDENTIALS_FILENAME)) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALS_FILENAME)));
                String temp = input.readLine();
                setUrl(temp);
                setToken(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
