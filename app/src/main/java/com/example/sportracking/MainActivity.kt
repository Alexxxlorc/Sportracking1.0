package com.example.sportracking

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.sportracking.core.FragmentCommunicator
import com.example.sportracking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FragmentCommunicator {

    // 1. Agregamos el binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Inicializamos el binding y cambiamos el setContentView
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 3. Implementamos la función del comunicador que pide el profe
    override fun manageLoader(isVisible: Boolean) {
        binding.loaderView.isVisible = isVisible
    }
}