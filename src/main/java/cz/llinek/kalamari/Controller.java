package cz.llinek.kalamari;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

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

import cz.llinek.kalamari.dataTypes.RequestCallback;

public class Controller {
    private static String url;
    private static String token;
    private static SimpleDateFormat timestampFormatter;

    public static void updateTimetable() {

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
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(() -> runLater.run(response.toString());
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
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            Toast.makeText(context, "Wrong request", Toast.LENGTH_LONG).show();
                            System.err.println(response.toString()
                        });
                    }
                } catch (Throwable e) {
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(() -> {
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
                        FileManager.fileWrite(Constants.CREDENTIALSFILENAME, url + '\n' + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            basicScreen();
                        });
                    }
                } catch (Throwable e) {
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(() -> {
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
        if (FileManager.exists(Constants.CREDENTIALSFILENAME)) {
            Toast.makeText(context, "loginexists", Toast.LENGTH_SHORT).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALSFILENAME)));
                        String url = bufferedReader.readLine();
                        bufferedReader.readLine();
                        long expirationTime = Long.parseLong(bufferedReader.readLine());
                        if (System.currentTimeMillis() < expirationTime - 100000) {
                            rHandler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(() -> runAfter);
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
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(() -> {
                                Toast.makeText(context, "Wrong login", Toast.LENGTH_LONG).show();
                                System.err.println(response.toString());
                                loginScreen();
                            });
                        }

                        if (connection.getResponseCode() == 200) {
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(() -> {
                                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                            });
                            JSONObject res = new JSONObject(response.toString());
                            setUrl(url);
                            setToken(res.getString("access_token"));
                            FileManager.fileWrite(Constants.CREDENTIALSFILENAME, url + "\n" + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                            Handler handler = new Handler(context.getMainLooper());
                            handler.post(runAfter);
                        }
                    } catch (Throwable e) {
                        Handler mainHandler = new Handler(context.getMainLooper());
                        mainHandler.post(() -> {
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
        if (FileManager.exists(Constants.CREDENTIALSFILENAME)) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALSFILENAME)));
                String temp = input.readLine();
                setUrl(temp);
                setToken(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
