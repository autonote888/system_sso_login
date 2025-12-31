package com.example.login1pro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- TEMA WARNA PREMIUM ---
val JituNavy = Color(0xFF00235B)
val JituBlueSoft = Color(0xFF0052CC)
val JituGold = Color(0xFFD4AF37)
val PureWhite = Color(0xFFFFFFFF)
val SoftGray = Color(0xFFF7F9FC)
val InputGray = Color(0xFFF1F3F5)
val TextGray = Color(0xFF6C757D)
val ErrorRed = Color(0xFFE53935)

enum class Screen { Splash, Login, LoginOptions, Register, ForgotPass, Dashboard }

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AppNavigation() } }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var isEnglish by remember { mutableStateOf(false) }

    AnimatedContent(targetState = currentScreen, label = "NavAnim") { screen ->
        when (screen) {
            Screen.Splash -> JituSplash { currentScreen = Screen.Login }
            Screen.Login -> JituLogin(isEnglish, { isEnglish = !isEnglish }, { currentScreen = Screen.LoginOptions }, { currentScreen = Screen.Register }, { currentScreen = Screen.Dashboard })
            Screen.LoginOptions -> JituLoginOptions(isEnglish, { currentScreen = Screen.Login }, { currentScreen = Screen.ForgotPass }, { currentScreen = Screen.Dashboard })
            Screen.Register -> JituRegister(isEnglish) { currentScreen = Screen.Login }
            Screen.ForgotPass -> JituForgotPass(isEnglish) { currentScreen = Screen.LoginOptions }
            Screen.Dashboard -> JituDashboard(isEnglish) { currentScreen = Screen.Login }
        }
    }
}

@Composable
fun JituSplash(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(2000); onTimeout() }
    Box(Modifier.fillMaxSize().background(JituNavy), contentAlignment = Alignment.Center) {
        Image(painterResource(id = R.drawable.keuangan), null, Modifier.size(120.dp))
    }
}

@Composable
fun JituLogin(isEnglish: Boolean, onLangToggle: () -> Unit, onGoToOptions: () -> Unit, onGoToRegister: () -> Unit, onLoginSuccess: () -> Unit) {
    var nrpNip by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nrpError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(PureWhite)) {
        TextButton(onClick = onLangToggle, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp)) {
            Text(text = if (isEnglish) "ID | EN" else "EN | ID", color = JituNavy, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.4f))
            BrandingHeader(isEnglish) // Meneruskan status bahasa ke slogan
            Spacer(Modifier.weight(0.2f))

            Surface(
                modifier = Modifier.fillMaxWidth().graphicsLayer(translationX = shakeOffset.value).shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color.LightGray),
                shape = RoundedCornerShape(32.dp), color = PureWhite
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().background(SoftGray).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text(text = if (isEnglish) "SECURE ACCESS" else "MASUK APLIKASI", fontSize = 10.sp, fontWeight = FontWeight.Black, color = JituNavy.copy(0.4f), letterSpacing = 3.sp)
                    }
                    Column(Modifier.padding(24.dp)) {
                        JituInput(
                            value = nrpNip,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 18) {
                                    nrpNip = input
                                    nrpError = null
                                }
                            },
                            label = "NRP / NIP",
                            isNumber = true,
                            errorText = nrpError
                        )
                        Spacer(Modifier.height(16.dp))
                        JituInput(password, { password = it }, "Password", isPass = true)
                        Spacer(Modifier.height(32.dp))
                        MorphingButton(text = if(isEnglish) "SIGN IN" else "MASUK", isLoading = isLoading, onClick = {
                            if (nrpNip.length != 6 && nrpNip.length != 18) {
                                nrpError = if(isEnglish) "Invalid digits" else "Wajib 6 atau 18 digit"
                                scope.launch { repeat(5) { shakeOffset.animateTo(15f, tween(40)); shakeOffset.animateTo(-15f, tween(40)) }; shakeOffset.animateTo(0f) }
                            } else { scope.launch { isLoading = true; delay(2000); onLoginSuccess(); isLoading = false } }
                        })
                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onGoToOptions() }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if(isEnglish) "Other options" else "Opsi masuk lainnya", color = JituBlueSoft, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = JituBlueSoft)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Row {
                Text(text = if(isEnglish) "New here? " else "Belum punya akun? ", color = TextGray, fontSize = 14.sp)
                Text(text = if(isEnglish) "Create Account" else "Daftar Sekarang", color = JituBlueSoft, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable { onGoToRegister() } )
            }
            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
fun JituRegister(isEnglish: Boolean, onBack: () -> Unit) {
    var nrpNip by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var norek by remember { mutableStateOf("") }
    var nrpError by remember { mutableStateOf<String?>(null) }
    var norekError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(PureWhite)) {
        IconButton(onClick = onBack, Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 45.dp).background(SoftGray, CircleShape).size(36.dp)) { Icon(Icons.Rounded.ArrowBackIosNew, null, tint = JituNavy, modifier = Modifier.size(16.dp)) }

        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(70.dp)); BrandingHeader(isEnglish); Spacer(Modifier.height(28.dp))

            Surface(modifier = Modifier.fillMaxWidth().graphicsLayer(translationX = shakeOffset.value).shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color.LightGray), shape = RoundedCornerShape(32.dp), color = PureWhite) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().background(SoftGray).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text(text = if (isEnglish) "NEW REGISTRATION" else "PENDAFTAR BARU", fontSize = 10.sp, fontWeight = FontWeight.Black, color = JituNavy.copy(0.4f), letterSpacing = 3.sp)
                    }
                    Column(Modifier.padding(24.dp)) {
                        JituInput(nrpNip, { if (it.all { c -> c.isDigit() } && it.length <= 18) { nrpNip = it; nrpError = null } }, "NRP / NIP", isNumber = true, errorText = nrpError)
                        Spacer(Modifier.height(12.dp))
                        JituInput(email, { email = it }, "Email")
                        Spacer(Modifier.height(12.dp))
                        JituInput(password, { password = it }, "Password", isPass = true)
                        Spacer(Modifier.height(12.dp))
                        JituInput(norek, { if (it.all { c -> c.isDigit() } && it.length <= 5) { norek = it; norekError = null } }, "Nomor Rekening", isNumber = true, errorText = norekError, supportingText = if(isEnglish) "*Last 5 digits" else "*5 digit terakhir")
                        Spacer(Modifier.height(28.dp))
                        MorphingButton(text = if(isEnglish) "REGISTER" else "DAFTAR SEKARANG", isLoading = isLoading, onClick = {
                            if ((nrpNip.length != 6 && nrpNip.length != 18) || norek.length != 5) {
                                if (nrpNip.length != 6 && nrpNip.length != 18) nrpError = if(isEnglish) "6 or 18 digits" else "Wajib 6/18 digit"
                                if (norek.length != 5) norekError = if(isEnglish) "5 digits" else "Wajib 5 digit"
                                scope.launch { repeat(5) { shakeOffset.animateTo(15f, tween(40)); shakeOffset.animateTo(-15f, tween(40)) }; shakeOffset.animateTo(0f) }
                            } else { scope.launch { isLoading = true; delay(2000); onBack(); isLoading = false } }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun BrandingHeader(isEnglish: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(16.dp), color = PureWhite, shadowElevation = 4.dp) {
            Box(contentAlignment = Alignment.Center) { Image(painterResource(id = R.drawable.jitu), null, modifier = Modifier.size(42.dp)) }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Text("JITU", fontSize = 22.sp, fontWeight = FontWeight.Black, color = JituNavy)
            Spacer(Modifier.width(6.dp))
            Text("PRESISI", fontSize = 22.sp, fontWeight = FontWeight.Light, color = JituGold)
        }
        // Slogan otomatis berubah bahasa
        Text(
            text = if (isEnglish) "Salary and Performance Allowance" else "Gaji dan Tunjangan Kinerja",
            fontSize = 11.sp,
            color = TextGray.copy(0.7f),
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun MorphingButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    val widthPercent by animateFloatAsState(targetValue = if (isLoading) 0.2f else 1f, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "")
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(
            onClick = { if (!isLoading) onClick() },
            modifier = Modifier.fillMaxWidth(widthPercent).height(56.dp),
            shape = if (isLoading) CircleShape else RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(JituNavy)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PureWhite, strokeWidth = 3.dp)
            else Text(text = text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun JituInput(value: String, onValueChange: (String) -> Unit, label: String, isPass: Boolean = false, isNumber: Boolean = false, errorText: String? = null, supportingText: String? = null) {
    var isFocused by remember { mutableStateOf(false) }
    var isVis by remember { mutableStateOf(false) }
    Column {
        TextField(
            value = value, onValueChange = onValueChange, placeholder = { Text(text = label, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused }.shadow(if (isFocused) 8.dp else 0.dp, RoundedCornerShape(20.dp)).border(if (isFocused || errorText != null) 2.dp else 1.dp, if (errorText != null) ErrorRed else if (isFocused) JituBlueSoft else Color.LightGray.copy(0.4f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
            trailingIcon = { if (isPass) IconButton(onClick = { isVis = !isVis }) { Icon(if (isVis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } },
            visualTransformation = if (isPass && !isVis) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(focusedContainerColor = PureWhite, unfocusedContainerColor = InputGray, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
        )
        if (errorText != null) Text(text = errorText, fontSize = 11.sp, color = ErrorRed, modifier = Modifier.padding(start = 20.dp, top = 4.dp), fontWeight = FontWeight.Bold)
        else if (supportingText != null) Text(text = supportingText, fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(start = 20.dp, top = 4.dp))
    }
}

@Composable
fun JituLoginOptions(isEnglish: Boolean, onBack: () -> Unit, onGoToForgot: () -> Unit, onLoginSuccess: () -> Unit) {
    Box(Modifier.fillMaxSize().background(SoftGray)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
            Spacer(Modifier.height(50.dp)); IconButton(onClick = onBack, Modifier.background(PureWhite, CircleShape)) { Icon(Icons.Rounded.Close, null) }
            Spacer(Modifier.height(32.dp)); Text(if(isEnglish) "Sign-in Methods" else "Metode Masuk", fontSize = 26.sp, fontWeight = FontWeight.Black, color = JituNavy)
            Spacer(Modifier.height(32.dp))
            ElegantOptionCard(if(isEnglish) "Biometric" else "Biometrik", Icons.Rounded.Fingerprint, JituBlueSoft)
            Spacer(Modifier.height(12.dp)); ElegantOptionCard("Google Account", Icons.Rounded.AccountCircle, Color(0xFFDB4437))
            Spacer(Modifier.height(12.dp)); ElegantOptionCard(if(isEnglish) "Forgot Password" else "Lupa Password", Icons.Rounded.VpnKey, JituGold, onGoToForgot)
        }
    }
}

@Composable
fun ElegantOptionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(32.dp), color = PureWhite, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), color) }
            Spacer(Modifier.width(16.dp)); Text(text = title, fontWeight = FontWeight.Bold, color = JituNavy, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ArrowForwardIos, null, Modifier.size(12.dp), Color.LightGray)
        }
    }
}

@Composable
fun JituForgotPass(isEnglish: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if(isEnglish) "OTP Verification" else "Halaman OTP") }
}

@Composable
fun JituDashboard(isEnglish: Boolean, onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Button(onLogout) { Text(if(isEnglish) "Logout" else "Keluar") } }
}