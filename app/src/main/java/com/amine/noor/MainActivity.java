package com.amine.noor;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private WebView webView;

    public class NoorBridge {

        @JavascriptInterface
        public void downloadFile(
                String url,
                String fileName,
                String mimeType
        ) {

            try {

                DownloadManager.Request request =
                        new DownloadManager.Request(Uri.parse(url));

                request.setTitle(fileName);
                request.setDescription("نور - تنزيل");

                request.setNotificationVisibility(
                        DownloadManager.Request
                                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                );

                request.setAllowedOverMetered(true);
                request.setAllowedOverRoaming(true);

                request.setMimeType(
                        mimeType == null || mimeType.isEmpty()
                                ? "application/octet-stream"
                                : mimeType
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    ContentValues values = new ContentValues();

                    values.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            fileName
                    );

                    values.put(
                            MediaStore.Downloads.MIME_TYPE,
                            mimeType == null
                                    ? "application/octet-stream"
                                    : mimeType
                    );

                    values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS
                                    + "/Noor"
                    );

                    request.setDestinationInExternalFilesDir(
                            MainActivity.this,
                            Environment.DIRECTORY_DOWNLOADS,
                            "Noor/" + fileName
                    );

                } else {

                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            "Noor/" + fileName
                    );

                }

                DownloadManager manager =
                        (DownloadManager)
                                getSystemService(
                                        Context.DOWNLOAD_SERVICE
                                );

                if (manager == null) {
                    throw new Exception(
                            "DownloadManager unavailable"
                    );
                }

                manager.enqueue(request);

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "بدأ التنزيل: " + fileName,
                                Toast.LENGTH_SHORT
                        ).show()
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "تعذر بدء تنزيل الملف",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }


        @JavascriptInterface
        public void saveText(
                String fileName,
                String text
        ) {

            try {

                byte[] data =
                        (text == null ? "" : text)
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                if (Build.VERSION.SDK_INT >= 29) {

                    ContentValues values =
                            new ContentValues();

                    values.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            fileName
                    );

                    values.put(
                            MediaStore.Downloads.MIME_TYPE,
                            "text/plain"
                    );

                    values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS
                                    + "/Noor"
                    );

                    Uri uri =
                            getContentResolver().insert(
                                    MediaStore.Downloads
                                            .EXTERNAL_CONTENT_URI,
                                    values
                            );

                    if (uri == null) {
                        throw new Exception(
                                "MediaStore insert failed"
                        );
                    }

                    OutputStream output =
                            getContentResolver()
                                    .openOutputStream(uri);

                    if (output == null) {
                        throw new Exception(
                                "Output stream failed"
                        );
                    }

                    try {

                        output.write(data);
                        output.flush();

                    } finally {

                        output.close();

                    }

                } else {

                    File downloads =
                            Environment
                                    .getExternalStoragePublicDirectory(
                                            Environment
                                                    .DIRECTORY_DOWNLOADS
                                    );

                    File folder =
                            new File(
                                    downloads,
                                    "Noor"
                            );

                    if (!folder.exists()
                            && !folder.mkdirs()) {

                        throw new Exception(
                                "Cannot create folder"
                        );
                    }

                    File file =
                            new File(
                                    folder,
                                    fileName
                            );

                    FileOutputStream output =
                            new FileOutputStream(file);

                    try {

                        output.write(data);
                        output.flush();

                    } finally {

                        output.close();

                    }
                }

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "تم حفظ الملف في Downloads/Noor",
                                Toast.LENGTH_LONG
                        ).show()
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "تعذر حفظ النص",
                                Toast.LENGTH_LONG
                        ).show()
                );

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.addJavascriptInterface(
                new NoorBridge(),
                "NoorAndroid"
        );

        webView.setWebViewClient(
                new WebViewClient()
        );

        webView.loadUrl(
                "file:///android_asset/index.html"
        );

        setContentView(webView);
    }


    @Override
    public void onBackPressed() {

        if (webView != null
                && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();

        }
    }
                    }
