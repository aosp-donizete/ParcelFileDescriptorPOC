package com.android.consumefiles

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.consumefiles.ui.theme.ConsumeFilesTheme
import com.android.sharefiles.IShareFiles
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IShareFilesWrapper(
    private val context: Context,
    private var iShareFiles: IShareFiles? = null
) {
    private suspend fun assertConnected() {
        if (iShareFiles?.asBinder()?.isBinderAlive != true) {
            suspendCoroutine { cont ->
                val sc = object: ServiceConnection {
                    override fun onServiceConnected(
                        name: ComponentName?,
                        service: IBinder?
                    ) {
                        iShareFiles = IShareFiles.Stub.asInterface(service)
                        assert(iShareFiles != null)
                        cont.resume(Unit)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        iShareFiles = null
                    }
                }
                val intent = Intent().apply {
                    component = ComponentName.createRelative(
                        "com.android.sharefiles",
                        ".MainService"
                    )
                }
                context.bindService(intent, sc, Context.BIND_AUTO_CREATE)
            }
        }
    }

    suspend fun getAvailableFiles(): List<String> {
        assertConnected()
        return iShareFiles!!.availableFiles
    }

    suspend fun getFileDescriptor(file: String): ParcelFileDescriptor {
        assertConnected()
        return iShareFiles!!.getFileDescriptor(file)
    }
}

class BinderActivity : ComponentActivity() {

    private val iShareFiles by lazy {
        IShareFilesWrapper(this)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsumeFilesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val files = remember {
                        mutableStateListOf<String>()
                    }

                    LaunchedEffect(Unit) {
                        files.addAll(iShareFiles.getAvailableFiles())
                    }

                    val scope = rememberCoroutineScope()

                    LazyColumn {
                        items(files) {
                            Card(onClick = {
                                scope.launch {
                                    val random = File(cacheDir, UUID.randomUUID().toString())

                                    val parcel = iShareFiles.getFileDescriptor(it)
                                    val output = random.outputStream()
                                    val input = FileInputStream(parcel.fileDescriptor)

                                    input.copyTo(output)

                                    arrayOf(parcel, output, input).forEach(Closeable::close)

                                    val uri = Uri.fromFile(random)
                                    val intent = Intent(
                                        this@BinderActivity,
                                        MainActivity::class.java
                                    ).apply {
                                        setData(uri)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                            }) {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }
}