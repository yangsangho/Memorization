package kr.yangbob.memorization.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*
import kr.yangbob.memorization.*
import kr.yangbob.memorization.databinding.ActivityMainLayoutDashboardBinding
import kr.yangbob.memorization.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModel()
    private var doubleBackToExitPressedOnce = false
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isMainFirst = model.isFirst(SETTING_IS_FIRST_MAIN)
        if (isMainFirst) startActivity(Intent(this, StartActivity::class.java))

        setTimer(this)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        setContentView(R.layout.activity_main)

        // ToolBar 설정
        toolBar.title = getString(R.string.app_name)
        setSupportActionBar(toolBar)

        model.getDormantQstList().observe(this, Observer {
            if (it.isEmpty()) {
                if (dormantBtn.visibility == View.VISIBLE) {
                    dormantBtn.visibility = View.GONE
                    dormantCnt.visibility = View.GONE
                }
            } else {
                if (dormantBtn.visibility == View.GONE) {
                    dormantBtn.visibility = View.VISIBLE
                    dormantCnt.visibility = View.VISIBLE
                }
                if (it.size > 99) dormantCnt.text = "99+"
                else dormantCnt.text = "${it.size}"
            }
        })

        dormantBtn.setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java).apply {
                putExtra("isDormant", true)
            })
        }

        // Viewpager 및 TabLayout 설정
        mainViewPager.adapter = MainFragmentAdapter(lifecycle, supportFragmentManager)
        mainViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.getTabAt(position)?.select()
            }
        })
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mainViewPager.currentItem = tab?.position ?: 0
            }
        })
        if(isMainFirst) mainViewPager.currentItem = 1

        //    UNKNOWN = 0
        //    UPDATE_NOT_AVAILABLE = 1
        //    UPDATE_AVAILABLE = 2
        //    DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS = 3
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        IN_APP_UPDATE_RECV_ID
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.resetIsPossibleClick()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        IN_APP_UPDATE_RECV_ID
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_main_write -> {
            if (model.checkIsPossibleClick()) {
                startActivityForResult(Intent(this, CreateActivity::class.java), 2)
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.main_toast_oneMore_back, Toast.LENGTH_SHORT).show()
        Handler().postDelayed(
                { doubleBackToExitPressedOnce = false },
                2000
        )
    }

    // 전체 문제 쪽에서 버튼 눌러서 activity 갔다가 돌아왔을 때 전체문제 탭이 선택되도록
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IN_APP_UPDATE_RECV_ID){
            if(resultCode != RESULT_OK){
                Toast.makeText(this, R.string.update_cancel, Toast.LENGTH_SHORT).show()
//                (-1)RESULT_OK: 사용자가 업데이트를 수락했습니다. 즉시 업데이트인 경우 앱에 업데이트 제어 권한이 주어졌을 때는 이미 Google Play가 업데이트를 완료한 상태여야 하기 때문에 개발자는 이 콜백을 수신하지 못할 수 있습니다.
//                (0)RESULT_CANCELED: 사용자가 업데이트를 거부하거나 취소했습니다.
//                (1)ActivityResult.RESULT_IN_APP_UPDATE_FAILED: 기타 오류로 인해 사용자가 동의하지 못했거나 업데이트가 진행되지 못했습니다.
            }
        } else {
            tabLayout.getTabAt(1)?.select()
            mainViewPager.currentItem = 1
        }
    }
}

class MainFragmentAdapter(mainLifeCycle: Lifecycle, fm: FragmentManager) :
        FragmentStateAdapter(fm, mainLifeCycle) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment =
            MainFragment.newInstance(position == 0)
}

class MainFragment : Fragment() {
    private val model: MainViewModel by sharedViewModel()
    private lateinit var binding: ActivityMainLayoutDashboardBinding
    private var testRecordCnt = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.activity_main_layout_dashboard, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val isToday = bundle.getBoolean("isToday")
            binding.isToday = isToday
            binding.model = model
            binding.fragment = this
            binding.isNoItemViewActivate = false
            binding.isBtn1Activate = true
            binding.isBtn2Activate = true
            if (isToday) {
                binding.dashboardChart.setCount(7)
                val observeList = model.getQstRecordList()
                observeList.observe(viewLifecycleOwner, Observer { rawList ->
                    model.setTodayTestCount()
                    if (testRecordCnt != rawList.size) {
                        testRecordCnt = rawList.size
                        if (rawList.isNotEmpty()) {
                            if(model.isFirst(SETTING_IS_FIRST_TODAY)) startTutorial(isToday = true)

                            val map = rawList.groupBy { qstRecord -> qstRecord.challenge_stage }.mapValues { it.value.size }.toMutableMap()
                            STAGE_LIST.filter { it.ordinal > 0 }.forEach { if (!map.containsKey(it.ordinal)) map[it.ordinal] = 0 }
                            val reviewCnt = map[Stage.REVIEW.ordinal]
                            map.remove(Stage.REVIEW.ordinal)
                            map[Stage.AFTER_MONTH.ordinal] = (map[Stage.AFTER_MONTH.ordinal]
                                    ?: 0) + (reviewCnt ?: 0)
                            binding.isNoItemViewActivate = false
                            binding.isHelpIconActivate = true
                            binding.isBtn2Activate = true  // 이건 없어도 될 것 같은데 말야
                            binding.dashboardChart.setDataList(map.toSortedMap().values.toList())
                        } else {
                            binding.isHelpIconActivate = false
                            binding.isNoItemViewActivate = true
                            binding.isBtn2Activate = false
                            binding.dashboardChart.setDataList(listOf())
                        }
                    }
                    binding.isBtn1Activate = !model.setTodayCardData()
                })
            } else {
                binding.dashboardChart.setCount(8)
                val observeList = model.getQstList()
                observeList.observe(viewLifecycleOwner, Observer { rawList ->
                    model.setEntireCardData()
                    if (rawList.isNotEmpty()) {
                        if(model.isFirst(SETTING_IS_FIRST_ENTIRE)) startTutorial(isToday = false)

                        val map = rawList.groupBy { qst -> qst.cur_stage }.mapValues { it.value.size }.toMutableMap()
                        STAGE_LIST.filter { it.ordinal < 8 }.forEach { if (!map.containsKey(it.ordinal)) map[it.ordinal] = 0 }
                        binding.isNoItemViewActivate = false
                        binding.dashboardChart.setDataList(map.toSortedMap().values.toList())
                        binding.isBtn1Activate = true
                        binding.isHelpIconActivate = true
                    } else {
                        binding.isHelpIconActivate = false
                        binding.isNoItemViewActivate = true
                        binding.dashboardChart.setDataList(listOf())
                        binding.isBtn1Activate = false
                    }
                    model.setTestCompletionRate()
                })
            }
        }
    }

    companion object {
        fun newInstance(isToday: Boolean) = MainFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isToday", isToday)
            }
        }
    }

    fun startTutorial(isToday: Boolean) {
        startActivity(Intent(context, TutorialActivity::class.java).apply {
            putExtra(EXTRA_TO_TUTORIAL, if (isToday) "today" else "entire")
        })
    }

    fun clickTestBtn(view: View) {
        if (model.checkIsPossibleClick()) {
            startActivity(Intent(context, TestActivity::class.java))
        }
    }

    fun clickEntireList(view: View) {
        if (model.checkIsPossibleClick()) {
            startActivityForResult(
                    Intent(context, EntireActivity::class.java),
                    0
            )  // main으로 복귀할 때 2번쩨 page로 가도록
        }
    }

    fun clickTodayRecord(view: View) {
        if (model.checkIsPossibleClick()) {
            startActivity(Intent(context, ResultActivity::class.java).apply {
                putExtra(EXTRA_TO_RESULT_DATESTR, todayDate.dateInt)
            })
        }
    }

    fun clickEntireRecord(view: View) {
        if (model.checkIsPossibleClick()) {
            startActivityForResult(
                    Intent(context, CalendarActivity::class.java),
                    1
            ) // main으로 복귀할 때 2번쩨 page로 가도록
        }
    }
}