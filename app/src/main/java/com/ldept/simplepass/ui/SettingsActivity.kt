package com.ldept.simplepass.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.ldept.simplepass.R
import com.ldept.simplepass.biometrics.BiometricAuthentication
import com.ldept.simplepass.biometrics.BiometricAuthenticationListener
import com.ldept.simplepass.database.PasswordDatabase
import com.ldept.simplepass.datasync.*
import com.ldept.simplepass.util.CollapsingToolbarStateChangeListener
import com.ldept.simplepass.util.DatabaseFileOperations
import com.ldept.simplepass.util.Network
import com.ldept.simplepass.util.SplashScreenAnimation
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.databinding.ActivitySettingsBinding
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread


class SettingsActivity : AppCompatActivity(), BiometricAuthenticationListener{

    private var dbxClientV2 : DbxClientV2? = null
    private lateinit var sharedPrefs : SharedPreferences
    private lateinit var dropboxViewModel : DropboxViewModel
    private lateinit var dropboxRepository: DropboxRepository
    private var isViewModelInitialized = false

    private lateinit var fingerprintSwitch : SwitchCompat
    private var shouldDisplayDialog = false
    private lateinit var backButton : ImageButton
    private lateinit var settingsContentView : View
    private lateinit var settingsSplashScreenView : View
    private lateinit var splashScreenTextView : TextView
    private lateinit var binding : ActivitySettingsBinding

    companion object{
        const val REQUEST_CREATE_FILE : Int = 1
        const val REQUEST_OPEN_FILE : Int = 2
        const val RESULT_BACKUP_RESTORED : Int = 3

        // Name of database uploaded to Dropbox
        const val DROPBOX_FILE_NAME : String = "SimplePassBackup.db"
        const val DROPBOX_FILE_TEMP_NAME : String = "temp.db"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = getSharedPreferences(packageName, MODE_PRIVATE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val replyIntent = Intent()

        settingsContentView = binding.settingsContentView
        settingsSplashScreenView = binding.splashScreenLayout.splashScreen
        splashScreenTextView = binding.splashScreenLayout.splashScreenTextview

        backButton = binding.backButton
        backButton.setOnClickListener{
            goBackOrDisplayDialog()
        }

        val titleTextView : TextView = binding.titleTextViewToolbar
        val appBarLayout : AppBarLayout = binding.appBarLayout
        appBarLayout.addOnOffsetChangedListener( object : CollapsingToolbarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, currentState: State) {
                if(currentState == State.COLLAPSED){
                    titleTextView.visibility = View.VISIBLE
                } else {
                    titleTextView.visibility = View.GONE
                }

            }
        } )

        fingerprintSwitch = binding.fingerprintSwitch
        val fingerprintSetting : ConstraintLayout = binding.fingerprintSetting
        BiometricAuthentication.showBiometricLoginOption(this,fingerprintSetting)

        val isFingerprintUnlocked = sharedPrefs.getBoolean("fingerprint-unlock",false)
        fingerprintSwitch.isChecked = isFingerprintUnlocked

        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
                BiometricAuthentication.showBiometricAuthentication(
                    this,
                    this
                )
            else
                if(isFingerprintUnlocked)
                    sharedPrefs.edit().putBoolean("fingerprint-unlock", false).apply()

        }

        val changePasswordButton : Button = binding.changePasswordButton
        changePasswordButton.setOnClickListener {
            val intent = Intent(this,AuthenticationSettingsActivity::class.java)
            startActivity(intent)
        }

        val backupButton : Button = binding.backupButton
        backupButton.setOnClickListener{
            createBackupFile()
        }

        val importButton : Button = binding.importButton
        importButton.setOnClickListener {
            importBackupFile()
        }




        // Licenses ==================================================================
        val licensesButton : Button = binding.licenseActivityButton

        licensesButton.setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.licenses))
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }

        // Dropbox ===================================================================

        val dropboxLoginButton : Button = binding.dropboxLoginButton
        val dropboxLogoutButton : Button = binding.dropboxLogoutButton
        val dropboxUploadButton : Button = binding.dropboxUploadButton
        val dropboxLastModifiedTextView : TextView = binding.dropboxLastModified


        dropboxLogoutButton.visibility = View.GONE
        dropboxUploadButton.visibility = View.GONE

        dropboxLoginButton.setOnClickListener{
            // Sends an Intent to Dropbox App or web browser to log in to Dropbox
            // Then we can get the access token on our activity's OnResume
            Auth.startOAuth2Authentication(this@SettingsActivity, getString(R.string.APP_KEY));
        }
        dropboxLogoutButton.setOnClickListener {
            sharedPrefs.edit().remove("access-token").apply()
            sharedPrefs.edit().remove("logged-account").apply()
            updateLogInUI()
        }
        dropboxUploadButton.setOnClickListener {
            if (Network.isConnectedToNetwork(this)) {

                SplashScreenAnimation.crossfade(settingsContentView, settingsSplashScreenView)
                splashScreenTextView.text = getString(R.string.uploading_database)
                shouldDisplayDialog = true
                dropboxViewModel.uploadDatabaseFile(
                    this,
                    getDatabasePath("password_database_encrypted")
                )

            } else {
                Toast.makeText(this,getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            }
        }
        val dropboxDownloadButton : Button = binding.dropboxDownloadButton
        dropboxDownloadButton.setOnClickListener{
            if(Network.isConnectedToNetwork(this)) {
                SplashScreenAnimation.crossfade(settingsContentView, settingsSplashScreenView)
                splashScreenTextView.text = getString(R.string.dropbox_download)
                shouldDisplayDialog = true
                dropboxViewModel.downloadDatabaseFile(this)
                setResult(RESULT_BACKUP_RESTORED, replyIntent)
            } else {
                Toast.makeText(this,getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            }
        }



    }

    override fun onBackPressed() {
        goBackOrDisplayDialog()
    }

    fun goBackOrDisplayDialog(){
        if(shouldDisplayDialog){
            downloadUploadDialog()
        } else {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    override fun onResume() {
        super.onResume()
        generateOrGetDropboxAccessToken()
        initDbxClient()
        initDropboxViewModel()

        if(isViewModelInitialized) {
            val dropboxLastModifiedTextView : TextView = findViewById(R.id.dropbox_last_modified)
            dropboxViewModel.lastModified.observe(this, androidx.lifecycle.Observer {
                    lastModifiedDate ->
                if(lastModifiedDate == Date(0L)){
                    dropboxLastModifiedTextView.text = getString(R.string.dropbox_no_file)
                } else {
                    "${getString(R.string.dropbox_file_modification)}: $lastModifiedDate".also {
                        dropboxLastModifiedTextView.text = it
                    }
                }

            })

            dropboxViewModel.hasDownloadOrUploadFinished.observe(this, { hasFinished ->
                if(hasFinished){
                    putSaltIntoSharedPrefs()
                    SplashScreenAnimation.crossfade(settingsSplashScreenView, settingsContentView)
                    shouldDisplayDialog = false
                    dropboxViewModel.downloadOrUploadFinishHandled()
                    updateLogInUI()

                }
            })
        }
        updateLogInUI()
    }

    private fun putSaltIntoSharedPrefs() {
        val saltFile = File(filesDir, "salt")
        if (saltFile.exists()) {
            val salt : String = saltFile.readText().dropLast(1)
            sharedPrefs.edit().putString("pbkdf2-salt", salt).apply()
            saltFile.delete()
        }
    }
    private fun downloadUploadDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.import_or_export_ongoing))
        builder.setMessage(getString(R.string.import_dialog_message))
        builder.setPositiveButton(getString(R.string.import_dialog_exit), DialogInterface.OnClickListener { dialog, _ ->
            finish()
            dialog.dismiss()
        })
        builder.setNegativeButton(R.string.import_dialog_wait, DialogInterface.OnClickListener{ dialog, _ ->
            dialog.dismiss()
        })
        val alertDialog = builder.create()

        alertDialog.show()
    }
    private fun updateLogInUI(){
        setAccountInfoVisibility()
        setDropboxButtonsVisibility()
    }

    private fun initDbxClient(){
        if(dbxClientV2 == null && isAccessTokenSaved()){
            dbxClientV2 = DropboxClient().getClient(getAccessTokenFromSharedPrefs())
        }
    }
    private fun initDropboxViewModel(){
        if(dbxClientV2 != null) {
            dropboxRepository = DropboxRepository(dbxClientV2!!)
            val dropboxViewModelFactory = DropboxViewModelFactory(dropboxRepository)
            dropboxViewModel = ViewModelProvider(this,dropboxViewModelFactory).get(DropboxViewModel::class.java)

            if(Network.isConnectedToNetwork(this)) {
                dropboxViewModel.getAccountInfo(sharedPrefs)
                dropboxViewModel.backupLastModified()
            }
            updateLogInUI()
            isViewModelInitialized = true
        } else {
            isViewModelInitialized = false
        }
    }
    private fun setDropboxButtonsVisibility(){
        val dropboxLoginButton : Button = binding.dropboxLoginButton
        val dropboxLogoutButton : Button = binding.dropboxLogoutButton
        val dropboxUploadButton : Button = binding.dropboxUploadButton
        val dropboxDownloadButton : Button = binding.dropboxDownloadButton
        if(isAccessTokenSaved()){
            dropboxLoginButton.visibility = View.GONE
            dropboxLogoutButton.visibility = View.VISIBLE
            dropboxUploadButton.visibility = View.VISIBLE
            dropboxDownloadButton.visibility = View.VISIBLE
        } else {
            dropboxLoginButton.visibility = View.VISIBLE
            dropboxLogoutButton.visibility = View.GONE
            dropboxUploadButton.visibility = View.GONE
            dropboxDownloadButton.visibility = View.GONE
        }
    }

    private fun setAccountInfoVisibility(){
        val accountInfoTextView: TextView = binding.dropboxAccountInfo
        val lastModifiedTextView : TextView = binding.dropboxLastModified
        if(isAccountInfoInPrefs()) {
            val email = sharedPrefs.getString("logged-account", "").toString()
            val accountInfoText = "${getString(R.string.logged_account)}: $email"
            accountInfoTextView.text = accountInfoText
            accountInfoTextView.visibility = View.VISIBLE
            lastModifiedTextView.visibility = View.VISIBLE

        } else {
            lastModifiedTextView.visibility = View.GONE
            accountInfoTextView.visibility = View.GONE
        }
    }
    private fun getAccessTokenFromSharedPrefs() : String {
        return sharedPrefs.getString("access-token","").toString()
    }
    private fun isAccountInfoInPrefs() : Boolean {
        return sharedPrefs.contains("logged-account")
    }
    private fun isAccessTokenSaved() : Boolean {
        return sharedPrefs.contains("access-token")
    }
    private fun generateOrGetDropboxAccessToken() {
        val accessToken : String? = Auth.getOAuth2Token()
        if (accessToken != null && !isAccessTokenSaved()){
            sharedPrefs.edit().putString("access-token", accessToken).apply()

            Toast.makeText(this, getString(R.string.dropbox_login_success), Toast.LENGTH_LONG).show()
        }
    }


    // Local import / backup

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_FILE && resultCode == RESULT_OK){
            shouldDisplayDialog = true
            data?.data?.also { uri ->

                val inStream = getDatabasePath("password_database_encrypted").inputStream()
                val outStream = contentResolver.openOutputStream(uri)
                inStream.use { input ->
                    outStream.use { output ->
                        output?.let { input.copyTo(it) }
                    }
                }
                Toast.makeText(this, "Backup created", Toast.LENGTH_LONG).show()
            }
            shouldDisplayDialog = false
        }
        else if (requestCode == REQUEST_OPEN_FILE && resultCode == RESULT_OK){
            shouldDisplayDialog = true
            data?.data?.also{ uri ->
                val from = contentResolver.openInputStream(uri)
                val to = getDatabasePath("password_database_encrypted").outputStream()
                from?.let {
                    DatabaseFileOperations.copyFile(it, to)
                    val dbFile = getDatabasePath("password_database_encrypted")
                    DatabaseFileOperations.removeWALAndSHM(dbFile)
                    PasswordDatabase.closeDatabase()
                }
                Toast.makeText(this, getString(R.string.database_import_success), Toast.LENGTH_LONG).show()



            }
            shouldDisplayDialog = false
        }
    }
    private fun createBackupFile(){
        try {
            val currentDate = LocalDateTime.now()

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formattedDate = currentDate.format(formatter)

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "application/vnd.sqlite3"
            intent.putExtra(Intent.EXTRA_TITLE, "SimplePass backup $formattedDate.db")
            startActivityForResult(intent, REQUEST_CREATE_FILE)

        } catch (e : Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()

        }
    }

    private fun importBackupFile(){
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            startActivityForResult(intent, REQUEST_OPEN_FILE)
        } catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        sharedPrefs.edit().putBoolean("fingerprint-unlock", true).apply()
    }

    override fun onAuthenticationError(errorCode: Int, errString: String) {
        fingerprintSwitch.isChecked = false
    }

    override fun onAuthenticationFailed() {
        fingerprintSwitch.isChecked = false
    }

}