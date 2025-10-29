package com.smartvisions.app

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.content.ClipboardManager
import android.content.Context
import java.io.File
import java.io.IOException

class ImageProcessingActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var loadImageButton: Button
    private lateinit var extractTextButton: Button
    private lateinit var extractedTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var userEmailText: TextView
    private lateinit var subscribeButton: Button
    private lateinit var subscriptionStatusText: TextView
    private lateinit var copyTextButton: Button
    private lateinit var ocrMethodGroup: android.widget.RadioGroup
    private lateinit var radioMlkit: android.widget.RadioButton
    private lateinit var radioCraft: android.widget.RadioButton

    private var currentImageUri: Uri? = null
    private var currentBitmap: Bitmap? = null

    // ML Kit Text Recognizer (fallback)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Custom OCR Processor
    private lateinit var ocrProcessor: OCRProcessor

    // Subscription status
    private var isUserSubscribed = false

    // AdMob
    private var interstitialAd: InterstitialAd? = null

    // Activity result launcher for gallery
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            loadImageFromUri(it)
        }
    }

    // Activity result launcher for camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentImageUri != null) {
            loadImageFromUri(currentImageUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_processing)

        // Initialize OCR Processor
        ocrProcessor = OCRProcessor(this)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        imageView = findViewById(R.id.image_view)
        loadImageButton = findViewById(R.id.load_image_button)
        extractTextButton = findViewById(R.id.extract_text_button)
        extractedTextView = findViewById(R.id.extracted_text_view)
        logoutButton = findViewById(R.id.logout_button)
        userEmailText = findViewById(R.id.user_email_text)
        subscribeButton = findViewById(R.id.subscribe_button)
        subscriptionStatusText = findViewById(R.id.subscription_status_text)
        copyTextButton = findViewById(R.id.copy_text_button)
        ocrMethodGroup = findViewById(R.id.ocr_method_group)
        radioMlkit = findViewById(R.id.radio_mlkit)
        radioCraft = findViewById(R.id.radio_craft)

        extractTextButton.isEnabled = false

        // Initialize AdMob
        MobileAds.initialize(this) { }
        loadInterstitialAd()

        // Afficher l'email de l'utilisateur connecté et vérifier l'abonnement
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userEmailText.text = "Connecté en tant que: ${currentUser.email}"
            checkSubscriptionStatus(currentUser.uid)
        } else {
            userEmailText.text = "Utilisateur non connecté"
            subscriptionStatusText.text = "Statut: Non connecté"
        }
    }

    private fun setupClickListeners() {
        loadImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        extractTextButton.setOnClickListener {
            extractTextFromImage()
        }

        logoutButton.setOnClickListener {
            logout()
        }

        subscribeButton.setOnClickListener {
            toggleSubscription()
        }

        copyTextButton.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Galerie", "Appareil photo")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choisir la source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            currentImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(currentImageUri)
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis().toString()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir("Pictures")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            currentBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imageView.setImageBitmap(currentBitmap)
            extractTextButton.isEnabled = true
            extractedTextView.text = "Texte extrait apparaîtra ici..."
        } catch (e: IOException) {
            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractTextFromImage() {
        currentBitmap?.let { bitmap ->
            // Show loading message
            extractedTextView.text = "Extraction en cours..."

            // Check selected OCR method
            val selectedMethod = when (ocrMethodGroup.checkedRadioButtonId) {
                R.id.radio_mlkit -> "mlkit"
                R.id.radio_craft -> "craft"
                else -> "mlkit"
            }

            // Use appropriate OCR processor
            ocrProcessor.extractText(bitmap, selectedMethod) { extractedText ->
                runOnUiThread {
                    extractedTextView.text = if (extractedText.isNotEmpty()) {
                        extractedText
                    } else {
                        "Aucun texte détecté dans l'image"
                    }
                    // Show copy button after text extraction
                    updateCopyButtonVisibility()
                }
            }
        } ?: run {
            Toast.makeText(this, "Veuillez d'abord charger une image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ocrProcessor.close()
    }

    private fun logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkSubscriptionStatus(userId: String) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId).child("subscription")

        userRef.get().addOnSuccessListener { snapshot ->
            val isSubscribed = snapshot.getValue(Boolean::class.java) ?: false
            updateSubscriptionUI(isSubscribed)
        }.addOnFailureListener {
            updateSubscriptionUI(false)
        }
    }

    private fun toggleSubscription() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return

        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(currentUser.uid).child("subscription")

        // Vérifier l'état actuel
        userRef.get().addOnSuccessListener { snapshot ->
            val isCurrentlySubscribed = snapshot.getValue(Boolean::class.java) ?: false
            val newSubscriptionState = !isCurrentlySubscribed

            // Mettre à jour dans Firebase
            userRef.setValue(newSubscriptionState).addOnSuccessListener {
                updateSubscriptionUI(newSubscriptionState)
                val message = if (newSubscriptionState) "Abonnement activé !" else "Abonnement désactivé"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSubscriptionUI(isSubscribed: Boolean) {
        isUserSubscribed = isSubscribed
        if (isSubscribed) {
            subscriptionStatusText.text = "Statut: Abonné ✅"
            subscriptionStatusText.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Vert
            subscribeButton.text = "Se désabonner"
            subscribeButton.setTextColor(android.graphics.Color.parseColor("#FF5722")) // Rouge
        } else {
            subscriptionStatusText.text = "Statut: Non abonné"
            subscriptionStatusText.setTextColor(android.graphics.Color.parseColor("#FF9800")) // Orange
            subscribeButton.text = "S'abonner"
            subscribeButton.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Vert
        }
        updateCopyButtonVisibility()
    }

    private fun updateCopyButtonVisibility() {
        val hasText = extractedTextView.text.isNotEmpty() &&
                     extractedTextView.text != "Sélectionnez une image pour commencer..." &&
                     extractedTextView.text != "Extraction en cours..." &&
                     extractedTextView.text != "Aucun texte détecté dans l'image"

        if (hasText) {
            copyTextButton.visibility = android.view.View.VISIBLE
            if (isUserSubscribed) {
                copyTextButton.text = "Copier le texte"
                copyTextButton.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Vert
            } else {
                copyTextButton.text = "Regarder une pub pour copier"
                copyTextButton.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // Orange
            }
        } else {
            copyTextButton.visibility = android.view.View.GONE
        }
    }

    private fun copyTextToClipboard() {
        if (isUserSubscribed) {
            performCopy()
        } else {
            showInterstitialAd()
        }
    }

    private fun performCopy() {
        val textToCopy = extractedTextView.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("Texte extrait", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Texte copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Ad dismissed, now allow copy
                    performCopy()
                    // Load next ad
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    // Ad failed to show, allow copy anyway
                    performCopy()
                }
            }
            interstitialAd?.show(this)
        } else {
            // No ad available, allow copy anyway
            performCopy()
            Toast.makeText(this, "Publicité non disponible, copie autorisée", Toast.LENGTH_SHORT).show()
        }
    }
}