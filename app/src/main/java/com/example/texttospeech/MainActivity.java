package com.example.texttospeech;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private EditText etNotes;
    private EditText etKeyword;
    private EditText etTypedNotes;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView title = findViewById(R.id.title);
        Button btnRecord = findViewById(R.id.btnRecord);
        etNotes = findViewById(R.id.etNotes);
        etKeyword = findViewById(R.id.etKeyword);
        etTypedNotes = findViewById(R.id.etTypedNotes);
        Button btnHighlight = findViewById(R.id.btnHighlight);
        Button btnSpeak = findViewById(R.id.btnSpeak);

        // Initialize Text to Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Text to Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnRecord.setOnClickListener(v -> startSpeechToText());

        btnHighlight.setOnClickListener(v -> {
            String keyword = etKeyword.getText().toString().trim();
            if (!keyword.isEmpty()) {
                highlightKeyword(keyword);
            } else {
                Toast.makeText(this, "Enter a keyword to highlight", Toast.LENGTH_SHORT).show();
            }
        });

        btnSpeak.setOnClickListener(v -> {
            String typedText = etTypedNotes.getText().toString();
            String transcribedText = etNotes.getText().toString();
            String textToSpeak = !typedText.isEmpty() ? typedText : transcribedText;

            if (!textToSpeak.isEmpty()) {
                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(this, "No text to read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etNotes.setText(result.get(0));
            }
        }
    }

    private void highlightKeyword(String keyword) {
        String noteText = etNotes.getText().toString();
        SpannableString spannable = new SpannableString(noteText);

        int index = noteText.toLowerCase().indexOf(keyword.toLowerCase());
        while (index >= 0) {
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.teal_700)),
                    index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = noteText.toLowerCase().indexOf(keyword.toLowerCase(), index + keyword.length());
        }

        etNotes.setText(spannable);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}