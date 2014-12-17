package translate;

import db.Word;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.IIOException;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.MalformedInputException;

/**
 * Created by viacheslav on 17.12.14.
 */
public class Translator {
    public static final String url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=";
    public static final String key = "dict.1.1.20141116T162732Z.6367256efc30c377.79e985914e47344e3856d26681dce9dca74d9fd6";
    public static final String key2 = "trnsl.1.1.20130920T125424Z.3a9e8a0c7919077f.0298f380e28264cff2e9fa18ef9b5ab51ff08ec3";

    public static final String detect = "https://translate.yandex.net/api/v1.5/tr.json/detect?key=";
    public static final String default_language = "en-ru";

    public static String detectLanguage(String text) {
        String request = detect + key2 + "&text=" + text;
        try {
            URL url = new URL(request);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.connect();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192]; // 8KB
            for (int read; (read = reader.read(buffer)) > 0; ) {
                builder.append(buffer, 0, read);
            }
            String result = builder.toString();
            JSONObject jsonObject = new JSONObject(result);
            String lang = jsonObject.getString("lang");
            if ("en".equals(lang))
                return "en-ru";
            else
                return "ru-en";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return default_language;
    }

    public static String getJson(String text) {
        if (text == null)
            throw new NullPointerException("given text is null");

        String lang = "en-ru";
        String encodedTest = "";

        try {
            encodedTest = URLEncoder.encode(text, "utf-8");
            lang = detectLanguage(encodedTest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String request = url + key + "&lang=" + lang;
        request += "&text=" + encodedTest;


        try {
            URL url = new URL(request);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.connect();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192]; // 8KB
            for (int read; (read = reader.read(buffer)) > 0; ) {
                builder.append(buffer, 0, read);
            }
            String result = builder.toString();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Word translate(String text) {
        String json = getJson(text);
        JSONObject jsonObject = new JSONObject(json);

        JSONArray def = jsonObject.getJSONArray("def");
        String translation = null;
        if (def.length() > 0)
            translation = def.getJSONObject(0).getJSONArray("tr").getJSONObject(0).getString("text");

        return new Word(-2, text, translation, json);
    }

    public static void main(String[] args) {
        String json = Translator.getJson("home");
        System.out.println(json);

        System.out.println(translate("home").translation);

        try {
            PrintWriter out = new PrintWriter("ans.html");
            out.println(json);
            out.close();
        }catch (Exception e)
        {e.printStackTrace();}
        detectLanguage("home");
    }
}
