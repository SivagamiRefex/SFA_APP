package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.databinding.ActivityAssignedTaskBinding
import com.example.sfa.databinding.ActivityTaskCreationBinding
import com.example.sfa.presentation.ui.fragment.CompletedTaskFragment
import com.example.sfa.presentation.ui.fragment.PendingTaskFragment
import com.example.sfa.presentation.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssignedTaskActivity:AppCompatActivity() {
    private lateinit var binding: ActivityAssignedTaskBinding
    private val taskViewModel: TaskViewModel by viewModels()
    lateinit var dbController: DBController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignedTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        dbController = DBController(applicationContext)
        binding.layoutToolbar.tvTitle.text="Assigned Task"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        setupViewPager(binding.tabViewpager)
        binding.tabTablayout.setupWithViewPager(binding.tabViewpager)
    }
    private fun setupViewPager(viewpager: ViewPager) {
        var adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(PendingTaskFragment(), "Pending")
        adapter.addFragment(CompletedTaskFragment(), "Completed")
        viewpager.setAdapter(adapter)
    }

    class ViewPagerAdapter : FragmentPagerAdapter {
        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        public constructor(supportFragmentManager: FragmentManager)
                : super(supportFragmentManager)

        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }


        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
        }

        override fun getCount(): Int {
            return fragmentList1.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)
        }
    }
}