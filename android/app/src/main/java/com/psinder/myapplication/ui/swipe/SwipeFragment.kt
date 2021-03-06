package com.psinder.myapplication.ui.swipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.psinder.myapplication.ui.MainActivity
import com.psinder.myapplication.R
import com.psinder.myapplication.databinding.FragmentSwipeBinding
import com.yuyakaido.android.cardstackview.*
import com.psinder.myapplication.entity.Profile
import com.psinder.myapplication.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
// TODO: Use https://github.com/yuyakaido/CardStackView
@AndroidEntryPoint
class SwipeFragment : Fragment(), CardStackListener {

    private val adapter = ProfilesAdapter()
    private lateinit var layoutManager: CardStackLayoutManager
    private val manager by lazy { CardStackLayoutManager(context, this) }
//    private lateinit var binding: ViewDataBinding


    val viewModel: SwipeViewModel by viewModels()
    private lateinit var binding: FragmentSwipeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate<FragmentSwipeBinding>(inflater,
            R.layout.fragment_swipe,container,false)

        Fresco.initialize(context)

        layoutManager = CardStackLayoutManager(context, this).apply {
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }

        binding.stackView.layoutManager = layoutManager
        binding.stackView.adapter = adapter

        setupCardStackView(binding.stackView)
        setupButton(binding.stackView)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { viewState ->
                    Log.d("ViewStateLog", viewState.toString())
                    renderViewState(viewState)
                }
            }
        }

        return binding.root
    }

    private fun renderViewState(viewState: SwipeViewModel.ViewState) {
        when (viewState) {
            is SwipeViewModel.ViewState.Loading -> {
                binding.stackView.isVisible = false
                binding.progressBar.isVisible = true
            }
            is SwipeViewModel.ViewState.Data -> {
                binding.stackView.isVisible = true
                (binding.stackView.adapter as ProfilesAdapter).apply {
                    setProfiles(viewState.userList)
                }
                binding.progressBar.isVisible = false
            }
        }
    }

//    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawers()
//        } else {
//            super.onBackPressed()
//        }
//    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        val psynaId = adapter.getProfiles().get(manager.topPosition - 1).id
        if (manager.topPosition == adapter.itemCount - 5) {
//            paginate()
        }
        if (direction == Direction.Left) {
            // Dislike
            viewModel.dislikePsyna(psynaId)
        }
        if (direction == Direction.Right) {
            // Like
            viewModel.likePsyna(psynaId)
        }

    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }
//
//    private fun setupNavigation() {
//        // Toolbar
//        val toolbar = findViewById<Toolbar>(com.yuyakaido.android.cardstackview.R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//        // DrawerLayout
//        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, com.yuyakaido.android.cardstackview.R.string.open_drawer, com.yuyakaido.android.cardstackview.R.string.close_drawer)
//        actionBarDrawerToggle.syncState()
//        drawerLayout.addDrawerListener(actionBarDrawerToggle)
//
//        // NavigationView
//        val navigationView = findViewById<NavigationView>(com.yuyakaido.android.cardstackview.R.id.navigation_view)
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                com.yuyakaido.android.cardstackview.R.id.reload -> reload()
//                com.yuyakaido.android.cardstackview.R.id.add_spot_to_first -> addFirst(1)
//                com.yuyakaido.android.cardstackview.R.id.add_spot_to_last -> addLast(1)
//                com.yuyakaido.android.cardstackview.R.id.remove_spot_from_first -> removeFirst(1)
//                com.yuyakaido.android.cardstackview.R.id.remove_spot_from_last -> removeLast(1)
//                com.yuyakaido.android.cardstackview.R.id.replace_first_spot -> replace()
//                com.yuyakaido.android.cardstackview.R.id.swap_first_for_last -> swap()
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//    }

    private fun setupCardStackView(cardStackView: CardStackView) {
        initialize(cardStackView)
    }

    private fun setupButton(cardStackView: CardStackView) {
        val skip = binding.skipButton
        skip.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        val rewind = binding.rewindButton
        rewind.setOnClickListener {
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(DecelerateInterpolator())
                .build()
            manager.setRewindAnimationSetting(setting)
            cardStackView.rewind()
        }

        val like = binding.likeButton
        like.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
    }

    private fun initialize(cardStackView: CardStackView) {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun paginate() {
        val old = adapter.getProfiles()
        val new = old.plus(createProfiles())
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun reload() {
        val old = adapter.getProfiles()
        val new = createProfiles()
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addFirst(size: Int) {
        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            for (i in 0 until size) {
                add(manager.topPosition, createProfile())
            }
        }
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addLast(size: Int) {
        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            addAll(List(size) { createProfile() })
        }
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeFirst(size: Int) {
        if (adapter.getProfiles().isEmpty()) {
            return
        }

        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(manager.topPosition)
            }
        }
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeLast(size: Int) {
        if (adapter.getProfiles().isEmpty()) {
            return
        }

        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(this.size - 1)
            }
        }
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun replace() {
        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            removeAt(manager.topPosition)
            add(manager.topPosition, createProfile())
        }
        adapter.setProfiles(new)
        adapter.notifyItemChanged(manager.topPosition)
    }

    private fun swap() {
        val old = adapter.getProfiles()
        val new = mutableListOf<Profile>().apply {
            addAll(old)
            val first = removeAt(manager.topPosition)
            val last = removeAt(this.size - 1)
            add(manager.topPosition, last)
            add(first)
        }
        val callback = ProfileDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setProfiles(new)
        result.dispatchUpdatesTo(adapter)
    }

    fun createProfile(): Profile {
        return Profile(
            34,
            100,
            1,
            "Alya",
            "https://sun9-49.userapi.com/impg/phAQReMA5qa6Z67a19uwvb39PKdu6kL-MuetrA/mTJQrWPdv9s.jpg?size=1080x1027&quality=96&sign=764698d9ba05155df1d408c068264c29&type=album"
        )
    }

    fun createProfiles(): List<Profile> {
        return listOf(
            Profile(
                    34,
                    100,
                    1,
                    "Alya",
                    "https://sun9-49.userapi.com/impg/phAQReMA5qa6Z67a19uwvb39PKdu6kL-MuetrA/mTJQrWPdv9s.jpg?size=1080x1027&quality=96&sign=764698d9ba05155df1d408c068264c29&type=album"
                ),
                Profile(
                    36,
                    4,
                    2,
                    "Sasha",
                    "https://sun9-85.userapi.com/impg/Q2se4IRckmyUSHghWQZfsR9DdaenD4GJn1lOyg/NMIQjWiAG_w.jpg?size=1440x2160&quality=95&sign=37db0e18d778d48ff42114f5e92058a9&type=album"
                )
        )
    }
}