package br.com.marketpay.conductor.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import br.com.marketpay.conductor.R;


public class SobreActivity extends AppCompatActivity {
    private TextView textViewAppName;
    private TextView textViewAppVersion;
    private TextView textViewDeviceModelo;
    private TextView textViewVersionSO;
    private TextView textViewDescricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

        getFindViewById();

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            setView(pinfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setView(PackageInfo pinfo) {
        textViewAppName.setText(R.string.app_name);
        textViewAppVersion.setText(String.format("Versão do Aplicativo: %s", String.valueOf(pinfo.versionName)));
        textViewVersionSO.setText(String.format("Versão do Sistema Operacional: %s", String.valueOf(Build.VERSION.RELEASE)));
        textViewDeviceModelo.setText(String.format("Modelo: %s", String.valueOf(Build.MODEL)));
    }

    private void getFindViewById() {
        textViewAppName = findViewById(R.id.appName);
        textViewAppVersion = findViewById(R.id.appVersion);
        textViewDeviceModelo = findViewById(R.id.deviceModelo);
        textViewVersionSO = findViewById(R.id.versionSO);
        textViewDescricao = findViewById(R.id.descricao);
    }

}
