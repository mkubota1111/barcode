package gov.cdc.cra.crabarcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.apache.commons.lang.StringEscapeUtils;

public class MainActivity extends AppCompatActivity {

    private static final String DEFAULT_URL = "file:///android_asset/index1.html";
    private static final int PATIENT_BARCODE = 0;
    private static final int LOT_BARCODE = 1;

    private static final String PATIENT_JS_CALLBACK = "patientBarcodeScan";
    private static final String LOT_JS_CALLBACK = "lotBarcodeScan";

    private static final String BARCODE_TYPE = "barcodeType";

    private WebView mWebView;

    private int mBarcodeType;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BARCODE_TYPE, mBarcodeType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(this, "barcodeJS");
        mWebView.loadUrl(DEFAULT_URL);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBarcodeType = savedInstanceState.getInt(BARCODE_TYPE);
    }

    @JavascriptInterface
    public void startPatientBarcodeScan() {
        mBarcodeType = PATIENT_BARCODE;
        startBarcodeCamera();
    }

    @JavascriptInterface
    public void startLotBarcodeScan() {
        mBarcodeType = LOT_BARCODE;
        startBarcodeCamera();
    }

    private void startBarcodeCamera() {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    String code = barcode.displayValue;
                    showMessage(code);
                }
                else {
                    Toast.makeText(this, "No barcode found", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showMessage(String message) {
        String functionCall = "void";
        if (PATIENT_BARCODE == mBarcodeType) {
            functionCall = PATIENT_JS_CALLBACK;
        }
        else if (LOT_BARCODE == mBarcodeType) {
            functionCall = LOT_JS_CALLBACK;
        }
        String escapedMessage = StringEscapeUtils.escapeJavaScript(message);
        String javascript = String.format("javascript: %s('%s')", functionCall, escapedMessage);
        mWebView.loadUrl(javascript);
    }
}
