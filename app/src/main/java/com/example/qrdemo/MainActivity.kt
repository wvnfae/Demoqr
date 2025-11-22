package com.example.qrdemo

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QRScreen()
                }
            }
        }
    }
}

@Composable
fun QRScreen() {
    // Date/time string (system time), cập nhật mỗi giây
    var dateTime by remember { mutableStateOf(nowString()) }

    // Counter: tăng +1 mỗi giây (bắt đầu từ 0)
    var counter by remember { mutableStateOf(0) }

    // LaunchedEffect để cập nhật dateTime và counter mỗi 1s
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            dateTime = nowString()
            counter += 1
        }
    }

    // InfiniteTransition để tạo hiệu ứng fade (mờ dần) cho chữ "Effective"
    val infiniteTransition = rememberInfiniteTransition()
    val fadeAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val screenWidth = LocalConfiguration.current.screenWidthDp

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .padding(12.dp)
    ) {
        // Name at top-right
        Text(
            text = "Nguyễn Trung",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Main centered content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // White card with QR
            Box(
                modifier = Modifier
                    .width((screenWidth * 0.8).dp)
                    .aspectRatio(0.7f)
                    .background(color = androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val qrContent = "https://example.com/attendance?id=V22120120&user=NguyenTrung&ts=${System.currentTimeMillis()}"
                    val qrBitmap = remember(qrContent) { generateQRCode(qrContent, 600, 600) }

                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(220.dp)
                        )
                    } else {
                        Text("Không thể tạo QR", color = androidx.compose.ui.graphics.Color.Red)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Refresh QR code", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // "Effective" text - fade in/out bằng fadeAlpha
                    val greenColor = androidx.compose.ui.graphics.Color(0xFF00C853)
                    Text(
                        text = "Effective",
                        color = greenColor,
                        fontSize = 16.sp,
                        modifier = Modifier.alpha(fadeAlpha),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hiển thị cả ngày giờ và counter dưới cùng
            Text(
                text = dateTime,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = counter.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Small instruction text left-bottom in Chinese (mô phỏng ảnh)
        Text(
            text = "尊敬的员工您好，您已进入企业场所，请注意出入登记与手机拍照。",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            fontSize = 12.sp
        )
    }
}

// Helper: format current time
fun nowString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

// Generate QR bitmap using ZXing
fun generateQRCode(contents: String, width: Int, height: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, null)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
