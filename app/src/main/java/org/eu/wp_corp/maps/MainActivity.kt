package org.eu.wp_corp.maps

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.balysv.materialmenu.MaterialMenuDrawable
import com.google.android.material.navigation.NavigationView
import org.eu.wp_corp.maps.hdtgo.HDTGoActivity
import org.eu.wp_corp.maps.view.CardPagerAdapter
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import ru.semper_viventem.backdrop.BackdropBehavior


class MainActivity : AppCompatActivity() {
    private fun <T : CoordinatorLayout.Behavior<*>> View.findBehavior(): T = layoutParams.run {
        if (this !is CoordinatorLayout.LayoutParams) throw IllegalArgumentException("View's layout params should be CoordinatorLayout.LayoutParams")

        (layoutParams as CoordinatorLayout.LayoutParams).behavior as? T
            ?: throw IllegalArgumentException("Layout's behavior is not current behavior")
    }

    private val materialMenu: MaterialMenuDrawable by lazy {
        MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
    }

    private lateinit var backdropBehavior: BackdropBehavior
    private lateinit var map: MapView

    private val adapter = CardPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        val frontLayout = findViewById<View>(R.id.frontLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val pager = findViewById<ViewPager2>(R.id.pager)

        toolbar.navigationIcon = materialMenu

        toolbar.inflateMenu(R.menu.toolbar)
        toolbar.menu[0].run {
            (actionView as ImageButton).setOnClickListener {
                onOptionsItemSelected(this)
            }
        }

        backdropBehavior = frontLayout.findBehavior()
        with(backdropBehavior) {
            attachBackLayout(R.id.backLayout)
        }

        backdropBehavior.addOnDropListener(object : BackdropBehavior.OnDropListener {
            override fun onDrop(dropState: BackdropBehavior.DropState, fromUser: Boolean) {
                toolbar.navigationIcon = materialMenu
                materialMenu.animateIconState(
                    when (dropState) {
                        BackdropBehavior.DropState.CLOSE -> MaterialMenuDrawable.IconState.BURGER
                        BackdropBehavior.DropState.OPEN -> MaterialMenuDrawable.IconState.X
                    }
                )

                val pagerAlpha = when (dropState) {
                    BackdropBehavior.DropState.CLOSE -> 1F
                    BackdropBehavior.DropState.OPEN -> 0F
                }

                pager.animate().alpha(pagerAlpha).setDuration(300).start()
            }
        })

        toolbar.setTitle(R.string.app_name)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.main_map -> {
                    toolbar.title = it.title
                }
                R.id.main_pedibus -> {
                    toolbar.title = it.title
                }
                R.id.main_events -> {
                    toolbar.title = it.title
                }
                R.id.main_hdtgo -> {
                    startActivity(Intent(this, HDTGoActivity::class.java))
                }
            }

            backdropBehavior.close()
            true
        }

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.controller.setZoom(14.0)
        map.controller.setCenter(GeoPoint(45.1667, 5.7167));

        run {
            pager.adapter = adapter
            pager.offscreenPageLimit = 1

            val pagePeek = resources.getDimension(R.dimen.small_gap)
            val pageMargin = resources.getDimension(R.dimen.normal_gap)
            val pageTranslation = pagePeek + pageMargin

            pager.setPageTransformer { page: View, position: Float ->
                page.translationX = -pageTranslation * position
            }

            pager.addItemDecoration(object : RecyclerView.ItemDecoration() {
                private val pageMargin = pageMargin.toInt()

                override fun getItemOffsets(
                    outRect: Rect, _view: View, _parent: RecyclerView, _state: RecyclerView.State
                ) {
                    outRect.right = this.pageMargin
                    outRect.left = this.pageMargin
                }
            })
        }

        backend.queueSession("ws://192.168.0.13:8765/", Unit)

        adapter.init(this)
        adapter.activeCollectionName = "pedibus"

        // val currentItem = savedInstanceState?.getInt(ARG_LAST_MENU_ITEM) ?: DEFAULT_ITEM
        // navigationView.setCheckedItem(currentItem)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toolbar_avatar -> {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    item.actionView,
                    "avatar"
                )
                startActivity(Intent(this, OptionsActivity::class.java), options.toBundle())
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onBackPressed() {
        if (!backdropBehavior.close()) {
            finish()
        }
    }
}
