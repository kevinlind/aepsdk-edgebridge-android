/*
 Copyright 2022 Adobe. All rights reserved.

 This file is licensed to you under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software distributed under
 the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 OF ANY KIND, either express or implied. See the License for the specific language
 governing permissions and limitations under the License.
*/
package com.adobe.marketing.mobile.tutorial;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.adobe.marketing.mobile.Assurance;

import androidx.appcompat.app.AppCompatActivity;

/**
 * An activity representing the Assurance Connection Screen
 */
public class AssuranceActivity extends AppCompatActivity {
    Button btnConnectToAssuranceSession = null;
    EditText txtAssuranceSessionURL = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assurance);

        btnConnectToAssuranceSession = findViewById(R.id.btnConnectToAssuranceSession);
        txtAssuranceSessionURL =  findViewById(R.id.txtAssuranceSessionURL);

        //Setup button events
        btnConnectToAssuranceSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Assurance.startSession(txtAssuranceSessionURL.getText().toString());
            }
        });
    }
}