package com.example.brokskeeping.Functionality

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.OtherFunctionality
import com.example.brokskeeping.R
import java.util.Locale

object Reused_functions {

    fun showSettingsMenu(context: Context, anchorView: View, db: DatabaseHelper) {
        val popup = PopupMenu(context, anchorView)
        popup.menu.add(context.getString(R.string.yearly_reset))
        popup.menu.add(context.getString(R.string.language))

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                context.getString(R.string.yearly_reset) -> {
                    AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.confirm_reset))
                        .setMessage(context.getString(R.string.are_you_sure_you_want_to_perform_a_yearly_reset_this_action_cannot_be_undone))
                        .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            OtherFunctionality.yearlyReset(db)
                        }
                        .setNegativeButton(context.getString(R.string.no), null)
                        .show()
                    true
                }
                context.getString(R.string.language) -> {
                    showLanguageSelectionDialog(context)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showLanguageSelectionDialog(context: Context) {
        val languages = arrayOf("English", "Czech")
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.select_language))
            .setItems(languages) { _, which ->
                val localeCode = when (which) {
                    0 -> "en" // English
                    1 -> "cs" // Czech
                    else -> "en"
                }
                showRestartWarningDialog(context, localeCode)
            }
            .show()
    }

    private fun showRestartWarningDialog(context: Context, localeCode: String) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.language_change))
            .setMessage(context.getString(R.string.app_will_restart_to_apply_language))
            .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                saveLanguagePreference(context, localeCode)
                setLocale(context, localeCode)
                restartApp(context)
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (context is Activity) {
            context.startActivity(intent)
            context.finishAffinity() // Finish all activities
        } else {
            // If context is not activity, just start the launcher activity
            context.startActivity(intent)
        }
    }

}
