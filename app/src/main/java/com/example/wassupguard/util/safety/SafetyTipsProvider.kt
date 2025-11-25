package com.example.wassupguard.util.safety

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

data class SafetyTip(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
    val actionUrl: String? = null
)

class SafetyTipsProvider(
    private val tips: List<SafetyTip> = defaultTips()
) {

    fun tipsOfTheDay(count: Int = 3): List<SafetyTip> {
        if (tips.isEmpty()) return emptyList()
        val indexSeed = LocalDate.now(ZoneId.systemDefault()).dayOfYear
        val rotated = tips.drop(indexSeed % tips.size) + tips.take(indexSeed % tips.size)
        return rotated.take(count)
    }

    companion object {
        private fun defaultTips(): List<SafetyTip> = listOf(
            SafetyTip(
                title = "Verify media senders",
                description = "Malware often arrives from spoofed business accounts. Long-press the chat header and verify the phone number or business badge before downloading attachments."
            ),
            SafetyTip(
                title = "Disable auto-download",
                description = "Keep WhatsApp's auto-download off for Documents/Photos so malicious payloads can't land silently. Only download files you expect."
            ),
            SafetyTip(
                title = "Keep Google Play Protect on",
                description = "Play Protect adds an extra scan layer when you sideload APKs shared via WhatsApp. Check Settings ▸ Security ▸ Play Protect."
            ),
            SafetyTip(
                title = "Archive suspicious chats",
                description = "If a contact keeps spamming shortened links or random OTP requests, archive or block them. This prevents drive-by taps."
            ),
            SafetyTip(
                title = "Share minimal permissions",
                description = "When opening shared APKs, never grant Accessibility or SMS permissions unless you initiated the install from a trusted vendor."
            )
        )
    }
}

