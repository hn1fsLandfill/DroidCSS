package eu.hn1f.droidcss

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceActivity

class SettingsActivity: PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        var sharedPrefs: SharedPreferences? = null;
        try {
            sharedPrefs = getSharedPreferences("SomeSettings", MODE_WORLD_READABLE)
        } catch (_: Exception) {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Oops!")
                .setMessage("DroidCSS can't load settings, is the module enabled?")
                .setPositiveButton("OK") { _, _ -> super.finish() }
                .setOnDismissListener { super.finish() }
                .create()
            dialog.show()
            return
        }
        val redirSysUI = findPreference("redirSysUI") as CheckBoxPreference
        val noIconBGs = findPreference("noIconBGs") as CheckBoxPreference

        redirSysUI.isChecked = sharedPrefs!!.getBoolean("redirSysUI", false)
        noIconBGs.isChecked = sharedPrefs.getBoolean("removeIconBGs", true)

        redirSysUI.setOnPreferenceChangeListener(object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, value: Any?): Boolean {
                val bool = if(value == true) true else false
                sharedPrefs.edit().apply {
                    putBoolean("redirSysUI", bool)
                    apply()
                }
                return true
            }
        })

        noIconBGs.setOnPreferenceChangeListener(object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, value: Any?): Boolean {
                val bool = if(value == true) true else false
                sharedPrefs.edit().apply {
                    putBoolean("removeIconBGs", bool)
                    apply()
                }
                return true
            }
        })
    }
}