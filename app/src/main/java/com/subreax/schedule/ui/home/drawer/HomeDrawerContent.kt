package com.subreax.schedule.ui.home.drawer

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.schedule.data.model.ScheduleOwner
import com.subreax.schedule.ui.theme.ScheduleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerContent(
    currentScheduleOwner: ScheduleOwner,
    scheduleOwners: List<ScheduleOwner>,
    onScheduleOwnerClicked: (ScheduleOwner) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(windowInsets = WindowInsets(0.dp), modifier = modifier) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Расписание ТулГУ",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Группы",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyMedium
        )

        scheduleOwners.forEach {
            SelectableDrawerItem(
                selected = currentScheduleOwner.id == it.id,
                onClick = { onScheduleOwnerClicked(it) },
            ) {
                Text(
                    text = it.id,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeDrawerContentPreview() {
    val scheduleOwners = listOf(ScheduleOwner("220431"), ScheduleOwner("620221"))

    ScheduleTheme {
        HomeDrawerContent(
            currentScheduleOwner = scheduleOwners.first(),
            scheduleOwners = scheduleOwners,
            onScheduleOwnerClicked = {},
            modifier = Modifier.fillMaxHeight()
        )
    }
}
