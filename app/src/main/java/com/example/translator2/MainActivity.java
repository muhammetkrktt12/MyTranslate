package com.example.translator2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.translator2.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    private ActivityMainBinding binding;
    int fromLanguageCode,toLanguageCode =0;
    int languageCode;


    String[] fromLanguage = {"-dan çevir","English","Arapça","Afrikaca","Almanca","Belarusça","Bulgarca","Çekçe","Çince",
            "Farsça","Fransızca","Hintçe","İspanyolca","İsvecçe","İtalyanca","Japonca","Katalanca","Korece","Portekizce","Rusça","Türkçe","Ukraynaca","Vietnamca",
            "Yunanca"};
    String[] toLanguage = {"-e çevir","English","Arapça","Afrikaca","Almanca","Belarusça","Bulgarca","Çekçe","Çince",
            "Farsça","Fransızca","Hintçe","İspanyolca","İsvecçe","İtalyanca","Japonca","Katalanca","Korece","Portekizce","Rusça","Türkçe","Ukraynaca","Vietnamca",
            "Yunanca"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        binding.fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fromSpinner.setAdapter(fromAdapter);


        binding.toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toSpinner.setAdapter(toAdapter);


        binding.btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.TranslatedTv.setVisibility(View.VISIBLE);
                binding.TranslatedTv.setText("");

                if(binding.idEditSource.getText().toString().isEmpty()) {

                    Toast.makeText(MainActivity.this,"Lütfen çevirmek istediğiniz metni yazın",Toast.LENGTH_SHORT).show();

                }
                else if (fromLanguageCode == 0) {

                    Toast.makeText(MainActivity.this,"Lütfen Kaynak Dilini Seçin",Toast.LENGTH_SHORT).show();
                }
                else if(toLanguageCode == 0) {

                    Toast.makeText(MainActivity.this,"Lütfen Çevirmek İstediğiniz Dili Seçin",Toast.LENGTH_SHORT).show();
                }
                else {

                    translateText(fromLanguageCode,toLanguageCode,binding.idEditSource.getText().toString());
                }

            }
        });


        binding.vMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO)) {

                        Snackbar.make(view,"Permission Needed ! ",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //Request Permission
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                            }
                        }).show();

                    }
                    else {

                        //Request Permission
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    }
                }
                else {

                    //Permission Granted - Make Intent
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Çevirmek için bir şeyler söyle");
                    activityResultLauncher.launch(intent);
                }


            }
        });

    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {

        binding.TranslatedTv.setText("Model indiriliyor, lütfen bekleyin...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                binding.TranslatedTv.setText("Çevriliyor...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.TranslatedTv.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Çeviri Yapılamadı ! Tekrar Deneyin",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Model indirilemedi ! Lütfen internet bağlantınızı kontrol edin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getLanguageCode(String language) {

       int languageCode = 0;

        switch (language) {

            case "English":
                languageCode =  FirebaseTranslateLanguage.EN;
                break;
            case "Arapça":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Afrikaca":
                languageCode = FirebaseTranslateLanguage.AF;
                break;

            case "Almanca":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Belarusça":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarca":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Çekçe":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Çince":
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            case "Farsça":
                languageCode = FirebaseTranslateLanguage.FA;
                break;
            case "Fransızca":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Hintçe":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "İspanyolca":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "İsvecçe":
                languageCode = FirebaseTranslateLanguage.SV;
                break;
            case "İtalyanca":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japonca":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Korece":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Katalanca":
                languageCode = FirebaseTranslateLanguage.KA;
                break;
            case "Portekizce":
                languageCode = FirebaseTranslateLanguage.PT;
                break;
            case "Rusça":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Türkçe":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Ukraynaca":
                languageCode = FirebaseTranslateLanguage.UK;
                break;
            case "Vietnamca":
                languageCode = FirebaseTranslateLanguage.VI;
                break;
            case "Yunanca":
                languageCode = FirebaseTranslateLanguage.EL;
                break;

            default:
                languageCode = 0;

        }

        return languageCode;

    }



    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null) {
                    ArrayList<String> sonuc = intentFromResult.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    binding.idEditSource.setText(sonuc.get(0));
                    }

                }
            }
        });


        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result) {

                    //Permission Granted
                    //Intent

                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Çevirmek için bir şeyler söyle");
                    activityResultLauncher.launch(intent);
                }
                else {

                    //Permission Denied

                    Toast.makeText(MainActivity.this,"İzin Gerekli",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}