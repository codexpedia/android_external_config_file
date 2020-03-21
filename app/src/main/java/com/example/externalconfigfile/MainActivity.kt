package com.example.externalconfigfile

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"

    }

    // /storage/emulated/0/Documents/test.txt or link /sdcard/Documents/config.json
    private var CONFIG_FILE_PATH = "${getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/config.json"
    private val REQUEST_PERMISSIONS = 100
    private val PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "CONFIG_FILE_PATH: ${CONFIG_FILE_PATH}")

        if (checkPermission(PERMISSIONS_REQUIRED)) {
            showFileData()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "requestCode: $requestCode")
        Log.d(TAG, "Permissions:" + permissions.contentToString())
        Log.d(TAG, "grantResults: " + grantResults.contentToString())

        if (requestCode == REQUEST_PERMISSIONS) {
            var hasGrantedPermissions = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    hasGrantedPermissions = false
                    break
                }
            }

            if (hasGrantedPermissions) {
                showFileData()
            } else {
                finish()
            }

        } else {
            finish()
        }
    }

    private fun showFileData() {
        val targetFile = File(CONFIG_FILE_PATH)
        val targetFileContent = if (targetFile.exists()) {
            readFile(targetFile)
        } else {
            ""
        }

        val config = Gson().fromJson(targetFileContent, Config::class.java)

        val stringBuilder = StringBuilder()
        stringBuilder.append("\n")
        stringBuilder.append("file location: $CONFIG_FILE_PATH")
        stringBuilder.append("\n")
        stringBuilder.append("file content: $targetFileContent")
        stringBuilder.append("\n")
        stringBuilder.append("environment: ${config.environment}")

        Log.d("file_debug", stringBuilder.toString())
        tv_result.text = stringBuilder.toString()
    }

    private fun readFile(file: File) : String {
        var resultStr = ""
        try {
            val fileInputStream = FileInputStream(file)
            val size = fileInputStream.available()
            val buffer = ByteArray(size)
            fileInputStream.read(buffer)
            resultStr = String(buffer, Charset.forName("UTF-8"))
            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resultStr
    }

    private fun checkPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}