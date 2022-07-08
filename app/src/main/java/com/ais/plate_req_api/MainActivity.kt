package com.ais.plate_req_api

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.ais.plate_req_api.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.main_fragment)
        setupActionBarWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.another_menu, menu)
        return true
    }

    @SuppressLint("NewApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menu_item_1 -> { // Open website for API documentation
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Visit API Documentation")
                builder.setMessage("Are you sure?, this will open in your default browser.")
                builder.setIcon(R.drawable.ic_baseline_warning_24)
                builder.setPositiveButton("yes") { _, _ ->
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://docs.platerecognizer.com/?shell#introduction")
                    )
                    startActivity(browserIntent)
                }
                builder.setNegativeButton("no") { _, _ ->
                }
                builder.show()
            }
            R.id.menu_item_2 -> { // Exit app.
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Exit Plate Req API")
                builder.setMessage("Are you sure?")
                builder.setIcon(R.drawable.ic_baseline_warning_24)
                builder.setPositiveButton("yes") { _, _ ->
                    finishAndRemoveTask()
                }
                builder.setNegativeButton("no") { _, _ ->
                }
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
