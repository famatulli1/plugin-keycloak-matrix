# Keycloak Matrix 2FA Plugin

A Keycloak authentication plugin that implements two-factor authentication using Matrix messaging.

## Features

- Second factor authentication using Matrix messages
- Configurable OTP length and validity period
- Customizable message templates
- Internationalization support (English and French)
- Secure OTP generation and validation
- Full integration with Keycloak's authentication flow

## Prerequisites

- Keycloak 21.x or higher
- Java 11 or higher
- A Matrix account for the bot
- Maven 3.8+ (for building)

## Building

```bash
mvn clean package
```

The built JAR will be in the `target` directory.

## Installation

1. Stop your Keycloak server if it's running

2. Copy the JAR file to the Keycloak providers directory:
   ```bash
   cp target/keycloak-matrix-2fa-1.0-SNAPSHOT.jar /path/to/keycloak/providers/
   ```

3. Start Keycloak

## Configuration

### 1. Create a Matrix Bot Account

1. Register a new user account on your Matrix server
2. Get the access token:
   - Log in to Element or another Matrix client
   - Go to User Settings > Help & About > Advanced
   - Click "Access Token" to reveal your token
   - Copy and save this token securely

### 2. Configure the Authenticator

1. Log in to the Keycloak Admin Console
2. Select your realm
3. Go to Authentication > Flows
4. Copy the "Browser" flow using the "Copy" button
5. Rename it (e.g., "Browser with Matrix 2FA")
6. Click "+" in the "Forms" row
7. Add "Matrix 2FA" from the list
8. Set it as "REQUIRED" or "ALTERNATIVE"
9. Click the "Actions" gear icon on the "Matrix 2FA" row
10. Configure the following settings:
    - Matrix Server URL (e.g., https://matrix.org)
    - Bot User ID (e.g., @securitybot:matrix.org)
    - Bot Access Token (from step 1)
    - Message Template (optional)
    - OTP Validity Period (in seconds)
    - OTP Length
    - Matrix User ID Attribute

### 3. Set Up User Matrix IDs

Users need to have their Matrix ID stored in their user attributes:

1. Go to Users
2. Select a user
3. Go to Attributes
4. Add the Matrix ID attribute (default name: "matrix_id")
5. Set the value to their Matrix ID (e.g., @user:matrix.org)

### 4. Bind the Authentication Flow

1. Go to Authentication > Bindings
2. Set "Browser Flow" to your new flow
3. Click "Save"

## Testing

1. Log out of Keycloak
2. Try to log in to a client application
3. After entering username/password, you should receive a Matrix message with the OTP
4. Enter the OTP to complete authentication

## Troubleshooting

### Common Issues

1. **OTP not received**
   - Check Matrix server URL and bot credentials
   - Verify user's Matrix ID is correctly set
   - Check Keycloak logs for Matrix communication errors

2. **Authentication Failed**
   - Ensure OTP is entered within validity period
   - Check if Matrix ID attribute is correctly configured
   - Verify bot has permission to send messages

### Logging

To enable debug logging, add the following to `standalone.xml` or `standalone-ha.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:logging:...">
    <logger category="org.keycloak.matrix">
        <level name="DEBUG"/>
    </logger>
</subsystem>
```

## Security Considerations

- Use HTTPS for Matrix server communication
- Store bot access tokens securely
- Use appropriate OTP validity periods
- Monitor authentication logs
- Implement rate limiting on your Matrix server

---

# Plugin d'authentification Matrix 2FA pour Keycloak

[See English version above]

Un plugin d'authentification Keycloak qui implémente une authentification à deux facteurs via Matrix.

## Fonctionnalités

- Authentification à deux facteurs via messages Matrix
- Longueur et durée de validité du code OTP configurables
- Modèles de messages personnalisables
- Support multilingue (anglais et français)
- Génération et validation sécurisées des OTP
- Intégration complète avec le flux d'authentification Keycloak

## Prérequis

- Keycloak 21.x ou supérieur
- Java 11 ou supérieur
- Un compte Matrix pour le bot
- Maven 3.8+ (pour la compilation)

## Compilation

```bash
mvn clean package
```

Le fichier JAR sera généré dans le répertoire `target`.

## Installation

1. Arrêtez votre serveur Keycloak s'il est en cours d'exécution

2. Copiez le fichier JAR dans le répertoire providers de Keycloak :
   ```bash
   cp target/keycloak-matrix-2fa-1.0-SNAPSHOT.jar /chemin/vers/keycloak/providers/
   ```

3. Démarrez Keycloak

## Configuration

### 1. Créer un compte bot Matrix

1. Inscrivez un nouveau compte utilisateur sur votre serveur Matrix
2. Obtenez le jeton d'accès :
   - Connectez-vous à Element ou un autre client Matrix
   - Allez dans Paramètres utilisateur > Aide & À propos > Avancé
   - Cliquez sur "Jeton d'accès" pour révéler votre jeton
   - Copiez et conservez ce jeton en sécurité

### 2. Configurer l'authentificateur

1. Connectez-vous à la console d'administration Keycloak
2. Sélectionnez votre royaume
3. Allez dans Authentication > Flows
4. Copiez le flux "Browser" en utilisant le bouton "Copy"
5. Renommez-le (ex: "Browser avec Matrix 2FA")
6. Cliquez sur "+" dans la ligne "Forms"
7. Ajoutez "Matrix 2FA" depuis la liste
8. Définissez-le comme "REQUIRED" ou "ALTERNATIVE"
9. Cliquez sur l'icône "Actions" sur la ligne "Matrix 2FA"
10. Configurez les paramètres suivants :
    - URL du serveur Matrix (ex: https://matrix.org)
    - ID utilisateur du bot (ex: @securitybot:matrix.org)
    - Jeton d'accès du bot (de l'étape 1)
    - Modèle de message (optionnel)
    - Période de validité OTP (en secondes)
    - Longueur OTP
    - Attribut ID utilisateur Matrix

### 3. Configurer les ID Matrix des utilisateurs

Les utilisateurs doivent avoir leur ID Matrix stocké dans leurs attributs :

1. Allez dans Users
2. Sélectionnez un utilisateur
3. Allez dans Attributes
4. Ajoutez l'attribut ID Matrix (nom par défaut : "matrix_id")
5. Définissez la valeur à leur ID Matrix (ex: @utilisateur:matrix.org)

### 4. Lier le flux d'authentification

1. Allez dans Authentication > Bindings
2. Définissez "Browser Flow" sur votre nouveau flux
3. Cliquez sur "Save"

## Test

1. Déconnectez-vous de Keycloak
2. Essayez de vous connecter à une application cliente
3. Après avoir saisi nom d'utilisateur/mot de passe, vous devriez recevoir un message Matrix avec l'OTP
4. Saisissez l'OTP pour terminer l'authentification

## Dépannage

### Problèmes courants

1. **OTP non reçu**
   - Vérifiez l'URL du serveur Matrix et les informations d'identification du bot
   - Vérifiez que l'ID Matrix de l'utilisateur est correctement défini
   - Consultez les logs Keycloak pour les erreurs de communication Matrix

2. **Échec d'authentification**
   - Assurez-vous que l'OTP est saisi pendant sa période de validité
   - Vérifiez que l'attribut ID Matrix est correctement configuré
   - Vérifiez que le bot a la permission d'envoyer des messages

### Journalisation

Pour activer la journalisation de débogage, ajoutez ceci à `standalone.xml` ou `standalone-ha.xml` :

```xml
<subsystem xmlns="urn:jboss:domain:logging:...">
    <logger category="org.keycloak.matrix">
        <level name="DEBUG"/>
    </logger>
</subsystem>
```

## Considérations de sécurité

- Utilisez HTTPS pour la communication avec le serveur Matrix
- Stockez les jetons d'accès du bot de manière sécurisée
- Utilisez des périodes de validité OTP appropriées
- Surveillez les logs d'authentification
- Implémentez une limitation de débit sur votre serveur Matrix