package com.nyumbahub.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A3C5E)),
        contentAlignment = Alignment.Center
    ) {
        // Centered image with natural shape — not stretched
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .aspectRatio(0.75f)
                .scale(scale)
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_for_opening_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Subtle overlay
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )
        }

        // App name at top
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ELMAZ", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, letterSpacing = 4.sp)
            Text("Find your perfect home in Rwanda",
                fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        }

        // Button at bottom
        Button(
            onClick = onFinished,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.75f)
                .height(54.dp)
                .offset(y = (-48).dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE87722))
        ) {
            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
