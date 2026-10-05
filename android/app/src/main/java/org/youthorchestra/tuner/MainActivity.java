package org.youthorchestra.tuner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import java.io.*;
import java.util.*;

/** An offline-only trusted WebView; external lessons always leave the WebView. */
public final class MainActivity extends Activity {
 private static final String HOST="appassets.androidplatform.net";
 private static final String HOME="https://"+HOST+"/assets/index.html";
 private static final Set<String> ASSETS=new HashSet<>(Arrays.asList("index.html","style.css","pitch.js","app.js","icon.svg","violin-wood.jpg"));
 private WebView web; private PermissionRequest pending;
 private boolean trusted(Uri u){return "https".equals(u.getScheme())&&HOST.equals(u.getHost());}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().setStatusBarColor(0xff0b0b0d);getWindow().setNavigationBarColor(0xff0b0b0d);
  WebView.setWebContentsDebuggingEnabled(false);web=new WebView(this);setContentView(web);web.setOnApplyWindowInsetsListener(new android.view.View.OnApplyWindowInsetsListener(){public android.view.WindowInsets onApplyWindowInsets(android.view.View v,android.view.WindowInsets insets){v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;}});
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(false);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setSafeBrowsingEnabled(true);s.setSupportMultipleWindows(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setMediaPlaybackRequiresUserGesture(true);
  CookieManager.getInstance().setAcceptCookie(false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){if(trusted(r.getUrl()))return false;openExternal(r.getUrl());return true;}
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){Uri u=r.getUrl();String name=u.getLastPathSegment();if(trusted(u)&&("/assets/"+name).equals(u.getPath())&&ASSETS.contains(name)){try{String type=name.endsWith(".js")?"application/javascript":name.endsWith(".css")?"text/css":name.endsWith(".svg")?"image/svg+xml":name.endsWith(".jpg")?"image/jpeg":"text/html";Map<String,String> h=new HashMap<>();h.put("X-Content-Type-Options","nosniff");return new WebResourceResponse(type,"UTF-8",200,"OK",h,getAssets().open(name));}catch(IOException e){return blocked();}}return blocked();}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onPermissionRequest(final PermissionRequest request){runOnUiThread(new Runnable(){public void run(){if(!trusted(request.getOrigin())||!Arrays.asList(request.getResources()).contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)){request.deny();return;}if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});}else{if(pending!=null)pending.deny();pending=request;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},11);}}});}
   @Override public void onPermissionRequestCanceled(PermissionRequest request){if(pending==request)pending=null;}
  });web.loadUrl(HOME);
 }
 private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));}
 private void openExternal(Uri uri){if(!"https".equals(uri.getScheme()))return;String h=uri.getHost();if(!"www.youtube.com".equals(h)&&!"a440-studio-tuner.k-lee-augsburg-2018.chatgpt.site".equals(h))return;try{Intent i=new Intent(Intent.ACTION_VIEW,uri);i.addCategory(Intent.CATEGORY_BROWSABLE);startActivity(i);}catch(android.content.ActivityNotFoundException e){Toast.makeText(this,"Install a browser or YouTube to open this link.",Toast.LENGTH_LONG).show();}}
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);if(code==11&&pending!=null){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)pending.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});else pending.deny();pending=null;}}
 @Override protected void onPause(){if(web!=null){web.evaluateJavascript("if(typeof stopListening==='function')stopListening();if(typeof stopTone==='function')stopTone();",null);web.onPause();}super.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){if(pending!=null){pending.deny();pending=null;}if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
