package smartvision

import kotlin.io.readLine

class Utilisateur(val email: String, val motDePasse: String) {
    fun sAuthentifier(): Boolean {
        // Simulation d'authentification
        return email.isNotEmpty() && motDePasse.length >= 6
    }
}

class Document(val imagePath: String) {
    fun traiterOCR(): String {
        // Simulation de traitement OCR
        return "Texte extrait de $imagePath : 'Bonjour, ceci est un exemple de texte reconnu.'"
    }
}

fun main() {
    println("=== SmartVision OCR App ===")
    println("Bienvenue dans l'application mobile SmartVision")

    // Simulation d'authentification
    print("Entrez votre email: ")
    val email = readLine() ?: ""
    print("Entrez votre mot de passe: ")
    val password = readLine() ?: ""

    val user = Utilisateur(email, password)
    if (user.sAuthentifier()) {
        println("✓ Authentification réussie !")

        // Simulation de scan
        println("\n--- Mode Scan ---")
        print("Entrez le chemin de l'image à scanner (ou 'test.jpg'): ")
        val imagePath = readLine() ?: "test.jpg"

        val doc = Document(imagePath)
        val texteExtrait = doc.traiterOCR()
        println("📷 Scan en cours...")
        println("✅ Texte reconnu: $texteExtrait")

        // Simulation abonnement
        print("\nÊtes-vous abonné ? (oui/non): ")
        val isAbonne = readLine()?.lowercase() == "oui"

        if (isAbonne) {
            println("💾 Document sauvegardé dans le cloud !")
        } else {
            println("ℹ️  Abonnement requis pour sauvegarder.")
        }

        println("\n📱 Notification: 'Nouvelles annonces disponibles !'")

    } else {
        println("✗ Authentification échouée. Vérifiez vos credentials.")
    }

    println("\n=== Fin de la simulation ===")
}
