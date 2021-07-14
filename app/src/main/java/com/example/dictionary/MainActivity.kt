package com.example.dictionary

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.io.SyncFailedException
import java.lang.Exception

@Serializable
data class Dictionarys(
    @SerialName("list")
    val dictionaryList: List<dictionary>,
)

@Serializable
data class Dictionary(
    val keyword: String
    val description: String
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var helloButton = findViewById<Button>(R.id.helloButton)
        var addButton = findViewById<Button>(R.id.addButton)
        var getButton = findViewById<Button>(R.id.getButton)
        var resultText = findViewById<TextView>(R.id.resultText)

        helloButton.setOnClickListener()
        {
            val helloJson = JSONObject()
            HttpClient.postAsync("https://gotestinaly.herokuapp.com/v1/Ping", helloJson)    //https://gotestinaly.herokuapp.com/v1/Ping
            {
                val message = it.getString("message")
                resultText.setText(message)
                Log.i(TAG, "非同期でPOST:${it}")
            }
        }


        addButton.setOnClickListener()
        {
            val handleError: (Exception) -> Unit = {
                e -> Log.e(TAG, "エラー：${e}")
            }

            val addJson = JSONObject()
            addJson.put("keyword", "techno")
            addJson.put("description", "company")
            HttpClient.postAsync("https://gotestinaly.herokuapp.com/v1/AddDictionary", addJson, onFailed = handleError)
            {

                val keyword = it.getString("keyword")
                val description = it.getString("description")
                resultText.setText("登録しました。\n単語：" + keyword + "意味：" + description)
                Log.i(TAG, "非同期でPOST:${it}")
            }

            //resultText.setText("準備中です！！")
        }



        getButton.setOnClickListener()
        {
            val getJson = JSONObject()
            HttpClient.postAsync("https://gotestinaly.herokuapp.com/v1/GetList", getJson)    //https://gotestinaly.herokuapp.com/v1/Ping
            {
                Log.i(TAG, "非同期でPOST:${it}")

                val dic = Json.decodeFromString<Dictionarys>(it)
                resultText.setText(dic)

                //resultText.setText(it.toString())
            }
        }
    }
}

class HttpClient {
    companion object {
        val instance = OkHttpClient()

        //同期的にPOSTする場合
        fun post(url: String, json: JSONObject): JSONObject {
            val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            val response = instance.newCall(request).execute()
            if(!response.isSuccessful) throw IOException("Unexpected cod $response");
            return JSONObject(response.body?.string().orEmpty())
        }

        //非同期でPOSTする場合
        fun postAsync(url: String, json: JSONObject, onFailed: (Exception) -> Unit = {}, onSuccess: (JSONObject) -> Unit = {}) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    coroutineScope {
                        async(Dispatchers.Default) { post(url, json) }.await().let {
                            onSuccess(it)
                        }
                    }
                } catch (e: Exception) {
                    onFailed(e)
                }
            }
        }
    }
}