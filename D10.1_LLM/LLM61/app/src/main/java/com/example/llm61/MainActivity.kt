package com.example.llm61

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.llm61.ui.theme.LLM61Theme
import com.example.llm61.data.local.AppDatabase
import com.example.llm61.data.local.QuizAttemptEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.stripe.android.PaymentConfiguration

class MainActivity : ComponentActivity() {
//    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // TEMP — Room smoke test. Delete this whole launch block after verifying.
//        GlobalScope.launch {
//            val db = AppDatabase.getInstance(applicationContext)
//            db.quizAttemptDao().insertAttempt(
//                QuizAttemptEntity(
//                    topic = "Test Topic",
//                    timestamp = System.currentTimeMillis(),
//                    totalQuestions = 3,
//                    correctCount = 2,
//                    incorrectCount = 1
//                )
//            )
//        }
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        setContent {
            LLM61Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}