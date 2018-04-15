/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abc.product.app.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;

import ai.api.ui.AIButton;

public class TTS extends UtteranceProgressListener{

    private TextToSpeech textToSpeech;
    private AIButton aiButton;
    private HashMap<String,String> params = new HashMap<>();

    public void init(final Context context,final AIButton aiButton) {
        this.aiButton = aiButton;
        params.put("utteranceId","1");
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                }
            });
            textToSpeech.setOnUtteranceProgressListener(this);
        }
    }

    public void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    @Override
    public void onStart(String utteranceId) {
        System.out.println();
    }

    @Override
    public void onDone(String utteranceId) {
        aiButton.getAIService().startListening();
    }

    @Override
    public void onError(String utteranceId) {
        System.out.println();
    }
}
