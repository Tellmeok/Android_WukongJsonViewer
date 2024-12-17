package com.wukong.json.viewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.wukong.viewer.util.WuKongAssetser
import com.android.wukong.viewer.util.json.WuKongJSONViewer
import com.wukong.json.viewer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.clickMeButton.setOnClickListener {

            val jsonViewer: WuKongJSONViewer = WuKongJSONViewer()
            var json = WuKongAssetser.getAssetsAsJson("json/example.json")
            jsonViewer.show("example", json, null)

        }
    }
}
