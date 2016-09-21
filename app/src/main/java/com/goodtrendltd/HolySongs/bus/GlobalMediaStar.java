package com.goodtrendltd.HolySongs.bus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.goodtrendltd.HolySongs.CustomApplication;
import com.goodtrendltd.HolySongs.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vincent on 15/11/30.
 */
public class GlobalMediaStar
{
    final static String KEY = "GlobalMediaStarVisitorId";


    public static String getVisitorId(Activity context)
    {

        SharedPreferences read = context.getSharedPreferences(KEY, Activity.MODE_WORLD_READABLE);
        String visitorId = read.getString(KEY, "");
        if (visitorId.equals("")) {
            String rooturl = "http://xinxiwang.me/visitor";
            DefaultHttpClient httpClient = null;
            String responseContent = null;
            try {

                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                SSLSocketFactory sf = newSslSocketFactory(context);

                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                httpClient = new DefaultHttpClient(ccm, params);

                HttpGet httpPost = new HttpGet(rooturl);
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity(); // 获取响应实体
                if (entity != null) {
                    responseContent = EntityUtils.toString(entity, "UTF-8");
                }
                if (responseContent != null && responseContent.length() > 0 && responseContent.equals("-1") == false) {
                    JSONObject jsonObject;
                    jsonObject = new JSONObject(responseContent);
                    visitorId = jsonObject.get("id").toString();

                    SharedPreferences.Editor editor = context.getSharedPreferences(KEY, Activity.MODE_WORLD_WRITEABLE).edit();
                    //步骤2-2：将获取过来的值放入文件
                    editor.putString(KEY, visitorId);
                    //步骤3：提交
                    editor.commit();
                }
            } catch (Exception e) {
                Log.v("globalmediastar"," record eventType exception:  " + e.toString());
            } finally {
                // 关闭连接,释放资源
                httpClient.getConnectionManager().shutdown();
            }
        }
        Log.v("globalmediastar",visitorId + " -- visitorId ");
        return visitorId;
    }

    public static String STAR_URL = "https://xinxiwang.me/events";
//    public static String STAR_URL = "https://stats.globalmediaoutreach.com/api/v2/events/app";

    public static void decision(Activity context)
    {
        postInfo(context,"decision");
    }

    public static void recordRun(Activity context)
    {
        postInfo(context,"open");
    }

    public static void recordGospel(Activity context)
    {
        postInfo(context,"gospel");
    }

    public static void recordInstall(Activity context)
    {
        postInfo(context,"install");
        //star_baidu
        //star_360
        //star_xiaomi
        //star_qq
        //postInfo(context,"star_qq");
    }

    public static void recordOpenChat(Activity context){

        CustomApplication application = (CustomApplication)context.getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("open-chat")
                .setLabel("open-chat")
                .setValue(System.currentTimeMillis())
                .build());
        Log.v("globalmediastar"," -- chat open-chat -- ");
    }

    public static void recordFirstChat(Activity context)
    {
        CustomApplication application = (CustomApplication)context.getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("first-chat")
                .setLabel("first-chat")
                .setValue(System.currentTimeMillis())
                .build());
        Log.v("globalmediastar"," -- chat first-chat -- ");
    }

    public static void recordChatMsg(Activity context,String name,String phone,String address,String categoryId){

        Map<String,String> createMap = new HashMap<String,String>();
        createMap.put("phone",phone);
        createMap.put("category_id",categoryId);
        createMap.put("name",name);
        createMap.put("app_name","jdt");
        createMap.put("address",address);

        String charset = "utf-8";
        String url = "http://www.jdtapps.me/api/chat/enter";

        Log.v("globalmediastar"," -- chat msg 01 -- " + name + " - " + phone + " - " + address + " - " + categoryId);
        try
        {
//            httpPost = new HttpPost(url);
//
//            //设置参数
//            List<NameValuePair> list = new ArrayList<NameValuePair>();
//            Iterator iterator = createMap.entrySet().iterator();
//            while(iterator.hasNext()){
//                Map.Entry<String,String> elem = (Map.Entry<String, String>) iterator.next();
//                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
//            }
//            if(list.size() > 0){
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
//                httpPost.setEntity(entity);
//            }
//            HttpResponse response = httpClient.execute(httpPost);
//            if(response != null){
//                HttpEntity resEntity = response.getEntity();
//                if(resEntity != null){
//
//                }
//            }


            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

//            createMap.put("phone",phone);
//            createMap.put("category_id",categoryId);
//            createMap.put("name",name);
//            createMap.put("app_name","jdt");
//            createMap.put("address",address);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("category_id", categoryId));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("app_name", "bible"));
            params.add(new BasicNameValuePair("address", address));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

            HttpResponse response = client.execute(httpPost);
            if (200 == response.getStatusLine().getStatusCode())
            {
                //response.getEntity().getContent();
            }

            Log.v("globalmediastar"," -- chat msg 02 -- ");
        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.v("globalmediastar"," -- chat msg 03 -- "+ex.toString());
        }
    }

    static void postInfo(Activity context,String eventType){

        CustomApplication application = (CustomApplication)context.getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(eventType)
                .setLabel(eventType)
                .setValue(System.currentTimeMillis())
                .build());

        Log.v("globalmediastar"," post star - "+eventType);
        String rooturl = STAR_URL;
        DefaultHttpClient httpClient = null;
        String responseContent = null;
        try {

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = newSslSocketFactory(context);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            httpClient =  new DefaultHttpClient(ccm, params);

            HttpPost httpPost = new HttpPost(rooturl);
//            CLog.v("recordRun url: " + rooturl);
            List<NameValuePair> formParams = new ArrayList<NameValuePair>(); //构建POST请求的表单参数
            formParams.add(new BasicNameValuePair("key", BibleSetting.getGMOEventAPIKey(context)));
            formParams.add(new BasicNameValuePair("appName", BibleSetting.getAppName(context)));
            formParams.add(new BasicNameValuePair("visitorId", getVisitorId(context)));
            formParams.add(new BasicNameValuePair("eventType", eventType));
            formParams.add(new BasicNameValuePair("language", context.getResources().getConfiguration().locale.getLanguage()));
            formParams.add(new BasicNameValuePair("deviceType", "Android"));
            formParams.add(new BasicNameValuePair("timestamp", "" + getTimeStamp()));
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity(); // 获取响应实体
//            CLog.v("recordRun params: "+System.currentTimeMillis()+" language: "+context.getResources().getConfiguration().locale.getLanguage());
            if (entity != null) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
//                Log.v("recordRun response: ",responseContent);
//                CLog.v( " record :  " +eventType+ " -- "+responseContent);
            }
            Log.v("globalmediastar", " record :  " +eventType+ " -- "+responseContent + " -- "+getTimeStamp());
//            CLog.v("recordRun response: "+System.currentTimeMillis()+" language: "+context.getResources().getConfiguration().locale.getLanguage());
        }
        catch (Exception e)
        {
//            CLog.v("recordRun Exception: "+e.toString());
//            CLog.v("recordRun response: "+responseContent);
            Log.v("globalmediastar"," record eventType exception:  " + e.toString());
        }
        finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static void postChapter(int bookId,int chapterId) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            String url = "http://godlife.com/api/v1.0/bible/languages/en/versions/CEV/books/"+ getShortenedBookNameByBookId(String.valueOf(bookId)) +"/chapters/"+chapterId;
            url = url + "?key="+BibleSetting.getChatAPIKey(CustomApplication.getContext());

            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            Log.v("globalmediastar"," -- pass  -- "+result);
        } catch (Exception e) {
            Log.v("globalmediastar","发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    static SSLSocketFactory newSslSocketFactory(Context context) {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            InputStream in;
            //TODO: testing
            Log.d("HttpClientUtil", "using harvestinitiative server");
            KeyStore trusted = KeyStore.getInstance("BKS");
            in = context.getResources().openRawResource(R.raw.gmochatssl);
            try {
                // Initialize the keystore with the provided trusted certificates
                // Also provide the password of the keystore
                trusted.load(in, "gmochat".toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate
            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sf;

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    static String urlEncode(String sUrl)
    {
        StringBuffer urlOK = new StringBuffer();
        for(int i=0; i<sUrl.length(); i++)
        {
            char ch=sUrl.charAt(i);
            switch(ch)
            {
                case '<': urlOK.append("%3C"); break;
                case '>': urlOK.append("%3E"); break;
                case '/': urlOK.append("%2F"); break;
                case ' ': urlOK.append("%20"); break;
                case ':': urlOK.append("%3A"); break;
                case '-': urlOK.append("%2D"); break;
                default: urlOK.append(ch); break;
            }
        }
        return urlOK.toString();

    }

    static String getShortenedBookNameByBookId(String bookId){
        String[][] bookInfo = new String[66][3];
        bookInfo[0][0] = "Gen"; bookInfo[0][1] = "Genesis"; bookInfo[0][2] = "1";
        bookInfo[1][0] = "Exod"; bookInfo[1][1] = "Exodus"; bookInfo[1][2] = "2";
        bookInfo[2][0] = "Lev"; bookInfo[2][1] = "Leviticus"; bookInfo[2][2] = "3";
        bookInfo[3][0] = "Num"; bookInfo[3][1] = "Numbers"; bookInfo[3][2] = "4";
        bookInfo[4][0] = "Deut"; bookInfo[4][1] = "Deuteronomy"; bookInfo[4][2] = "5";
        bookInfo[5][0] = "Josh"; bookInfo[5][1] = "Joshua"; bookInfo[5][2] = "6";
        bookInfo[6][0] = "Judg"; bookInfo[6][1] = "Judges"; bookInfo[6][2] = "7";
        bookInfo[7][0] = "Ruth"; bookInfo[7][1] = "Ruth"; bookInfo[7][2] = "8";
        bookInfo[8][0] = "1Sam"; bookInfo[8][1] = "1 Samuel"; bookInfo[8][2] = "9";
        bookInfo[9][0] = "2Sam"; bookInfo[9][1] = "2 Samuel"; bookInfo[9][2] = "10";
        bookInfo[10][0] = "1Kgs"; bookInfo[10][1] = "1 Kings"; bookInfo[10][2] = "11";
        bookInfo[11][0] = "2Kgs"; bookInfo[11][1] = "2 Kings"; bookInfo[11][2] = "12";
        bookInfo[12][0] = "1Chr"; bookInfo[12][1] = "1 Chronicles"; bookInfo[12][2] = "13";
        bookInfo[13][0] = "2Chr"; bookInfo[13][1] = "2 Chronicles"; bookInfo[13][2] = "14";
        bookInfo[14][0] = "Ezra"; bookInfo[14][1] = "Ezra"; bookInfo[14][2] = "15";
        bookInfo[15][0] = "Neh"; bookInfo[15][1] = "Nehemiah"; bookInfo[15][2] = "16";
        bookInfo[16][0] = "Esth"; bookInfo[16][1] = "Esther"; bookInfo[16][2] = "17";
        bookInfo[17][0] = "Job"; bookInfo[17][1] = "Job"; bookInfo[17][2] = "18";
        bookInfo[18][0] = "Ps"; bookInfo[18][1] = "Psalms"; bookInfo[18][2] = "19";
        bookInfo[19][0] = "Prov"; bookInfo[19][1] = "Proverbs"; bookInfo[19][2] = "20";
        bookInfo[20][0] = "Eccl"; bookInfo[20][1] = "Ecclesiastes"; bookInfo[20][2] = "21";
        bookInfo[21][0] = "Song"; bookInfo[21][1] = "Song of Songs"; bookInfo[21][2] = "22";
        bookInfo[22][0] = "Isa"; bookInfo[22][1] = "Isaiah"; bookInfo[22][2] = "23";
        bookInfo[23][0] = "Jer"; bookInfo[23][1] = "Jeremiah"; bookInfo[23][2] = "24";
        bookInfo[24][0] = "Lam"; bookInfo[24][1] = "Lamentations"; bookInfo[24][2] = "25";
        bookInfo[25][0] = "Ezek"; bookInfo[25][1] = "Ezekiel"; bookInfo[25][2] = "26";
        bookInfo[26][0] = "Dan"; bookInfo[26][1] = "Daniel"; bookInfo[26][2] = "27";
        bookInfo[27][0] = "Hos"; bookInfo[27][1] = "Hosea"; bookInfo[27][2] = "28";
        bookInfo[28][0] = "Joel"; bookInfo[28][1] = "Joel"; bookInfo[28][2] = "29";
        bookInfo[29][0] = "Amos"; bookInfo[29][1] = "Amos"; bookInfo[29][2] = "30";
        bookInfo[30][0] = "Obad"; bookInfo[30][1] = "Obadiah"; bookInfo[30][2] = "31";
        bookInfo[31][0] = "Jonah"; bookInfo[31][1] = "Jonah"; bookInfo[31][2] = "32";
        bookInfo[32][0] = "Mic"; bookInfo[32][1] = "Micah"; bookInfo[32][2] = "33";
        bookInfo[33][0] = "Nah"; bookInfo[33][1] = "Nahum"; bookInfo[33][2] = "34";
        bookInfo[34][0] = "Hab"; bookInfo[34][1] = "Habakkuk"; bookInfo[34][2] = "35";
        bookInfo[35][0] = "Zeph"; bookInfo[35][1] = "Zephaniah"; bookInfo[35][2] = "36";
        bookInfo[36][0] = "Hag"; bookInfo[36][1] = "Haggai"; bookInfo[36][2] = "37";
        bookInfo[37][0] = "Zech"; bookInfo[37][1] = "Zechariah"; bookInfo[37][2] = "38";
        bookInfo[38][0] = "Mal"; bookInfo[38][1] = "Malachi"; bookInfo[38][2] = "39";
        bookInfo[39][0] = "Matt"; bookInfo[39][1] = "Matthew"; bookInfo[39][2] = "40";
        bookInfo[40][0] = "Mark"; bookInfo[40][1] = "Mark"; bookInfo[40][2] = "41";
        bookInfo[41][0] = "Luke"; bookInfo[41][1] = "Luke"; bookInfo[41][2] = "42";
        bookInfo[42][0] = "John"; bookInfo[42][1] = "John"; bookInfo[42][2] = "43";
        bookInfo[43][0] = "Acts"; bookInfo[43][1] = "Acts"; bookInfo[43][2] = "44";
        bookInfo[44][0] = "Rom"; bookInfo[44][1] = "Romans"; bookInfo[44][2] = "45";
        bookInfo[45][0] = "1Cor"; bookInfo[45][1] = "1 Corinthians"; bookInfo[45][2] = "46";
        bookInfo[46][0] = "2Cor"; bookInfo[46][1] = "2 Corinthians"; bookInfo[46][2] = "47";
        bookInfo[47][0] = "Gal"; bookInfo[47][1] = "Galatians"; bookInfo[47][2] = "48";
        bookInfo[48][0] = "Eph"; bookInfo[48][1] = "Ephesians"; bookInfo[48][2] = "49";
        bookInfo[49][0] = "Phil"; bookInfo[49][1] = "Philippians"; bookInfo[49][2] = "50";
        bookInfo[50][0] = "Col"; bookInfo[50][1] = "Colossians"; bookInfo[50][2] = "51";
        bookInfo[51][0] = "1Thess"; bookInfo[51][1] = "1 Thessalonians"; bookInfo[51][2] = "52";
        bookInfo[52][0] = "2Thess"; bookInfo[52][1] = "2 Thessalonians"; bookInfo[52][2] = "53";
        bookInfo[53][0] = "1Tim"; bookInfo[53][1] = "1 Timothy"; bookInfo[53][2] = "54";
        bookInfo[54][0] = "2Tim"; bookInfo[54][1] = "2 Timothy"; bookInfo[54][2] = "55";
        bookInfo[55][0] = "Titus"; bookInfo[55][1] = "Titus"; bookInfo[55][2] = "56";
        bookInfo[56][0] = "Phlm"; bookInfo[56][1] = "Philemon"; bookInfo[56][2] = "57";
        bookInfo[57][0] = "Heb"; bookInfo[57][1] = "Hebrews"; bookInfo[57][2] = "58";
        bookInfo[58][0] = "Jas"; bookInfo[58][1] = "James"; bookInfo[58][2] = "59";
        bookInfo[59][0] = "1Pet"; bookInfo[59][1] = "1 Peter"; bookInfo[59][2] = "60";
        bookInfo[60][0] = "2Pet"; bookInfo[60][1] = "2 Peter"; bookInfo[60][2] = "61";
        bookInfo[61][0] = "1John"; bookInfo[61][1] = "1 John"; bookInfo[61][2] = "62";
        bookInfo[62][0] = "2John"; bookInfo[62][1] = "2 John"; bookInfo[62][2] = "63";
        bookInfo[63][0] = "3John"; bookInfo[63][1] = "3 John"; bookInfo[63][2] = "64";
        bookInfo[64][0] = "Jude"; bookInfo[64][1] = "Jude"; bookInfo[64][2] = "65";
        bookInfo[65][0] = "Rev"; bookInfo[65][1] = "Revelation"; bookInfo[65][2] = "66";


        for(int i = 0;i<66;i++){
            if(bookInfo[i][2].equals(bookId)==true){
                return bookInfo[i][0];
            }
        }

        return "";
    }

    static String getTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        String longTimestamp = Long.toString(now.getTime());
        String timestamp = longTimestamp.substring(0, 10);
//        Utils.Log("timestamp " + timestamp);
        //Utils.Log(" Mllis " +  System.currentTimeMillis());
        return timestamp;
    }
}
