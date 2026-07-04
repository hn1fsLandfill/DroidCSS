@file:Suppress("DEPRECATION")

package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceScreen
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity: PreferenceActivity() {
    var sharedPrefs: SharedPreferences? = null;

    fun saveColor(packageId: String, color: Int) {
        sharedPrefs!!.edit().apply {
            putInt("tintcolor_${packageId}", color)
            apply()
        }
    }

    fun showColorPicker(packageId: String) {
        val colorPicker = ColorPicker(this)
        colorPicker.color = sharedPrefs!!.getInt("tintcolor_${packageId}", Color.WHITE)
        val dialog = AlertDialog.Builder(this)
            .setView(colorPicker)
            .setPositiveButton(android.R.string.ok) { _, _ -> saveColor(packageId, colorPicker.color) }
            .setOnDismissListener { _ -> saveColor(packageId, colorPicker.color) }
            .create()

        dialog.show()
    }

    fun appSettings(app: ApplicationInfo, icon: Drawable) {
        val main = layoutInflater.inflate(R.layout.app_settings, null)

        val dialog = AlertDialog.Builder(this)
            .setView(main)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .create()

        val appDetail = main.findViewById<TextView>(R.id.appDetail)
        appDetail.setCompoundDrawablesRelative(icon, null, null, null)
        appDetail.text = app.loadLabel(packageManager)

        val forceIconJail = main.findViewById<Switch>(R.id.noIconBG)
        forceIconJail.isChecked = sharedPrefs!!.getBoolean("forceintoiconjail_${app.packageName}", false)

        forceIconJail.setOnCheckedChangeListener { _, bool ->
            sharedPrefs!!.edit().apply {
                putBoolean("forceintoiconjail_${app.packageName}", bool)
                apply()
            }
        }

        val tintIcon = main.findViewById<Switch>(R.id.tintIcon)
        tintIcon.isChecked = sharedPrefs!!.getBoolean("tinticon_${app.packageName}", false)

        tintIcon.setOnCheckedChangeListener { _, bool ->
            sharedPrefs!!.edit().apply {
                putBoolean("tinticon_${app.packageName}", bool)
                apply()
            }
        }

        val tintColorToggle = main.findViewById<Button>(R.id.tintColorToggle)
        tintColorToggle.setOnClickListener { _ ->
            showColorPicker(app.packageName)
        }

        val themePicker = main.findViewById<Spinner>(R.id.themePicker)
        themePicker.adapter = ArrayAdapter.createFromResource(this, R.array.themes, android.R.layout.simple_list_item_1)

        val themeSelected = sharedPrefs!!.getString("theme_${app.packageName}", "Default")
        val themesArray = resources.getStringArray(R.array.themes)

        if(themeSelected != null)
            themePicker.setSelection(themesArray.indexOf(themeSelected), false)

        themePicker.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                sharedPrefs!!.edit().apply {
                    putString("theme_${app.packageName}", themesArray[position])
                    apply()
                }
            }

            override fun onNothingSelected(ignored: AdapterView<*>?) {}
        }

        dialog.show()
    }

    fun checkboxSetting(key: String, default: Boolean) {
        val preference = findPreference(key) as CheckBoxPreference
        preference.isChecked = sharedPrefs!!.getBoolean(key, default)
        preference.setOnPreferenceChangeListener(object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(p0: Preference?, value: Any?): Boolean {
                val bool = if(value == true) true else false
                sharedPrefs!!.edit().apply {
                    putBoolean(key, bool)
                    apply()
                }
                return true
            }
        })
    }

    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        addPreferencesFromResource(R.xml.settings)
        checkboxSetting("redirSysUI", false)
        checkboxSetting("removeIconBGs", true)
        checkboxSetting("holoFrameworkDialogs", false)

        val appList = findPreference("appList") as PreferenceScreen
        for(app in packageManager.getInstalledApplications(0)) {
            if(app.isResourceOverlay) continue;
            val icon = app.loadIcon(packageManager)
            icon.setBounds(0, 0, 64, 64)
            val label = app.loadLabel(packageManager)

            val preference = Preference(this)
            preference.icon = icon
            preference.title = label
            preference.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(ignored: Preference?): Boolean {
                    appSettings(app, preference.icon)
                    return true;
                }
            }

            appList.addPreference(preference)
        }
    }
}