package com.kumano_ryo.shijubo.kumano_dormitoryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button button1 = (Button) findViewById(R.id.settings_ok);
        button1.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.settings_cancel);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.settings_ok:
                EditText editID = (EditText) findViewById(R.id.user_id);
                String id = editID.getText().toString();

                EditText editPass = (EditText) findViewById(R.id.pass);
                String token = editPass.getText().toString();

                if(id.equals("") || token.equals(""))
                {
                    Toast.makeText(this, "IDとPasswordを入力して下さい", Toast.LENGTH_SHORT).show();
                    break;
                }
                editID.setText("");
                editPass.setText("");
                setToken(id, token);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;

            case R.id.settings_cancel:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                finish();
                break;
        }
    }

    private void setToken(String id, String token)
    {
        SharedPreferences preferences = getSharedPreferences("a1", MODE_PRIVATE);
        String ak = preferences.getString("ak", "");

        SharedPreferences.Editor editor;
        editor = preferences.edit();

        if(ak.equals(""))
        {
            ak = UUID.randomUUID().toString();
            editor.putString("ak", ak);
        }

        Crypto crypto = new Crypto(
                new SharedPrefsBackedKeyChain(this),
                new SystemNativeCryptoLibrary());
        if(!crypto.isAvailable()){
            return;
        }
        try {
            byte[] encryptedKey = crypto.encrypt(token.getBytes(), new Entity(ak));
            editor.putString("ek", Base64.encodeToString(encryptedKey, Base64.DEFAULT));
            editor.putString("id", id);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (CryptoInitializationException e){
            e.printStackTrace();
        }catch(KeyChainException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        editor.apply();
    }
}
