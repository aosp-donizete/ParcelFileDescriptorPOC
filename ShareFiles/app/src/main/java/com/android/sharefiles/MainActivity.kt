package com.android.sharefiles

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.sharefiles.ui.theme.ShareFilesTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startForegroundService(
            Intent(this, MainService::class.java)
        )

        setContent {
            ShareFilesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Click to share a file by using FileProvider mechanism",
                        )
                        LazyColumn {
                            items(
                                with(MainFileProvider) {
                                    realImagesPath.listFiles() ?: emptyArray()
                                }
                            ) {
                                Card(
                                    onClick = {
                                        val uri = with(MainFileProvider) {
                                            getUriForFile(it)
                                        }
                                        val type = contentResolver.getType(uri)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, type)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        val chooser = Intent.createChooser(intent, "Select an app")
                                        startActivity(chooser)
                                    }
                                ) {
                                    Text(text = it.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}