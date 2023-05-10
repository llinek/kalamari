package cz.llinek.kalamari;

import android.content.Context;
import android.os.Handler;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import cz.llinek.kalamari.dataTypes.Change;
import cz.llinek.kalamari.dataTypes.Hour;
import cz.llinek.kalamari.dataTypes.RequestCallback;
import cz.llinek.kalamari.dataTypes.Subject;

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
                        FileManager.fileWrite(Constants.TIMETABLE_FILENAME, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(context, () -> System.err.println(e.getMessage()));
                    }
                }
            }
        });
    }

    public static void runOnUiThread(Context context, Runnable run) {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(run);
    }
    public static Subject getSubjectById(Context context, int id) {
        String response;
        if (FileManager.exists(Constants.TIMETABLE_FILENAME)) {
            response = FileManager.readFile(Constants.TIMETABLE_FILENAME);
        } else {
            updateTimetable(context);
            response = FileManager.readFile(Constants.TIMETABLE_FILENAME);
        }
        try {
            JSONArray subjects = new JSONObject(response).getJSONArray("Subjects");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subject = subjects.getJSONObject(i);
                if (subject.getInt("Id") == id) {
                    return new Subject(subject.getInt("Id"), subject.getString("Abbrev"), subject.getString("Name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Hour[][] parseTimetable(Context context) {
        String response;
        if (FileManager.exists(Constants.TIMETABLE_FILENAME)) {
            response = FileManager.readFile(Constants.TIMETABLE_FILENAME);
        } else {
            updateTimetable(context);
            response = FileManager.readFile(Constants.TIMETABLE_FILENAME);
        }
        try {
            JSONObject rozvrh = new JSONObject(response);
            runOnUiThread(context, () -> Toast.makeText(context, response, Toast.LENGTH_LONG).show());
            System.out.println(response.replaceAll(",", ",\n"));
            int minHours = -1;
            int maxHours = -1;
            int days = 0;
            for (int i = rozvrh.getJSONArray("Days").length(); i < 0; i--) {
                if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length() > 0) {
                    days++;
                    for (int j = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length(); j > 0; j--) {
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") < minHours || minHours == -1) {
                            minHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") > maxHours || maxHours == -1) {
                            maxHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                    }
                }
            }
            Hour[][] hours = new Hour[days][maxHours - minHours];
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
                        runOnUiThread(context, () -> System.err.println("\n\n\nno change\n\n\n\n\n" + e.getMessage()));
                        hours[dayId][hourId] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        runOnUiThread(context, () -> System.err.println("\n\n\nchange err, fallback to timetable without changes\n\n\n\n\n" + e.getMessage()));
                        hours[dayId][hourId] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    }
                }*/
                JSONArray atoms = rozvrh.getJSONArray("Days").getJSONObject(dayId).getJSONArray("Atoms");
                for (int i = 0; i < atoms.length(); i++) {
                    JSONObject hour = atoms.getJSONObject(i);
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
                        hours[dayId][hour.getInt("HourId") - minHours] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, change, hour.getString("Theme"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(context, () -> System.err.println("\n\n\nno change\n\n\n\n\n" + e.getMessage()));
                        hours[dayId][hour.getInt("HourId") - minHours] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        runOnUiThread(context, () -> System.err.println("\n\n\nchange err, fallback to timetable without changes\n\n\n\n\n" + e.getMessage()));
                        hours[dayId][hour.getInt("HourId") - minHours] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    }
                }
            }
            return hours;
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(context, () -> System.err.println(e.getMessage()));
        }
        return null;
    }

    public static void performRequest(Context context, String appendix, RequestCallback runLater) {
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
                    System.out.println(getToken());
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        runOnUiThread(context, () -> runLater.run(response.toString()));
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
                            Toast.makeText(context, "Wrong request", Toast.LENGTH_LONG).show();
                            System.err.println(response.toString());
                        });
                    }
                } catch (Throwable e) {
                    runOnUiThread(context, () -> {
                        Toast.makeText(context, "Exception", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
                }
            }
        }); thread.setDaemon(true);
        thread.start();
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

    public static void login(Context context, String url, String user, String pwd) {
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
                        runOnUiThread(context, () -> {
                            basicScreen();
                        });
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

    public static void login(Context context, Runnable runAfter) {
        Toast.makeText(context, "login", Toast.LENGTH_SHORT).show();
        if (FileManager.exists(Constants.CREDENTIALS_FILENAME)) {
            Toast.makeText(context, "loginexists", Toast.LENGTH_SHORT).show();
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
                                loginScreen();
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
        } else {
            Toast.makeText(context, "else", Toast.LENGTH_SHORT).show();
            loginScreen();
        }
    }

    public static void onActivityStart() {
        setTimestampFormatter(new SimpleDateFormat(Constants.TIMESTAMP));
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
