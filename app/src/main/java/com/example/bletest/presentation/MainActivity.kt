package com.example.bletest.presentation

import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.bletest.R
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.ui.theme.BLETestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLETestTheme {
                CompositionLocalProvider(
                    LocalBluetoothIconIDs provides createBtIconsList(),
                    LocalSizes provides Sizes()
                ) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigationHost(
                            navController = rememberNavController(),
                            startDestination = NavigationItem.Devices.route
                        )
                    }
                }
            }
        }
    }
}

//create random list of devices images(provided randomly for models by view model)
@Composable
fun createBtIconsList() : List<Int> {
    val typedIcons: TypedArray = LocalContext.current.resources.obtainTypedArray(R.array.bluetooth_icons)
    val ids = typedIcons.toResourceIDs()
    typedIcons.recycle()
    return ids
}

fun TypedArray.toResourceIDs() : List<Int> {
    return ArrayList<Int>().apply {
        for (i in 0..< this@toResourceIDs.length()) {
            add(this@toResourceIDs.getResourceId(i, 0))
        }
    }
}

val LocalBluetoothIconIDs = compositionLocalOf { listOf<Int>() }

data class Sizes(
    val listItemIconSize : Dp = 60.dp,
    val paddingMedium : Dp = 16.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingTiny: Dp = 4.dp,
    val paddingXTiny: Dp = 2.dp,
)
val LocalSizes = compositionLocalOf { Sizes() }

