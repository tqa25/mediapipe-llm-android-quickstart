package com.example.quickstart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler.ProgressListener

import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickModel: Button
    private lateinit var tvModelPath: TextView
    private lateinit var btnGenerate: Button
    private lateinit var etPrompt: EditText
    private lateinit var tvOutput: TextView

    private var modelFilePath: String? = null
    private var llm: LlmInference? = null

    private val pickModel =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // copy vào internal storage để có đường dẫn file
                modelFilePath = copyToInternal(uri)
                tvModelPath.text = "Model: ${modelFilePath ?: "(lỗi copy)"}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPickModel = findViewById(R.id.btnPickModel)
        tvModelPath = findViewById(R.id.tvModelPath)
        btnGenerate = findViewById(R.id.btnGenerate)
        etPrompt = findViewById(R.id.etPrompt)
        tvOutput = findViewById(R.id.tvOutput)

        // Nếu trước đó đã copy model vào filesDir thì dùng lại
        val existing = File(filesDir, "model.task")
        if (existing.exists()) {
            modelFilePath = existing.absolutePath
            tvModelPath.text = "Model: ${modelFilePath}"
        }

        btnPickModel.setOnClickListener {
            pickModel.launch(arrayOf("*/*")) // bạn chọn file .task trong Download
        }

        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                tvOutput.text = "Vui lòng nhập prompt."
                return@setOnClickListener
            }
            val path = modelFilePath
            if (path.isNullOrEmpty()) {
                tvOutput.text = "Chưa có model. Nhấn 'Chọn model (.task) lần đầu'."
                return@setOnClickListener
            }
            runInference(path, prompt)
        }
    }

    private fun runInference(path: String, prompt: String) {
        tvOutput.text = "Đang khởi tạo model...\n"
        if (llm == null) {
            val opts = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(path)
                .setMaxTopK(64)
                .setResultListener(
                    ProgressListener<String> { partial: String?, done: Boolean ->
                        runOnUiThread {
                            if (!partial.isNullOrEmpty()) {
                                tvOutput.append(partial) // String là CharSequence
                            }
                            if (done) {
                                tvOutput.append("\n\n[Done]")
                            }
                        }
                    }
                )
                .setErrorListener(
                    ErrorListener { e: RuntimeException ->
                        runOnUiThread { tvOutput.append("\n[Error] ${e.message}") }
                    }
                )
                .build()

            llm = LlmInference.createFromOptions(this, opts)


        }

        tvOutput.append("Đang generate...\n")
        // Streaming
        llm?.generateResponseAsync(prompt)
    }


    private fun copyToInternal(uri: Uri): String? {
        return try {
            val name = queryDisplayName(uri) ?: "model.task"
            val dst = File(filesDir, "model.task") // cố định tên
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dst).use { output -> input.copyTo(output) }
            }
            dst.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return if (it.moveToFirst() && idx >= 0) it.getString(idx) else null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        llm?.close()
        llm = null
    }

}
