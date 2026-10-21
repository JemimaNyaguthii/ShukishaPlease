package com.jem.shukishaplease.features.trip

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jem.shukishaplease.core.ui.theme.*
import kotlinx.coroutines.delay

class IncomingCallAlertActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 14+ Full-Screen Intent Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.canUseFullScreenIntent()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (ignored: Exception) {
                }
            }
        }

        // Show over lockscreen & turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val destName = intent.getStringExtra("EXTRA_DEST_NAME") ?: "Nyeri Town"
        val distText = intent.getStringExtra("EXTRA_DIST_TEXT") ?: "400 m"

        acquireWakeLock()
        startAlertSoundAndVibration()

        setContent {
            ShukishaPleaseTheme {
                // Prevent accidental back press dismissal
                BackHandler(enabled = true) {}

                AlarmScreen(
                    destName = destName,
                    distanceText = distText,
                    onGetOff = {
                        stopAlertSoundAndVibration()
                        releaseWakeLock()
                        finish()
                    },
                    onRemindMe = {
                        stopAlertSoundAndVibration()
                        releaseWakeLock()
                        finish()
                    }
                )
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "ShukishaPlease:ArrivalAlarmWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
            Log.d("IncomingCallAlert", "Acquired wake lock for arrival alarm.")
        } catch (e: Exception) {
            Log.e("IncomingCallAlert", "Failed to acquire wake lock: ${e.localizedMessage}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d("IncomingCallAlert", "Released wake lock.")
            }
        } catch (ignored: Exception) {
        }
    }

    private fun startAlertSoundAndVibration() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 800, 300, 800, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("IncomingCallAlert", "Error playing alarm audio: ${e.localizedMessage}")
        }
    }

    private fun stopAlertSoundAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlertSoundAndVibration()
        releaseWakeLock()
    }
}

// ==========================================
// 1. STAGE BADGE COMPONENT
// ==========================================
@Composable
fun StageBadge() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = NeoBlack,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Text(
            text = "YOUR STAGE IS HERE",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = NeoYellow,
            letterSpacing = 1.5.sp
        )
    }
}

// ==========================================
// 2. SHUKISHA MASCOT VECTOR COMPONENT
// ==========================================
@Composable
fun ShukishaMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 6f)

        // Arms (Black lines with purple ball hands)
        val leftArmPath = Path().apply {
            moveTo(w * 0.28f, h * 0.42f)
            lineTo(w * 0.12f, h * 0.22f)
        }
        drawPath(leftArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPurple, radius = 14f, center = Offset(w * 0.12f, h * 0.22f))
        drawCircle(color = NeoBlack, radius = 14f, center = Offset(w * 0.12f, h * 0.22f), style = stroke)

        val rightArmPath = Path().apply {
            moveTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.88f, h * 0.22f)
        }
        drawPath(rightArmPath, color = NeoBlack, style = stroke)
        drawCircle(color = NeoPurple, radius = 14f, center = Offset(w * 0.88f, h * 0.22f))
        drawCircle(color = NeoBlack, radius = 14f, center = Offset(w * 0.88f, h * 0.22f), style = stroke)

        // Short Legs & Purple Feet
        drawLine(NeoBlack, Offset(w * 0.40f, h * 0.82f), Offset(w * 0.40f, h * 0.90f), strokeWidth = 6f)
        drawCircle(color = NeoPurple, radius = 12f, center = Offset(w * 0.40f, h * 0.92f))
        drawCircle(color = NeoBlack, radius = 12f, center = Offset(w * 0.40f, h * 0.92f), style = stroke)

        drawLine(NeoBlack, Offset(w * 0.60f, h * 0.82f), Offset(w * 0.60f, h * 0.90f), strokeWidth = 6f)
        drawCircle(color = NeoPurple, radius = 12f, center = Offset(w * 0.60f, h * 0.92f))
        drawCircle(color = NeoBlack, radius = 12f, center = Offset(w * 0.60f, h * 0.92f), style = stroke)

        // Main Yellow Body (Rounded Rect)
        val bodyRect = RoundRect(
            rect = Rect(left = w * 0.25f, top = h * 0.25f, right = w * 0.75f, bottom = h * 0.82f),
            cornerRadius = CornerRadius(28f, 28f)
        )
        val bodyPath = Path().apply { addRoundRect(bodyRect) }
        drawPath(bodyPath, color = NeoYellow)
        drawPath(bodyPath, color = NeoBlack, style = stroke)

        // Teal Cap with Brim pointing right
        val capPath = Path().apply {
            moveTo(w * 0.23f, h * 0.25f)
            cubicTo(w * 0.23f, h * 0.05f, w * 0.77f, h * 0.05f, w * 0.77f, h * 0.25f)
            close()
        }
        drawPath(capPath, color = NeoTeal)
        drawPath(capPath, color = NeoBlack, style = stroke)

        val brimPath = Path().apply {
            moveTo(w * 0.60f, h * 0.25f)
            lineTo(w * 0.96f, h * 0.23f)
            lineTo(w * 0.90f, h * 0.29f)
            lineTo(w * 0.60f, h * 0.29f)
            close()
        }
        drawPath(brimPath, color = NeoTeal)
        drawPath(brimPath, color = NeoBlack, style = stroke)

        // Angry Eyebrows
        drawLine(NeoBlack, Offset(w * 0.35f, h * 0.35f), Offset(w * 0.46f, h * 0.41f), strokeWidth = 6f)
        drawLine(NeoBlack, Offset(w * 0.65f, h * 0.35f), Offset(w * 0.54f, h * 0.41f), strokeWidth = 6f)

        // Big White Eyes & Black Pupils
        val eyeLeft = Offset(w * 0.41f, h * 0.48f)
        val eyeRight = Offset(w * 0.59f, h * 0.48f)

        drawCircle(color = PureWhite, radius = 16f, center = eyeLeft)
        drawCircle(color = NeoBlack, radius = 16f, center = eyeLeft, style = stroke)
        drawCircle(color = NeoBlack, radius = 7f, center = Offset(w * 0.41f, h * 0.48f))

        drawCircle(color = PureWhite, radius = 16f, center = eyeRight)
        drawCircle(color = NeoBlack, radius = 16f, center = eyeRight, style = stroke)
        drawCircle(color = NeoBlack, radius = 7f, center = Offset(w * 0.59f, h * 0.48f))

        // Wide Open Surprised Mouth with Pink Tongue
        drawOval(
            color = NeoBlack,
            topLeft = Offset(w * 0.43f, h * 0.58f),
            size = Size(w * 0.14f, h * 0.16f)
        )
        drawOval(
            color = NeoPink,
            topLeft = Offset(w * 0.45f, h * 0.66f),
            size = Size(w * 0.10f, h * 0.08f)
        )

        // Small Blue Sweat Drop on right side
        val sweatPath = Path().apply {
            moveTo(w * 0.78f, h * 0.36f)
            cubicTo(w * 0.82f, h * 0.40f, w * 0.82f, h * 0.45f, w * 0.78f, h * 0.47f)
            cubicTo(w * 0.74f, h * 0.45f, w * 0.74f, h * 0.40f, w * 0.78f, h * 0.36f)
            close()
        }
        drawPath(sweatPath, color = NeoSkyBlue)
        drawPath(sweatPath, color = NeoBlack, style = Stroke(width = 3f))
    }
}

// ==========================================
// 3. ALARM HERO COMPONENT
// ==========================================
@Composable
fun AlarmHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val bounceY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier.size(310.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dotted Yellow Ring Canvas with Corner Rays
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPt = Offset(size.width / 2f, size.height / 2f)

            // Dotted rotating ring
            rotate(rotationAngle, centerPt) {
                drawCircle(
                    color = NeoYellow,
                    radius = 145f,
                    center = centerPt,
                    style = Stroke(
                        width = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f)
                    )
                )
            }

            // Four Corner Alarm Rays
            drawLine(NeoBlack, Offset(size.width * 0.12f, size.height * 0.12f), Offset(size.width * 0.18f, size.height * 0.18f), strokeWidth = 8f, cap = StrokeCap.Round)
            drawLine(NeoBlack, Offset(size.width * 0.88f, size.height * 0.12f), Offset(size.width * 0.82f, size.height * 0.18f), strokeWidth = 8f, cap = StrokeCap.Round)
            drawLine(NeoBlack, Offset(size.width * 0.12f, size.height * 0.88f), Offset(size.width * 0.18f, size.height * 0.82f), strokeWidth = 8f, cap = StrokeCap.Round)
            drawLine(NeoBlack, Offset(size.width * 0.88f, size.height * 0.88f), Offset(size.width * 0.82f, size.height * 0.82f), strokeWidth = 8f, cap = StrokeCap.Round)
        }

        // Pink Center Circle (~230dp)
        Surface(
            modifier = Modifier.size(230.dp),
            shape = CircleShape,
            color = NeoPink,
            border = BorderStroke(3.dp, NeoBlack)
        ) {}

        // Mascot Inside Circle
        ShukishaMascot(
            modifier = Modifier
                .size(190.dp)
                .offset(y = bounceY.dp - 12.dp)
        )
    }
}

// ==========================================
// 4. HOLD-TO-CONFIRM BUTTON COMPONENT (~1 sec hold)
// ==========================================
@Composable
fun HoldToConfirmButton(
    onConfirmed: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val startTime = System.currentTimeMillis()
            while (isPressed && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / 1000f).coerceIn(0f, 1f)
                if (holdProgress >= 1f) {
                    onConfirmed()
                    break
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(36.dp),
        color = NeoBlack,
        border = BorderStroke(2.dp, NeoBlack)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Hold progress sweep overlay
            if (holdProgress > 0f) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = holdProgress),
                    shape = RoundedCornerShape(36.dp),
                    color = PureWhite.copy(alpha = 0.25f)
                ) {}
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yellow Circle with Check Icon
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = NeoYellow,
                    border = BorderStroke(2.dp, NeoBlack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Check", tint = NeoBlack, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Hold — I'm awake",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
            }
        }
    }
}

// ==========================================
// MAIN ALARM SCREEN COMPOSABLE
// ==========================================
@Composable
fun AlarmScreen(
    destName: String = "Nyeri Town",
    distanceText: String = "400 m",
    onGetOff: () -> Unit,
    onRemindMe: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NeoCoral
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Badge
            StageBadge()

            // 2. Hero Illustration Area (~300dp)
            AlarmHero()

            // 3. Headline Group
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-20).dp)
            ) {
                Text(
                    text = "SHUKISHA!",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$destName in $distanceText",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Grab your bag and tell the driver \"Shukisha hapa!\"",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoBlack,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Hold-To-Confirm Button & Secondary Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HoldToConfirmButton(onConfirmed = onGetOff)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ride on, I'll alight later",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onRemindMe() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    ShukishaPleaseTheme {
        AlarmScreen(
            destName = "Nyeri Town",
            distanceText = "400 m",
            onGetOff = {},
            onRemindMe = {}
        )
    }
}
