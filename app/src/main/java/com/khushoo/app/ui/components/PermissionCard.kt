package com.khushoo.app.ui.components

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.khushoo.app.R
import com.khushoo.app.ui.theme.PearlBlueAccent
import com.khushoo.app.ui.theme.PearlNavyDark
import com.khushoo.app.ui.theme.PureWhite

@Composable
fun PermissionCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasSmsPerm by remember { mutableStateOf(checkPerm(context, Manifest.permission.SEND_SMS)) }
    var hasPhonePerm by remember { mutableStateOf(checkPerm(context, Manifest.permission.READ_PHONE_STATE)) }
    var hasCallLogPerm by remember { mutableStateOf(checkPerm(context, Manifest.permission.READ_CALL_LOG)) }
    var hasNotifPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPerm(context, Manifest.permission.POST_NOTIFICATIONS)
            } else true
        )
    }

    var isCallScreenerHeld by remember {
        mutableStateOf(checkCallScreeningRole(context))
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasSmsPerm = results[Manifest.permission.SEND_SMS] ?: hasSmsPerm
        hasPhonePerm = results[Manifest.permission.READ_PHONE_STATE] ?: hasPhonePerm
        hasCallLogPerm = results[Manifest.permission.READ_CALL_LOG] ?: hasCallLogPerm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotifPerm = results[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotifPerm
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isCallScreenerHeld = checkCallScreeningRole(context)
    }

    val allPermsGranted = hasSmsPerm && hasPhonePerm && hasCallLogPerm && hasNotifPerm && isCallScreenerHeld

    AnimatedVisibility(
        visible = !allPermsGranted,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            color = Color(0xFFFFFBEB),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.permissions_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = stringResource(R.string.permissions_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!hasSmsPerm || !hasPhonePerm || !hasCallLogPerm || !hasNotifPerm) {
                        Button(
                            onClick = {
                                val perms = mutableListOf(
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_CALL_LOG
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                requestPermissionsLauncher.launch(perms.toTypedArray())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PearlNavyDark,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_grant_permissions),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!isCallScreenerHeld && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        OutlinedButton(
                            onClick = {
                                val roleManager = context.getSystemService(RoleManager::class.java)
                                if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
                                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                    roleLauncher.launch(intent)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_set_call_screener),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PearlNavyDark
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun checkPerm(context: Context, perm: String): Boolean {
    return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
}

private fun checkCallScreeningRole(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
    } else {
        true
    }
}
