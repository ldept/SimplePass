# SimplePass
Password Manager for Android 8.0.0 and newer.

Allows for secure storage of passwords in an encrypted database.


## Features
- Completely offline (if you want it to be :) )
- Database encrypted with 256-bit AES in CBC mode using SQLCipher
- Key used to encrypt the database is derived from user-provided password using 100000 iterations of PBKDF2withHmacSHA512
- Biometric authentication used as two-factor authentication (2FA)
- Integration with Dropbox - sync database manually between devices using Dropbox
- Restricted access to Dropbox: 
    - App only has access to its folder in your Dropbox - your data on Dropbox is secure
    - Basic account info is needed for all apps using Dropbox (it's only used for displaying currently logged account)
- Local export and import of the database
- Screenshot prevention for added security
- Minimum of permissions - only to allow the features above
- Easy to use interface supporting light and dark theme




## TODO:
- Add autofill integration