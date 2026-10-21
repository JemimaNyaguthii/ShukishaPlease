package com.jem.shukishaplease.core.ota

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.CoffeeBrown
import com.jem.shukishaplease.core.ui.theme.DeepEspresso
import com.jem.shukishaplease.core.ui.theme.PureWhite
import com.jem.shukishaplease.core.ui.theme.WarmMutedBrown

@Composable
fun OtaUpdateDialog(
    updateInfo: OtaUpdateInfo,
    onDismiss: () -> Unit,
    onUpdateNow: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = { Text(text = "OTA Update Available! (${updateInfo.versionName})", fontWeight = FontWeight.Bold, color = DeepEspresso) },
        text = {
            Column {
                Text(text = updateInfo.releaseNotes, fontSize = 13.sp, color = WarmMutedBrown)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Tap 'Update Now' to download and install Over-The-Air.", fontSize = 12.sp, color = DeepEspresso, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdateNow(updateInfo.apkUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown, contentColor = PureWhite)
            ) {
                Text("Update Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = WarmMutedBrown)
            }
        }
    )
}
