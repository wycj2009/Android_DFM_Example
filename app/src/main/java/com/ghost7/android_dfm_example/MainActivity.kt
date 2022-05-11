package com.ghost7.android_dfm_example

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghost7.android_dfm_example.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DFM_CONFIRMATION_REQUESTCODE = 101
        private const val DFM_NAME = "my_dynamic_feature"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var splitInstallManager: SplitInstallManager
    private var sessionId = 0

    private val splitInstallStateUpdatedListener = SplitInstallStateUpdatedListener { splitInstallSessionState ->
        val status = splitInstallSessionState.status()
        val statusName = when (status) {
            0 -> "UNKNOWN"
            1 -> "PENDING"
            2 -> "DOWNLOADING"
            3 -> "DOWNLOADED"
            4 -> "INSTALLING"
            5 -> "INSTALLED"
            6 -> "FAILED"
            7 -> "CANCELED"
            8 -> "REQUIRES_USER_CONFIRMATION"
            9 -> "CANCELING"
            else -> ""
        }

        binding.run {
            statusTv.text = """
                    sessionId = $sessionId
                    status = $statusName
                """.trimIndent()
            detailTv.text = ""
        }

        when (status) {
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                try {
                    splitInstallManager.startConfirmationDialogForResult(splitInstallSessionState, this, DFM_CONFIRMATION_REQUESTCODE)
                } catch (e: IntentSender.SendIntentException) {
                    binding.detailTv.text = e.toString()
                }
            }
            SplitInstallSessionStatus.DOWNLOADING -> {
                binding.detailTv.text = "${splitInstallSessionState.bytesDownloaded()} of ${splitInstallSessionState.totalBytesToDownload()} bytes downloaded"
            }
            SplitInstallSessionStatus.FAILED -> {
                binding.detailTv.text = "${splitInstallSessionState.errorCode()}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager.registerListener(splitInstallStateUpdatedListener)

        binding.launchFeatureBtn.setOnClickListener { launchFeature() }
        binding.startInstallFeatureBtn.setOnClickListener { startInstallFeature() }
        binding.deferredUninstallFeatureBtn.setOnClickListener { deferredUninstallFeature() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DFM_CONFIRMATION_REQUESTCODE) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(binding.root, "Beginning Installation", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "User declined installation", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchFeature() {
        if (splitInstallManager.installedModules.contains(DFM_NAME)) {
            startActivity(Intent("com.ghost7.my_dynamic_feature.MyFeatureActivity"))
        } else {
            Snackbar.make(binding.root, "Feature not yet installed", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startInstallFeature() {
        val splitInstallRequest = SplitInstallRequest.newBuilder()
                .addModule(DFM_NAME)
                .build()

        splitInstallManager.startInstall(splitInstallRequest)
                .addOnSuccessListener { sessionId ->
                    this.sessionId = sessionId
                    Snackbar.make(binding.root, "Module installation started", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Snackbar.make(binding.root, "Module installation failed: $exception", Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun deferredUninstallFeature() {
        splitInstallManager.deferredUninstall(listOf(DFM_NAME))
                .addOnSuccessListener {
                    this.sessionId = sessionId
                    Snackbar.make(binding.root, "Module uninstallation started", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Snackbar.make(binding.root, "Module uninstallation failed: $exception", Snackbar.LENGTH_SHORT).show()
                }
    }

}