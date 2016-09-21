package com.goodtrendltd.HolySongs.chat;

/**
 * Created by jliston on 9/9/15.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.goodtrendltd.HolySongs.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.KeyStore;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class HttpClientUtil {

    private static Context context;
    private static String rooturl;

    public static void setContext(Context context) {
        HttpClientUtil.context = context;
    }


    private SSLSocketFactory newSslSocketFactory() {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            InputStream in;
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
    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = newSslSocketFactory();
            //set rooturl
            if (inChina()){
                rooturl = "https://xinxiwang.me";
            }
            else {
                rooturl = "https://commchannels.globalmediaoutreach.com/api/app";
            }
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public String sendHttpsRequestByPost(String url, Map<String, String> params) {
        String responseContent = null;
        HttpClient httpClient = getNewHttpClient();

        try {
            Log.d("HttpClientUtil-https", "sendHttpsRequestByPost in progress...");
            Log.d("HttpClientUtil", "url: "+rooturl+url);

            HttpPost httpPost = new HttpPost(rooturl+url);
            List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 构建POST请求的表单参数
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity(); // 获取响应实体
            if (entity != null) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            Log.e("HttpClientUtil-https", e.toString());
        }
        finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

    public String sendHttpsRequestByPut(String url, Map<String, String> params) {
        String responseContent = null;
        HttpClient httpClient = getNewHttpClient();

        try {
            Log.d("HttpClientUtil-https", "sendHttpsRequestByPut in progress...");
            Log.d("HttpClientUtil", "url: "+rooturl+url);

            HttpPut httpPut = new HttpPut(rooturl+url);
            List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 构建PUT请求的表单参数
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPut.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity(); // 获取响应实体
            if (entity != null) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            Log.e("HttpClientUtil-https", e.toString());
        }
        finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

    public String sendHttpsRequestByGet(String url, Map<String, String> params) {
        String responseContent = null;
        HttpClient httpClient = getNewHttpClient();

        try {
            Log.d("HttpClientUtil-https", "sendHttpsRequestByGet in progress...");
            Log.d("HttpClientUtil", "url: "+rooturl+url);

            List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 构建POST请求的表单参数
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            String paramsString = URLEncodedUtils.format(formParams, "UTF-8");
            HttpGet httpGet = new HttpGet(rooturl+url+"?"+paramsString);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity(); // 获取响应实体
            if (entity != null) {
                responseContent = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            Log.e("HttpClientUtil-https", e.toString());
        }
        finally {
            // 关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }
    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @return country code or null
     */
    public static Boolean inChina() {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm==null) {
                return true; //if don't have this system service, always get the china network
            }
            final String simCountry = tm.getSimCountryIso().toLowerCase(Locale.US);
            final String networkCountry = tm.getNetworkCountryIso().toLowerCase(Locale.US);
            Log.d("HttpClientUtil", "networkCountry: "+networkCountry);
            Log.d("HttpClientUtil", "simCountry: "+simCountry);

            if (networkCountry.equals("cn") || networkCountry.equals("")) { // Network country code is available
                return true;
            }
            else if (simCountry.equals("cn") || simCountry.equals("")) { // SIM country code is available
                return true;
            }
        }
        catch (Exception e) {
            Log.e("HttpClientUtil-https", e.toString());
        }
        return false;
    }

    public static String parseRegisterResponse(String response) {
        String key = "";
        try {
            JSONObject jObject = new JSONObject(response);

            key = jObject.optString("user_key").toString();
        } catch (JSONException e) {
            Log.e("https-json", e.toString());
        }
        return key;
    }

    public static Boolean parseSuccessResponse(String response) {
        try {
            JSONObject jObject = new JSONObject(response);
            if (jObject.getString("success").equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            Log.e("https-json", e.toString());
            return false;
        }
    }

    public static ResponseObject parseMessageResponse(String response) {
        ResponseObject responseObject = new ResponseObject();
        ArrayList<ChatMessage> msgArray = new ArrayList<ChatMessage>();
        Integer pageNext = null;
        Integer pagePrev = null;
        Boolean success = false;
        Integer sentId = -1;

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(response);
            try {

                JSONArray jmsgArray = jObject.getJSONArray("messages");

                for (int i = 0; i < jmsgArray.length(); i++) {
                    try {
                        ChatMessage message = new ChatMessage();
                        JSONObject jmessage = jmsgArray.getJSONObject(i);
                        // Pulling items from the array
                        Integer id = jmessage.getInt("id");
                        message.setId(id);
                        message.setBody(jmessage.getString("message"));
                        message.setDateFromString(jmessage.getString("date"));
                        message.setReadFlag(jmessage.getBoolean("read"));
                        message.setDirection(jmessage.getString("direction"));

                        msgArray.add(0,message);

                    } catch (JSONException e) {
                        Log.e("https-json", e.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("https-json", "Error parsing date: " + e.toString());
                    }
                }
            } catch (JSONException e) {
                Log.e("https-json", e.toString());
            }
            try {
                JSONObject jpageObject = jObject.getJSONObject("paging");
                if (jpageObject.getString("next") == "null") {
                    pageNext = 0;
                } else {
                    pageNext = jpageObject.getInt("next");
                }
                if (jpageObject.getString("previous") == "null") {
                    pagePrev = 0;
                } else {
                    pagePrev = jpageObject.getInt("prev");
                }
            } catch (JSONException e) {
                Log.e("https-json", e.toString());
            }
            try {
                if (jObject.getString("success").equals("true")) {
                    success = true;
                    Log.d("https-json", "successfully got jo");
                } else {
                    success = false;
                }
                if (jObject.has("id")) {
                    if (jObject.getString("id") == "null") {
                        sentId = -1;
                    } else {
                        sentId = jObject.getInt("id");
                    }
                    Log.d("https-json", "has id: "+sentId);
                }
            } catch (JSONException e) {
                Log.e("https-json", e.toString());
            }
        }
        catch (JSONException e) { //catch object creation
            e.printStackTrace();
            Log.e("https-json", "Could not create json object. " + e.toString());
        }
        responseObject.setMessages(msgArray);
        responseObject.setPageNext(pageNext);
        responseObject.setPagePrev(pagePrev);
        responseObject.setSuccess(success);
        responseObject.setId(sentId);

        return responseObject;
    }

    public static ResponseObject makeDummyMessages(Integer count) {
        ResponseObject responseObject = new ResponseObject();
        ArrayList<ChatMessage> msgArray = new ArrayList<ChatMessage>();
        Random rand = new Random();

        ChatMessage message = new ChatMessage();
        // Make message
        message.setId(100);
        message.setBody("Hi, I have a question about praying.");
        message.setDate(new Date(System.currentTimeMillis() - 10600000));
        message.setReadFlag(true);
        message.setDirection("in");
        msgArray.add(message);

        // Make message
        ChatMessage message1 = new ChatMessage();
        message1.setId(101);
        message1.setBody("Hello, my name is Matt, thank you for your message.  Jesus teaches us how to pray in Matthew 6. " +
                "What specifically would you like to know about praying?");
        message1.setDate(new Date(System.currentTimeMillis() - 3500000));
        message1.setReadFlag(true);
        message1.setDirection("out");
        msgArray.add(message1);

        // Make message
        ChatMessage message2 = new ChatMessage();
        message2.setId(102);
        message2.setBody("" +
                "If God already knows the future, why do I need to pray for things " +
                "if he already knows what's going to happen?  Will I change his mind?");
        message2.setDate(new Date(System.currentTimeMillis()-1500000));
        message2.setReadFlag(true);
        message2.setDirection("in");
        msgArray.add(message2);

        // Make message
        ChatMessage message3 = new ChatMessage();
        message3.setId(102);
        message3.setBody("" +
                "Thanks for your insightful question. Well, it's true that God has a plan for " +
                "each of us and knows what our future holds, but that doesn't mean we shouldn't be " +
                "having conversation with him!  Prayer is having a conversation with God, and it has much more " +
                "purpose than just asking for things to happen, although that is one reason why we pray.  In 1 John 5:14-15, God " +
                "promises that when we ask for things that are in accordance with his will, he will give us what we ask for. Other reasons " +
                "to pray are to ask for guidance on making decisions, to thank and praise God for good things he has done,");
        message3.setDate(new Date(System.currentTimeMillis()));
        message3.setReadFlag(true);
        message3.setDirection("out");
        msgArray.add(message3);

        Log.d("HttpClientUtil","making dummy messages");

        responseObject.setMessages(msgArray);
        responseObject.setPageNext(1);
        responseObject.setPagePrev(0);
        responseObject.setSuccess(true);

        return responseObject;
    }

    public static class ResponseObject {
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
        Integer pageNext = 0;
        Integer pagePrev = 0;
        Boolean success;
        Integer id = -1;

        public void setMessages(ArrayList<ChatMessage> messages) {
            this.messages= messages;
        }
        public void setPageNext(Integer pageNext) {
            this.pageNext = pageNext;
        }
        public void setPagePrev(Integer pagePrev) {
            this.pagePrev = pagePrev;
        }
        public void setSuccess(Boolean success) {
            this.success = success;
        }
        public void setId(Integer id) {this.id = id;}
        public Integer getId() {return this.id;}

        public Boolean isSuccessful() {
            return this.success;
        }
        public Integer getPageNext() {
            return this.pageNext;
        }
        public Integer getPagePrev() {
            return this.pagePrev;
        }
        public ArrayList<ChatMessage> getMessages() {
            return messages;
        }
        public Integer getMessageCount() {
            return this.messages.size();
        }
    }
}