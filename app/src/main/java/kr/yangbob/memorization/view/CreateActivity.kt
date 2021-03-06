package kr.yangbob.memorization.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_create.*
import kr.yangbob.memorization.EXTRA_TO_TUTORIAL
import kr.yangbob.memorization.R
import kr.yangbob.memorization.databinding.ActivityCreateBinding
import kr.yangbob.memorization.viewmodel.CreateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateActivity : AppCompatActivity() {

    private val model: CreateViewModel by viewModel()
    private var menu: MenuItem? = null
    private lateinit var etQstData: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        val binding: ActivityCreateBinding = DataBindingUtil.setContentView(this, R.layout.activity_create)
        binding.lifecycleOwner = this
        binding.model = model

        toolBar.title = getString(R.string.add_appbar_title)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (model.isFirst()) {
            startActivity(Intent(this, TutorialActivity::class.java).apply {
                putExtra(EXTRA_TO_TUTORIAL, "create")
            })
        }

        qstLayout.setOnClickListener {
            qstData.requestFocus()
        }
        answerLayout.setOnClickListener {
            answerData.requestFocus()
        }

        adView.loadAd(AdRequest.Builder().build())

        etQstData = qstData
    }

    override fun onResume() {
        super.onResume()
        model.resetIsPossibleClick()
        etQstData.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create, menu)
        this.menu = menu?.findItem(R.id.action_add_save)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.action_add_save -> {
            if (model.checkIsPossibleClick()) {
                if (model.isPossibleInsert()) {
                    if (model.insertQst()) finish()
                    else Toast.makeText(this, R.string.toast_duplication_qst, Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, R.string.toast_need_input_qst, Toast.LENGTH_SHORT).show()
                model.resetIsPossibleClick()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
